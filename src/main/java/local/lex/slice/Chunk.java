package local.lex.slice;

public record Chunk(byte[] data, int order) implements Comparable<Chunk> {

    @Override
    public int compareTo(Chunk other) {
        if (this == other) {
            return 0;
        }

        return Integer.compare(this.order, other.order());
    }
}
