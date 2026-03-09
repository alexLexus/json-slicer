package local.lex.slice;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import local.lex.slice.serde.NodePathSerializer;
import lombok.Data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

@Data
@JsonSerialize(using = NodePathSerializer.class)
public final class NodePath implements Comparable<NodePath> {

    @Data
    public static class Fragment implements Comparable<Fragment> {

        private final String name;

        private final int index;

        private Node node;

        public boolean hasName() {
            return name != null;
        }

        @Override
        public int compareTo(Fragment other) {
            if (this == other) {
                return 0;
            }

            if (this.name == null && other.name != null) {
                return -1;
            }

            if (this.name != null && other.name == null) {
                return 1;
            }

            if (this.name != null) {
                int cmp = this.name.compareTo(other.name);
                if (cmp != 0) {
                    return cmp;
                }
            }

            return Integer.compare(this.index, other.index);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof Fragment other)) {
                return false;
            }

            return Objects.equals(this.name, other.name) && this.index == other.index;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, index);
        }
    }

    private final LinkedList<Fragment> fragments = new LinkedList<>();

    public void append(Fragment fragment) {
        Objects.requireNonNull(fragment, "Can't add null fragment");
        fragments.addLast(fragment);
    }

    public void prepend(Fragment fragment) {
        Objects.requireNonNull(fragment, "Can't add null fragment");
        fragments.addFirst(fragment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NodePath other)) {
            return false;
        }

        return fragments.equals(other.fragments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fragments);
    }

    @Override
    public int compareTo(NodePath other) {
        Iterator<Fragment> it1 = this.fragments.iterator();
        Iterator<Fragment> it2 = other.fragments.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            Fragment f1 = it1.next();
            Fragment f2 = it2.next();
            int cmp = f1.compareTo(f2);
            if (cmp != 0) {
                return cmp;
            }
        }

        return Integer.compare(this.fragments.size(), other.fragments.size());
    }
}
