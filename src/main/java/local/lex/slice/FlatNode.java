package local.lex.slice;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record FlatNode(NodePath path, String data, int order) {

}
