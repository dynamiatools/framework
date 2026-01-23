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
package tools.dynamia.commons.ops;

import org.springframework.beans.BeanUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.PropertyInfo;

import java.util.*;

/**
 * Specialized class for object instantiation and cloning operations.
 * <p>
 * This class provides efficient methods for creating new instances and cloning objects
 * with support for both shallow and deep copy strategies. It handles collections, maps,
 * arrays, and nested objects with automatic type detection and recursive cloning.
 * </p>
 *
 * <h2>Core Features</h2>
 * <ul>
 *   <li><strong>Bean Instantiation:</strong> Create new instances with or without constructor arguments</li>
 *   <li><strong>Shallow Cloning:</strong> Fast copy excluding collections and arrays</li>
 *   <li><strong>Deep Cloning:</strong> Recursive copy including all nested structures</li>
 *   <li><strong>Smart Type Handling:</strong> Automatic detection of immutable types</li>
 *   <li><strong>Exclusion Support:</strong> Optional property exclusion in cloning</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Create instances
 * Person person = ObjectCloner.newInstance(Person.class);
 * Person withArgs = ObjectCloner.newInstance(Person.class, "John", 30);
 *
 * // Shallow clone (excludes collections/arrays)
 * Person shallowCopy = ObjectCloner.clone(person, "id", "createdAt");
 *
 * // Deep clone (includes collections/arrays)
 * Person deepCopy = ObjectCloner.deepClone(person);
 * }</pre>
 *
 * @author Ing. Mario Serrano Leones
 * @version 26.1
 * @since 26.1
 */
public final class ObjectCloner {

    private static final LoggingService LOGGER = new SLF4JLoggingService(ObjectCloner.class);

    /**
     * Private constructor to prevent instantiation.
     */
    private ObjectCloner() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Creates a new instance of the specified class using the default constructor.
     *
     * @param <T>   the type of the class
     * @param clazz the class to instantiate
     * @return a new instance of the class
     * @throws tools.dynamia.commons.reflect.ReflectionException if instantiation fails
     *
     * Example:
     * <pre>{@code
     * Person person = ObjectCloner.newInstance(Person.class);
     * }</pre>
     */
    public static <T> T newInstance(final Class<T> clazz) {
        try {
            return BeanUtils.instantiateClass(clazz);
        } catch (Exception e) {
            throw new tools.dynamia.commons.reflect.ReflectionException("Cannot create instance of " + clazz, e);
        }
    }

    /**
     * Creates a new instance of the specified class using a constructor with arguments.
     *
     * @param <T>   the type of the class
     * @param clazz the class to instantiate
     * @param args  constructor arguments
     * @return a new instance of the class
     * @throws tools.dynamia.commons.reflect.ReflectionException if instantiation fails
     *
     * Example:
     * <pre>{@code
     * Person person = ObjectCloner.newInstance(Person.class, "John", 30);
     * }</pre>
     */
    public static <T> T newInstance(final Class<T> clazz, Object... args) {
        try {
            Class<?>[] paramTypes = Arrays.stream(args)
                    .map(Object::getClass)
                    .toArray(Class<?>[]::new);
            return BeanUtils.instantiateClass(clazz.getConstructor(paramTypes), args);
        } catch (Exception e) {
            throw new tools.dynamia.commons.reflect.ReflectionException(
                    "Cannot create instance of " + clazz + " with provided arguments", e);
        }
    }

    /**
     * Creates a new instance from class name.
     *
     * @param <T>       the type of the class
     * @param className fully qualified class name
     * @return a new instance of the class
     * @throws tools.dynamia.commons.reflect.ReflectionException if instantiation fails
     *
     * Example:
     * <pre>{@code
     * Person person = ObjectCloner.newInstance("com.example.Person");
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className) {
        try {
            Class<T> clazz = (Class<T>) Class.forName(className);
            return newInstance(clazz);
        } catch (ClassNotFoundException e) {
            throw new tools.dynamia.commons.reflect.ReflectionException("Class not found: " + className, e);
        }
    }

    /**
     * Creates a simple shallow clone from source object, including only standard Java types and primitives.
     * Collections, maps, and arrays are automatically excluded from cloning.
     * <p>
     * This method is suitable for simple objects where you don't need to clone nested collections or complex objects.
     * For a complete deep copy including collections and nested objects, use {@link #deepClone(Object, String...)}.
     * </p>
     *
     * @param <T>                the type of the source object
     * @param source             the object to clone
     * @param excludedProperties optional additional property names to exclude from cloning
     * @return a shallow clone of the source object
     *
     * @see #deepClone(Object, String...) for deep cloning including collections and nested objects
     *
     * Example:
     * <pre>{@code
     * class Person {
     *     String name;
     *     int age;
     *     List<Address> addresses; // This will NOT be cloned
     * }
     *
     * Person original = new Person();
     * original.setName("John");
     * original.setAge(30);
     * original.setAddresses(List.of(new Address("Street 1")));
     *
     * Person shallowCopy = ObjectCloner.clone(original);
     * // shallowCopy.name = "John", shallowCopy.age = 30
     * // shallowCopy.addresses = null (excluded)
     * }</pre>
     */
    public static <T> T clone(T source, String... excludedProperties) {
        @SuppressWarnings("unchecked")
        Class<T> sourceClass = (Class<T>) source.getClass();
        T clon = newInstance(sourceClass);

        // Build list of properties to exclude (collections, arrays, and user-specified)
        Set<String> propsToExclude = new HashSet<>();
        if (excludedProperties != null) {
            propsToExclude.addAll(Arrays.asList(excludedProperties));
        }

        // Identify and exclude collection and array properties
        tools.dynamia.commons.ObjectOperations.getPropertiesInfo(sourceClass)
                .stream()
                .filter(p -> !isShallowClonable(p))
                .forEach(p -> propsToExclude.add(p.getName()));

        // Copy properties excluding collections, arrays, and user-specified properties
        BeanUtils.copyProperties(source, clon, propsToExclude.toArray(new String[0]));

        return clon;
    }

    /**
     * Creates a deep clone of the source object, including all properties, collections, and maps.
     * Unlike the simple {@link #clone(Object, String...)} method, this performs a recursive deep copy
     * where collections and maps are cloned by creating new instances and cloning each element.
     * <p>
     * This method handles:
     * <ul>
     *   <li>Primitive types and wrappers (copied by value)</li>
     *   <li>Strings and other immutable types (shared reference, safe)</li>
     *   <li>Collections (List, Set, etc.) - creates new collection and clones each element</li>
     *   <li>Maps - creates new map and clones both keys and values</li>
     *   <li>Arrays - creates new array and clones each element</li>
     *   <li>Custom objects - recursively cloned</li>
     * </ul>
     * </p>
     *
     * @param <T>                the type of the source object
     * @param source             the object to clone
     * @param excludedProperties optional property names to exclude from cloning
     * @return a deep clone of the source object
     * @throws tools.dynamia.commons.reflect.ReflectionException if the object cannot be cloned
     *
     * Example:
     * <pre>{@code
     * class Person {
     *     String name;
     *     List<Address> addresses;
     * }
     *
     * Person original = new Person();
     * original.setName("John");
     * original.setAddresses(List.of(new Address("Street 1")));
     *
     * Person deepCopy = ObjectCloner.deepClone(original);
     * // deepCopy.addresses is a new list with cloned Address objects
     * }</pre>
     */
    public static <T> T deepClone(T source, String... excludedProperties) {
        if (source == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Class<T> sourceClass = (Class<T>) source.getClass();

        // Handle immutable and primitive types
        if (isImmutableType(sourceClass)) {
            return source;
        }

        T cloned = newInstance(sourceClass);

        Set<String> propsToExclude = new HashSet<>();
        if (excludedProperties != null) {
            propsToExclude.addAll(Arrays.asList(excludedProperties));
        }

        // Get all properties and clone them recursively
        List<PropertyInfo> properties = tools.dynamia.commons.ObjectOperations.getPropertiesInfo(sourceClass);
        for (PropertyInfo property : properties) {
            if (propsToExclude.contains(property.getName())) {
                continue;
            }

            try {
                Object value = property.getValue(source);
                if (value == null) {
                    continue;
                }

                Object clonedValue = deepCloneValue(value, property.getType());
                property.setValue(cloned, clonedValue);
            } catch (Exception e) {
                LOGGER.warn("Cannot deep clone property: " + property.getName() + " of class " + sourceClass.getName(), e);
            }
        }

        return cloned;
    }

    /**
     * Deep clones a value based on its type, handling collections, maps, arrays, and objects.
     *
     * @param value        the value to clone
     * @param expectedType the expected type of the value
     * @return the cloned value
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static Object deepCloneValue(Object value, Class<?> expectedType) {
        if (value == null) {
            return null;
        }

        Class<?> valueClass = value.getClass();

        // Handle immutable types (String, primitives wrappers, etc.)
        if (isImmutableType(valueClass)) {
            return value;
        }

        // Handle Collections
        if (value instanceof Collection) {
            Collection<?> sourceCollection = (Collection<?>) value;
            Collection clonedCollection;

            // Try to create the same type of collection
            if (value instanceof List) {
                clonedCollection = new ArrayList<>(sourceCollection.size());
            } else if (value instanceof Set) {
                clonedCollection = new HashSet<>(sourceCollection.size());
            } else {
                // Try to create instance of the same collection type
                try {
                    clonedCollection = (Collection) valueClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    clonedCollection = new ArrayList<>(sourceCollection.size());
                }
            }

            // Clone each element
            for (Object item : sourceCollection) {
                if (item == null) {
                    clonedCollection.add(null);
                } else {
                    clonedCollection.add(deepCloneValue(item, item.getClass()));
                }
            }

            return clonedCollection;
        }

        // Handle Maps
        if (value instanceof Map) {
            Map<?, ?> sourceMap = (Map<?, ?>) value;
            Map clonedMap;

            // Try to create the same type of map
            try {
                clonedMap = (Map) valueClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                clonedMap = new HashMap<>(sourceMap.size());
            }

            // Clone each key-value pair
            for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
                Object clonedKey = entry.getKey() == null ? null :
                        deepCloneValue(entry.getKey(), entry.getKey().getClass());
                Object clonedValue = entry.getValue() == null ? null :
                        deepCloneValue(entry.getValue(), entry.getValue().getClass());
                clonedMap.put(clonedKey, clonedValue);
            }

            return clonedMap;
        }

        // Handle Arrays
        if (valueClass.isArray()) {
            int length = java.lang.reflect.Array.getLength(value);
            Class<?> componentType = valueClass.getComponentType();
            Object clonedArray = java.lang.reflect.Array.newInstance(componentType, length);

            for (int i = 0; i < length; i++) {
                Object item = java.lang.reflect.Array.get(value, i);
                if (item != null) {
                    Object clonedItem = deepCloneValue(item, componentType);
                    java.lang.reflect.Array.set(clonedArray, i, clonedItem);
                }
            }

            return clonedArray;
        }

        // Handle custom objects recursively
        try {
            return deepClone(value);
        } catch (Exception e) {
            LOGGER.warn("Cannot deep clone object of type: " + valueClass.getName() + ", returning original reference", e);
            return value;
        }
    }

    /**
     * Checks if a type is immutable and can be safely shared between clones.
     *
     * @param type the type to check
     * @return true if the type is immutable
     */
    static boolean isImmutableType(Class<?> type) {
        return type.isPrimitive() ||
                type == String.class ||
                type == Boolean.class ||
                type == Byte.class ||
                type == Character.class ||
                type == Short.class ||
                type == Integer.class ||
                type == Long.class ||
                type == Float.class ||
                type == Double.class ||
                type.isEnum() ||
                java.time.temporal.Temporal.class.isAssignableFrom(type) ||
                java.util.Date.class.isAssignableFrom(type);
    }

    /**
     * Checks if a property can be shallow cloned (primitive types and immutables).
     *
     * @param property the property to check
     * @return true if the property can be shallow cloned
     */
    private static boolean isShallowClonable(PropertyInfo property) {
        return property!=null && property.isShallowClonable();
    }
}
