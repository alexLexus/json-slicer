package node;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO описать
 */
public interface Node extends Comparable<Node>, Weightable {

    class JsonWalker {

        private static Node traverse(JsonNode json) {
            return traverse(json, null, 0);
        }

        private static Node traverse(JsonNode json, String name, int index) {
            Node node;
            switch (json.getNodeType()) {
                case OBJECT -> {
                    node = createEquivalentNode(json, name, index);
                    int ind = 0;
                    for (Map.Entry<String, JsonNode> prop : json.properties()) {
                        Node child = traverse(prop.getValue(), prop.getKey(), ind++);
                        node.append(child);
                    }
                }
                case ARRAY -> {
                    node = createEquivalentNode(json, name, index);
                    for (int i = 0; i < json.size(); i++) {
                        Node child = traverse(json.get(i), null, i);
                        node.append(child);
                    }
                }
                default -> node = createEquivalentNode(json, name, index);
            }

            return node;
        }

        private static Node createEquivalentNode(JsonNode json, String name, int index) {
            return switch (json.getNodeType()) {
                case ARRAY -> new ArrayNode(name, index);
                case OBJECT -> new ObjectNode(name, index);
                default -> new ValueNode(name, index, json.asText());
            };
        }
    }

    record Chunk(String text, int order) implements Comparable<Chunk>, Weightable {

        @Override
        public int compareTo(Chunk o) {
            return Integer.compare(order, o.order);
        }

        public boolean isEmpty() {
            return text == null || text.isBlank();
        }

        @Override
        public long getWeight() {
            return isEmpty() ? 0 : text.getBytes(StandardCharsets.UTF_8).length;
        }
    }

    enum Type {
        ARRAY, OBJECT, VALUE
    }

    static Node fromJson(JsonNode json) {
        return JsonWalker.traverse(json);
    }

    Type getType();

    String getName();

    int getIndex();

    // @JsonIgnore
    // Node getParent();

    // void setParent(Node parent);

    void append(Node node);

    void remove(Node node);

    void merge(Node node);

    List<Node> getChildren();

    String getText();

    Set<Chunk> getChunks();

    Node nipOff(long weight);

    boolean isEmpty();

    boolean isContainer();

    boolean isValue();
}