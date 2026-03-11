package local.lex.slice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.*;

@Getter
@Setter
public class Node implements Comparable<Node> {

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        OBJECT('o'),
        ARRAY('a'),
        STRING('s');

        private final char code;
    }

    private final Type type;

    private final NodePath.Fragment path;

    private final Map<NodePath.Fragment, Node> children = new TreeMap<>();

    private final List<Chunk> chunks = new ArrayList<>();

    @JsonIgnore
    private Node parent;

    public Node(Node.Type type, String name, int index) {
        this.type = type;
        this.path = new NodePath.Fragment(type, name, index);
    }

    public int weight() {
        int partSize = chunks.isEmpty() ? 0 : chunks.stream().mapToInt(it -> it.data().length).sum();
        int childrenSize = children.values().stream().mapToInt(Node::weight).sum();
        return partSize + childrenSize;
    }

    public void append(Node child) {
        if (child == null) {
            return;
        }
        Node existing = children.get(child.getPath());
        if (existing != null) {
            existing.merge(child);
        } else {
            children.put(child.getPath(), child);
            child.setParent(this);
        }
    }

    public void remove(Node child) {
        if (child == null) {
            return;
        }
        Node existing = children.get(child.getPath());
        if (existing != null && existing.getType() == child.getType()) {
            remove(existing.getPath());
        }
    }

    public void remove(NodePath.Fragment path) {
        if (path == null) {
            return;
        }
        Node removed = children.remove(path);
        if (removed != null) {
            removed.setParent(null);
        }
    }

    public void merge(Node other) {
        if (other == null) {
            return;
        }
        if (!Objects.equals(this.path, other.getPath())) {
            throw new IllegalArgumentException("Can only merge slices with same path");
        }
        if (this.type != other.getType()) {
            throw new IllegalStateException("Type mismatch during merge: " + this.type + " vs " + other.getType());
        }
        mergeData(other);
        mergeChildren(other);
    }

    public Node slice(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Can only nip off part with size greater then zero");
        }
        if (this.type == Type.STRING) {
            return sliceChunk(size);
        } else {
            return sliceChildren(size);
        }
    }

    public void addChunk(Chunk chunk) {
        this.chunks.add(chunk);
    }

    @Override
    public int compareTo(Node o) {
        return Integer.compare(weight(), o.weight());
    }

    private void mergeData(Node other) {
        if (other == null || other.weight() <= 0) {
            return;
        }
        List<Chunk> otherData = other.getChunks();
        if (otherData == null || otherData.isEmpty()) {
            return;
        }

        if (this.chunks.isEmpty()) {
            this.chunks.addAll(otherData);
            this.chunks.sort(Comparator.naturalOrder());
            return;
        }

        List<Chunk> combined = new ArrayList<>(this.chunks);
        combined.addAll(otherData);
        combined.sort(Comparator.naturalOrder());

        int expectedOrder = combined.get(0).order();
        for (Chunk c : combined) {
            if (c.order() != expectedOrder) {
                throw new IllegalArgumentException("Can't merge data: unexpected chunk order sequence (got "
                        + c.order() + ", expected " + expectedOrder + ")");
            }
            expectedOrder++;
        }

        this.chunks.clear();
        this.chunks.addAll(combined);
    }

    public NodePath pathToParent() {
        NodePath path = new NodePath();
        Node node = this;
        while (node != null) {
            path.prepend(node.path);
            node = node.parent;
        }
        return path;
    }

    private void mergeChildren(Node other) {
        for (Node otherChild : other.getChildren().values()) {
            Node existing = this.children.get(otherChild.getPath());
            if (existing != null) {
                existing.merge(otherChild);
            } else {
                this.children.put(otherChild.getPath(), otherChild);
                otherChild.setParent(this);
            }
        }
    }

    private Node sliceChunk(int size) {
        if (this.getChunks().isEmpty()) {
            throw new IllegalStateException("No chunks to nip off");
        }
        if (this.getChunks().size() != 1) {
            throw new IllegalStateException("Can only nip off from single chunk with order 0: chunks="
                    + this.getChunks().size() + " order=" + this.getChunks().get(0).order());
        }

        Node copy = new Node(this.getType(), this.getPath().name(), this.getPath().index());
        Chunk original = this.chunks.get(0);
        byte[] data = original.data();
        if (size >= data.length) {
            copy.addChunk(original);
            this.chunks.clear();
        } else {
            byte[] toMove = Arrays.copyOfRange(data, 0, size);
            byte[] toStore = Arrays.copyOfRange(data, size, data.length);
            copy.addChunk(new Chunk(toMove, original.order()));
            this.chunks.clear();
            this.chunks.add(new Chunk(toStore, original.order() + 1));
        }
        return copy;
    }

    private Node sliceChildren(int size) {
        Node copy = new Node(this.getType(), this.getPath().name(), this.getPath().index());
        int remaining = size;
        List<Node> childrenCopy = new ArrayList<>(children.values());
        childrenCopy.sort(Comparator.naturalOrder());
        for (Node child : childrenCopy) {
            if (remaining <= 0) {
                break;
            }
            int childWidth = child.weight();
            if (remaining >= childWidth) {
                copy.append(child);
                this.remove(child.getPath());
            } else {
                Node sliced = child.slice(remaining);
                copy.append(sliced);
                if (child.weight() == 0) {
                    this.remove(child);
                }
            }
            remaining = size - copy.weight();
        }
        return copy;
    }
}