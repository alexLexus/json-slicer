package local.lex.slice.serde;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import local.lex.slice.NodePath;

import java.io.IOException;

public class NodePathSerializer extends JsonSerializer<NodePath> {

    @Override
    public void serialize(NodePath value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.toString());
    }
}
