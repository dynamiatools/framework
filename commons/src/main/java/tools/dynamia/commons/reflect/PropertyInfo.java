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
package tools.dynamia.commons.reflect;

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.StringUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * PropertyInfo.
 *
 * @author Mario A. Serrano Leones
 */
public class PropertyInfo implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -7216416220508812001L;

    /**
     * The name.
     */
    private final String name;

    /**
     * The type.
     */
    private final Class<?> type;

    /**
     * The owner class.
     */
    private final Class<?> ownerClass;

    /**
     * The generic type.
     */
    private Class<?> genericType;

    /**
     * The collection.
     */
    private boolean collection;

    /**
     * The access mode.
     */
    private final AccessMode accessMode;

    private Method readMethod;
    private Method writeMethod;

    /**
     * Instantiates a new property info.
     *
     * @param name       the name
     * @param type       the type
     * @param ownerClass the owner class
     * @param accessMode the access mode
     */
    public PropertyInfo(String name, Class<?> type, Class<?> ownerClass, AccessMode accessMode) {
        this.name = name;
        this.type = type;
        this.ownerClass = ownerClass;
        this.accessMode = accessMode;
    }

    /**
     * Checks if is collection.
     *
     * @return true, if is collection
     */
    public boolean isCollection() {
        return collection;
    }

    /**
     * Checks if is enum.
     *
     * @return true, if is enum
     */
    public boolean isEnum() {
        return getType().isEnum();
    }

    /**
     * Checks if is array.
     *
     * @return true, if is array
     */
    public boolean isArray() {
        return getType().isArray();
    }

    /**
     * Check if this property is primitive or belong to java.lang, java.util or java.math packages
     *
     */
    public boolean isStandardClass() {
        return BeanUtils.isStantardClass(getType());
    }

    /**
     * Checks if is.
     *
     * @param clazz the clazz
     * @return true, if successful
     */
    public boolean is(Class<?> clazz) {
        return BeanUtils.isAssignable(getType(), clazz);
    }

    /**
     * Sets the collection.
     *
     * @param collection the new collection
     */
    public void setCollection(boolean collection) {
        this.collection = collection;
    }

    /**
     * Gets the generic type.
     *
     * @return the generic type
     */
    public Class<?> getGenericType() {
        return genericType;
    }

    /**
     * Sets the generic type.
     *
     * @param genericType the new generic type
     */
    public void setGenericType(Class<?> genericType) {
        this.genericType = genericType;
    }

    /**
     * Gets the access mode.
     *
     * @return the access mode
     */
    public AccessMode getAccessMode() {
        return accessMode;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Gets the owner class.
     *
     * @return the owner class
     */
    public Class<?> getOwnerClass() {
        return ownerClass;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return StringUtils.capitalize(StringUtils.addSpaceBetweenWords(name));
    }

    public Method getReadMethod() {
        return readMethod;
    }

    public Method getWriteMethod() {
        return writeMethod;
    }

    public void setWriteMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
    }

    public void setReadMethod(Method readMethod) {
        this.readMethod = readMethod;
    }

    public Field getField() {
        if (ownerClass != null) {
            try {
                return BeanUtils.getField(ownerClass, getName());
            } catch (NoSuchFieldException ignored) {

            }
        }
        return null;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        Boolean result = null;
        Field field = getField();
        if (field != null) {
            result = field.isAnnotationPresent(annotationClass);
        }

        if ((result == null || result == Boolean.FALSE) && readMethod != null) {
            result = readMethod.isAnnotationPresent(annotationClass);
        }

        if (result == null) {
            result = false;
        }

        return result;
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        A result = null;
        Field field = getField();
        if (field != null) {
            result = field.getAnnotation(annotationClass);
        }

        if (result == null && readMethod != null) {
            result = readMethod.getAnnotation(annotationClass);
        }
        return result;
    }
}
