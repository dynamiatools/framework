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

import org.springframework.util.ReflectionUtils;
import tools.dynamia.commons.StringUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Specialized class for low-level reflection operations.
 * <p>
 * This class provides utilities for working with reflection, including field access,
 * method discovery, annotation scanning, generic type inspection, and class hierarchy
 * traversal. Uses Spring's ReflectionUtils for improved AOT compatibility.
 * </p>
 *
 * <h2>Core Features</h2>
 * <ul>
 *   <li><strong>Field Discovery:</strong> Find fields by name with dot notation support</li>
 *   <li><strong>Property Info:</strong> Get metadata about bean properties</li>
 *   <li><strong>Annotation Scanning:</strong> Find methods/fields with annotations</li>
 *   <li><strong>Generic Type Inspection:</strong> Extract generic type information</li>
 *   <li><strong>Class Hierarchy:</strong> Traverse inheritance chain</li>
 *   <li><strong>AOT Compatible:</strong> Uses Spring ReflectionUtils for GraalVM support</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Field discovery
 * Field nameField = ReflectionHelper.getField(Person.class, "name");
 * Field cityField = ReflectionHelper.getField(Person.class, "address.city"); // nested
 *
 * // Get all fields
 * List<Field> fields = ReflectionHelper.getAllFields(Person.class);
 *
 * // Property info
 * PropertyInfo prop = ReflectionHelper.getPropertyInfo(Person.class, "name");
 * List<PropertyInfo> props = ReflectionHelper.getPropertiesInfo(Person.class);
 *
 * // Annotation scanning
 * Method[] annotated = ReflectionHelper.getMethodsWithAnnotation(MyClass.class, Override.class);
 * Field[] fields = ReflectionHelper.getFieldsWithAnnotation(MyClass.class, Column.class);
 *
 * // Generic types
 * Class<?> genericType = ReflectionHelper.getFieldGenericType(listField);
 * }</pre>
 *
 * @author Ing. Mario Serrano Leones
 * @version 26.1
 * @since 26.1
 */
public final class ReflectionHelper {

    /**
     * Private constructor to prevent instantiation.
     */
    private ReflectionHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Retrieves a field from a class by name, with support for nested fields using dot notation.
     * <p>
     * This method searches for a field in the given class and its superclasses.
     * Supports nested field navigation using dot notation (e.g., "address.city").
     * Uses Spring's {@link ReflectionUtils} for improved AOT compatibility.
     * </p>
     *
     * @param clazz     the class to search for the field
     * @param fieldName the name of the field, supports dot notation for nested fields
     * @return the Field object matching the specified name
     * @throws NoSuchFieldException if the field is not found in the class hierarchy
     *
     * Example:
     * <pre>{@code
     * // Simple field
     * Field nameField = ReflectionHelper.getField(Person.class, "name");
     * nameField.setAccessible(true);
     * Object value = nameField.get(personInstance);
     *
     * // Nested field with dot notation
     * Field cityField = ReflectionHelper.getField(Employee.class, "address.city");
     *
     * // Use in generic utilities
     * public <T> Object getFieldValue(T object, String fieldName) throws Exception {
     *     Field field = ReflectionHelper.getField(object.getClass(), fieldName);
     *     field.setAccessible(true);
     *     return field.get(object);
     * }
     * }</pre>
     */
    public static Field getField(final Class clazz, final String fieldName) throws NoSuchFieldException {
        if (fieldName.contains(".")) {
            // Handle nested field navigation
            final int dotIndex = fieldName.indexOf('.');
            final String parentField = fieldName.substring(0, dotIndex);
            final String childField = fieldName.substring(dotIndex + 1);

            Field parentFieldObj = ReflectionUtils.findField(clazz, parentField);
            if (parentFieldObj == null) {
                throw new NoSuchFieldException("Field '" + parentField + "' not found in class " + clazz.getName());
            }

            return getField(parentFieldObj.getType(), childField);
        } else {
            // Use Spring ReflectionUtils for better AOT compatibility
            Field field = ReflectionUtils.findField(clazz, fieldName);
            if (field == null) {
                throw new NoSuchFieldException("Field '" + fieldName + "' not found in class " + clazz.getName());
            }
            return field;
        }
    }

    /**
     * Retrieves all fields declared in the specified class and its superclasses.
     * <p>
     * This method traverses the entire class hierarchy and collects all fields,
     * with superclass fields appearing first in the returned list.
     * Uses Spring's {@link ReflectionUtils} for improved compatibility with
     * AOT compilation and GraalVM native image generation.
     * </p>
     *
     * @param clazz the class to retrieve fields from
     * @return a list containing all fields from the class and its superclasses
     *
     * Example:
     * <pre>{@code
     * List<Field> fields = ReflectionHelper.getAllFields(Customer.class);
     * for (Field field : fields) {
     *     System.out.println("Field: " + field.getName() + ", Type: " + field.getType());
     * }
     *
     * // Filter specific fields
     * List<Field> stringFields = ReflectionHelper.getAllFields(MyClass.class).stream()
     *     .filter(f -> f.getType() == String.class)
     *     .collect(Collectors.toList());
     *
     * // Get field names
     * List<String> fieldNames = ReflectionHelper.getAllFields(MyClass.class).stream()
     *     .map(Field::getName)
     *     .collect(Collectors.toList());
     * }</pre>
     */
    public static List<Field> getAllFields(final Class clazz) {
        List<Field> allFields = new ArrayList<>();
        // Use Spring ReflectionUtils to traverse class hierarchy
        ReflectionUtils.doWithFields(clazz, allFields::add);
        return allFields;
    }

    /**
     * Retrieves property metadata for a specific property in a class.
     * <p>
     * Returns a {@link PropertyInfo} object containing detailed metadata about the property,
     * including its type, getter/setter methods, annotations, and more.
     * Uses the framework's caching mechanism for improved performance.
     * </p>
     *
     * @param clazz        the class containing the property
     * @param propertyName the name of the property
     * @return PropertyInfo object with property metadata, or null if not found
     *
     * Example:
     * <pre>{@code
     * PropertyInfo prop = ReflectionHelper.getPropertyInfo(Person.class, "name");
     *
     * if (prop != null) {
     *     System.out.println("Property: " + prop.getName());
     *     System.out.println("Type: " + prop.getType());
     *     System.out.println("Readable: " + prop.isReadable());
     *     System.out.println("Writable: " + prop.isWritable());
     *
     *     // Use property metadata
     *     Object value = prop.getValue(personInstance);
     *     prop.setValue(personInstance, "New Value");
     * }
     * }</pre>
     */
    public static PropertyInfo getPropertyInfo(final Class clazz, final String propertyName) {
        PropertyInfo info = null;

        try {
            if (propertyName.contains(".")) {
                final String child = propertyName.substring(propertyName.indexOf('.') + 1);
                final String parent = propertyName.substring(0, propertyName.indexOf('.'));
                final Class parentClass = clazz.getMethod(formatGetMethod(parent)).getReturnType();
                info = getPropertyInfo(parentClass, child);
            } else {
                final BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(clazz);
                for (PropertyDescriptor prp : beanInfo.getPropertyDescriptors()) {
                    if (prp.getName().equals(propertyName)) {
                        info = getPropertyInfo(prp);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new ReflectionException(e);
        }
        return info;
    }

    /**
     * Converts a JavaBeans PropertyDescriptor into a PropertyInfo object containing
     * detailed metadata about the property.
     *
     * <p>
     * This helper method extracts and analyzes property characteristics including:
     * <ul>
     *   <li>Property name and type</li>
     *   <li>Access mode (read-only, write-only, or read-write)</li>
     *   <li>Owner class (where the property is declared)</li>
     *   <li>Generic type information (for collections and parameterized types)</li>
     *   <li>Collection detection</li>
     * </ul>
     * </p>
     *
     * <p>
     * The 'class' property is automatically filtered out and will return null.
     * Properties without a defined type are also excluded.
     * </p>
     *
     * @param prop the PropertyDescriptor from JavaBeans introspection
     * @return a PropertyInfo object with complete metadata, or null if the property
     * should be filtered (e.g., 'class' property or properties without a type)
     */
    private static PropertyInfo getPropertyInfo(final PropertyDescriptor prop) {
        PropertyInfo info = null;
        String name = prop.getName();

        if (!name.equals("class") && prop.getPropertyType() != null) {
            Class type = prop.getPropertyType();
            Class ownerClass = null;
            AccessMode accessMode = null;

            boolean readable = prop.getReadMethod() != null;
            boolean writable = prop.getWriteMethod() != null;

            if (readable && writable) {
                accessMode = AccessMode.READ_WRITE;
            } else if (readable) {
                accessMode = AccessMode.READ_ONLY;
            } else if (writable) {
                accessMode = AccessMode.WRITE_ONLY;
            }

            if (readable) {
                ownerClass = prop.getReadMethod().getDeclaringClass();
            } else if (writable) {
                ownerClass = prop.getWriteMethod().getDeclaringClass();
            }

            info = new PropertyInfo(name, type, ownerClass, accessMode);

            try {
                info.setGenericType(getMethodGenericType(prop.getReadMethod()));
            } catch (Exception e) {
                info.setGenericType(null);
            }
            if (isAssignable(type, Collection.class)) {
                info.setCollection(true);
            }
        }

        return info;

    }

    /**
     * Retrieves metadata for all properties in a class.
     * <p>
     * Returns a list of {@link PropertyInfo} objects, one for each property in the class.
     * Uses the framework's caching mechanism for improved performance.
     * </p>
     *
     * @param clazz the class to inspect
     * @return list of PropertyInfo objects for all properties in the class
     *
     * Example:
     * <pre>{@code
     * List<PropertyInfo> properties = ReflectionHelper.getPropertiesInfo(Person.class);
     *
     * // Print all properties
     * properties.forEach(prop -> {
     *     System.out.println(prop.getName() + ": " + prop.getType());
     * });
     *
     * // Filter writable properties
     * List<PropertyInfo> writableProps = properties.stream()
     *     .filter(PropertyInfo::isWritable)
     *     .collect(Collectors.toList());
     *
     * // Get property names
     * List<String> names = properties.stream()
     *     .map(PropertyInfo::getName)
     *     .collect(Collectors.toList());
     * }</pre>
     */
    public static List<PropertyInfo> getPropertiesInfo(final Class clazz) {
        var cached = ClassReflectionInfo.getFromCache(clazz);

        if (cached == null) {
            try {
                List<PropertyInfo> infos = new ArrayList<>();
                BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(clazz);
                for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {

                    final PropertyInfo pi = getPropertyInfo(descriptor);
                    if (pi != null) {
                        infos.add(pi);
                    }
                }
                cached = new ClassReflectionInfo(clazz, infos);
                ClassReflectionInfo.addToCache(cached);
            } catch (IntrospectionException e1) {
                throw new ReflectionException(e1);
            }
        }
        return cached.properties();
    }

    /**
     * Finds all methods in a class that are annotated with a specific annotation.
     * <p>
     * Searches the class and its superclasses for methods with the specified annotation.
     * Returns an array of Method objects that have the annotation.
     * </p>
     *
     * @param targetClass     the class to search
     * @param annotationClass the annotation class to look for
     * @return array of methods with the specified annotation, empty array if none found
     *
     * Example:
     * <pre>{@code
     * // Find all @PostConstruct methods
     * Method[] initMethods = ReflectionHelper.getMethodsWithAnnotation(
     *     MyService.class, PostConstruct.class);
     *
     * for (Method method : initMethods) {
     *     method.invoke(instance);
     * }
     *
     * // Find custom annotation
     * Method[] transactional = ReflectionHelper.getMethodsWithAnnotation(
     *     MyRepository.class, Transactional.class);
     *
     * // Find REST endpoints
     * Method[] getEndpoints = ReflectionHelper.getMethodsWithAnnotation(
     *     MyController.class, GetMapping.class);
     * }</pre>
     */
    public static Method[] getMethodsWithAnnotation(Class<?> targetClass, Class<? extends Annotation> annotationClass) {
        if (targetClass == null || annotationClass == null) {
            return new Method[0];
        }

        List<Method> methods = new ArrayList<>();
        ReflectionUtils.doWithMethods(targetClass, method -> {
            if (method.isAnnotationPresent(annotationClass)) {
                methods.add(method);
            }
        });

        return methods.toArray(new Method[0]);
    }

    /**
     * Finds all fields in a class that are annotated with a specific annotation.
     * <p>
     * Searches the class and its superclasses for fields with the specified annotation.
     * Returns an array of Field objects that have the annotation.
     * </p>
     *
     * @param targetClass     the class to search
     * @param annotationClass the annotation class to look for
     * @return array of fields with the specified annotation, empty array if none found
     *
     * Example:
     * <pre>{@code
     * // Find all @Autowired fields
     * Field[] autowired = ReflectionHelper.getFieldsWithAnnotation(
     *     MyService.class, Autowired.class);
     *
     * // Find JPA columns
     * Field[] columns = ReflectionHelper.getFieldsWithAnnotation(
     *     MyEntity.class, Column.class);
     *
     * for (Field field : columns) {
     *     Column column = field.getAnnotation(Column.class);
     *     System.out.println("DB Column: " + column.name());
     * }
     *
     * // Find validation annotations
     * Field[] notNull = ReflectionHelper.getFieldsWithAnnotation(
     *     MyDTO.class, NotNull.class);
     * }</pre>
     */
    public static Field[] getFieldsWithAnnotation(Class<?> targetClass, Class<? extends Annotation> annotationClass) {
        if (targetClass == null || annotationClass == null) {
            return new Field[0];
        }

        List<Field> fields = new ArrayList<>();
        ReflectionUtils.doWithFields(targetClass, field -> {
            if (field.isAnnotationPresent(annotationClass)) {
                fields.add(field);
            }
        });

        return fields.toArray(new Field[0]);
    }

    /**
     * Gets the first field in a class that has a specific annotation.
     * <p>
     * This is a convenience method when you only need the first matching field.
     * Returns null if no field with the annotation is found.
     * </p>
     *
     * @param targetClass     the class to search
     * @param annotationClass the annotation class to look for
     * @return the first field with the annotation, or null if none found
     *
     * Example:
     * <pre>{@code
     * // Find @Id field in JPA entity
     * Field idField = ReflectionHelper.getFirstFieldWithAnnotation(
     *     MyEntity.class, Id.class);
     *
     * if (idField != null) {
     *     idField.setAccessible(true);
     *     Object id = idField.get(entityInstance);
     * }
     *
     * // Find @Version field for optimistic locking
     * Field versionField = ReflectionHelper.getFirstFieldWithAnnotation(
     *     MyEntity.class, Version.class);
     * }</pre>
     */
    public static Field getFirstFieldWithAnnotation(Class<?> targetClass, Class<? extends Annotation> annotationClass) {
        Field[] fields = getFieldsWithAnnotation(targetClass, annotationClass);
        return fields.length > 0 ? fields[0] : null;
    }

    /**
     * Extracts the generic type parameter from a field.
     * <p>
     * For fields with generic types like {@code List<String>}, this method returns {@code String.class}.
     * Handles common collection types and returns the first type parameter.
     * </p>
     *
     * @param field the field to inspect
     * @return the generic type class, or null if not available
     *
     * Example:
     * <pre>{@code
     * class MyClass {
     *     List<String> names;
     *     Set<Integer> numbers;
     * }
     *
     * Field namesField = MyClass.class.getDeclaredField("names");
     * Class<?> genericType = ReflectionHelper.getFieldGenericType(namesField);
     * // genericType = String.class
     *
     * Field numbersField = MyClass.class.getDeclaredField("numbers");
     * Class<?> numberType = ReflectionHelper.getFieldGenericType(numbersField);
     * // numberType = Integer.class
     * }</pre>
     */
    public static Class<?> getFieldGenericType(final Field field) {
        Type genericType = field.getGenericType();
        return extractGenericType(genericType);
    }

    /**
     * Extracts the generic type parameter from a getter method.
     * <p>
     * For methods that return generic types like {@code List<String>},
     * this method returns {@code String.class}.
     * </p>
     *
     * @param getterMethod the getter method to inspect
     * @return the generic type class, or null if not available
     *
     * Example:
     * <pre>{@code
     * class MyClass {
     *     public List<String> getNames() { ... }
     * }
     *
     * Method getter = MyClass.class.getMethod("getNames");
     * Class<?> genericType = ReflectionHelper.getMethodGenericType(getter);
     * // genericType = String.class
     * }</pre>
     */
    public static Class<?> getMethodGenericType(final Method getterMethod) {
        Type genericReturnType = getterMethod.getGenericReturnType();
        return extractGenericType(genericReturnType);
    }

    /**
     * Gets the generic type of a field by its name in a class.
     * <p>
     * Convenience method that combines field lookup and generic type extraction.
     * </p>
     *
     * @param clazz     the class containing the field
     * @param fieldName the name of the field
     * @return the generic type class, or null if not available
     * @throws NoSuchFieldException if the field is not found
     *
     * Example:
     * <pre>{@code
     * Class<?> genericType = ReflectionHelper.getFieldGenericType(
     *     MyClass.class, "items");
     * }</pre>
     */
    public static Class<?> getFieldGenericType(final Class clazz, final String fieldName) throws NoSuchFieldException {
        Field field = getField(clazz, fieldName);
        return getFieldGenericType(field);
    }

    /**
     * Checks if a class is assignable from another class.
     * <p>
     * Determines if an object of class 'a' can be assigned to variables or members of class 'b'.
     * This is a null-safe wrapper around {@link Class#isAssignableFrom(Class)}.
     * </p>
     *
     * @param a the source class
     * @param b the target class
     * @return true if 'a' is assignable to 'b', false otherwise
     *
     * Example:
     * <pre>{@code
     * boolean canAssign = ReflectionHelper.isAssignable(Integer.class, Number.class);
     * // true - Integer can be assigned to Number
     *
     * boolean canAssignString = ReflectionHelper.isAssignable(String.class, CharSequence.class);
     * // true - String can be assigned to CharSequence
     *
     * // Use in validation
     * if (ReflectionHelper.isAssignable(value.getClass(), expectedType)) {
     *     // Safe to assign
     * }
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    public static boolean isAssignable(final Class a, final Class b) {
        boolean assignable = false;
        if (a != null && b != null) {
            assignable = b.isAssignableFrom(a);
        }
        return assignable;
    }

    /**
     * Checks if a class is a primitive wrapper (e.g., Integer, Boolean, Double).
     * <p>
     * Returns true for wrapper classes like {@code java.lang.Integer}, {@code java.lang.Boolean}, etc.
     * Returns false for primitives themselves and for other classes.
     * </p>
     *
     * @param clazz the class to check
     * @return true if the class is a primitive wrapper, false otherwise
     *
     * Example:
     * <pre>{@code
     * boolean isWrapper = ReflectionHelper.isPrimitiveWrapper(Integer.class); // true
     * boolean isPrimitive = ReflectionHelper.isPrimitiveWrapper(int.class); // false
     * boolean isString = ReflectionHelper.isPrimitiveWrapper(String.class); // false
     *
     * // Use in type checking
     * if (ReflectionHelper.isPrimitiveWrapper(field.getType())) {
     *     // Handle wrapper types specially
     * }
     * }</pre>
     */
    public static boolean isPrimitiveWrapper(final Class clazz) {
        if (clazz == null) {
            return false;
        }
        return clazz == Boolean.class ||
               clazz == Byte.class ||
               clazz == Character.class ||
               clazz == Short.class ||
               clazz == Integer.class ||
               clazz == Long.class ||
               clazz == Float.class ||
               clazz == Double.class;
    }

    /**
     * Extracts the generic type from a Type object.
     * <p>
     * Internal helper method for extracting generic type parameters from
     * ParameterizedType instances.
     * </p>
     *
     * @param type the Type to extract from
     * @return the generic type class, or null if not available
     */
    private static Class<?> extractGenericType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs != null && typeArgs.length > 0) {
                Type typeArg = typeArgs[0];
                if (typeArg instanceof Class) {
                    return (Class<?>) typeArg;
                } else if (typeArg instanceof ParameterizedType) {
                    return (Class<?>) ((ParameterizedType) typeArg).getRawType();
                }
            }
        }
        return null;
    }

    /**
     * Format the property to a getter method name. "age" return "getAge"
     *
     * @param propertyName the property name
     * @return the string
     */
    public static String formatGetMethod(final String propertyName) {
        return "get" + StringUtils.capitalize(propertyName);
    }

    /**
     * Format the property to boolea getter method name . "visible" return
     * "isVisible"
     *
     * @param propertyName the property name
     * @return the string
     */
    public static String formatBooleanGetMethod(final String propertyName) {
        return "is" + StringUtils.capitalize(propertyName);
    }

    /**
     * Format the property to a setter method name. "age" return "setAge"
     *
     * @param propertyName the property name
     * @return the string
     */
    public static String formatSetMethod(final String propertyName) {
        return "set" + StringUtils.capitalize(propertyName);
    }


}
