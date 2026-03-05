package node;

import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@EqualsAndHashCode(callSuper = true)
public class ObjectNode extends NodeBase {

    @EqualsAndHashCode.Exclude
    private final Map<String, Node> children = new TreeMap<>();

    protected ObjectNode(String name, int index) {
        super(Type.OBJECT, name, index);
    }

    @Override
    public long getWeight() {
        return children.values().stream()
                .mapToLong(Node::getWeight)
                .sum();
    }

    @Override
    public void append(Node node) {
        String name = node.getName();
        children.put(name, node);
        // node.setParent(this);
    }

    @Override
    public void remove(Node node) {
        String name = node.getName();
        if (children.remove(name) != null) {
           //  node.setParent(null);
        }
    }

    @Override
    public List<Node> getChildren() {
        return List.copyOf(children.values());
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public boolean isEmpty() {
        return children.isEmpty() || children.values().stream()
                .allMatch(Node::isEmpty);
    }

    @Override
    public void merge(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Cannot marge node with null");
        }

        if (node.getType() != type) {
            throw new IllegalArgumentException("Cannot merge nodes with different types: this type=" + type
                    + ", other type=" + node.getType());
        }

        for (Node toAppend : node.getChildren()) {
            String name = node.getName();
            if (children.containsKey(name)) {
                children.get(name).merge(toAppend);
            } else {
                append(toAppend);
            }
        }
    }

    @Override
    public Node nipOff(long weight) {
        return null;
    }
}
