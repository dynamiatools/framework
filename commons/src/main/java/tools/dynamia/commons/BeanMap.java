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
 * Serialization: Implements {@link Serializable} for persistence and data transfer.
 *
 * @author Mario A. Serrano Leones
 */
public class BeanMap extends HashMap<String, Object> implements Serializable {

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
     * Optional string representation of the bean (usually from {@code toString()}).
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
        stringRepresentation = bean.toString();
        if (bean instanceof Mappable mappable) {
            putAll(mappable.toMap());
        } else {
            putAll(BeanUtils.getValuesMaps("", bean));
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
}
