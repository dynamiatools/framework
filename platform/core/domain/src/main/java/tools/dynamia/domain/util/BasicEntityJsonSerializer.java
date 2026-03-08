package tools.dynamia.domain.util;


import tools.dynamia.commons.URLable;
import tools.dynamia.domain.AbstractEntity;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.jackson.JsonValueSerializer;

import java.util.Set;

public class BasicEntityJsonSerializer extends JsonValueSerializer {

    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String CLASS = "class";
    public static final String URL = "url";


    public BasicEntityJsonSerializer(JavaType nominalType, JavaType valueType, boolean staticTyping, TypeSerializer vts, ValueSerializer<?> ser, AnnotatedMember accessor, Set<String> ignoredProperties) {
        super(nominalType, valueType, staticTyping, vts, ser, accessor, ignoredProperties);
    }

    public BasicEntityJsonSerializer(JsonValueSerializer src, BeanProperty property, TypeSerializer vts, ValueSerializer<?> ser, boolean forceTypeInfo) {
        super(src, property, vts, ser, forceTypeInfo);
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext context) {
        if (value instanceof AbstractEntity<?> entity && entity.getId() != null) {
            gen.writeStartObject();
            gen.writeStringProperty(CLASS, entity.getClass().getName());
            gen.writeStringProperty(NAME, entity.toName());
            if (entity.getId() instanceof Long) {
                gen.writeNumberProperty(ID, (Long) entity.getId());
            } else {
                gen.writeStringProperty(ID, entity.getId().toString());
            }

            if (value instanceof URLable) {
                gen.writeStringProperty(URL, ((URLable) value).toURL());
            }


            gen.writeEndObject();
        }
    }
}
