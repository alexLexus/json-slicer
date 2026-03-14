package local.lex.json;

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

class PartitionerImplTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Test
    public void test() {
        DataSize limit = DataSize.ofBytes(100);
        String input = """
                Кириллица: Привет, как дела? Это тестовый абзац для проверки работы системы. 
                Latin: Hello, this is a sample paragraph mixing languages to ensure UTF-8 handling. 
                Numbers: 0123456789 2026 3.14 100% 
                Комбинации: тест123 test456 смешениеABC и цифры7890. 
                Дополнение для длины: ещё немного текста, чтобы суммарная длина достигла примерно семисот знаков — 
                добавляем фразы, повторяем идеи, вставляем разнообразные символы: © ™ ✓ — и пробелы.
                """;
        JsonNode json = TextNode.valueOf(input);

        Partitioner partitioner = new PartitionerImpl();
        List<Partition> partitions = partitioner.split(json, limit);

        int maxSize = partitions.stream().map(Partition::data)
                .map(it -> it.asText().getBytes(StandardCharsets.UTF_8).length)
                .max(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);

        String output = partitions.stream()
                .map(Partition::data)
                .map(JsonNode::asText)
                .collect(Collectors.joining());

        assertFalse(partitions.isEmpty());
        assertTrue(limit.toBytes() >= maxSize);
        assertEquals(input, output);
    }
}