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
        for (Field field : Viewers.getFields(descriptor)) {
            PropertyInfo fieldInfo = field.getPropertyInfo();
            if (fieldInfo.isAnnotationPresent(JsonIgnore.class)) {
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
        return object;
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
