package node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class NodeTest {

    public static final String JSON = """
            {
              "orders": [
                {
                  "id": "ORD-001",
                  "date": "2024-01-15",
                  "items": [
                    {
                      "product": "laptop",
                      "price": 999.99
                    },
                    {
                      "product": "mouse",
                      "price": 29.99
                    }
                  ]
                },
                {
                  "id": "ORD-002",
                  "date": "2024-01-16",
                  "items": [
                    {
                      "product": "keyboard",
                      "price": 89.99
                    },
                    {
                      "product": "monitor",
                      "price": 299.99
                    }
                  ]
                }
              ],
              "metadata": {
                "version": "1.0",
                "timestamp": "2024-01-20T10:30:00Z"
              }
            }
            """;

    private final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Test
    public void test() {
        JsonNode json = mapper.readTree(JSON);
        Node node = Node.fromJson(json);

        System.out.println(mapper.writeValueAsString(node));
    }
}
