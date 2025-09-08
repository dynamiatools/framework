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
import java.lang.reflect.Modifier;

/**
 * <p>
 * PropertyInfo encapsulates metadata about a property of a Java class, including its name, type, owner class, access mode, and reflection details.
 * It provides utility methods for introspection, such as checking if the property is a collection, array, enum, or standard Java class, and for accessing annotations and field/method information.
 * </p>
 *
 * <p>
 * This class is commonly used in frameworks and libraries that require runtime introspection of class properties, such as serialization, mapping, UI generation, or validation.
 * </p>
 *
 * <p>
 * PropertyInfo is immutable except for generic type, collection flag, and read/write methods, which may be set after construction for advanced reflection scenarios.
 * </p>
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
public class PropertyInfo implements Serializable {

    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = -7216416220508812001L;

    /**
     * The property name.
     */
    private final String name;

    /**
     * The property type.
     */
    private final Class<?> type;

    /**
     * The class that owns this property.
     */
    private final Class<?> ownerClass;

    /**
     * The generic type of the property, if applicable (e.g., for collections).
     */
    private Class<?> genericType;

    /**
     * Indicates if the property is a collection type.
     */
    private boolean collection;

    /**
     * The access mode (read/write) for this property.
     */
    private final AccessMode accessMode;

    /**
     * The method used to read the property value (getter).
     */
    private Method readMethod;

    /**
     * The method used to write the property value (setter).
     */
    private Method writeMethod;

    /**
     * Constructs a new PropertyInfo instance with the specified metadata.
     *
     * @param name       the property name
     * @param type       the property type
     * @param ownerClass the class that owns the property
     * @param accessMode the access mode (read/write)
     */
    public PropertyInfo(String name, Class<?> type, Class<?> ownerClass, AccessMode accessMode) {
        this.name = name;
        this.type = type;
        this.ownerClass = ownerClass;
        this.accessMode = accessMode;
    }

    /**
     * Returns true if the property is a collection type (e.g., List, Set).
     *
     * @return true if the property is a collection
     */
    public boolean isCollection() {
        return collection;
    }

    /**
     * Returns true if the property type is an enum.
     *
     * @return true if the property is an enum
     */
    public boolean isEnum() {
        return getType().isEnum();
    }

    /**
     * Returns true if the property type is an array.
     *
     * @return true if the property is an array
     */
    public boolean isArray() {
        return getType().isArray();
    }

    /**
     * Returns true if the property type is a primitive or belongs to java.lang, java.util, or java.math packages.
     *
     * @return true if the property is a standard Java class
     */
    public boolean isStandardClass() {
        return BeanUtils.isStantardClass(getType());
    }

    /**
     * Returns true if the property type is assignable from the specified class.
     *
     * @param clazz the class to check
     * @return true if the property type is assignable from clazz
     */
    public boolean is(Class<?> clazz) {
        return BeanUtils.isAssignable(getType(), clazz);
    }

    /**
     * Sets whether the property is a collection type.
     *
     * @param collection true if the property is a collection
     */
    public void setCollection(boolean collection) {
        this.collection = collection;
    }

    /**
     * Returns the generic type of the property, if applicable.
     *
     * @return the generic type
     */
    public Class<?> getGenericType() {
        return genericType;
    }

    /**
     * Sets the generic type of the property.
     *
     * @param genericType the generic type to set
     */
    public void setGenericType(Class<?> genericType) {
        this.genericType = genericType;
    }

    /**
     * Returns the access mode (read/write) for this property.
     *
     * @return the access mode
     */
    public AccessMode getAccessMode() {
        return accessMode;
    }

    /**
     * Returns the property name.
     *
     * @return the property name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the property type.
     *
     * @return the property type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the class that owns this property.
     *
     * @return the owner class
     */
    public Class<?> getOwnerClass() {
        return ownerClass;
    }

    /**
     * Returns a human-readable string representation of the property name, with spaces and capitalization.
     *
     * @return a formatted property name
     */
    @Override
    public String toString() {
        return StringUtils.capitalize(StringUtils.addSpaceBetweenWords(name));
    }

    /**
     * Returns the method used to read the property value (getter), or null if not available.
     *
     * @return the read method
     */
    public Method getReadMethod() {
        return readMethod;
    }

    /**
     * Returns the method used to write the property value (setter), or null if not available.
     *
     * @return the write method
     */
    public Method getWriteMethod() {
        return writeMethod;
    }

    /**
     * Sets the method used to write the property value (setter).
     *
     * @param writeMethod the write method to set
     */
    public void setWriteMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
    }

    /**
     * Sets the method used to read the property value (getter).
     *
     * @param readMethod the read method to set
     */
    public void setReadMethod(Method readMethod) {
        this.readMethod = readMethod;
    }

    /**
     * Returns the {@link Field} object for this property, or null if not found.
     *
     * @return the field representing this property, or null if not found
     */
    public Field getField() {
        if (ownerClass != null) {
            try {
                return BeanUtils.getField(ownerClass, getName());
            } catch (NoSuchFieldException ignored) {

            }
        }
        return null;
    }

    /**
     * Returns true if the property or its getter method is annotated with the specified annotation class.
     *
     * @param annotationClass the annotation class to check
     * @return true if the annotation is present
     */
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

    /**
     * Returns the annotation of the specified type present on the property or its getter method, or null if not found.
     *
     * @param annotationClass the annotation class to retrieve
     * @param <A> the annotation type
     * @return the annotation instance, or null if not present
     */
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

    /**
     * Returns true if the property is marked as transient (not persisted).
     *
     * @return true if the property is transient
     */
    public boolean isTransient() {
        Field field = getField();
        if (field != null) {
            return Modifier.isTransient(field.getModifiers());
        } else {
            return false;
        }
    }
}
