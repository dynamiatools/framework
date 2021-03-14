/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.commons.reflect.ReflectionException;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.viewers.util.Viewers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class JsonViewDescriptorDeserializer extends StdDeserializer<Object> {

    private ViewDescriptor viewDescriptor;
    private StdDateFormat dateFormat = new StdDateFormat();
    private static final LoggingService LOGGER = new SLF4JLoggingService(JsonViewDescriptorDeserializer.class);

    public JsonViewDescriptorDeserializer(ViewDescriptor viewDescriptor) {
        this(viewDescriptor, null);

    }

    public JsonViewDescriptorDeserializer(ViewDescriptor viewDescriptor, Class<Object> t) {
        super(t);
        this.viewDescriptor = viewDescriptor;
    }


    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        return parseNode(viewDescriptor.getBeanClass(), node, viewDescriptor);

    }

    private Object parseNode(Class type, JsonNode node, ViewDescriptor viewDescriptor) {
        Object object = BeanUtils.newInstance(type);
        for (Field field : Viewers.getFields(viewDescriptor)) {
            PropertyInfo fieldInfo = field.getPropertyInfo();
            String fieldName = field.getName();
            JsonNode fieldNode = node.get(fieldName);
            if (fieldNode == null) {
                continue;
            }

            if (fieldInfo.isCollection()) {
                Collection collection = (Collection) BeanUtils.invokeGetMethod(object, fieldInfo);
                if (collection == null) {
                    if (fieldInfo.getType() == List.class) {
                        collection = new ArrayList();
                    } else if (fieldInfo.getType() == Set.class) {
                        collection = new HashSet();
                    } else {
                        collection = (Collection) BeanUtils.newInstance(fieldInfo.getType());
                    }
                    BeanUtils.setFieldValue(fieldInfo, object, collection);
                }

                ViewDescriptor collectionDescriptor = Viewers.findViewDescriptor(fieldInfo.getGenericType(), "json-form");
                if (collectionDescriptor == null) {
                    collectionDescriptor = Viewers.getViewDescriptor(fieldInfo.getGenericType(), "form");
                }
                String parentName = BeanUtils.findParentPropertyName(type, fieldInfo.getGenericType());
                if (field.getParams().get("parentName") != null) {
                    parentName = field.getParams().get("parentName").toString();
                }
                for (JsonNode child : fieldNode) {
                    Object item = parseNode(fieldInfo.getGenericType(), child, collectionDescriptor);
                    BeanUtils.invokeSetMethod(item, parentName, object);
                    collection.add(item);
                }

            } else {
                Object fieldValue = getNodeValue(fieldInfo, fieldNode);
                try {
                    BeanUtils.invokeSetMethod(object, fieldName, fieldValue);
                } catch (ReflectionException e) {
                    LOGGER.warn("Cannot parse json to field " + fieldName + " = " + fieldValue + ": " + e.getMessage());
                }


            }
        }
        return object;
    }

    public static Object getNodeValue(PropertyInfo fieldInfo, JsonNode fieldNode) {
        Object fieldValue = null;

        if (DomainUtils.isEntity(fieldInfo.getType()) && fieldNode.get("id") != null) {
            long id = fieldNode.get("id").asLong();
            fieldValue = DomainUtils.lookupCrudService().find(fieldInfo.getType(), id);
        } else if (fieldInfo.is(String.class)) {
            fieldValue = fieldNode.textValue();
        } else if (fieldInfo.is(Long.class) || fieldInfo.is(long.class)) {
            fieldValue = fieldNode.longValue();
        } else if (fieldInfo.is(Integer.class) || fieldInfo.is(int.class)) {
            fieldValue = fieldNode.intValue();
        } else if (fieldInfo.is(Float.class) || fieldInfo.is(float.class)) {
            fieldValue = fieldNode.floatValue();
        } else if (fieldInfo.is(Double.class) || fieldInfo.is(double.class)) {
            fieldValue = fieldNode.doubleValue();
        } else if (fieldInfo.is(BigDecimal.class)) {
            fieldValue = fieldNode.decimalValue();
        } else if (fieldInfo.is(Boolean.class)) {
            fieldValue = fieldNode.booleanValue();
        }
        return fieldValue;
    }
}