package local.lex.json.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record Chunk(@JsonProperty("path") ChunkPath path,
                    @JsonProperty("data") JsonNode data) {

}
