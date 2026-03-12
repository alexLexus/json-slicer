package local.lex.slice.v2.json;

import java.util.LinkedList;

public record ChunkPath(LinkedList<PathFragment> fragments) {
    public static final String ROOT_NAME = "$";

    public enum PathFragmentKind {
        ARRAY, OBJECT, VALUE
    }

    public record PathFragment(PathFragmentKind kind, String name, Integer index) {

    }
}
