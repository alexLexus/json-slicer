package local.lex.slice.v2.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedList;

public record Chunk(ChunkPath path, JsonNode payload, Integer order) {

    public static Chunk empty() {
        return new Chunk(new ChunkPath(new LinkedList<>()), new ObjectNode(null), 0);
    }
}
