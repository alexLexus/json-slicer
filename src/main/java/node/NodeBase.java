package node;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public abstract class NodeBase implements Node {

    @Getter
    protected final Type type;

    @Getter
    protected final String name;

    @Getter
    protected final int index;

    // @Getter
    // @Setter
    // @EqualsAndHashCode.Exclude
    // @JsonIgnore
    // protected Node parent;


    protected NodeBase(Type type, String name, int index) {
        this.type = type;
        this.name = name;
        this.index = index;
    }

    @Override
    public boolean isContainer() {
        return type == Type.ARRAY || type == Type.OBJECT;
    }

    @Override
    public boolean isValue() {
        return type == Type.VALUE;
    }

    @Override
    public int compareTo(Node o) {
        return Long.compare(getWeight(), o.getWeight());
    }
}
