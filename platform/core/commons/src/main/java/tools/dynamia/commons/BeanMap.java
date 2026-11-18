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

package tools.dynamia.commons;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Represents a POJO object as a map, where each property is a key-value pair.
 * <p>
 * The {@code BeanMap} class extends {@link HashMap} to provide convenient access to bean properties as map entries.
 * It supports loading properties from a bean, custom metadata (id, name, beanClass, fields), and flexible access methods.
 * <p>
 * Typical use cases include serialization, dynamic property access, data transfer, and integration with frameworks that expect map-like structures.
 * <p>
 * Example usage:
 * <pre>
 *     BeanMap map = new BeanMap();
 *     map.load(myBean);
 *     Object value = map.get("propertyName");
 *     map.set("otherProperty", 123);
 * </pre>
 * <p>
 * Thread safety: This class is not thread-safe.
 * <p>
 * Serialization: Implements {@link Serializable} for persistence and data transfer, and {@link Jsonable} and
 * {@link Xmlable} to export the entries as JSON or XML (see {@link #toJson()} and {@link #toXml()}).
 *
 * @author Mario A. Serrano Leones
 */
public class BeanMap extends HashMap<String, Object> implements Serializable, Jsonable, Xmlable {

    /**
     * Optional identifier for the bean.
     */
    private Object id;
    /**
     * Optional name for the bean (usually the class simple name).
     */
    private String name;
    /**
     * The class of the bean represented by this map.
     */
    private Class beanClass;
    /**
     * Optional string representation of the bean (from the {@link InstanceName} member of the bean, or its {@code toString()}).
     */
    private String stringRepresentation;
    /**
     * Optional array of field names for the bean.
     */
    private String[] fields;

    /**
     * Constructs an empty {@code BeanMap}.
     */
    public BeanMap() {
    }

    /**
     * Loads the properties of the given bean into this map.
     * <p>
     * Sets metadata fields (beanClass, name, stringRepresentation) and populates the map with all bean properties.
     *
     * @param bean the bean object to load properties from
     */
    public void load(Object bean) {
        beanClass = bean.getClass();
        name = beanClass.getSimpleName();
        stringRepresentation = ObjectOperations.getInstanceName(bean);
        if (bean instanceof Mappable mappable) {
            putAll(mappable.toMap());
        } else {
            putAll(ObjectOperations.getValuesMaps("", bean));
        }
    }

    /**
     * Sets a key-value pair in the map.
     * <p>
     * Equivalent to {@link #put(String, Object)}.
     *
     * @param key   the property name
     * @param value the value to associate with the key
     */
    public void set(String key, Object value) {
        put(key, value);
    }

    /**
     * Gets the value associated with the specified key, cast to the desired type.
     * <p>
     * Returns {@code null} if the key is not present.
     *
     * @param key the property name
     * @param <T> the expected type of the value
     * @return the value associated with the key, or {@code null} if not present
     */
    public <T> T get(String key) {
        //noinspection unchecked
        return (T) super.get(key);
    }

    /**
     * Gets the name of the bean (usually the class simple name).
     *
     * @return the bean name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the bean.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the class of the bean represented by this map.
     *
     * @return the bean class
     */
    public Class getBeanClass() {
        return beanClass;
    }

    /**
     * Sets the class of the bean represented by this map.
     *
     * @param beanClass the class to set
     */
    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Gets the identifier of the bean.
     *
     * @return the bean id
     */
    public Object getId() {
        return id;
    }

    /**
     * Sets the identifier of the bean.
     *
     * @param id the id to set
     */
    public void setId(Object id) {
        this.id = id;
    }

    /**
     * Gets the array of field names for the bean.
     *
     * @return the array of field names
     */
    public String[] getFields() {
        return fields;
    }

    /**
     * Sets the array of field names for the bean.
     *
     * @param fields the array of field names to set
     */
    public void setFields(String[] fields) {
        this.fields = fields;
    }

    /**
     * Sets the string representation returned by {@link #toString()}, usually the bean's {@link InstanceName} or its {@code toString()}.
     *
     * @param stringRepresentation the string representation
     */
    public void setStringRepresentation(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    /**
     * Converts the entries of this map to a JSON object. Only the property entries are written; metadata such as
     * id, name or beanClass is not included.
     *
     * @return the JSON string
     */
    @Override
    public String toJson() {
        return StringPojoParser.convertPojoToJson(new LinkedHashMap<>(this));
    }

    /**
     * Converts the entries of this map to XML. The root element is named after {@link #getName()} (or
     * {@code BeanMap} if it is blank) and each entry becomes a child element. Only the property entries are
     * written; metadata such as id or beanClass is not included.
     *
     * @return the XML string
     */
    @Override
    public String toXml() {
        var rootName = name != null && !name.isBlank() ? name : "BeanMap";
        try {
            return StringPojoParser.createXmlMapper().writer().withRootName(rootName)
                    .writeValueAsString(new LinkedHashMap<>(this));
        } catch (tools.jackson.core.JacksonException e) {
            throw new XmlParsingException(e);
        }
    }

    /**
     * Returns a string representation of this bean map.
     * <p>
     * If {@code stringRepresentation} is set, returns it; otherwise, returns the bean name or the default map string.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        if (stringRepresentation != null) {
            return stringRepresentation;
        } else if (name != null) {
            return name;
        } else {
            return super.toString();
        }
    }

    public boolean isLoadable() {
        return beanClass != null && id != null;
    }

    /**
     * Tells whether the bean represented by this map is an instance of the given type.
     *
     * @param type the expected entity type
     * @return {@code true} if the bean class is known and assignable to {@code type}
     */
    public boolean isBeanOf(Class<?> type) {
        return type != null && beanClass != null && type.isAssignableFrom(beanClass);
    }

    /**
     * Tells whether an object is an instance of the given type, or a {@link BeanMap} that represents one. Useful in
     * place of {@code instanceof} when a value may be an entity or its read-only map.
     *
     * @param value the entity or BeanMap, may be {@code null}
     * @param type  the expected entity type
     * @return {@code true} if {@code value} is a {@code type} or a BeanMap of a {@code type}
     */
    public static boolean isBeanOf(Object value, Class<?> type) {
        if (value instanceof BeanMap map) {
            return map.isBeanOf(type);
        }
        return type != null && type.isInstance(value);
    }

    /**
     * Returns the entity class of a value: the {@link #getBeanClass()} of a BeanMap, or the class of any other object.
     *
     * @param value the entity or BeanMap, may be {@code null}
     * @return the entity class, or {@code null} if unknown
     */
    public static Class<?> beanClassOf(Object value) {
        if (value instanceof BeanMap map) {
            return map.getBeanClass();
        }
        return value != null ? value.getClass() : null;
    }

    /**
     * Returns the value of a key as a boolean. Booleans are returned as is, other values are parsed from their string form.
     *
     * @param key the property name
     * @return {@code true} only if the value is {@code true} (or the text "true"); {@code false} when missing
     */
    public boolean isTrue(String key) {
        Object value = super.get(key);
        if (value instanceof Boolean b) {
            return b;
        }
        return value != null && Boolean.parseBoolean(value.toString());
    }

    /**
     * Returns the value of a key as text.
     *
     * @param key the property name
     * @return the value's {@code toString()}, or {@code null} when missing
     */
    public String getString(String key) {
        Object value = super.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Returns the value of a key as a {@code Long}.
     *
     * @param key the property name
     * @return the numeric value converted to long, the parsed text, or {@code null} when missing or not a number
     */
    public Long getLong(String key) {
        Object value = super.get(key);
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value != null) {
            try {
                return Long.valueOf(value.toString().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Returns the value of a key as an {@code Integer}.
     *
     * @param key the property name
     * @return the numeric value converted to int, the parsed text, or {@code null} when missing or not a number
     */
    public Integer getInteger(String key) {
        Long value = getLong(key);
        return value != null ? Integer.valueOf(value.intValue()) : null;
    }

    /**
     * Returns the value of a key, or a default when the key is missing or {@code null}.
     *
     * @param key          the property name
     * @param defaultValue the value to use when there is none
     * @param <T>          the expected value type
     * @return the value or {@code defaultValue}
     */
    public <T> T getOrDefault(String key, T defaultValue) {
        T value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Tells whether the row is marked as selected, by the {@code selected} entry that table checkboxes write.
     *
     * @return {@code true} if the {@code selected} entry is true
     */
    public boolean isSelected() {
        return isTrue("selected");
    }

    /**
     * Marks or unmarks the row by writing the {@code selected} entry.
     *
     * @param selected the new selection state
     */
    public void setSelected(boolean selected) {
        put("selected", selected);
    }
}
