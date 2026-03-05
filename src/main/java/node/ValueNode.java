package node;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ValueNode extends NodeBase {

    private final Set<Chunk> chunks = new TreeSet<>();

    protected ValueNode(String name, int index, String text) {
        super(Type.VALUE, name, index);
        this.chunks.add(new Chunk(text, 0));
    }

    @Override
    public long getWeight() {
        return chunks.stream()
                .mapToLong(Weightable::getWeight)
                .sum();
    }

    @Override
    public void append(Node node) {
        throw new IllegalArgumentException("Can't append child to " + getType() + " node");
    }

    @Override
    public void remove(Node node) {
        throw new IllegalArgumentException("Can't remove child from " + getType() + " node");
    }

    @Override
    public List<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String getText() {
        return chunks.stream()
                .map(Chunk::text)
                .collect(Collectors.joining());
    }

    @Override
    public Set<Chunk> getChunks() {
        return Set.copyOf(chunks);
    }

    @Override
    public void merge(Node node) {
        if (node.getType() != Type.VALUE) {
            throw new IllegalArgumentException("Can't merge " + Type.VALUE + " with " + node.getType());
        }

        Set<Integer> hasOrders = chunks.stream()
                .map(Chunk::order)
                .collect(Collectors.toSet());

        Set<Chunk> newChunks = node.getChunks().stream()
                .filter(it -> !hasOrders.contains(it.order()))
                .collect(Collectors.toSet());

        chunks.addAll(newChunks);
    }

    @Override
    public Node nipOff(long weight) {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return chunks.isEmpty() || chunks.stream().allMatch(Chunk::isEmpty);
    }
}
