package tools.dynamia.domain.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.util.BeanUtil;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.URLable;
import tools.dynamia.domain.AbstractEntity;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BasicEntityJsonDeserializer extends StdDeserializer<AbstractEntity> {

    public BasicEntityJsonDeserializer() {
        this(null);
    }

    public BasicEntityJsonDeserializer(Class<?> vc) {
        super(vc);
    }

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

        var result = BeanUtils.newInstance(className);
        if (result instanceof AbstractEntity) {
            var entity = (AbstractEntity) result;
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
