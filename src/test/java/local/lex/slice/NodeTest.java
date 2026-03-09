package local.lex.slice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import local.lex.slice.utils.NodeUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

class NodeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Test
    public void test() {
        String input = """
                {
                    "test": 1,
                    "second": {
                        "prop1":"100",
                        "prop2":["item", 100, true],
                        "prop3":{
                            "field": "value value value value value value"
                        }
                    }
                }
                """;
        JsonNode json = mapper.readTree(input);
        Node root = NodeUtils.read(json);

        List<FlatNode> flat = NodeUtils.flattering(root);
        System.out.println(mapper.writeValueAsString(flat));
        System.out.println();

        System.out.println(NodeUtils.prettyPrint(root));
        System.out.println();

        List<Node> slices = new ArrayList<>();
        while (root.weight() > 0) {
            Node node = root.slice(5);
            slices.add(node);
            System.out.println(NodeUtils.prettyPrint(node));
            System.out.println();
        }

        slices.sort(Comparator.reverseOrder());
        Node merged = new Node(root.getType(), root.getPath());
        for (Node slice: slices) {
            merged.merge(slice);
        }
        System.out.println(NodeUtils.prettyPrint(merged));
        System.out.println();
    }
}