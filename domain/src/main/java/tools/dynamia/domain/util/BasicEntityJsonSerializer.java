package tools.dynamia.domain.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import tools.dynamia.commons.URLable;
import tools.dynamia.domain.AbstractEntity;

import java.io.IOException;

public class BasicEntityJsonSerializer extends JsonSerializer<AbstractEntity> {

    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String CLASS = "class";
    public static final String URL = "url";

    @Override
    public void serialize(AbstractEntity value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null && value.getId() != null) {
            gen.writeStartObject();
            gen.writeStringField(CLASS, value.getClass().getName());
            gen.writeStringField(NAME, value.toName());
            if (value.getId() instanceof Long) {
                gen.writeNumberField(ID, (Long) value.getId());
            } else {
                gen.writeStringField(ID, value.getId().toString());
            }

            if (value instanceof URLable) {
                gen.writeStringField(URL, ((URLable) value).toURL());
            }


            gen.writeEndObject();
        }
    }
}
