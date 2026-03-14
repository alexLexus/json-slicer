package local.lex.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.util.unit.DataSize;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PartitionerImpl implements Partitioner {

    /**
     * Разбивает json на части заданного размера в байтах с учетом пробелов.
     *
     * @param json исходный json
     * @param limit максимальный размер в байтах для текстовых узлов json
     * @return части json не превышающие заданный размер
     */
    @Override
    public List<Partition> split(JsonNode json, DataSize limit) {
        List<Partition> partitions = new ArrayList<>();
        traverseAndSplit(json, PartitionPath.ROOT_PATH_NAME, PartitionPath.ROOT_PATH_INDEX, limit.toBytes(), new PartitionPathImpl(), partitions);
        return partitions;
    }

    private void traverseAndSplit(JsonNode json, String name, int index, long limit, PartitionPath path, List<Partition> partitions) {
        if (json.isArray()) {
            path.append(getPathElement(json, name, index, PartitionPath.NOT_PARTITIONED_START_END, PartitionPath.NOT_PARTITIONED_START_END));
            int idx = 0;
            for (JsonNode item : json) {
                traverseAndSplit(item, null, idx++, limit, path, partitions);
            }
        } else if (json.isObject()) {
            path.append(getPathElement(json, name, index, PartitionPath.NOT_PARTITIONED_START_END, PartitionPath.NOT_PARTITIONED_START_END));
            int idx = 0;
            for (Map.Entry<String, JsonNode> property : json.properties()) {
                traverseAndSplit(property.getValue(), property.getKey(), idx++, limit, path, partitions);
            }
        } else if (json.isTextual()) {
            // Разделению подлежат только текстовые узлы, все другие листья обрезаться не будут
            String text = json.asText();
            if (stringByteSize(text) < limit) {
                // Если текстовый лист влезает, то ничего не делаем
                path.append(getPathElement(json, name, index, PartitionPath.NOT_PARTITIONED_START_END, PartitionPath.NOT_PARTITIONED_START_END));
                Partition partition = new Partition(path.copy(), json.deepCopy());
                partitions.add(partition);
            } else {
                // Размер текущей части текста
                int currentTextSize = 0;
                // Где в последний раз попался пробел
                int whiteSpacePosition = 0;
                // Индексы начала и конца подстроки (НЕ включительно), которую нужно вынести в отдельную часть
                int start = 0;
                int end = 0;
                for (end = 0; end < text.length(); ) {
                    int codePoint = text.codePointAt(end);
                    // Проверка на пробельный символ, при обрезке нужно будет откатиться до его позиции
                    boolean isWhitespace = Character.isWhitespace(codePoint);
                    if (isWhitespace) {
                        whiteSpacePosition = end;
                    }
                    // Подсчет размера знака при кодировке в UTF-8
                    int codePointSize = codePointUTF8BytesSize(codePoint);
                    currentTextSize += codePointSize;
                    if (currentTextSize >= limit) {
                        // Если текущий символ не пробельный, а последний пробельный символ находится в диапазоне от
                        // начального до конечного индекса, то обрезаем по нему
                        if (!isWhitespace && (start < whiteSpacePosition && end > whiteSpacePosition)) {
                            end = whiteSpacePosition;
                        }
                        TextNode payload = TextNode.valueOf(text.substring(start, end));
                        path.append(getPathElement(json, name, index, start, end));
                        Partition partition = new Partition(path.copy(), payload);
                        partitions.add(partition);
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
                    path.append(getPathElement(json, name, index, start, text.length()));
                    TextNode payload = TextNode.valueOf(text.substring(start));
                    Partition partition = new Partition(path.copy(), payload);
                    partitions.add(partition);
                }
            }
        } else {
            // Все другие листья уйдут как есть
            path.append(getPathElement(json, name, index, PartitionPath.NOT_PARTITIONED_START_END, PartitionPath.NOT_PARTITIONED_START_END));
            PartitionPath currentPath = path.copy();
            Partition partition = new Partition(currentPath, json.deepCopy());
            partitions.add(partition);
        }
        path.removeLast();
    }

    private PartitionPath.Element getPathElement(JsonNode json, String name, int index, int start, int end) {
        PartitionPath.ElementKind kind = PartitionPath.ElementKind.getMatchedType(json);
        return new PartitionPath.Element(kind, name, index, start, end);
    }

    private long stringByteSize(String s) {
        return s == null ? 0 : s.getBytes(StandardCharsets.UTF_8).length;
    }

    private int codePointUTF8BytesSize(int cp) {
        if (cp <= 0x7F) return 1;
        if (cp <= 0x7FF) return 2;
        if (cp <= 0xFFFF) return 3;
        else return 4;
    }
}
