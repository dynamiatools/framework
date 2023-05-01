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
package tools.dynamia.viewers.impl;

import org.yaml.snakeyaml.Yaml;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.io.Resource;
import tools.dynamia.io.converters.Converters;
import tools.dynamia.viewers.*;

import java.io.Reader;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import static tools.dynamia.viewers.util.ViewersExpressionUtil.$s;

/**
 * The Class YamlViewDescriptorReader.
 *
 * @author Mario A. Serrano Leones
 */
@Provider
public class YamlViewDescriptorReader implements ViewDescriptorReader {

    private static final String SORT_FIELDS = "sortFields";
    private static final String FIELD_REQUIRED = "required";
    private static final String VD_LAYOUT = "layout";
    private static final String FIELD_ICON = "icon";
    private static final String FIELD_SHOW_ICON_ONLY = "showIconOnly";
    private static final String FIELD_PARAMS = "params";
    private static final String FIELD_VISIBLE = "visible";
    private static final String FIELD_FIELDCLASS = "fieldClass";
    private static final String FIELD_INDEX = "index";
    private static final String FIELD_COMPONENT_CUSTOMIZER = "componentCustomizer";
    private static final String FIELD_COMPONENT_CLASS = "componentClass";
    private static final String FIELD_COMPONENT = "component";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_LABEL = "label";
    private static final String FIELD_COLLAPSE = "collapse";
    private static final String FIELD_ACTION = "action";
    private static final String VD_FIELDS = "fields";
    private static final String FIELD_CLASS = "class";
    private static final String VD_HIDDEN = "hidden";
    private static final String VD_RENDERER = "renderer";
    private static final String VD_CUSTOMIZER = "customizer";
    private static final String VD_DEVICE = "device";
    private static final String VD_EXTENDS = "extends";
    private static final String VD_MESSAGES = "messages";
    private static final String VD_ID = "id";
    private static final String VD_AUTOFIELDS = "autofields";
    private static final String VD_BEAN_CLASS = "beanClass";
    private static final String DEBUG = "debug";
    private static final String VD_VIEW = "view";
    private static final String GROUPS = "groups";
    private static final String EQUAL_SYMBOL = "=";


    /**
     * The logger.
     */
    private final LoggingService logger = new SLF4JLoggingService(YamlViewDescriptorReader.class);

    /**
     * The Constant TYPE_MAP.
     */
    private static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put("boolean", Boolean.class);
        TYPE_MAP.put("int", Integer.class);
        TYPE_MAP.put("float", Float.class);
        TYPE_MAP.put("double", Double.class);
        TYPE_MAP.put("date", Date.class);
        TYPE_MAP.put("long", Long.class);
        TYPE_MAP.put(FIELD_CLASS, Class.class);
        TYPE_MAP.put("string", String.class);
        TYPE_MAP.put("bigdecimal", BigDecimal.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.viewers.ViewDescriptorReader#read(com.dynamia.tools
     * .io.Resource, java.io.Reader, java.util.List)
     */
    @Override
    public ViewDescriptor read(Resource descriptorResource, Reader reader,
                               List<ViewDescriptorReaderCustomizer> customizers) {
        try {
            Yaml yml = new Yaml();

            Map map = yml.load(reader);


            parseExpressions(map);

            return runViewDescriptorReaderCustomizer(map, customizers);
        } catch (Exception ex) {
            throw new ViewDescriptorReaderException(
                    "Error parsing YML ViewDescriptor " + descriptorResource.getFilename() + ": " + ex.getMessage(),
                    ex);
        }
    }

    /**
     * Run view descriptor reader customizers.
     *
     * @param map         the map
     * @param customizers the customizers
     * @return the view descriptor
     * @throws Exception the exception
     */
    protected ViewDescriptor runViewDescriptorReaderCustomizer(Map map,
                                                               List<ViewDescriptorReaderCustomizer> customizers) throws Exception {
        ViewDescriptor descriptor = buildViewDescriptor(map);
        for (ViewDescriptorReaderCustomizer c : customizers) {
            c.customize(map, descriptor);
        }
        return descriptor;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.viewers.ViewDescriptorReader#getSupportedFileExtensions ()
     */
    @Override
    public String[] getSupportedFileExtensions() {
        return new String[]{"yml", "YML", "yaml"};
    }

    /**
     * Builds the view descriptor.
     *
     * @param map the map
     * @return the view descriptor
     * @throws Exception the exception
     */
    private ViewDescriptor buildViewDescriptor(Map<?, ?> map) throws Exception {
        DefaultViewDescriptor descriptor = null;

        if (!map.containsKey(VD_VIEW)) {
            throw new ViewDescriptorReaderException("[view] is a mandatory property");
        }

        Class<?> beanClass = null;

        if (get(map, VD_BEAN_CLASS) != null) {
            beanClass = Converters.convert(Class.class, get(map, VD_BEAN_CLASS).toString());
        }

        String view = (String) map.get(VD_VIEW);

        boolean autofields = true;
        if (isBoolean(get(map, VD_AUTOFIELDS))) {
            autofields = parseBoolean(get(map, VD_AUTOFIELDS));
        }
        descriptor = new DefaultViewDescriptor(beanClass, view, autofields);

        setValue(descriptor, VD_ID, String.class, map);
        setValue(descriptor, VD_MESSAGES, String.class, map);
        setValue(descriptor, VD_EXTENDS, String.class, map);
        setValue(descriptor, VD_DEVICE, String.class, map);
        setValue(descriptor, "viewCustomizerClass", VD_CUSTOMIZER, Class.class, map);
        setValue(descriptor, "customViewRenderer", VD_RENDERER, Class.class, map);

        parseHiddenFields(map, descriptor);
        parseFields(map, descriptor);
        parseParameters(map, descriptor);
        parseLayout(map, descriptor);
        parseGroups(map, descriptor);
        parseSortFields(map, descriptor);
        parseSortGroups(map, descriptor);
        return descriptor;
    }

    /**
     * Parses the boolean.
     *
     * @param value the value
     * @return true, if successful
     */
    private boolean parseBoolean(Object value) {
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * Checks if is boolean.
     *
     * @param value the value
     * @return true, if is boolean
     */
    private boolean isBoolean(Object value) {
        if (value == null) {
            return false;
        }

        String v = value.toString();
        return (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false"));

    }

    /**
     * Parses the sort groups.
     *
     * @param map        the map
     * @param descriptor the descriptor
     */
    @SuppressWarnings("unchecked")
    private void parseSortGroups(Map<?, ?> map, DefaultViewDescriptor descriptor) {
        if (get(map, "sortGroups") != null && get(map, "sortGroups") instanceof List) {
            descriptor.sortFieldGroups((List<String>) get(map, "sortGroups"));
        }
    }

    /**
     * Parses the sort fields.
     *
     * @param map        the map
     * @param descriptor the descriptor
     */
    @SuppressWarnings("unchecked")
    private void parseSortFields(Map<?, ?> map, DefaultViewDescriptor descriptor) {
        if (get(map, SORT_FIELDS) != null && get(map, SORT_FIELDS) instanceof List) {
            descriptor.sortFields((List<String>) map.get(SORT_FIELDS));
        }
    }

    /**
     * Parses the groups.
     *
     * @param map        the map
     * @param descriptor the descriptor
     */
    private void parseGroups(Map<?, ?> map, DefaultViewDescriptor descriptor) {
        // GROUPS
        if (map.containsKey(GROUPS) && map.get(GROUPS) instanceof Map<?, ?> groups) {
            for (Object object : groups.entrySet()) {
                Entry<?, ?> entry = (Entry<?, ?>) object;
                FieldGroup group = new FieldGroup((String) entry.getKey());
                group.setViewDescriptor(descriptor);
                if (entry.getValue() instanceof Map<?, ?> groupProps) {
                    setValue(group, FIELD_LABEL, String.class, groupProps);
                    setValue(group, FIELD_ICON, String.class, groupProps);
                    setValue(group, FIELD_DESCRIPTION, String.class, groupProps);
                    setValue(group, FIELD_INDEX, Integer.class, groupProps);
                    setValue(group, FIELD_COLLAPSE, Boolean.class, groupProps);

                    if (groupProps.containsKey(VD_FIELDS) && groupProps.get(VD_FIELDS) instanceof List<?> fieldsNames) {
                        for (Object fn : fieldsNames) {
                            Field field = descriptor.getField(fn.toString());
                            if (field == null) {
                                logger.warn(fn + " is not a field. ViewDescriptor "
                                        + descriptor.getViewTypeName() + " - " + descriptor.getBeanClass());
                            } else {
                                group.addField(field);
                            }
                        }
                        groupProps.remove(VD_FIELDS);
                    } else {
                        logger.warn("Field Group [" + group.getName() + "] is empty. ViewDescriptor "
                                + descriptor.getViewTypeName() + " - " + descriptor.getBeanClass());
                    }

                    if (groupProps.get(FIELD_PARAMS) instanceof Map groupParams) {
                        group.getParams().putAll(groupParams);
                    }

                }
                descriptor.addFieldGroup(group);
            }
        }
    }

    /**
     * Parses the layout.
     *
     * @param map        the map
     * @param descriptor the descriptor
     */
    private void parseLayout(Map<?, ?> map, DefaultViewDescriptor descriptor) {
        // LAYOUT
        if (map.containsKey(VD_LAYOUT) && map.get(VD_LAYOUT) instanceof Map<?, ?> layout) {
            for (Object object : layout.entrySet()) {
                Entry<?, ?> entry = (Entry<?, ?>) object;
                descriptor.getLayout().addParam(entry.getKey().toString(), entry.getValue());
            }
        }
    }

    /**
     * Parses the parameters.
     *
     * @param map        the map
     * @param descriptor the descriptor
     */
    private void parseParameters(Map<?, ?> map, DefaultViewDescriptor descriptor) {
        // PARAMETERS
        if (map.containsKey(FIELD_PARAMS) && map.get(FIELD_PARAMS) instanceof Map<?, ?> params) {
            for (Object object : params.entrySet()) {
                Entry<?, ?> entry = (Entry<?, ?>) object;
                Object value = getEntryValue(entry);
                descriptor.addParam(entry.getKey().toString(), value);
            }
        }
    }

    /**
     * Parses the fields.
     *
     * @param map        the map
     * @param descriptor the descriptor
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void parseFields(Map<?, ?> map, DefaultViewDescriptor descriptor) {
        // FIELDS
        if (map.containsKey(VD_FIELDS) && map.get(VD_FIELDS) instanceof Map<?, ?> fields) {
            for (Object obj : fields.entrySet()) {
                Entry<String, Map> entry = (Entry<String, Map>) obj;
                Field field = new Field();
                field.setName(entry.getKey());
                Map<?, ?> fieldProps = entry.getValue();

                setValue(field, FIELD_LABEL, String.class, fieldProps);
                setValue(field, FIELD_DESCRIPTION, String.class, fieldProps);
                setValue(field, FIELD_COMPONENT, String.class, fieldProps);
                setValue(field, FIELD_COMPONENT_CLASS, Class.class, fieldProps);
                setValue(field, FIELD_COMPONENT_CUSTOMIZER, String.class, fieldProps);
                setValue(field, FIELD_INDEX, Integer.class, fieldProps);
                setValue(field, FIELD_FIELDCLASS, Class.class, fieldProps);
                setValue(field, FIELD_VISIBLE, Boolean.class, fieldProps);
                setValue(field, FIELD_REQUIRED, Boolean.class, fieldProps);
                setValue(field, FIELD_ACTION, String.class, fieldProps);
                setValue(field, FIELD_ICON, String.class, fieldProps);
                setValue(field, FIELD_SHOW_ICON_ONLY, Boolean.class, fieldProps);

                parseFieldClassAlias(fieldProps, field);

                if (fieldProps != null && fieldProps.containsKey(FIELD_PARAMS)
                        && fieldProps.get(FIELD_PARAMS) instanceof Map<?, ?> fieldParams) {
                    for (Object object : fieldParams.entrySet()) {
                        Entry<?, ?> entry2 = (Entry<?, ?>) object;
                        Object value = getEntryValue(entry2);
                        field.addParam(entry2.getKey().toString(), value);
                    }
                }
                descriptor.addField(field);
            }
        } else {
            map.remove(VD_FIELDS);
        }
    }

    private Object getEntryValue(Entry<?, ?> entry) {
        Object value = entry.getValue();
        if (hasConverter(value)) {
            value = convertToObject(value.toString());
        } else if (isBoolean(value)) {
            value = parseBoolean(value);
        }
        return value;
    }

    /**
     * Parses the field class alias.
     *
     * @param fieldProps the field props
     * @param field      the field
     */
    private void parseFieldClassAlias(Map<?, ?> fieldProps, Field field) {
        if (fieldProps != null && fieldProps.get(FIELD_CLASS) != null
                && fieldProps.get(FIELD_CLASS) instanceof String) {
            try {
                String className = fieldProps.get(FIELD_CLASS).toString();
                field.setFieldClass(Class.forName(className.trim()));
            } catch (Exception e) {
                logger.warn("Cannot parse Class name for field " + field.getName() + ". Exception: " + e.getMessage());
            }
        }
    }

    /**
     * Checks for converter.
     *
     * @param value the value
     * @return true, if successful
     */
    private boolean hasConverter(Object value) {
        return value instanceof String && value.toString().contains(EQUAL_SYMBOL);
    }

    /**
     * Parses the hidden fields.
     *
     * @param map        the map
     * @param descriptor the descriptor
     * @throws Exception the exception
     */
    private void parseHiddenFields(Map<?, ?> map, DefaultViewDescriptor descriptor) {
        // HIDDEN FIELDS
        Collection<?> hidden = (Collection<?>) map.get(VD_HIDDEN);
        if (hidden != null) {
            List<String> fieldNames = new ArrayList<>();
            for (Object object : hidden) {
                fieldNames.add(object.toString());
            }
            descriptor.hideFields(fieldNames.toArray(new String[0]));
        }
        map.remove(VD_HIDDEN);
    }

    /**
     * Sets the value.
     *
     * @param obj             the obj
     * @param name            the name
     * @param clazz           the clazz
     * @param valueRepository the value repository
     */
    @SuppressWarnings("rawtypes")
    private void setValue(Object obj, String name, Class<?> clazz, Map valueRepository) {
        setValue(obj, name, name, clazz, valueRepository);
    }

    @SuppressWarnings("rawtypes")
    private void setValue(Object obj, String property, String name, Class<?> clazz, Map valueRepository) {
        if (valueRepository != null && valueRepository.containsKey(name) && valueRepository.get(name) != null) {

            Object value = valueRepository.get(name);
            if (value != null) {
                if (clazz != String.class) {
                    value = Converters.convert(clazz, value.toString());
                }
                try {
                    BeanUtils.invokeSetMethod(obj, property, value);
                } catch (Exception e) {
                    throw new ViewDescriptorReaderException(e.getClass().getName() + " : " + e.getMessage());
                }
            }
        }
    }

    /**
     * Convert to object.
     *
     * @param string the string
     * @return the object
     */
    private Object convertToObject(String string) {
        try {
            string = string.trim();
            String value = string.substring(0, string.indexOf(EQUAL_SYMBOL)).trim();
            String type = string.substring(string.indexOf(EQUAL_SYMBOL) + 1).trim();

            Class<?> clazz = TYPE_MAP.get(type.toLowerCase());
            return Converters.convert(clazz, value);

        } catch (Exception e) {
            return string;
        }
    }

    /**
     * Parses the expressions.
     *
     * @param map the map
     */
    private void parseExpressions(Map map) {
        try {
            for (Object object : map.entrySet()) {
                Map.Entry entry = (Map.Entry) object;
                if (entry.getValue() instanceof String) {
                    String parsedString = $s((String) entry.getValue());
                    entry.setValue(parsedString);
                } else if (entry.getValue() instanceof Map) {
                    parseExpressions((Map) entry.getValue());
                }
            }
        } catch (Exception e) {
            logger.warn("Cannot parse ${...} expressions: " + e.getMessage());
        }
    }

    /**
     * Gets the.
     *
     * @param map the map
     * @param key the key
     * @return the object
     */
    public Object get(Map map, String key) {
        Object value = map.get(key);
        if (value == null) {
            value = map.get(key.toLowerCase());
        }
        return value;
    }
}
