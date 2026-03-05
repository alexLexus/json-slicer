package node;

import lombok.EqualsAndHashCode;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
public class ArrayNode extends NodeBase {

    @EqualsAndHashCode.Exclude
    private final Map<Integer, Node> children = new TreeMap<>();

    protected ArrayNode(String name, int index) {
        super(Type.ARRAY, name, index);
    }

    @Override
    public long getWeight() {
        return children.values().stream()
                .mapToLong(Node::getWeight)
                .sum();
    }

    @Override
    public void append(Node node) {
        int index = node.getIndex();
        children.put(index, node);
        // node.setParent(this);
    }

    @Override
    public void remove(Node node) {
        int index = node.getIndex();
        if (children.remove(index) != null) {
            // node.setParent(null);
        }
    }

    @Override
    public List<Node> getChildren() {
        return List.copyOf(children.values());
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
            int index = toAppend.getIndex();
            if (children.containsKey(index)) {
                children.get(index).merge(toAppend);
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
