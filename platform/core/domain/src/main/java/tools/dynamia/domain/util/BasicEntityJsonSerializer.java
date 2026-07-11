package tools.dynamia.domain.util;


import tools.dynamia.commons.URLable;
import tools.dynamia.domain.AbstractEntity;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class BasicEntityJsonSerializer extends ValueSerializer<Object> {

    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String CLASS = "class";
    public static final String URL = "url";

    public BasicEntityJsonSerializer() {
    }

    @Override
    public void serialize(Object value,
                          JsonGenerator gen,
                          SerializationContext context) {

        if (value instanceof AbstractEntity<?> entity
                && entity.getId() != null) {

            gen.writeStartObject();

            gen.writeStringProperty(CLASS, entity.getClass().getName());
            gen.writeStringProperty(NAME, entity.toName());

            if (entity.getId() instanceof Long l) {
                gen.writeNumberProperty(ID, l);
            } else {
                gen.writeStringProperty(ID, entity.getId().toString());
            }

            if (value instanceof URLable urlable) {
                gen.writeStringProperty(URL, urlable.toURL());
            }

            gen.writeEndObject();
        } else {
            gen.writeNull();
        }
    }
}
