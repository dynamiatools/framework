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

import org.junit.Test;
import tools.dynamia.commons.reflect.AccessMode;
import tools.dynamia.commons.reflect.PropertyInfo;

import javax.swing.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ObjectOperationsTest {


    /**
     * Test of invokeMethod method, of class BeanUtils.
     */
    @Test
    public void testInvokeMethod() {
        Object bean = "Bean";
        String methodName = "length";

        Object[] args = null;
        Object expResult = 4;
        Object result = ObjectOperations.invokeMethod(bean, methodName, args);

        assertEquals(expResult, result);
    }

    /**
     * Test of formatGetMethod method, of class BeanUtils.
     */
    @Test
    public void testFormatGetMethod() {
        String propertyName = "text";
        String expResult = "getText";
        String result = ObjectOperations.formatGetMethod(propertyName);
        assertEquals(expResult, result);
    }

    /**
     * Test of formatSetMethod method, of class BeanUtils.
     */
    @Test
    public void testFormatSetMethod() {
        String propertyName = "text";
        String expResult = "setText";
        String result = ObjectOperations.formatSetMethod(propertyName);
        assertEquals(expResult, result);
    }

    /**
     * Test of invokeGetMethod method, of class BeanUtils.
     */
    @Test
    public void testInvokeGetMethod() {
        Object bean = new JLabel("texto");
        String propertyName = "text";
        Object expResult = "texto";
        Object result = ObjectOperations.invokeGetMethod(bean, propertyName);
        assertEquals(expResult, result);
    }

    /**
     * Test of getGenericTypeClass method, of class BeanUtils.
     */
    @Test
    public void testGetGenericTypeClass() {
        Class expResult = String.class;
        DummyClass testGeneriClass = new DummyClass();
        Class result = testGeneriClass.getGenericType();
        assertEquals(expResult, result);
    }
    @Test
    public void testGetFieldGenericType() throws NoSuchFieldException {
        Class expResult = String.class;
        Class result = ObjectOperations.getFieldGenericType(SomeBean.class, "names");
        assertEquals(expResult, result);
    }
    @Test
    public void testTemplateParse() {
        Map<String, Object> vars = new HashMap();
        vars.put("var", "parser");
        vars.put("var2", "xD");
        String parsed = SimpleTemplateEngine.parse("probando el ${var} muahahah ${var2}", vars);
        assertEquals("probando el parser muahahah xD", parsed);

    }
    @Test
    public void testGetField() throws NoSuchFieldException {
        Field field = ObjectOperations.getField(DummyClass.class, "name");
        assertNotNull(field);
    }
    @Test
    public void testGetField_Subfield() throws NoSuchFieldException {
        Field field = ObjectOperations.getField(SomeBean.class, "dummy.name");
        assertNotNull(field);
        assertEquals("name", field.getName());
        assertEquals(DummyClass.class, field.getDeclaringClass());
        assertEquals(String.class, field.getType());
    }
    @Test
    public void testGetPropertyInfo() {
        PropertyInfo expected = new PropertyInfo("name", String.class, DummyClass.class, AccessMode.READ_WRITE);

        PropertyInfo result = ObjectOperations.getPropertyInfo(DummyClass.class, "name");
        assertEquals(expected.getAccessMode(), result.getAccessMode());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getOwnerClass(), result.getOwnerClass());
        assertEquals(expected.getType(), result.getType());
    }
    @Test
    public void testGetPropertyInfo_SubProperty() {
        PropertyInfo pinfo = ObjectOperations.getPropertyInfo(SomeBean.class, "dummy.name");
        assertNotNull(pinfo);
        assertEquals("name", pinfo.getName());
        assertEquals(DummyClass.class, pinfo.getOwnerClass());
        assertEquals(String.class, pinfo.getType());
    }
    @Test
    public void testGetPropertiesInfo() {
        List<PropertyInfo> infos = ObjectOperations.getPropertiesInfo(ChildClass.class);

        for (PropertyInfo propertyInfo : infos) {
        }
        assertEquals(8, infos.size());
    }
    @Test
    public void testSetMethod() {
        ChildClass child = new ChildClass();

        ObjectOperations.invokeSetMethod(child, "visible", true);
        assertTrue(child.isVisible());

        SomeBean bean = new SomeBean();
        ObjectOperations.invokeSetMethod(bean, "size", 2);

        assertEquals(Integer.valueOf(2), bean.getSize());
    }

    @Test
    public void testSetMethodWithPath() {
        SomeBean bean = new SomeBean();
        ObjectOperations.invokeSetMethod(bean, "dummy.name", "TheDummy");
        assertEquals("TheDummy", bean.getDummy().getName());
    }

    @Test
    public void testIsAssignable() {
        assertTrue(ObjectOperations.isAssignable(Integer.class, Object.class));
    }

    /**
     * Test getFieldValue method
     */
    @Test
    public void testGetFieldValue() {
        DummyClass dummy = new DummyClass();
        dummy.setName("TestName");

        Object result = ObjectOperations.getFieldValue("name", dummy);
        assertEquals("TestName", result);
    }

    /**
     * Test setFieldValue method
     */
    @Test
    public void testSetFieldValue() {
        DummyClass dummy = new DummyClass();
        ObjectOperations.setFieldValue("name", dummy, "NewName");

        assertEquals("NewName", dummy.getName());
    }

    /**
     * Test setFieldValue with PropertyInfo
     */
    @Test
    public void testSetFieldValueWithPropertyInfo() {
        DummyClass dummy = new DummyClass();
        PropertyInfo propertyInfo = new PropertyInfo("name", String.class, DummyClass.class, AccessMode.READ_WRITE);

        ObjectOperations.setFieldValue(propertyInfo, dummy, "PropertyName");
        assertEquals("PropertyName", dummy.getName());
    }

    /**
     * Test newInstance with default constructor
     */
    @Test
    public void testNewInstance() {
        DummyClass instance = ObjectOperations.newInstance(DummyClass.class);
        assertNotNull(instance);
    }

    /**
     * Test newInstance with class name
     */
    @Test
    public void testNewInstanceWithClassName() {
        Object instance = ObjectOperations.newInstance("java.lang.String");
        assertNotNull(instance);
        assertTrue(instance instanceof String);
    }

    /**
     * Test newInstance with arguments
     */
    @Test
    public void testNewInstanceWithArgs() {
        String instance = ObjectOperations.newInstance(String.class, "test");
        assertNotNull(instance);
        assertEquals("test", instance);
    }

    /**
     * Test invokeBooleanGetMethod
     */
    @Test
    public void testInvokeBooleanGetMethod() {
        ChildClass child = new ChildClass();
        child.setVisible(true);

        Object result = ObjectOperations.invokeBooleanGetMethod(child, "visible");
        assertEquals(true, result);
    }

    /**
     * Test invokeBooleanGetMethod with path
     */
    @Test
    public void testInvokeBooleanGetMethodWithPath() {
        SomeBean bean = new SomeBean();
        bean.setVisible(true);

        Object result = ObjectOperations.invokeBooleanGetMethod(bean, "visible");
        assertEquals(true, result);
    }

    /**
     * Test formatBooleanGetMethod
     */
    @Test
    public void testFormatBooleanGetMethod() {
        String result = ObjectOperations.formatBooleanGetMethod("visible");
        assertEquals("isVisible", result);
    }

    /**
     * Test invokeGetMethod with PropertyInfo
     */
    @Test
    public void testInvokeGetMethodWithPropertyInfo() {
        DummyClass dummy = new DummyClass();
        dummy.setName("Test");
        PropertyInfo propertyInfo = ObjectOperations.getPropertyInfo(DummyClass.class, "name");

        Object result = ObjectOperations.invokeGetMethod(dummy, propertyInfo);
        assertEquals("Test", result);
    }

    /**
     * Test invokeSetMethod with PropertyInfo
     */
    @Test
    public void testInvokeSetMethodWithPropertyInfo() {
        DummyClass dummy = new DummyClass();
        PropertyInfo propertyInfo = ObjectOperations.getPropertyInfo(DummyClass.class, "name");

        ObjectOperations.invokeSetMethod(dummy, propertyInfo, "NewValue");
        assertEquals("NewValue", dummy.getName());
    }

    /**
     * Test getAllFields
     */
    @Test
    public void testGetAllFields() {
        List<Field> fields = ObjectOperations.getAllFields(ChildClass.class);
        assertNotNull(fields);
        assertTrue(fields.size() > 2); // Should include parent class fields
    }

    /**
     * Test setupBean with Map
     */
    @Test
    public void testSetupBeanWithMap() {
        DummyClass dummy = new DummyClass();
        Map<String, Object> values = new HashMap<>();
        values.put("name", "BeanName");
        values.put("date", new Date());

        ObjectOperations.setupBean(dummy, values);
        assertEquals("BeanName", dummy.getName());
        assertNotNull(dummy.getDate());
    }

    /**
     * Test setupBean with another bean
     */
    @Test
    public void testSetupBeanWithBean() {
        DummyClass source = new DummyClass();
        source.setName("SourceName");
        Date date = new Date();
        source.setDate(date);

        DummyClass target = new DummyClass();
        ObjectOperations.setupBean(target, source);

        assertEquals("SourceName", target.getName());
        assertEquals(date, target.getDate());
    }

    /**
     * Test isPrimitiveWrapper
     */
    @Test
    public void testIsPrimitiveWrapper() {
        assertTrue(ObjectOperations.isPrimitiveWrapper(Integer.class));
        assertTrue(ObjectOperations.isPrimitiveWrapper(Boolean.class));
        assertTrue(ObjectOperations.isPrimitiveWrapper(Double.class));
        assertFalse(ObjectOperations.isPrimitiveWrapper(String.class));
    }

    /**
     * Test getPrimitiveWrapperType
     */
    @Test
    public void testGetPrimitiveWrapperType() {
        assertEquals(Integer.class, ObjectOperations.getPrimitiveWrapperType(int.class));
        assertEquals(Boolean.class, ObjectOperations.getPrimitiveWrapperType(boolean.class));
        assertEquals(Double.class, ObjectOperations.getPrimitiveWrapperType(double.class));
        assertNull(ObjectOperations.getPrimitiveWrapperType(String.class));
    }

    /**
     * Test getWrappedPrimitiveType
     */
    @Test
    public void testGetWrappedPrimitiveType() {
        assertEquals(int.class, ObjectOperations.getWrappedPrimitiveType(Integer.class));
        assertEquals(boolean.class, ObjectOperations.getWrappedPrimitiveType(Boolean.class));
        assertEquals(double.class, ObjectOperations.getWrappedPrimitiveType(Double.class));
        assertNull(ObjectOperations.getWrappedPrimitiveType(String.class));
    }

    /**
     * Test isAnnotated
     */
    @Test
    public void testIsAnnotated() {
        assertTrue(ObjectOperations.isAnnotated(Deprecated.class, DummyClass.class));
        assertFalse(ObjectOperations.isAnnotated(Override.class, DummyClass.class));
    }

    /**
     * Test getMethodsWithAnnotation
     */
    @Test
    public void testGetMethodsWithAnnotation() {
        Method[] methods = ObjectOperations.getMethodsWithAnnotation(AnnotatedBean.class, TestAnnotation.class);
        assertNotNull(methods);
        assertEquals(1, methods.length);
        assertEquals("annotatedMethod", methods[0].getName());
    }

    /**
     * Test getFieldsWithAnnotation
     */
    @Test
    public void testGetFieldsWithAnnotation() {
        Field[] fields = ObjectOperations.getFieldsWithAnnotation(DummyClass.class, Deprecated.class);
        assertNotNull(fields);
        assertEquals(1, fields.length);
        assertEquals("name", fields[0].getName());
    }

    /**
     * Test getFirstFieldWithAnnotation
     */
    @Test
    public void testGetFirstFieldWithAnnotation() {
        Field field = ObjectOperations.getFirstFieldWithAnnotation(DummyClass.class, Deprecated.class);
        assertNotNull(field);
        assertEquals("name", field.getName());
    }

    /**
     * Test getValuesMaps
     */
    @Test
    public void testGetValuesMaps() {
        DummyClass dummy = new DummyClass();
        dummy.setName("TestName");
        dummy.setDate(new Date());

        Map<String, Object> values = ObjectOperations.getValuesMaps(dummy);
        assertNotNull(values);
        assertEquals("TestName", values.get("name"));
        assertNotNull(values.get("date"));
    }

    /**
     * Test getValuesMaps with prefix
     */
    @Test
    public void testGetValuesMapsWithPrefix() {
        DummyClass dummy = new DummyClass();
        dummy.setName("TestName");

        Map<String, Object> values = ObjectOperations.getValuesMaps("dummy.", dummy);
        assertNotNull(values);
        assertEquals("TestName", values.get("dummy.name"));
    }

    /**
     * Test getValuesMaps with default value
     */
    @Test
    public void testGetValuesMapsWithDefaultValue() {
        DummyClass dummy = new DummyClass();

        Map<String, Object> values = ObjectOperations.getValuesMaps("", dummy, "default");
        assertNotNull(values);
        assertEquals("default", values.get("name"));
    }

    /**
     * Test clone method
     */
    @Test
    public void testClone() {
        DummyClass original = new DummyClass();
        original.setName("Original");
        original.setDate(new Date());

        DummyClass cloned = ObjectOperations.clone(original);
        assertNotNull(cloned);
        assertEquals(original.getName(), cloned.getName());
        assertEquals(original.getDate(), cloned.getDate());
    }

    /**
     * Test clone with excluded properties
     */
    @Test
    public void testCloneWithExcludedProperties() {
        DummyClass original = new DummyClass();
        original.setName("Original");
        original.setDate(new Date());

        DummyClass cloned = ObjectOperations.clone(original, "date");
        assertNotNull(cloned);
        assertEquals(original.getName(), cloned.getName());
        assertNull(cloned.getDate());
    }

    /**
     * Test newBeanMap
     */
    @Test
    public void testNewBeanMap() {
        DummyClass dummy = new DummyClass();
        dummy.setName("TestName");

        BeanMap beanMap = ObjectOperations.newBeanMap(dummy);
        assertNotNull(beanMap);
        assertEquals("TestName", beanMap.get("name"));
    }

    /**
     * Test findParentPropertyName
     */
    @Test
    public void testFindParentPropertyName() {
        String propertyName = ObjectOperations.findParentPropertyName(DummyClass.class, SomeBean.class);
        assertEquals("dummy", propertyName);
    }

    /**
     * Test getInstanceName with annotated field
     */
    @Test
    public void testGetInstanceName() {
        NamedBean bean = new NamedBean();
        bean.id = "ID-123";
        bean.name = "BeanName";

        String instanceName = ObjectOperations.getInstanceName(bean);
        assertEquals("BeanName", instanceName);
    }

    /**
     * Test getInstanceName with toString fallback
     */
    @Test
    public void testGetInstanceNameWithToString() {
        DummyClass dummy = new DummyClass();
        String instanceName = ObjectOperations.getInstanceName(dummy);
        assertNotNull(instanceName);
    }

    /**
     * Test getInstanceName with null
     */
    @Test
    public void testGetInstanceNameWithNull() {
        String instanceName = ObjectOperations.getInstanceName(null);
        assertEquals("", instanceName);
    }

    /**
     * Test isStantardClass
     */
    @Test
    public void testIsStandardClass() {
        assertTrue(ObjectOperations.isStantardClass(String.class));
        assertTrue(ObjectOperations.isStantardClass(Integer.class));
        assertTrue(ObjectOperations.isStantardClass(Date.class));
        assertTrue(ObjectOperations.isStantardClass(int.class));
        assertFalse(ObjectOperations.isStantardClass(DummyClass.class));
        assertFalse(ObjectOperations.isStantardClass(null));
    }

    /**
     * Test isValidClassName
     */
    @Test
    public void testIsValidClassName() {
        assertTrue(ObjectOperations.isValidClassName("java.lang.String"));
        assertTrue(ObjectOperations.isValidClassName("com.example.MyClass"));
        assertFalse(ObjectOperations.isValidClassName(""));
        assertFalse(ObjectOperations.isValidClassName(null));
        assertFalse(ObjectOperations.isValidClassName("invalid-class-name"));
    }

    /**
     * Test findClass
     */
    @Test
    public void testFindClass() {
        Class<?> clazz = ObjectOperations.findClass("java.lang.String");
        assertNotNull(clazz);
        assertEquals(String.class, clazz);

        Class<?> notFound = ObjectOperations.findClass("com.nonexistent.Class");
        assertNull(notFound);

        Class<?> invalid = ObjectOperations.findClass("invalid-name");
        assertNull(invalid);
    }

    /**
     * Test getMethodGenericType
     */
    @Test
    public void testGetMethodGenericType() throws NoSuchMethodException {
        Method method = GenericMethodBean.class.getMethod("getItems");
        Class<?> genericType = ObjectOperations.getMethodGenericType(method);
        assertEquals(String.class, genericType);
    }

    /**
     * Test getGenericTypeInterface
     */
    @Test
    public void testGetGenericTypeInterface() {
        GenericInterfaceImpl impl = new GenericInterfaceImpl();
        Class<?> genericType = ObjectOperations.getGenericTypeInterface(impl, GenericInterface.class);
        assertEquals(Integer.class, genericType);
    }

    // Test annotation for testing
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @interface TestAnnotation {
    }

    // Test classes for annotation testing
    static class AnnotatedBean {
        @TestAnnotation
        public void annotatedMethod() {
        }

        public void normalMethod() {
        }
    }

    // Test class for InstanceName
    static class NamedBean {
        private String id;

        @InstanceName
        private String name;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    // Test class for generic method testing
    static class GenericMethodBean {
        public List<String> getItems() {
            return new LinkedList<>();
        }
    }

    // Test interface for generic interface testing
    interface GenericInterface<T> {
        T getValue();
    }

    static class GenericInterfaceImpl implements GenericInterface<Integer> {
        @Override
        public Integer getValue() {
            return 42;
        }
    }

    @Deprecated
    static class DummyClass extends LinkedList<String> {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        @Deprecated
        private String name;
        private Date date;

        public Class getGenericType() {
            return ObjectOperations.getGenericTypeClass(this);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    static class ChildClass extends DummyClass {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private int age;
        private boolean visible;

        public int getAge() {
            return age;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }
    }

    static class SomeBean {

        private Integer size;
        private boolean visible;
        private DummyClass dummy = new DummyClass();
        @SuppressWarnings("unused")
        private List<String> names;

        public DummyClass getDummy() {
            return dummy;
        }

        public void setDummy(DummyClass dummy) {
            this.dummy = dummy;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }
    }
}
