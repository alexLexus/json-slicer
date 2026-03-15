package local.lex.json.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.util.unit.DataSize;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Chunker {

    public List<Chunk> chunk(JsonNode json, DataSize limit) {
        List<Chunk> result = new ArrayList<>();
        traverse(json, ChunkPath.ROOT_NAME, ChunkPath.ROOT_INDEX, limit.toBytes(), new Stack<>(), result);

        return result;
    }

    private void traverse(JsonNode json, String name, int index, long limit, Stack<ChunkPath.Fragment> path,
                          List<Chunk> result) {

        if (json.isContainerNode()) {
            // При посещении контейнера (объект или массив) нужно просто сохранить путь до этого узла в стеке и
            // рекурсивно обрабатываем дочерние
            ChunkPath.Kind kind = json.isArray() ? ChunkPath.Kind.ARRAY : ChunkPath.Kind.OBJECT;
            ChunkPath.Fragment fragment = new ChunkPath.Fragment(kind, name, index, 0, 0);
            path.push(fragment);

            if (json.isArray()) {
                for (int i = 0; i < json.size(); i++) {
                    traverse(json.get(i), null, i, limit, path, result);
                }

            } else {
                int idx = 0;
                for (Map.Entry<String, JsonNode> property : json.properties()) {
                    traverse(property.getValue(), property.getKey(), idx++, limit, path, result);
                }
            }

            // После убираем элемент из пути
            path.pop();

        } else if (json.isTextual()) {
            // Разделяться на куски будут только текстовые узлы
            String text = json.asText();
            if (stringSize(text) < limit) {
                // Если текст полностью помещается в лимиты, просто копируем
                result.add(getValueNode(json.deepCopy(), name, index, path));

            } else {
                // Иначе разделение на куски
                splitString(text, name, index, limit, path, result);
            }

        } else {
            // Не текстовые листья дерева уйдет как есть
            result.add(getValueNode(json.deepCopy(), name, index, path));
        }
    }

    private void splitString(String text, String name, int index, long limit, Stack<ChunkPath.Fragment> path,
                             List<Chunk> result) {
        // Размер текущей части текста
        int currentTextSize = 0;

        // Где в последний раз попался пробел
        int whiteSpacePosition = 0;

        // Индексы начала и конца подстроки (НЕ включительно), которую нужно вынести в отдельную часть
        int start = 0, end = 0;
        for (end = 0; end < text.length(); ) {
            // Работать нужно не с char, а с int codePoint, чтобы не разрывать суррогатные пары и правильно увеличивать
            // индекс итерации
            int codePoint = text.codePointAt(end);

            // Проверка на пробельный символ, при обрезке нужно будет откатиться до его позиции
            boolean isWhitespace = Character.isWhitespace(codePoint);
            if (isWhitespace) {
                whiteSpacePosition = end;
            }

            // Подсчет размера знака при кодировке в UTF-8 и подсчет общего размера текса
            int codePointSize = codePointUTF8BytesSize(codePoint);
            currentTextSize += codePointSize;

            // Если лимит превышен
            if (currentTextSize >= limit) {

                // Если текущий символ не пробельный, а последний пробельный символ находится в диапазоне от
                // начального до конечного индекса, то обрезаем по нему
                if (!isWhitespace && (start < whiteSpacePosition && end > whiteSpacePosition)) {
                    end = whiteSpacePosition;
                }

                // Обрезка текста
                String substring = text.substring(start, end);
                TextNode payload = TextNode.valueOf(substring);
                result.add(getValueChunk(payload, name, index, path, start, end));

                // Сброс размера до размера текущего знака, так как образка происходит по end НЕ включительно,
                // то есть знак на позиции end попадет в следующую часть, а не в эту
                currentTextSize = codePointSize;
                start = end;
            }

            // В строке могут быть суррогатные пары, нужно это учитывать, поэтому счетчик увеличивается не на 1,
            // а на количество знаков которые занимает потенциально возможная пара
            end += Character.charCount(codePoint);
        }

        // Добавление остатка строки
        if (end <= text.length()) {
            TextNode payload = TextNode.valueOf(text.substring(start));
            result.add(getValueChunk(payload, name, index, path, start, text.length()));
        }
    }

    private Chunk getValueChunk(JsonNode json, String name, int index, Stack<ChunkPath.Fragment> path,
                                int strat, int end) {
        ChunkPath.Fragment fragment = new ChunkPath.Fragment(ChunkPath.Kind.CHUNK, name, index, strat, end);
        ChunkPath currentPath = new ChunkPath();
        currentPath.getFragments().addAll(path);
        currentPath.getFragments().add(fragment);

        return new Chunk(currentPath, json);
    }

    private Chunk getValueNode(JsonNode json, String name, int index, Stack<ChunkPath.Fragment> path) {
        ChunkPath.Fragment fragment = new ChunkPath.Fragment(ChunkPath.Kind.VALUE, name, index, 0, 0);
        ChunkPath currentPath = new ChunkPath();
        currentPath.getFragments().addAll(path);
        currentPath.getFragments().add(fragment);

        return new Chunk(currentPath, json);
    }

    private int stringSize(String value) {
        return value == null ? 0 : value.getBytes(StandardCharsets.UTF_8).length;
    }

    private int codePointUTF8BytesSize(int cp) {
        if (cp <= 0x7F) return 1;
        if (cp <= 0x7FF) return 2;
        if (cp <= 0xFFFF) return 3;
        else return 4;
    }
}
