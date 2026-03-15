package local.lex.json.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssemblerTest {

    @Test
    public void test() {
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
        Assembler assembler = new Assembler();

        List<Chunk> chunks = chunker.chunk(json, limit);
        assembler.assemble(chunks);
    }
}