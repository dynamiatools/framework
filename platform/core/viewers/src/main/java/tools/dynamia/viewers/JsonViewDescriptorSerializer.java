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


import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonViewDescriptorSerializer extends StdSerializer<Object> {

    private static final LoggingService LOGGER = new SLF4JLoggingService(JsonViewDescriptorSerializer.class);

    private static final StdDateFormat FULL_DATE_FORMAT = new StdDateFormat();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final DateFormat BASIC_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final Map<Class<?>, ViewDescriptor> DESCRIPTOR_CACHE = new ConcurrentHashMap<>();

    private final ViewDescriptor viewDescriptor;

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

        // Group fields by their root prefix (part before the first dot).
        // Fields without a dot are stored under their own name as key.
        Map<String, List<Field>> groups = new LinkedHashMap<>();
        for (Field field : Viewers.getFields(viewDescriptor)) {
            String name = field.getName();
            String root = name.contains(".") ? name.substring(0, name.indexOf('.')) : name;
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(field);
        }

        for (Map.Entry<String, List<Field>> entry : groups.entrySet()) {
            List<Field> groupFields = entry.getValue();
            boolean isPathGroup = groupFields.size() > 1 || groupFields.get(0).getName().contains(".");

            if (isPathGroup) {
                // All fields in this group are path-based; render them as a nested object
                serializePathGroup(entry.getKey(), groupFields, value, gen, provider);
            } else {
                Field field = groupFields.get(0);
                PropertyInfo fieldInfo = field.getPropertyInfo();
                if (field.isCollection() && fieldInfo != null) {
                    serializeCollectionField(field, fieldInfo, value, gen, provider);
                } else {
                    serializeSimpleField(field, fieldInfo, value, gen);
                }
            }
        }

        gen.writeEndObject();
    }

    /**
     * Writes a group of dot-path fields as a nested JSON object, recursively.
     * Supports arbitrarily deep paths, e.g.:
     * <pre>
     * category.id, category.name, category.type.name  →
     * "category": { "id": 1, "name": "...", "type": { "name": "..." } }
     * </pre>
     *
     * @param rootName  the JSON key to write (current nesting level)
     * @param fields    the fields whose names begin with rootName (full original paths)
     * @param value     the root bean being serialized
     * @param pathSoFar the dot-path prefix already consumed (used to read values from the root bean)
     * @param gen       the JSON generator
     */
    private void serializePathGroup(String rootName, List<Field> fields, Object value,
                                    String pathSoFar, JsonGenerator gen) {
        // Split fields into: leaf fields (no more dots after stripping rootName)
        // and sub-groups (need another nesting level).
        Map<String, List<Field>> subGroups = new LinkedHashMap<>();
        List<Field> leafFields = new ArrayList<>();

        for (Field field : fields) {
            if (!field.isVisible()) {
                continue;
            }
            String fullName = field.getName();
            // Strip the current root prefix to get the remainder
            String remainder = fullName.contains(".") ? fullName.substring(fullName.indexOf('.') + 1) : fullName;
            if (remainder.contains(".")) {
                // Still nested — group by the next segment
                String nextRoot = remainder.substring(0, remainder.indexOf('.'));
                subGroups.computeIfAbsent(nextRoot, k -> new ArrayList<>()).add(field);
            } else {
                leafFields.add(field);
            }
        }

        // Only open the object if there is something to write
        boolean hasLeafValues = leafFields.stream().anyMatch(f -> {
            try {
                return ObjectOperations.invokeGetMethod(value, f.getName()) != null;
            } catch (Exception e) {
                return false;
            }
        });
        boolean hasSubGroups = !subGroups.isEmpty();

        if (!hasLeafValues && !hasSubGroups) {
            return;
        }

        gen.writeObjectPropertyStart(rootName);

        // Write leaf fields
        for (Field field : leafFields) {
            String fullName = field.getName();
            String subName = fullName.contains(".") ? fullName.substring(fullName.lastIndexOf('.') + 1) : fullName;
            try {
                Object fieldValue = ObjectOperations.invokeGetMethod(value, fullName);
                if (fieldValue != null) {
                    writeField(gen, subName, fieldValue);
                }
            } catch (Exception e) {
                LOGGER.warn("Cannot write path field " + fullName + " to json: " + e.getMessage());
            }
        }

        // Recurse into sub-groups
        for (Map.Entry<String, List<Field>> sub : subGroups.entrySet()) {
            // Re-root the field names so the next level sees them starting from sub.getKey()
            String nextRoot = sub.getKey();
            List<Field> subFields = sub.getValue().stream()
                    .map(f -> {
                        // Trim one prefix level from the field name for the recursive call
                        String trimmed = f.getName().substring(f.getName().indexOf('.') + 1);
                        return new PathAliasField(f, trimmed);
                    })
                    .collect(java.util.stream.Collectors.toList());
            serializePathGroup(nextRoot, subFields, value, pathSoFar, gen);
        }

        gen.writeEndObject();
    }

    /** Overload used by the top-level serialize() method — starts pathSoFar as empty. */
    private void serializePathGroup(String rootName, List<Field> fields, Object value,
                                    JsonGenerator gen, SerializationContext provider) {
        serializePathGroup(rootName, fields, value, "", gen);
    }

    /**
     * A lightweight wrapper around a {@link Field} that overrides only {@link #getName()}
     * so that recursive calls see the trimmed path rather than the full original path.
     */
    private static class PathAliasField extends Field {
        private final Field delegate;
        private final String aliasName;

        PathAliasField(Field delegate, String aliasName) {
            super(aliasName);
            this.delegate = delegate;
            this.aliasName = aliasName;
        }

        @Override
        public String getName() {
            return aliasName;
        }

        @Override
        public boolean isVisible() {
            return delegate.isVisible();
        }

        @Override
        public java.util.Map<String, Object> getParams() {
            return delegate.getParams();
        }
    }

    private void serializeCollectionField(Field field, PropertyInfo fieldInfo, Object value,
                                          JsonGenerator gen, SerializationContext provider) {
        Collection<?> collection = null;
        try {
            collection = (Collection<?>) ObjectOperations.invokeGetMethod(value, field.getName());
            // trigger lazy-loading check: if it throws, we fall back below
            int size = collection.size();
            if (size == 0) return;
        } catch (Throwable e) {
            if (field.isEntity()) {
                String parentName = ObjectOperations.findParentPropertyName(viewDescriptor.getBeanClass(), fieldInfo.getGenericType());
                if (parentName != null) {
                    collection = DomainUtils.lookupCrudService().find(fieldInfo.getGenericType(), parentName, value);
                }
            } else {
                collection = null;
                LOGGER.warn("Cannot serialize collection " + field.getName() + " of class " + viewDescriptor.getBeanClass() + ": " + e.getMessage());
            }
        }

        if (collection != null && !collection.isEmpty()) {
            gen.writeArrayPropertyStart(field.getName());
            ViewDescriptor collectionDescriptor = getFieldViewDescriptor(fieldInfo.getGenericType());
            JsonViewDescriptorSerializer collectionSerializer = new JsonViewDescriptorSerializer(collectionDescriptor);
            for (Object item : collection) {
                collectionSerializer.serialize(item, gen, provider);
            }
            gen.writeEndArray();
        }
    }

    private void serializeSimpleField(Field field, PropertyInfo fieldInfo, Object value, JsonGenerator gen) {
        if (!isSerializable(field, fieldInfo)) {
            return;
        }

        String fieldName = field.getName();
        try {
            Object fieldValue = fieldInfo != null && fieldInfo.is(boolean.class)
                    ? ObjectOperations.invokeBooleanGetMethod(value, fieldName)
                    : ObjectOperations.invokeGetMethod(value, fieldName);

            if (fieldInfo != null && fieldInfo.isAnnotationPresent(Reference.class)) {
                serializeReferenceField(gen, fieldName, fieldValue, fieldInfo);
            } else {
                writeField(gen, fieldName, fieldValue);
            }
        } catch (ReflectionException e) {
            LOGGER.warn("Cannot write field " + fieldName + " to json: " + e.getMessage());
        }
    }

    private boolean isSerializable(Field field, PropertyInfo fieldInfo) {
        if (!field.isVisible()) {
            return false;
        }

        if (fieldInfo != null) {
            if (fieldInfo.isAnnotationPresent(JsonIgnore.class)) {
                return false;
            }

            return !fieldInfo.isTransient();
        }


        return true;
    }

    private void serializeReferenceField(JsonGenerator gen, String fieldName, Object fieldValue, PropertyInfo fieldInfo) {
        Reference reference = fieldInfo.getAnnotation(Reference.class);
        @SuppressWarnings("unchecked")
        EntityReferenceRepository<Serializable> repository = DomainUtils.getEntityReferenceRepositoryByAlias(reference.value());
        EntityReference<?> entityReference = repository != null ? repository.load((Serializable) fieldValue) : null;
        if (entityReference != null) {
            gen.writeStartObject(fieldName);
            writeField(gen, "id", entityReference.getId());
            writeField(gen, "name", entityReference.getName());
            gen.writeEndObject();
        } else {
            writeField(gen, fieldName, fieldValue);
        }

    }

    private ViewDescriptor getFieldViewDescriptor(Class<?> clazz) {
        return DESCRIPTOR_CACHE.computeIfAbsent(clazz, type -> {
            ViewDescriptor descriptor = Viewers.findViewDescriptor(type, "json");
            if (descriptor == null) {
                descriptor = Viewers.findViewDescriptor(type, "tree");
            }
            if (descriptor == null) {
                descriptor = Viewers.getViewDescriptor(type, "table");
            }
            return descriptor;
        });
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

            gen.writeObjectPropertyStart(fieldName);
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
            case "ISO8601" -> FULL_DATE_FORMAT;
            case "date" -> DATE_FORMAT;
            case "time" -> TIME_FORMAT;
            default -> BASIC_DATE_FORMAT;
        };


        gen.writeStringProperty(fieldName, df.format(date));
    }

    private void writeTemporalField(JsonGenerator gen, String fieldName, TemporalAccessor value) {

        Field field = viewDescriptor.getField(fieldName);
        String format = null;
        if (field != null && field.getParams().containsKey("format")) {
            format = (String) field.getParams().get("format");
        }

        String valueStr;
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

}


