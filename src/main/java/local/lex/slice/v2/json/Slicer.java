package local.lex.slice.v2.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class Slicer {
    private static final Chunk EMPTY_CHUNK = Chunk.empty();
    private static Long CACHED_EMPTY_CHUNK_SIZE = null;

    private final Stack<ChunkPath.PathFragment> pathStack = new Stack<>();
    private final List<Chunk> out = new ArrayList<>();
    private final SizeMeter meter;
    private final long limit;

    public Slicer(SizeMeter meter, long limit) {
        this.meter = meter;
        this.limit = limit;
    }

    public void chunk(JsonNode node) throws Exception {
        traverse(node, ChunkPath.ROOT_NAME, 0);
    }

    public List<Chunk> result() {
        return out;
    }

    private void traverse(JsonNode node, String name, int index) throws Exception {
        ChunkPath.PathFragmentKind kind = getFragmentKind(node);
        ChunkPath.PathFragment currentFragment = getCurrentFragment(kind, name, index);
        pathStack.push(currentFragment);
        switch (kind) {
            case ARRAY -> {
                int ind = 0;
                for (JsonNode child : node) {
                    traverse(child, name, ind++);
                }
            }
            case OBJECT -> {
                int ind = 0;
                for (Map.Entry<String, JsonNode> child : node.properties()) {
                    traverse(child.getValue(), child.getKey(), ind++);
                }
            }
            case VALUE -> {
                ChunkPath path = getCurrentPath();
                Chunk chunk;
                long nodeSize = meter.sizeOf(node);
                long pathSize = meter.sizeOf(path);
                long remaining = limit - (nodeSize + pathSize - emptyChunkSize());
                if (remaining >= 0) {
                    chunk = new Chunk(path, node.deepCopy(), 0);
                    out.add(chunk);
                } else {
                    long sliceSize = limit - pathSize - emptyChunkSize();
                    String str = node.asText();
                    List<String> slices = splitByBytes(str, (int) sliceSize);
                    int order = 0;
                    for (String s : slices) {
                        TextNode text = TextNode.valueOf(s);
                        chunk = new Chunk(path, text, order++);
                        out.add(chunk);
                    }
                }
            }
        }
        pathStack.pop();
    }

    private ChunkPath.PathFragment getCurrentFragment(ChunkPath.PathFragmentKind kind, String name, int index) {
        return new ChunkPath.PathFragment(kind, name, index);
    }

    private ChunkPath getCurrentPath() {
        return new ChunkPath(new LinkedList<>(pathStack));
    }

    private ChunkPath.PathFragmentKind getFragmentKind(JsonNode node) {
        return switch (node.getNodeType()) {
            case OBJECT -> ChunkPath.PathFragmentKind.OBJECT;
            case ARRAY -> ChunkPath.PathFragmentKind.ARRAY;
            default -> ChunkPath.PathFragmentKind.VALUE;
        };
    }

    // LLM
    public static List<String> splitByBytes(String input, int maxBytes) {
        List<String> parts = new ArrayList<>();
        if (input == null || input.isEmpty() || maxBytes <= 0) return parts;

        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        int startByte = 0;
        int len = bytes.length;

        while (startByte < len) {
            int endByte = Math.min(startByte + maxBytes, len);

            // Не ломаем UTF-8 символы: откатываемся, если попали в середину многобайтового символа
            while (endByte > startByte && (bytes[endByte - 1] & 0xC0) == 0x80) {
                endByte--;
            }
            if (endByte == startByte) {
                endByte = Math.min(startByte + 1, len);
            }

            String candidate = new String(bytes, startByte, endByte - startByte, StandardCharsets.UTF_8);

            // Если граница внутри слова (последний символ candidate не пробел) и не конец всей строки,
            // ищем ближайший пробел слева внутри candidate
            if (endByte < len && !Character.isWhitespace(candidate.charAt(candidate.length() - 1))) {
                int lastSpace = -1;
                for (int i = candidate.length() - 1; i >= 0; i--) {
                    if (Character.isWhitespace(candidate.charAt(i))) {
                        lastSpace = i;
                        break;
                    }
                }

                if (lastSpace != -1) {
                    // Включаем найденный пробел в предыдущую часть (т.е. оставляем пробелы)
                    String part = candidate.substring(0, lastSpace + 1);
                    parts.add(part);

                    // Считаем байтовую длину префикса (включая пробел) и сдвигаем startByte
                    String prefixWithSpace = candidate.substring(0, lastSpace + 1);
                    int prefixBytes = prefixWithSpace.getBytes(StandardCharsets.UTF_8).length;
                    startByte += prefixBytes;
                    continue;
                }
                // Если пробела нет — оставляем candidate целиком (разрыв внутри слова)
            }

            // Добавляем candidate целиком (сохранены все пробелы)
            parts.add(candidate);
            startByte = endByte;
        }

        return parts;
    }

    private long emptyChunkSize() throws Exception {
        if (CACHED_EMPTY_CHUNK_SIZE == null) {
            CACHED_EMPTY_CHUNK_SIZE = meter.sizeOf(EMPTY_CHUNK);
        }
        return CACHED_EMPTY_CHUNK_SIZE;
    }
}
