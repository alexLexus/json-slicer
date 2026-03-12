package local.lex.slice.v2.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

import java.util.List;

class SlicerTest {
    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String JSON_STRING = """
            {
              "test": "Long strong with whitespaces to slice Long strong with whitespaces to slice Long strong with\\nwhitespaces to slice Long strong with whitespaces to slice Long strong with whitespaces to slice Long strong with whitespaces to slice Long strong with whitespaces to slice ",
              "node": {
                "field-1": "text",
                "field-2": {
                  "prop": 100
                }
              }
            }
            """;

    @Test
    public void test() throws Exception {
        JsonNode json = MAPPER.readTree(JSON_STRING);
        SizeMeter meter = new SizeMeterImpl(MAPPER);
        Slicer slicer = new Slicer(meter, DataSize.ofBytes(200).toBytes());

        slicer.chunk(json);
        List<Chunk> chunks = slicer.result();

        for (Chunk chunk : chunks) {
            System.out.println(MAPPER.writeValueAsString(chunk));
        }
    }
}