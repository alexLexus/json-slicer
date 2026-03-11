package local.lex.slice.utils;

import com.fasterxml.jackson.databind.JsonNode;
import local.lex.slice.Chunk;
import local.lex.slice.FlatChunk;
import local.lex.slice.Node;
import local.lex.slice.NodePath;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class NodeUtils {

    public static Node read(JsonNode json) {
        return traverse(json, "$", 0);
    }

    public static List<FlatChunk> flattering(Node root) {
        List<FlatChunk> flatNodes = new ArrayList<>();
        Stack<Node> stack = new Stack<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (node.getType() == Node.Type.STRING) {
                NodePath path = node.pathToParent();
                for (Chunk chunk : node.getChunks()) {
                    FlatChunk flatNode = new FlatChunk(path, new String(chunk.data()), chunk.order());
                    flatNodes.add(flatNode);
                }
            } else {
                node.getChildren().values().forEach(stack::push);
            }
        }
        return flatNodes;
    }

    public static String prettyPrint(Node node) {
        StringBuilder sb = new StringBuilder();
        prettyPrint(node, sb, 0);
        return sb.toString();
    }

    private static Node traverse(JsonNode json, String name, int index) {
        Node node;
        if (json.isArray()) {
            node = new Node(Node.Type.ARRAY, name, index);
            for (int i = 0; i < json.size(); i++) {
                Node child = traverse(json.get(i), null, i);
                node.append(child);
            }
        } else if (json.isObject()) {
            node = new Node(Node.Type.OBJECT, name, index);
            int i = 0;
            for (Map.Entry<String, JsonNode> entry : json.properties()) {
                Node child = traverse(entry.getValue(), entry.getKey(), i++);
                node.append(child);
            }
        } else {
            byte[] data = json.asText().getBytes(StandardCharsets.UTF_8);
            Chunk chunk = new Chunk(data, 0);
            node = new Node(Node.Type.STRING, name, index);
            node.addChunk(chunk);
        }

        return node;
    }

    private static void prettyPrint(Node node, StringBuilder sb, int indent) {
        if (node == null) {
            return;
        }
        indent(sb, indent);
        // header: path, type, weight
        String key = node.getPath().hasName() ? node.getPath().name() : String.valueOf(node.getPath().index());
        sb.append(key)
                .append("(").append(node.getType()).append(")")
                .append("[").append(node.weight()).append("]")
                .append(System.lineSeparator());
        // chunks (if any)
        if (!node.getChunks().isEmpty()) {
            indent(sb, indent + 2);
            sb.append("chunks:");
            List<String> chunks = node.getChunks().stream()
                    .sorted(Comparator.naturalOrder())
                    .map(c -> "[" + c.order() + "]=" + new String(c.data()))
                    .collect(Collectors.toList());
            sb.append(String.join(", ", chunks));
            sb.append(System.lineSeparator());
        }
        // children (sorted)
        if (!node.getChildren().isEmpty()) {
            List<Node> kids = node.getChildren().values().stream()
                    .sorted(Comparator.naturalOrder())
                    .toList();
            for (Node child : kids) {
                prettyPrint(child, sb, indent + 2);
            }
        }
    }

    private static void indent(StringBuilder sb, int count) {
        sb.append(" ".repeat(Math.max(0, count)));
    }
}
