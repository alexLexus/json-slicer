package local.lex.slice.serde;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import local.lex.slice.NodePath;

import java.io.IOException;

public class NodePathSerializer extends JsonSerializer<NodePath> {

    @Override
    public void serialize(NodePath value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (NodePath.Fragment fragment : value.getFragments()) {
            String key = fragment.hasName() ? fragment.getName() : String.valueOf(fragment.getIndex());
            sb.append(fragment.getNode().getType().getCode()).append(":[").append(key).append("]");
        }
        gen.writeString(sb.toString());
    }
}
