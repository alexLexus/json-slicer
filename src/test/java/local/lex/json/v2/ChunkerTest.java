package local.lex.json.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ChunkerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void whenChunkTextNode_ThenChunkedCorrect() {
        String input = """
                Кириллица: Привет, как дела? Это тестовый абзац для проверки работы системы. 
                Latin: Hello, this is a sample paragraph mixing languages to ensure UTF-8 handling. 
                Numbers: 0123456789 2026 3.14 100% 
                Комбинации: тест123 test456 смешениеABC и цифры7890. 
                Дополнение для длины: ещё немного текста, чтобы суммарная длина достигла примерно семисот знаков — 
                добавляем фразы, повторяем идеи, вставляем разнообразные символы: © ™ ✓ — и пробелы.
                """;
        JsonNode json = TextNode.valueOf(input);
        DataSize limit = DataSize.ofBytes(100);
        Chunker chunker = new Chunker();

        List<Chunk> chunks = chunker.chunk(json, limit);

        int maxSize = chunks.stream()
                .map(Chunk::data)
                .map(it -> it.asText().getBytes(StandardCharsets.UTF_8).length)
                .max(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);

        String output = chunks.stream()
                .map(Chunk::data)
                .map(JsonNode::asText)
                .collect(Collectors.joining());

        assertFalse(chunks.isEmpty());
        assertTrue(limit.toBytes() >= maxSize);
        assertEquals(input, output);
    }

    @SneakyThrows
    @Test
    public void whenChunkObject_thenChunkCorrect() {
        // language=json
        String input = """
                {
                  "first": {
                    "property": "value"
                  },
                  "second": {
                    "sub_object_1": {
                      "name": "name value",
                      "array": [ 1, 2, 3 ],
                      "sub_object_2": {
                        "prop": {
                          "kind": "ARRAY",
                          "value": "\\"[3,2,1]\\""
                        }
                      }
                    }
                  }
                }
                """;
        JsonNode json = mapper.readTree(input);
        DataSize limit = DataSize.ofBytes(5);
        Chunker chunker = new Chunker();

        List<Chunk> chunks = chunker.chunk(json, limit);
        chunks.forEach(System.out::println);

        int maxSize = chunks.stream()
                .map(Chunk::data)
                .map(it -> it.asText().getBytes(StandardCharsets.UTF_8).length)
                .max(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);

        assertFalse(chunks.isEmpty());
        assertTrue(limit.toBytes() >= maxSize);
    }
}