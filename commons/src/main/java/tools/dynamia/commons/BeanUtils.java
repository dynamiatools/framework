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
package tools.dynamia.commons;

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.AccessMode;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.commons.reflect.ReflectionException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Reflection utility class for working with beans
 *
 * @author Ing. Mario Serrano Leones
 */
@SuppressWarnings("rawtypes")
public final class BeanUtils {

    /**
     * The Constant WRAPPERS.
     */
    private static final Map<Class, Class> WRAPPERS = new HashMap<>();

    /**
     * The Constant LOGGER.
     */
    private static final LoggingService LOGGER = new SLF4JLoggingService(BeanUtils.class);

    static {
        WRAPPERS.put(byte.class, Byte.class);
        WRAPPERS.put(short.class, Short.class);
        WRAPPERS.put(char.class, Character.class);
        WRAPPERS.put(int.class, Integer.class);
        WRAPPERS.put(long.class, Long.class);
        WRAPPERS.put(float.class, Float.class);
        WRAPPERS.put(double.class, Double.class);
        WRAPPERS.put(boolean.class, Boolean.class);
    }

    /**
     * Gets the field value.
     *
     * @param fieldName the field name
     * @param object    the object
     * @return the field value
     */
    public static Object getFieldValue(final String fieldName, final Object object) {
        Object value = null;
        try {
            final Field field = getField(object.getClass(), fieldName);
            field.setAccessible(true);
            value = field.get(object);
        } catch (NoSuchFieldException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cannot find field " + fieldName + " in " + object.getClass() + ". Returning null value");
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return value;
    }

    /**
     * Sets the field value.
     *
     * @param propertyInfo the property info
     * @param object       the object
     * @param value        the value
     */
    public static void setFieldValue(PropertyInfo propertyInfo, Object object, Object value) {
        setFieldValue(propertyInfo.getName(), object, value);
    }

    /**
     * Sets the field value.
     *
     * @param fieldName the field name
     * @param object    the object
     * @param value     the value
     */
    public static void setFieldValue(String fieldName, Object object, Object value) {

        try {
            final Field field = getField(object.getClass(), fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    /**
     * Instantiates a new bean utils.
     */
    private BeanUtils() {
        throw new IllegalAccessError("Hey this is private");
    }

    /**
     * New instance.
     *
     * @param <T>   the generic type
     * @param clazz the clazz
     * @return the t
     */
    public static <T> T newInstance(final Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new ReflectionException(e);
        }
    }

    /**
     * New instance.
     *
     * @param <T>       the generic type
     * @param className the class name
     * @return the t
     */
    public static <T> T newInstance(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }

        try {
            Class<T> clazz = (Class<T>) Class.forName(className);
            return newInstance(clazz);
        } catch (ClassNotFoundException e) {
            throw new ReflectionException(e);
        }
    }

    /**
     * Invoke an arbirtrary method from bean object pass as parameter.
     *
     * @param bean       the bean
     * @param methodName the method name
     * @param args       the args
     * @return the object
     */
    public static Object invokeMethod(final Object bean, final String methodName, final Object... args) {
        Object value = null;

        if (bean != null && methodName != null) {
            try {
                Method method = null;

                if (args != null && args.length > 0) {
                    Class argClass[] = new Class[args.length];
                    for (int i = 0; i < args.length; i++) {
                        argClass[i] = args[i].getClass();
                    }
                    method = bean.getClass().getMethod(methodName, argClass);
                    method.setAccessible(true);
                    value = method.invoke(bean, args);
                } else {
                    method = bean.getClass().getMethod(methodName);
                    method.setAccessible(true);
                    value = method.invoke(bean);
                }
            } catch (Exception ex) {
                throw new ReflectionException(ex);
            }
        }

        return value;
    }

    /**
     * Invoke an accessor method that follow the JavaBean convention Example:
     * <code>BeanUtil.invokeGetMethod(person,"name");</code> invoke:
     * <code>person.getName();</code>
     * <p>
     * You also can navigate
     *
     * <code>BeanUtil.invokeGetMetho(person,"country.name");</code> invoke:
     * <code>person.getCountry().getName();</code>
     *
     * @param bean         the bean
     * @param propertyName the property name
     * @return the object
     */
    public static Object invokeGetMethod(final Object bean, final String propertyName) {
        Object result = null;

        if (bean instanceof BeanMap) {
            result = ((BeanMap) bean).get(propertyName);
        } else if (propertyName.contains(".")) {
            final int dotIndex = propertyName.indexOf('.');
            final String subProperty = propertyName.substring(0, dotIndex);
            final Object subBean = invokeMethod(bean, formatGetMethod(subProperty));
            final String rest = propertyName.substring(dotIndex + 1, propertyName.length());
            result = invokeGetMethod(subBean, rest);
        } else {
            result = invokeMethod(bean, formatGetMethod(propertyName));
        }
        return result;
    }

    /**
     * Invoke an accessor method that follow the JavaBean convention for primitive
     * boolean types. Example:
     * <code>BeanUtil.invokeBooleanGetMethod(person,"active");</code> invoke:
     * <code>person.isActive();</code>
     * <p>
     * You also can navigate
     *
     * <code>BeanUtil.invokeBooleanGetMethod(person,"country.active");</code>
     * invoke: <code>person.getCountry().isActive();</code>
     *
     * @param bean         the bean
     * @param propertyName the property name
     * @return the object
     */
    public static Object invokeBooleanGetMethod(final Object bean, final String propertyName) {
        Object result = null;
        if (propertyName.contains(".")) {
            final int dotIndex = propertyName.indexOf('.');
            final String subProperty = propertyName.substring(0, dotIndex);
            final Object subBean = invokeMethod(bean, formatGetMethod(subProperty));
            final String rest = propertyName.substring(dotIndex + 1, propertyName.length());
            result = invokeGetMethod(subBean, rest);
        } else {
            result = invokeMethod(bean, formatBooleanGetMethod(propertyName));
        }

        return result;
    }

    /**
     * Invoker a getXXX or isXXX using PropertyInfo.
     *
     * @param bean     the bean
     * @param property the property
     * @return the object
     */
    public static Object invokeGetMethod(final Object bean, final PropertyInfo property) {
        if (bean instanceof BeanMap) {
            return ((BeanMap) bean).get(property.getName());
        } else if (property.is(boolean.class)) {
            return invokeBooleanGetMethod(bean, property.getName());
        } else {
            return invokeGetMethod(bean, property.getName());
        }
    }

    /**
     * Invoke the setXXX method from bean Object, where XXX = name <br/>
     * <p>
     * You can also navigate to the set method. Example<br/>
     * <br/>
     * <code>BeanUtils.invokeSetMethod(bean,"subBean.otherBean.name","MyBean")</code>
     * <br/>
     * <br/>
     * is translate like this:<br/>
     * <br/>
     * <code>bean.getSubBean().getOtherBean().setName("MyBean");</code><br/>
     *
     * @param bean  the bean
     * @param name  the name
     * @param value the value
     */
    @SuppressWarnings("unchecked")
    public static void invokeSetMethod(final Object bean, final String name, final Object value) {
        if (bean instanceof BeanMap) {
            ((BeanMap) bean).set(name, value);
            return;
        }


        String realName = name;
        Object realBean = bean;

        if (name.contains(".")) {
            final String path = name.substring(0, name.lastIndexOf('.'));
            realBean = invokeGetMethod(bean, path);
            realName = name.substring(name.lastIndexOf('.') + 1);
        }

        final String methodName = formatSetMethod(realName);
        final Class beanClass = realBean.getClass();
        Class valueClass = null;
        Object realValue = null;
        Method method = null;

        if (value instanceof ValueWrapper) {
            final ValueWrapper valueWrapper = (ValueWrapper) value;
            valueClass = valueWrapper.getValueClass();
            realValue = valueWrapper.getValue();
        } else {
            valueClass = value.getClass();
            realValue = value;
        }

        try {
            method = beanClass.getMethod(methodName, valueClass);
        } catch (NoSuchMethodException e) {
            if (isPrimitiveWrapper(valueClass)) {
                valueClass = getWrappedPrimitiveType(valueClass);
                try {
                    method = beanClass.getMethod(methodName, valueClass);
                } catch (Exception e1) {
                    throw new ReflectionException(e1);
                }
            } else {
                try {
                    method = beanClass.getMethod(methodName, valueClass.getSuperclass());
                } catch (Exception e1) {
                    throw new ReflectionException(e1);
                }
            }
        }

        if (method != null) {
            method.setAccessible(true);
            try {
                method.invoke(realBean, realValue);
            } catch (Exception e) {
                throw new ReflectionException(e);
            }
        }
    }

    /**
     * Invoker setXXX using property.
     *
     * @param bean     the bean
     * @param property the property
     * @param value    the value
     */
    public static void invokeSetMethod(final Object bean, final PropertyInfo property, final Object value) {
        invokeSetMethod(bean, property.getName(), value);
    }

    /**
     * Format the property to a getter method name. "age" return "getAge"
     *
     * @param propertyName the property name
     * @return the string
     */
    public static String formatGetMethod(final String propertyName) {
        String getMethod = "get" + StringUtils.capitalize(propertyName);
        return getMethod;
    }

    /**
     * Format the property to boolea getter method name . "visible" return
     * "isVisible"
     *
     * @param propertyName the property name
     * @return the string
     */
    public static String formatBooleanGetMethod(final String propertyName) {
        String getMethod = "is" + StringUtils.capitalize(propertyName);
        return getMethod;
    }

    /**
     * Format the property to a setter method name. "age" return "setAge"
     *
     * @param propertyName the property name
     * @return the string
     */
    public static String formatSetMethod(final String propertyName) {
        String setMethod = "set" + StringUtils.capitalize(propertyName);
        return setMethod;
    }

    /**
     * Get the class of the generic type defined in a class, only work for class
     * level generic type not at variable level, i mean:
     * <p>
     * class MyClass extends SomethingGeneric<String>{} //Works, return String.class
     * <p>
     * private SomethingGeneric<String> myVar; //Don't Work, return null
     * <p>
     * Tested only in Sun/Oracle JDK
     *
     * @param classRef the class ref
     * @return Class or null if fail
     */
    public static Class getGenericTypeClass(final Object classRef) {
        Class clazz = null;

        if (classRef != null) {
            Type type = classRef.getClass().getGenericSuperclass();
            if (type != null && type instanceof ParameterizedType) {
                clazz = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
            }
        }

        return clazz;
    }

    /**
     * @param classRef
     * @param interfaceClass
     * @return
     */
    public static Class getGenericTypeInterface(Object classRef, Class interfaceClass) {
        Class clazz = null;

        if (classRef != null) {
            Type[] types = classRef.getClass().getGenericInterfaces();
            for (Type type : types) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    if (parameterizedType.getRawType().getTypeName().equals(interfaceClass.getName())) {
                        clazz = (Class) parameterizedType.getActualTypeArguments()[0];
                        break;
                    }
                }
            }
        }

        return clazz;
    }

    /**
     * Return the generic type of the field ex: List<Integer> intList; return
     * Integer.class
     *
     * @param field the field
     * @return the field generic type
     */
    public static Class<?> getFieldGenericType(final Field field) {
        Class<?> genericClass = null;
        final Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            final ParameterizedType ptype = (ParameterizedType) genericType;
            genericClass = (Class<?>) ptype.getActualTypeArguments()[0];
        }
        return genericClass;
    }

    /**
     * Return the generic type of a getter Method, for example: a methdo with
     * following signature: List<String> getNames() will return String.class
     *
     * @param getterMethod the getter method
     * @return the method generic type
     */
    public static Class<?> getMethodGenericType(final Method getterMethod) {
        Class<?> genericClass = null;

        if (getterMethod != null) {
            final Type genericType = getterMethod.getGenericReturnType();
            if (genericType instanceof ParameterizedType) {
                final ParameterizedType ptype = (ParameterizedType) genericType;
                genericClass = (Class<?>) ptype.getActualTypeArguments()[0];
            }
        }
        return genericClass;
    }

    /**
     * Return the generic type of field ex: List<Integer> intList; return
     * Integer.class
     *
     * @param clazz     the clazz
     * @param fieldName the field name
     * @return the field generic type
     * @throws NoSuchFieldException the no such field exception
     */
    public static Class<?> getFieldGenericType(final Class clazz, final String fieldName) throws NoSuchFieldException {
        Field field = getField(clazz, fieldName);
        return getFieldGenericType(field);
    }

    /**
     * Gets the property info.
     *
     * @param clazz        the clazz
     * @param propertyName the property name
     * @return the property info
     */
    @SuppressWarnings("unchecked")
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
     * Gets the property info.
     *
     * @param prop the prop
     * @return the property info
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
            if (BeanUtils.isAssignable(type, Collection.class)) {
                info.setCollection(true);
            }
        }

        return info;

    }

    /**
     * Gets the properties info.
     *
     * @param clazz the clazz
     * @return the properties info
     */
    public static List<PropertyInfo> getPropertiesInfo(final Class clazz) {
        List<PropertyInfo> infos = new ArrayList<>();

        try {
            BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(clazz);
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {

                final PropertyInfo pi = getPropertyInfo(descriptor);
                if (pi != null) {
                    infos.add(pi);
                }
            }
        } catch (IntrospectionException e1) {
            throw new ReflectionException(e1);
        }
        return infos;
    }

    /**
     * Get the field object from the specified clazz, including all field declared
     * in the Class clazz or super classes. Field name support navegation, its means
     * you can get a field from subfieled, ex: getField(employee,"company.name");
     *
     * @param clazz     the clazz
     * @param fieldName the field name
     * @return the field
     * @throws NoSuchFieldException the no such field exception
     */
    public static Field getField(final Class clazz, final String fieldName) throws NoSuchFieldException {
        Field field = null;
        try {

            if (fieldName.contains(".")) {
                final String childField = fieldName.substring(fieldName.indexOf('.') + 1);
                final String parentField = fieldName.substring(0, fieldName.indexOf('.'));
                final Class parentFieldClazz = clazz.getDeclaredField(parentField).getType();
                field = getField(parentFieldClazz, childField);
            } else {
                field = clazz.getDeclaredField(fieldName);
            }
        } catch (NoSuchFieldException noSuchFieldException) {
            Class superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw noSuchFieldException;
            }
            field = getField(superClass, fieldName);
        }

        return field;
    }

    /**
     * Gets the all fields.
     *
     * @param clazz the clazz
     * @return the all fields
     */
    public static List<Field> getAllFields(final Class clazz) {
        List<Field> allFields = new ArrayList<>();
        allFields.addAll(Arrays.asList(clazz.getDeclaredFields()));

        if (clazz.getSuperclass() != null) {
            allFields.addAll(0, getAllFields(clazz.getSuperclass()));
        }

        return allFields;

    }

    /**
     * Setup bean.
     *
     * @param bean   the bean
     * @param values the values
     */
    public static void setupBean(final Object bean, final Map<String, Object> values) {
        if (bean != null && values != null) {
            for (String key : values.keySet()) {
                try {
                    invokeSetMethod(bean, key, values.get(key));
                } catch (Exception e) {
                    LOGGER.debug("WARN: Setting up bean " + e.getMessage());
                }
            }
        }
    }

    /**
     * Determine if an object from Class "a" can be assigned to variables or members
     * of Class "b".
     *
     * @param a the a
     * @param b the b
     * @return true, if is assignable
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
     * Check if clazz is a primitive wrapper, ex: java.lang.Boolean,
     * java.lang.Integer, etc,etc.
     *
     * @param clazz the clazz
     * @return true, if is primitive wrapper
     */
    public static boolean isPrimitiveWrapper(final Class<?> clazz) {
        return WRAPPERS.containsValue(clazz);
    }

    /**
     * Return the primitive wrapper type for the primitive type. boolean.class
     * return Boolean.class int.class return Integer.class etc, etc Return null if
     * primitiveClass is not a primitive type
     *
     * @param primitiveClass the primitive class
     * @return the primitive wrapper type
     */
    public static Class<?> getPrimitiveWrapperType(final Class<?> primitiveClass) {
        return WRAPPERS.get(primitiveClass);
    }

    /**
     * Return the primitive type for the primitive wrapper type Integer.class return
     * int.class Boolean.class return boolean.class, etc,etc,etc Return null if
     * wrapperClass is not a primitie wrapper type
     *
     * @param wrapperClass the wrapper class
     * @return the wrapped primitive type
     */
    public static Class<?> getWrappedPrimitiveType(final Class<?> wrapperClass) {
        Class<?> type = null;
        if (WRAPPERS.containsValue(wrapperClass)) {
            for (Map.Entry<Class, Class> entry : WRAPPERS.entrySet()) {
                if (entry.getValue().equals(wrapperClass)) {
                    type = entry.getKey();
                }
            }
        }
        return type;
    }


    /**
     * Checks if is annotated.
     *
     * @param annotationClass the annotation class
     * @param targetClass     the target class
     * @return true, if is annotated
     */
    public static boolean isAnnotated(Class annotationClass, Class targetClass) {
        Annotation annotation = targetClass.getAnnotation(annotationClass);
        return annotation != null;
    }

    /**
     * Gets the methods with annotation.
     *
     * @param targetClass     the target class
     * @param annotationClass the annotation class
     * @return the methods with annotation
     */
    public static Method[] getMethodsWithAnnotation(Class<?> targetClass, Class<? extends Annotation> annotationClass) {
        List<Method> annotatedMethods = new ArrayList<>();
        for (Method method : targetClass.getMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods.toArray(new Method[0]);
    }

    /**
     * Gets the fields with annotation.
     *
     * @param targetClass     the target class
     * @param annotationClass the annotation class
     * @return the Fields with annotation
     */
    public static Field[] getFieldsWithAnnotation(Class<?> targetClass, Class<? extends Annotation> annotationClass) {
        List<Field> annotatedFields = new ArrayList<>();
        for (Field field : targetClass.getFields()) {
            if (field.isAnnotationPresent(annotationClass)) {
                annotatedFields.add(field);
            }
        }
        return annotatedFields.toArray(new Field[0]);
    }

    /**
     * Load all bean standard properties into map
     *
     * @param bean
     * @return
     */
    public static Map<String, Object> getValuesMaps(Object bean) {
        return getValuesMaps("", bean, null);
    }

    /**
     * Load all beans standard properties into a map.
     *
     * @param prefix the prefix
     * @param bean   the bean
     * @return the values maps
     */
    public static Map<String, Object> getValuesMaps(String prefix, Object bean) {
        return getValuesMaps(prefix, bean, null);
    }

    /**
     * Load all beans standard ot enum properties into a map. If property value is null use
     * defaultValue. @see {@link PropertyInfo}.isStandardClass()
     *
     * @param prefix       the prefix
     * @param bean         the bean
     * @param defaultValue the default value
     * @return the values maps
     */
    public static Map<String, Object> getValuesMaps(String prefix, Object bean, Object defaultValue) {
        Map<String, Object> values = new HashMap<>();
        if (bean != null) {
            if (bean instanceof BeanMap) {
                values.putAll((Map) bean);
            } else {
                getPropertiesInfo(bean.getClass()).stream()
                        .filter(p -> (p.isStandardClass() || p.isEnum()) && !p.isArray() && !p.isCollection()).forEach(p -> {
                    try {
                        Object value = BeanUtils.invokeGetMethod(bean, p);
                        if (value == null) {
                            value = defaultValue;
                        }
                        values.put(prefix + p.getName(), value);
                    } catch (Exception e) {
                        // ignore
                    }

                });
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cannot get values map from bean because is null, returning empty map");
            }
        }

        return values;
    }

    /**
     * Create a simple clone from source object, its only include standard java
     * types and primitive. Collections and arrays are excluded. Optional you can
     * specify additional excluded properties
     *
     * @param source
     * @param excludedProperties
     * @return
     */
    public static <T> T clone(T source, String... excludedProperties) {
        Class<T> sourceClass = (Class<T>) source.getClass();
        T clon = newInstance(sourceClass);

        Map<String, Object> values = getValuesMaps("", source);
        if (excludedProperties != null) {
            for (String property : excludedProperties) {
                values.remove(property);
            }
        }

        setupBean(clon, values);

        return clon;
    }

    /**
     * Setup bean properties using another bean properties. Source object can be of
     * any type, this method extract source object properties values and names and
     * create a Map to setup bean.
     *
     * @param bean
     * @param source
     */
    public static void setupBean(Object bean, Object source) {
        Map<String, Object> values = getValuesMaps("", source);
        setupBean(bean, values);
    }

    /**
     * Create a new BeanMap for current bean
     *
     * @param bean
     * @return
     */
    public static BeanMap newBeanMap(Object bean) {
        BeanMap beanMap = new BeanMap();
        beanMap.load(bean);
        return beanMap;
    }

    /**
     * Find the name of property parent
     *
     * @param parentClass
     * @param subentityClass
     * @return
     */
    public static String findParentPropertyName(Class<?> parentClass, Class subentityClass) {
        List<PropertyInfo> infos = BeanUtils.getPropertiesInfo(subentityClass);
        for (PropertyInfo propertyInfo : infos) {
            if (isAssignable(propertyInfo.getType(), parentClass)) {
                return propertyInfo.getName();
            }
        }
        return StringUtils.uncapitalize(parentClass.getSimpleName());
    }

}
