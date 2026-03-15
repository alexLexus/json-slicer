package local.lex.json.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.LinkedList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class ChunkPath {
    public static final String ROOT_NAME = "$";
    public static final int ROOT_INDEX = -1;

    @Builder
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Fragment(@JsonProperty("kind") Kind kind,
                           @JsonProperty("name") String name,
                           @JsonProperty("index") Integer index,
                           @JsonProperty("start") Integer start,
                           @JsonProperty("end") Integer end) {
    }

    public enum Kind {
        ARRAY, OBJECT, VALUE, CHUNK
    }

    @Builder.Default
    private final int contactVersion = 1;

    @Builder.Default
    private final LinkedList<Fragment> fragments = new LinkedList<>();
}
