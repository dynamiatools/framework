package tools.dynamia.domain.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.URLable;
import tools.dynamia.domain.AbstractEntity;

import java.io.IOException;

@SuppressWarnings("ALL")
public class BasicEntityJsonDeserializer extends StdDeserializer<AbstractEntity> {

    public BasicEntityJsonDeserializer() {
        this(null);
    }

    public BasicEntityJsonDeserializer(Class<?> vc) {
        super(vc);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractEntity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        JsonNode node = p.getCodec().readTree(p);

        String className = node.get(BasicEntityJsonSerializer.CLASS).asText();
        String name = node.get(BasicEntityJsonSerializer.NAME).asText();
        Long id = node.get(BasicEntityJsonSerializer.ID).asLong();
        String url = null;
        var urlNode = node.get(BasicEntityJsonSerializer.URL);
        if (urlNode != null) {
            url = urlNode.asText();
        }

        var result = ObjectOperations.newInstance(className);
        if (result instanceof AbstractEntity entity) {
            //noinspection unchecked
            entity.setId(id);
            entity.name(name);
            if (url != null && entity instanceof URLable) {
                ((URLable) entity).url(url);
            }
            return (AbstractEntity) result;
        }


        return null;
    }
}
