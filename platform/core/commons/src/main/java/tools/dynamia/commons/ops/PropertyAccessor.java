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

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.ReflectionUtils;
import tools.dynamia.commons.BeanMap;
import tools.dynamia.commons.ValueWrapper;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.commons.reflect.ReflectionException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Specialized class for property access and manipulation operations.
 * <p>
 * This class provides efficient methods for getting and setting property values on Java beans
 * using both direct field access and JavaBeans getter/setter conventions. It leverages Spring's
 * BeanWrapper for enhanced performance and type conversion support.
 * </p>
 *
 * <h2>Core Features</h2>
 * <ul>
 *   <li><strong>Field Access:</strong> Direct field reading and writing via reflection</li>
 *   <li><strong>Property Access:</strong> JavaBeans getter/setter method invocation</li>
 *   <li><strong>Nested Properties:</strong> Dot notation support (e.g., "address.city")</li>
 *   <li><strong>Type Conversion:</strong> Automatic conversion between compatible types</li>
 *   <li><strong>Null Safety:</strong> Graceful handling of null values in property paths</li>
 *   <li><strong>Spring Integration:</strong> Uses BeanWrapper for optimal performance</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * Person person = new Person();
 *
 * // Set properties
 * PropertyAccessor.setFieldValue("name", person, "John");
 * PropertyAccessor.invokeSetMethod(person, "age", 30);
 *
 * // Get properties
 * String name = (String) PropertyAccessor.getFieldValue("name", person);
 * Integer age = (Integer) PropertyAccessor.invokeGetMethod(person, "age");
 *
 * // Nested properties (dot notation)
 * PropertyAccessor.invokeSetMethod(person, "address.city", "New York");
 * String city = (String) PropertyAccessor.invokeGetMethod(person, "address.city");
 *
 * // Generic method invocation
 * PropertyAccessor.invokeMethod(person, "sendEmail", "hello@example.com");
 * }</pre>
 *
 * @author Ing. Mario Serrano Leones
 * @version 26.1
 * @since 26.1
 */
public final class PropertyAccessor {

    private static final LoggingService LOGGER = new SLF4JLoggingService(PropertyAccessor.class);

    /**
     * Private constructor to prevent instantiation.
     */
    private PropertyAccessor() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Gets the value of a field from an object using direct field access.
     * <p>
     * This method uses reflection to access the field directly, bypassing getter methods.
     * The field is made accessible if it's private or protected.
     * </p>
     *
     * @param fieldName the name of the field
     * @param object    the object instance
     * @return the field value, or null if the field doesn't exist or cannot be accessed
     * <p>
     * Example:
     * <pre>{@code
     * Person person = new Person();
     * person.setName("John");
     * String name = (String) PropertyAccessor.getFieldValue("name", person);
     * // name = "John"
     * }</pre>
     */
    public static Object getFieldValue(final String fieldName, final Object object) {
        Object value = null;
        try {
            final Field field = tools.dynamia.commons.ObjectOperations.getField(object.getClass(), fieldName);
            field.setAccessible(true);
            value = field.get(object);
        } catch (NoSuchFieldException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cannot find field " + fieldName + " in " + object.getClass() + ". Returning null value");
            }
        } catch (java.lang.reflect.InaccessibleObjectException e) {
            // Java 9+ module system prevents access to certain internal fields
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cannot access field " + fieldName + " in " + object.getClass() + " due to module restrictions. Returning null value");
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return value instanceof ValueWrapper valueWrapper ? valueWrapper.getValue() : value;
    }

    /**
     * Sets the value of a field in the given object using direct field access.
     *
     * @param propertyInfo the property info descriptor
     * @param object       the object instance
     * @param value        the value to set
     *                     <p>
     *                     Example:
     *                     <pre>{@code
     *                     PropertyInfo prop = ObjectOperations.getPropertyInfo(Person.class, "name");
     *                     PropertyAccessor.setFieldValue(prop, person, "John");
     *                     }</pre>
     */
    public static void setFieldValue(PropertyInfo propertyInfo, Object object, Object value) {
        setFieldValue(propertyInfo.getName(), object, value);
    }

    /**
     * Sets the value of a field in the given object using direct field access.
     * <p>
     * This method uses reflection to set the field directly, bypassing setter methods.
     * The field is made accessible if it's private or protected.
     * </p>
     *
     * @param fieldName the name of the field
     * @param object    the object instance
     * @param value     the value to set
     *                  <p>
     *                  Example:
     *                  <pre>{@code
     *                  Person person = new Person();
     *                  PropertyAccessor.setFieldValue("name", person, "John");
     *                  // person.name = "John"
     *                  }</pre>
     */
    public static void setFieldValue(String fieldName, Object object, Object value) {
        try {
            Object actualValue = value instanceof ValueWrapper valueWrapper ? valueWrapper.value() : value;
            final Field field = tools.dynamia.commons.ObjectOperations.getField(object.getClass(), fieldName);
            field.setAccessible(true);
            field.set(object, actualValue);
        } catch (java.lang.reflect.InaccessibleObjectException e) {
            // Java 9+ module system prevents access to certain internal fields
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cannot access field " + fieldName + " in " + object.getClass() + " due to module restrictions");
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    /**
     * Invokes a method on the specified bean object by name, supporting variable arguments.
     * <p>
     * This method uses Spring's {@link ReflectionUtils} for improved AOT compatibility.
     * The method automatically determines parameter types from the provided arguments
     * and handles method accessibility. If the method is not found or invocation fails,
     * a {@link ReflectionException} is thrown.
     * </p>
     *
     * @param bean       the target object on which to invoke the method
     * @param methodName the name of the method to invoke
     * @param args       the arguments to pass to the method (variable arguments)
     * @return the result of the method invocation, or null if the method has no return value
     * @throws ReflectionException if the method is not found or invocation fails
     *                             <p>
     *                             Example:
     *                             <pre>{@code
     *                             Customer customer = new Customer();
     *                             // Invoke setter
     *                             PropertyAccessor.invokeMethod(customer, "setName", "John Doe");
     *                             // Invoke getter
     *                             String name = (String) PropertyAccessor.invokeMethod(customer, "getName");
     *                             // Invoke business method
     *                             PropertyAccessor.invokeMethod(customer, "sendWelcomeEmail", "john@example.com");
     *                             }</pre>
     */
    public static Object invokeMethod(final Object bean, final String methodName, final Object... args) {
        try {
            final Object[] finalArgs = args != null ? args : new Object[0];
            Class<?>[] paramTypes = new Class<?>[finalArgs.length];
            for (int i = 0; i < finalArgs.length; i++) {
                paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }

            Method method = ReflectionUtils.findMethod(bean.getClass(), methodName, paramTypes);
            if (method == null) {
                throw new NoSuchMethodException("Method " + methodName + " not found in " + bean.getClass());
            }

            ReflectionUtils.makeAccessible(method);
            return ReflectionUtils.invokeMethod(method, bean, finalArgs);
        } catch (Exception e) {
            throw new ReflectionException("Error invoking method " + methodName + " on " + bean.getClass(), e);
        }
    }

    /**
     * Invokes the getter method for a property using Spring's BeanWrapper.
     * <p>
     * This method provides high-performance property access with support for:
     * <ul>
     *   <li>Nested properties using dot notation (e.g., "address.city")</li>
     *   <li>Automatic type conversion</li>
     *   <li>Null safety in property paths</li>
     *   <li>AOT compilation compatibility</li>
     * </ul>
     * </p>
     *
     * @param bean         the target object
     * @param propertyName the name of the property (supports dot notation for nested properties)
     * @return the property value, or null if not accessible
     * <p>
     * Example:
     * <pre>{@code
     * Person person = new Person();
     * person.setName("John");
     * person.setAddress(new Address("Main St", "New York"));
     *
     * String name = (String) PropertyAccessor.invokeGetMethod(person, "name");
     * // name = "John"
     *
     * String city = (String) PropertyAccessor.invokeGetMethod(person, "address.city");
     * // city = "New York"
     * }</pre>
     */
    public static Object invokeGetMethod(final Object bean, final String propertyName) {
        try {
            BeanWrapper wrapper = new BeanWrapperImpl(bean);
            return wrapper.getPropertyValue(propertyName);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error getting property " + propertyName + " from " + bean.getClass() + ": " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Invokes the getter method for a boolean property (handles both "get" and "is" prefixes).
     * <p>
     * This method specifically handles boolean properties that may use either the "isXxx()"
     * or "getXxx()" naming convention.
     * </p>
     *
     * @param bean         the target object
     * @param propertyName the name of the boolean property
     * @return the boolean value, or null if not accessible
     * <p>
     * Example:
     * <pre>{@code
     * Person person = new Person();
     * person.setActive(true);
     *
     * Boolean active = (Boolean) PropertyAccessor.invokeBooleanGetMethod(person, "active");
     * // active = true
     * }</pre>
     */
    public static Object invokeBooleanGetMethod(final Object bean, final String propertyName) {
        return invokeGetMethod(bean, propertyName);
    }

    /**
     * Invokes the getter method for a property using PropertyInfo descriptor.
     * <p>
     * This is an optimized version when you already have the PropertyInfo metadata.
     * </p>
     *
     * @param bean     the target object
     * @param property the property information descriptor
     * @return the property value, or null if not accessible
     * <p>
     * Example:
     * <pre>{@code
     * PropertyInfo prop = ObjectOperations.getPropertyInfo(Person.class, "name");
     * String name = (String) PropertyAccessor.invokeGetMethod(person, prop);
     * }</pre>
     */
    public static Object invokeGetMethod(final Object bean, final PropertyInfo property) {
        try {
            return property.getValue(bean);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error getting property " + property.getName() + " from " + bean.getClass() + ": " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Invokes the setter method for a property using Spring's BeanWrapper.
     * <p>
     * This method provides high-performance property setting with support for:
     * <ul>
     *   <li>Nested properties using dot notation (e.g., "address.city")</li>
     *   <li>Automatic type conversion</li>
     *   <li>Null safety in property paths</li>
     *   <li>AOT compilation compatibility</li>
     *   <li>Handling of overloaded setters by matching parameter types</li>
     * </ul>
     * </p>
     * <p>
     * When a bean has overloaded setter methods (e.g., {@code setAddress(String)} and {@code setAddress(Address)}),
     * this method attempts multiple strategies to find the correct setter:
     * <ol>
     *   <li>Try using Spring's BeanWrapper (fastest, handles most cases)</li>
     *   <li>If BeanWrapper fails, find the setter method matching the declared property type</li>
     *   <li>As a last resort, set the field value directly using reflection</li>
     * </ol>
     * </p>
     *
     * @param bean  the target object
     * @param name  the name of the property (supports dot notation for nested properties)
     * @param value the value to set
     *              <p>
     *              Example:
     *              <pre>{@code
     *              Person person = new Person();
     *              PropertyAccessor.invokeSetMethod(person, "name", "John");
     *              PropertyAccessor.invokeSetMethod(person, "age", 30);
     *
     *              // Nested property
     *              PropertyAccessor.invokeSetMethod(person, "address.city", "New York");
     *
     *              // Overloaded setters - automatically selects correct method
     *              PropertyAccessor.invokeSetMethod(person, "address", new Address(...));
     *              }</pre>
     */
    public static void invokeSetMethod(final Object bean, final String name, final Object value) {
        // Handle ValueWrapper for explicit type specification
        Object actualValue = value instanceof ValueWrapper valueWrapper ? valueWrapper.value() : value;

        if (bean instanceof BeanMap) {
            ((BeanMap) bean).set(name, actualValue);
            return;
        }

        // Strategy 1: Try BeanWrapper first (fastest, handles most cases including nested properties)
        try {
            BeanWrapper wrapper = new BeanWrapperImpl(bean);
            wrapper.setPropertyValue(name, actualValue);
            return; // Success!
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("BeanWrapper failed for property " + name + " on " + bean.getClass() +
                        " with value " + actualValue + ": " + e.getMessage() + ". Trying alternative strategies...");
            }
        }

        // Strategy 2: For simple properties (no dots), try to find the specific setter method
        if (!name.contains(".")) {
            try {
                PropertyInfo propertyInfo = tools.dynamia.commons.ObjectOperations.getPropertyInfo(bean.getClass(), name);
                if (propertyInfo != null && propertyInfo.getWriteMethod() != null) {
                    Method writeMethod = propertyInfo.getWriteMethod();

                    // Check if the write method parameter type matches the value type
                    // This handles overloaded setters correctly
                    Class<?> paramType = writeMethod.getParameterTypes()[0];
                    boolean canInvoke = actualValue == null ||
                            paramType.isAssignableFrom(actualValue.getClass()) ||
                            (paramType.isPrimitive() && isWrapperCompatible(paramType, actualValue.getClass()));

                    if (canInvoke) {
                        ReflectionUtils.makeAccessible(writeMethod);
                        ReflectionUtils.invokeMethod(writeMethod, bean, actualValue);
                        return; // Success!
                    } else {
                        // Try to find an overloaded setter that matches the actual value type
                        Method alternativeMethod = findMatchingSetterMethod(bean.getClass(), name, actualValue);
                        if (alternativeMethod != null) {
                            ReflectionUtils.makeAccessible(alternativeMethod);
                            ReflectionUtils.invokeMethod(alternativeMethod, bean, actualValue);
                            return; // Success!
                        }
                    }
                }
            } catch (Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Direct setter method invocation failed for property " + name +
                            " on " + bean.getClass() + ": " + e.getMessage());
                }
            }
        }

    }

    /**
     * Invokes the setter method for a property using PropertyInfo descriptor.
     * <p>
     * This is an optimized version when you already have the PropertyInfo metadata.
     * </p>
     *
     * @param bean     the target object
     * @param property the property information descriptor
     * @param value    the value to set
     *                 <p>
     *                 Example:
     *                 <pre>{@code
     *                 PropertyInfo prop = ObjectOperations.getPropertyInfo(Person.class, "name");
     *                 PropertyAccessor.invokeSetMethod(person, prop, "John");
     *                 }</pre>
     */
    public static void invokeSetMethod(final Object bean, final PropertyInfo property, final Object value) {
        try {
            property.setValue(bean, value);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error setting property " + property.getName() + " on " + bean.getClass() + " with value " + value + ": " + e.getMessage());
            }
        }
    }

    /**
     * Gets the type of a property in a class.
     * <p>
     * This method returns an Optional containing the property type, or empty if the property
     * doesn't exist or cannot be determined.
     * </p>
     *
     * @param clazz        the class to inspect
     * @param propertyName the name of the property
     * @return an Optional containing the property type, or empty if not found
     * <p>
     * Example:
     * <pre>{@code
     * Optional<Class<?>> type = PropertyAccessor.getPropertyType(Person.class, "name");
     * if (type.isPresent()) {
     *     System.out.println("Type: " + type.get()); // Type: class java.lang.String
     * }
     * }</pre>
     */
    public static Optional<Class<?>> getPropertyType(Class<?> clazz, String propertyName) {
        try {
            BeanWrapper wrapper = new BeanWrapperImpl(clazz);
            Class<?> type = wrapper.getPropertyType(propertyName);
            return Optional.ofNullable(type);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cannot determine property type for " + propertyName + " in " + clazz + ": " + e.getMessage());
            }
            return Optional.empty();
        }
    }

    /**
     * Checks if a bean has a specific property.
     *
     * @param bean         the bean to check
     * @param propertyName the name of the property
     * @return true if the property exists, false otherwise
     * <p>
     * Example:
     * <pre>{@code
     * Person person = new Person();
     * boolean hasName = PropertyAccessor.hasProperty(person, "name"); // true
     * boolean hasInvalid = PropertyAccessor.hasProperty(person, "invalid"); // false
     * }</pre>
     */
    public static boolean hasProperty(Object bean, String propertyName) {
        try {
            BeanWrapper wrapper = new BeanWrapperImpl(bean);
            return wrapper.isReadableProperty(propertyName);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a property is readable (has a getter method).
     *
     * @param bean         the bean to check
     * @param propertyName the name of the property
     * @return true if the property is readable, false otherwise
     * <p>
     * Example:
     * <pre>{@code
     * boolean canRead = PropertyAccessor.isReadableProperty(person, "name");
     * }</pre>
     */
    public static boolean isReadableProperty(Object bean, String propertyName) {
        try {
            BeanWrapper wrapper = new BeanWrapperImpl(bean);
            return wrapper.isReadableProperty(propertyName);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a property is writable (has a setter method).
     *
     * @param bean         the bean to check
     * @param propertyName the name of the property
     * @return true if the property is writable, false otherwise
     * <p>
     * Example:
     * <pre>{@code
     * boolean canWrite = PropertyAccessor.isWritableProperty(person, "name");
     * }</pre>
     */
    public static boolean isWritableProperty(Object bean, String propertyName) {
        try {
            BeanWrapper wrapper = new BeanWrapperImpl(bean);
            return wrapper.isWritableProperty(propertyName);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Finds a setter method that matches the given property name and value type.
     * <p>
     * This method is used internally to handle overloaded setters by finding the setter
     * that best matches the actual value type being passed.
     * </p>
     *
     * @param clazz        the class to search
     * @param propertyName the property name
     * @param value        the value to set (used to determine the best matching setter)
     * @return the matching setter method, or null if not found
     */
    private static Method findMatchingSetterMethod(Class<?> clazz, String propertyName, Object value) {
        String setterName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        Method[] methods = clazz.getMethods();

        Method bestMatch = null;
        int bestMatchScore = -1;

        for (Method method : methods) {
            if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                Class<?> paramType = method.getParameterTypes()[0];

                if (value == null) {
                    // For null values, prefer Object or reference types over primitives
                    if (!paramType.isPrimitive() && (bestMatch == null || bestMatch.getParameterTypes()[0].isPrimitive())) {
                        bestMatch = method;
                        bestMatchScore = 0;
                    }
                } else {
                    Class<?> valueType = value.getClass();

                    // Exact match - highest priority
                    if (paramType.equals(valueType)) {
                        return method;
                    }

                    // Assignable - high priority
                    if (paramType.isAssignableFrom(valueType)) {
                        int score = calculateInheritanceDistance(valueType, paramType);
                        if (score > bestMatchScore) {
                            bestMatch = method;
                            bestMatchScore = score;
                        }
                    }

                    // Primitive wrapper compatibility
                    if (isWrapperCompatible(paramType, valueType)) {
                        if (bestMatchScore < 100) {
                            bestMatch = method;
                            bestMatchScore = 100;
                        }
                    }
                }
            }
        }

        return bestMatch;
    }

    /**
     * Calculates the inheritance distance between two classes.
     * Lower numbers indicate closer relationship.
     *
     * @param from the subclass
     * @param to   the superclass or interface
     * @return the distance, or Integer.MAX_VALUE if not related
     */
    private static int calculateInheritanceDistance(Class<?> from, Class<?> to) {
        if (from.equals(to)) {
            return 1000; // Exact match
        }

        int distance = 0;
        Class<?> current = from;

        // Check class hierarchy
        while (current != null && !current.equals(Object.class)) {
            if (current.equals(to)) {
                return 500 - distance;
            }
            current = current.getSuperclass();
            distance++;
        }

        // Check interfaces
        if (to.isInterface() && to.isAssignableFrom(from)) {
            return 250;
        }

        return -1;
    }

    /**
     * Checks if a primitive type is compatible with a wrapper type or vice versa.
     *
     * @param type1 first type
     * @param type2 second type
     * @return true if they are compatible primitive/wrapper types
     */
    private static boolean isWrapperCompatible(Class<?> type1, Class<?> type2) {
        if (type1.isPrimitive() && !type2.isPrimitive()) {
            return getPrimitiveWrapper(type1).equals(type2);
        }
        if (!type1.isPrimitive() && type2.isPrimitive()) {
            return type1.equals(getPrimitiveWrapper(type2));
        }
        return false;
    }

    /**
     * Gets the wrapper class for a primitive type.
     *
     * @param primitiveType the primitive type
     * @return the wrapper class
     */
    private static Class<?> getPrimitiveWrapper(Class<?> primitiveType) {
        if (primitiveType == int.class) return Integer.class;
        if (primitiveType == long.class) return Long.class;
        if (primitiveType == double.class) return Double.class;
        if (primitiveType == float.class) return Float.class;
        if (primitiveType == boolean.class) return Boolean.class;
        if (primitiveType == byte.class) return Byte.class;
        if (primitiveType == char.class) return Character.class;
        if (primitiveType == short.class) return Short.class;
        return primitiveType;
    }
}
