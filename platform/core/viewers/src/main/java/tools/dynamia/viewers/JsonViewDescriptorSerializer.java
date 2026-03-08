/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tools.dynamia.viewers;


import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.commons.Formatters;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.URLable;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.commons.reflect.ReflectionException;
import tools.dynamia.domain.EntityReference;
import tools.dynamia.domain.EntityReferenceRepository;
import tools.dynamia.domain.Reference;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.viewers.util.Viewers;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.databind.util.StdDateFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Date;

public class JsonViewDescriptorSerializer extends StdSerializer<Object> {

    private final ViewDescriptor viewDescriptor;
    private final StdDateFormat fullDateFormat = new StdDateFormat();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final DateFormat basicDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");


    private final static LoggingService LOGGER = new SLF4JLoggingService(JsonViewDescriptorSerializer.class);

    public JsonViewDescriptorSerializer(ViewDescriptor viewDescriptor) {
        this(viewDescriptor, null);

    }

    public JsonViewDescriptorSerializer(ViewDescriptor viewDescriptor, Class<Object> t) {
        super(t);
        this.viewDescriptor = viewDescriptor;
    }


    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext provider) {
        if (viewDescriptor == null) {
            return;
        }
        gen.writeStartObject();
        Object id = DomainUtils.findEntityId(value);
        if (id != null) {
            writeField(gen, "id", id);
        }
        // writeField(gen, "name", value.toString());

        for (Field field : Viewers.getFields(viewDescriptor)) {
            PropertyInfo fieldInfo = field.getPropertyInfo();
            if (field.isCollection() && fieldInfo != null) {
                Collection collection = null;
                try {
                    collection = (Collection) ObjectOperations.invokeGetMethod(value, field.getName());
                    collection.isEmpty();
                } catch (Throwable e) {
                    if (field.isEntity()) {
                        String parentName = ObjectOperations.findParentPropertyName(viewDescriptor.getBeanClass(), fieldInfo.getGenericType());
                        if (parentName != null) {
                            collection = DomainUtils.lookupCrudService().find(fieldInfo.getGenericType(), parentName, value);
                        }
                    } else {
                        collection = null;
                        LOGGER.warn("Cannot serialize collection " + field.getName() + "of class " + viewDescriptor.getBeanClass() + ": " + e.getMessage());
                    }
                }

                if (collection != null && !collection.isEmpty()) {
                    gen.writeStartArray(field.getName());
                    ViewDescriptor collectionDescriptor = getFieldViewDescriptor(fieldInfo.getGenericType());
                    JsonViewDescriptorSerializer collectionSerializer = new JsonViewDescriptorSerializer(collectionDescriptor);
                    for (Object item : collection) {
                        collectionSerializer.serialize(item, gen, provider);
                    }
                    gen.writeEndArray();
                }
            } else {

                String fieldName = field.getName();
                try {
                    Object fieldValue = field.getPropertyInfo() != null && field.getPropertyInfo().is(boolean.class) ? ObjectOperations.invokeBooleanGetMethod(value, field.getName()) : ObjectOperations.invokeGetMethod(value, fieldName);
                    if (fieldInfo != null && fieldInfo.isAnnotationPresent(Reference.class)) {
                        Reference reference = fieldInfo.getAnnotation(Reference.class);
                        EntityReferenceRepository repository = DomainUtils.getEntityReferenceRepositoryByAlias(reference.value());
                        @SuppressWarnings("unchecked") EntityReference entityReference = repository.load((Serializable) fieldValue);
                        if (entityReference != null) {
                            gen.writeStartObject(fieldName);
                            writeField(gen, "id", entityReference.getId());
                            writeField(gen, "name", entityReference.getName());
                            gen.writeEndObject();
                        } else {
                            writeField(gen, fieldName, fieldValue);
                        }
                    } else {
                        writeField(gen, fieldName, fieldValue);
                    }
                } catch (ReflectionException e) {
                    LOGGER.warn("Cannot write field " + fieldName + " to json: " + e.getMessage());
                }

            }
        }

        gen.writeEndObject();

    }

    private ViewDescriptor getFieldViewDescriptor(Class clazz) {
        ViewDescriptor descriptor = Viewers.findViewDescriptor(clazz, "json");

        if (descriptor == null) {
            descriptor = Viewers.findViewDescriptor(clazz, "tree");
        }

        if (descriptor == null) {
            descriptor = Viewers.getViewDescriptor(clazz, "table");
        }
        return descriptor;
    }

    private void writeField(JsonGenerator gen, String fieldName, Object fieldValue) {

        if (fieldValue instanceof Integer) {
            gen.writeNumberProperty(fieldName, (Integer) fieldValue);
        } else if (fieldValue instanceof Long) {
            gen.writeNumberProperty(fieldName, (Long) fieldValue);
        } else if (fieldValue instanceof Double) {
            gen.writeNumberProperty(fieldName, (Double) fieldValue);
        } else if (fieldValue instanceof Float) {
            gen.writeNumberProperty(fieldName, (Float) fieldValue);
        } else if (fieldValue instanceof BigDecimal) {
            gen.writeNumberProperty(fieldName, (BigDecimal) fieldValue);
        } else if (fieldValue instanceof String) {
            gen.writeStringProperty(fieldName, (String) fieldValue);
        } else if (fieldValue instanceof Date) {
            writeDateField(gen, fieldName, (Date) fieldValue);
        } else if (fieldValue instanceof TemporalAccessor) {
            writeTemporalField(gen, fieldName, (TemporalAccessor) fieldValue);
        } else if (fieldValue instanceof Boolean) {
            gen.writeBooleanProperty(fieldName, (Boolean) fieldValue);
        } else if (DomainUtils.isEntity(fieldValue)) {
            writeEntity(gen, fieldName, fieldValue);
        } else if (fieldValue != null) {
            gen.writePOJOProperty(fieldName, fieldValue);
        }
    }

    private void writeEntity(JsonGenerator gen, String fieldName, Object entity) {
        Field field = viewDescriptor.getField(fieldName);
        if (field.getParams().get("include") == Boolean.TRUE) {
            gen.writeName(fieldName);
            ViewDescriptor fieldDescritor = getFieldViewDescriptor(field.getFieldClass());
            JsonViewDescriptorSerializer serializer = new JsonViewDescriptorSerializer(fieldDescritor);
            serializer.serialize(entity, gen, null);
        } else {
            gen.writeStartObject(fieldName);
            Object id = DomainUtils.findEntityId(entity);
            if (id != null) {
                writeField(gen, "id", id);
            }
            gen.writeStringProperty("name", entity.toString());
            if (entity instanceof URLable) {
                gen.writeStringProperty("url", ((URLable) entity).toURL());
            }
            gen.writeEndObject();
        }
    }

    private void writeDateField(JsonGenerator gen, String fieldName, Date date) {

        Field field = viewDescriptor.getField(fieldName);
        String format = "basic";
        if (field != null && field.getParams().containsKey("format")) {
            format = (String) field.getParams().get("format");
        } else if ("creationDate".equals(fieldName) || "lastUpdate".equals(fieldName)) {
            format = "date";
        } else if ("creationTime".equals(fieldName)) {
            format = "time";
        }

        DateFormat df = switch (format) {
            case "ISO8601" -> fullDateFormat;
            case "date" -> dateFormat;
            case "time" -> timeFormat;
            default -> basicDateFormat;
        };


        gen.writeStringProperty(fieldName, df.format(date));
    }

    private void writeTemporalField(JsonGenerator gen, String fieldName, TemporalAccessor value) {

        Field field = viewDescriptor.getField(fieldName);
        String format = null;
        if (field != null && field.getParams().containsKey("format")) {
            format = (String) field.getParams().get("format");
        }

        String valueStr = null;
        if (format != null && !format.isEmpty()) {
            valueStr = DateTimeUtils.getFormatter(format).format(value);
        } else {
            valueStr = switch (value) {
                case java.time.LocalDateTime localDateTime -> Formatters.formatDateTime(localDateTime);
                case java.time.LocalDate localDate -> Formatters.formatDate(localDate);
                case java.time.LocalTime localTime -> Formatters.formatTime(localTime);
                case java.time.ZonedDateTime zonedDateTime -> Formatters.formatZonedDateTime(zonedDateTime);
                case java.time.OffsetDateTime offsetDateTime -> Formatters.formatOffsetDateTime(offsetDateTime);
                case java.time.Instant instant -> Formatters.formatInstant(instant);
                default -> value.toString();
            };
        }

        gen.writeStringProperty(fieldName, valueStr);
    }

    static class FieldMetadata {

        private String label;
        private String description;
        private String type;

        public FieldMetadata(Field field) {
            this.label = field.getLabel();
            this.description = field.getDescription();
            this.type = field.getFieldClass().getSimpleName();
        }


        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

}
