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
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.commons.reflect.ReflectionException;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.viewers.util.Viewers;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class JsonViewDescriptorDeserializer extends StdDeserializer<Object> {

    private final ViewDescriptor viewDescriptor;
    private static final LoggingService LOGGER = new SLF4JLoggingService(JsonViewDescriptorDeserializer.class);

    private static final Map<Class<?>, Function<JsonNode, Object>> TYPE_EXTRACTORS = new HashMap<>();
    private static final Map<Class<?>, ViewDescriptor> DESCRIPTOR_CACHE = new ConcurrentHashMap<>();

    static {
        TYPE_EXTRACTORS.put(String.class, JsonNode::stringValue);
        TYPE_EXTRACTORS.put(Long.class, JsonNode::longValue);
        TYPE_EXTRACTORS.put(long.class, JsonNode::longValue);
        TYPE_EXTRACTORS.put(Integer.class, JsonNode::intValue);
        TYPE_EXTRACTORS.put(int.class, JsonNode::intValue);
        TYPE_EXTRACTORS.put(Float.class, JsonNode::floatValue);
        TYPE_EXTRACTORS.put(float.class, JsonNode::floatValue);
        TYPE_EXTRACTORS.put(Double.class, JsonNode::doubleValue);
        TYPE_EXTRACTORS.put(double.class, JsonNode::doubleValue);
        TYPE_EXTRACTORS.put(BigDecimal.class, JsonNode::decimalValue);
        TYPE_EXTRACTORS.put(Boolean.class, JsonNode::asBoolean);
        TYPE_EXTRACTORS.put(boolean.class, JsonNode::asBoolean);
    }

    public JsonViewDescriptorDeserializer(ViewDescriptor viewDescriptor) {
        this(viewDescriptor, Object.class);
    }

    public JsonViewDescriptorDeserializer(ViewDescriptor viewDescriptor, Class<Object> t) {
        super(t);
        this.viewDescriptor = viewDescriptor;
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) {
        JsonNode node = jp.readValueAsTree();
        return parseNode(viewDescriptor.getBeanClass(), node, viewDescriptor);
    }

    private Object parseNode(Class<?> type, JsonNode node, ViewDescriptor descriptor) {
        Object object = ObjectOperations.newInstance(type);

        // Group fields by their root prefix (part before the first dot).
        // Fields without a dot are stored under their own name as key.
        Map<String, List<Field>> groups = new LinkedHashMap<>();
        for (Field field : Viewers.getFields(descriptor)) {
            String name = field.getName();
            String root = name.contains(".") ? name.substring(0, name.indexOf('.')) : name;
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(field);
        }

        for (Map.Entry<String, List<Field>> entry : groups.entrySet()) {
            List<Field> groupFields = entry.getValue();
            boolean isPathGroup = groupFields.size() > 1 || groupFields.getFirst().getName().contains(".");

            if (isPathGroup) {
                // Navigate the JsonNode by root key and recursively set nested values
                JsonNode groupNode = node.get(entry.getKey());
                if (groupNode != null) {
                    deserializePathGroup(object, groupFields, entry.getKey(), groupNode);
                }
            } else {
                Field field = groupFields.getFirst();
                PropertyInfo fieldInfo = field.getPropertyInfo();
                if (fieldInfo == null || fieldInfo.isAnnotationPresent(JsonIgnore.class)) {
                    continue;
                }
                JsonNode fieldNode = node.get(field.getName());
                if (fieldNode == null) {
                    continue;
                }
                if (fieldInfo.isCollection()) {
                    processCollectionField(object, field, fieldInfo, fieldNode, type);
                } else {
                    setSimpleField(object, field.getName(), fieldInfo, fieldNode);
                }
            }
        }
        return object;
    }

    /**
     * Recursively traverses a group of dot-path fields and sets their values on the target object.
     * <p>
     * For example, given fields {@code category.id}, {@code category.name}, {@code category.type.name}
     * and a {@code groupNode} that is the {@code "category"} JSON object, this method:
     * <ul>
     *   <li>Sets {@code object.category.id} from {@code groupNode.id}</li>
     *   <li>Sets {@code object.category.name} from {@code groupNode.name}</li>
     *   <li>Recurses into {@code groupNode.type} for {@code category.type.name}</li>
     * </ul>
     *
     * @param object      the root bean being populated
     * @param fields      fields whose names start with the current prefix (full original dot-path names)
     * @param prefix      the dot-path prefix consumed so far (e.g. {@code "category"})
     * @param currentNode the JsonNode corresponding to {@code prefix}
     */
    private void deserializePathGroup(Object object, List<Field> fields, String prefix, JsonNode currentNode) {
        // Separate leaf fields from those that need another level of nesting.
        // We strip the current prefix (e.g. "category") using its length to correctly
        // handle out-of-order fields and deep paths regardless of dot position.
        Map<String, List<Field>> subGroups = new LinkedHashMap<>();
        List<Field> leafFields = new ArrayList<>();

        for (Field field : fields) {
            String fullName = field.getName();
            // Strip the current prefix: "category.type.name" with prefix "category" → "type.name"
            String remainder = fullName.length() > prefix.length() + 1
                    ? fullName.substring(prefix.length() + 1)
                    : fullName;
            if (remainder.contains(".")) {
                // Still nested — group by the next segment (e.g. "type" from "type.name")
                String nextRoot = remainder.substring(0, remainder.indexOf('.'));
                subGroups.computeIfAbsent(nextRoot, k -> new ArrayList<>()).add(field);
            } else {
                // Leaf at this level (e.g. "id", "name")
                leafFields.add(field);
            }
        }

        // Set leaf values using the full dot-path on the root object
        for (Field field : leafFields) {
            String fullName = field.getName();  // e.g. "category.id"
            // The JSON key is the last segment of the full path
            String subName = fullName.contains(".") ? fullName.substring(fullName.lastIndexOf('.') + 1) : fullName;
            PropertyInfo fieldInfo = field.getPropertyInfo();
            if (fieldInfo == null || fieldInfo.isAnnotationPresent(JsonIgnore.class)) {
                continue;
            }
            JsonNode leafNode = currentNode.get(subName);
            if (leafNode == null) {
                continue;
            }
            Object fieldValue = getNodeValue(fieldInfo, leafNode);
            try {
                // invokeSetMethod supports dot-notation and auto-creates null intermediate objects
                ObjectOperations.invokeSetMethod(object, fullName, fieldValue);
            } catch (ReflectionException e) {
                LOGGER.warn("Cannot parse json path field " + fullName + " = " + fieldValue + ": " + e.getMessage());
            }
        }

        // Recurse into sub-groups, passing the extended prefix and the matching sub-node
        for (Map.Entry<String, List<Field>> sub : subGroups.entrySet()) {
            String nextRoot = sub.getKey();
            JsonNode subNode = currentNode.get(nextRoot);
            if (subNode != null) {
                deserializePathGroup(object, sub.getValue(), prefix + "." + nextRoot, subNode);
            }
        }
    }

    private void processCollectionField(Object object, Field field, PropertyInfo fieldInfo,
                                        JsonNode fieldNode, Class<?> parentType) {
        Collection<Object> collection = getOrCreateCollection(object, fieldInfo);
        ViewDescriptor collectionDescriptor = resolveCollectionDescriptor(fieldInfo.getGenericType());
        String parentName = resolveParentName(field, fieldInfo, parentType);

        for (JsonNode child : fieldNode) {
            Object item = parseNode(fieldInfo.getGenericType(), child, collectionDescriptor);
            ObjectOperations.invokeSetMethod(item, parentName, object);
            collection.add(item);
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> getOrCreateCollection(Object object, PropertyInfo fieldInfo) {
        Collection<Object> collection = (Collection<Object>) ObjectOperations.invokeGetMethod(object, fieldInfo);
        if (collection == null) {
            collection = createEmptyCollection(fieldInfo.getType());
            ObjectOperations.setFieldValue(fieldInfo, object, collection);
        }
        return collection;
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> createEmptyCollection(Class<?> type) {
        if (type == List.class) return new ArrayList<>();
        if (type == Set.class) return new HashSet<>();
        return (Collection<Object>) ObjectOperations.newInstance(type);
    }

    private String resolveParentName(Field field, PropertyInfo fieldInfo, Class<?> parentType) {
        Object customParent = field.getParams().get("parentName");
        return customParent != null
                ? customParent.toString()
                : ObjectOperations.findParentPropertyName(parentType, fieldInfo.getGenericType());
    }

    private void setSimpleField(Object object, String fieldName, PropertyInfo fieldInfo, JsonNode fieldNode) {
        Object fieldValue = getNodeValue(fieldInfo, fieldNode);
        try {
            ObjectOperations.invokeSetMethod(object, fieldName, fieldValue);
        } catch (ReflectionException e) {
            LOGGER.warn("Cannot parse json to field " + fieldName + " = " + fieldValue + ": " + e.getMessage());
        }
    }

    private ViewDescriptor resolveCollectionDescriptor(Class<?> genericType) {
        return DESCRIPTOR_CACHE.computeIfAbsent(genericType, type -> {
            ViewDescriptor descriptor = Viewers.findViewDescriptor(type, "json-form");
            return descriptor != null ? descriptor : Viewers.getViewDescriptor(type, "form");
        });
    }

    public static Object getNodeValue(PropertyInfo fieldInfo, JsonNode fieldNode) {
        if (DomainUtils.isEntity(fieldInfo.getType()) && fieldNode.get("id") != null) {
            return DomainUtils.lookupCrudService().find(fieldInfo.getType(), fieldNode.get("id").asLong());
        }
        Function<JsonNode, Object> extractor = TYPE_EXTRACTORS.get(fieldInfo.getType());
        return extractor != null ? extractor.apply(fieldNode) : null;
    }
}
