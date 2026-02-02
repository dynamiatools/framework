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
     *
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
     *
     * Example:
     * <pre>{@code
     * PropertyInfo prop = ObjectOperations.getPropertyInfo(Person.class, "name");
     * PropertyAccessor.setFieldValue(prop, person, "John");
     * }</pre>
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
     *
     * Example:
     * <pre>{@code
     * Person person = new Person();
     * PropertyAccessor.setFieldValue("name", person, "John");
     * // person.name = "John"
     * }</pre>
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
     *
     * Example:
     * <pre>{@code
     * Customer customer = new Customer();
     * // Invoke setter
     * PropertyAccessor.invokeMethod(customer, "setName", "John Doe");
     * // Invoke getter
     * String name = (String) PropertyAccessor.invokeMethod(customer, "getName");
     * // Invoke business method
     * PropertyAccessor.invokeMethod(customer, "sendWelcomeEmail", "john@example.com");
     * }</pre>
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
     *
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
     *
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
     *
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
     * </ul>
     * </p>
     *
     * @param bean  the target object
     * @param name  the name of the property (supports dot notation for nested properties)
     * @param value the value to set
     *
     * Example:
     * <pre>{@code
     * Person person = new Person();
     * PropertyAccessor.invokeSetMethod(person, "name", "John");
     * PropertyAccessor.invokeSetMethod(person, "age", 30);
     *
     * // Nested property
     * PropertyAccessor.invokeSetMethod(person, "address.city", "New York");
     * }</pre>
     */
    public static void invokeSetMethod(final Object bean, final String name, final Object value) {
        // Handle ValueWrapper for explicit type specification
        Object actualValue = value instanceof ValueWrapper valueWrapper ? valueWrapper.value() : value;


        if (bean instanceof BeanMap) {
            ((BeanMap) bean).set(name, actualValue);
            return;
        }


        try {
            BeanWrapper wrapper = new BeanWrapperImpl(bean);
            wrapper.setPropertyValue(name, actualValue);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error setting property " + name + " on " + bean.getClass() + " with value " + actualValue + ": " + e.getMessage());
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
     *
     * Example:
     * <pre>{@code
     * PropertyInfo prop = ObjectOperations.getPropertyInfo(Person.class, "name");
     * PropertyAccessor.invokeSetMethod(person, prop, "John");
     * }</pre>
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
     *
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
     *
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
     *
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
     *
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
}
