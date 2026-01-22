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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        assertTrue(ObjectOperations.isStandardClass(String.class));
        assertTrue(ObjectOperations.isStandardClass(Integer.class));
        assertTrue(ObjectOperations.isStandardClass(Date.class));
        assertTrue(ObjectOperations.isStandardClass(int.class));
        assertFalse(ObjectOperations.isStandardClass(DummyClass.class));
        assertFalse(ObjectOperations.isStandardClass(null));
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

    // ============================================================================
    // FUNCTIONAL METHODS TESTS
    // ============================================================================

    // Helper test classes for functional methods
    static class TestPerson {
        private String name;
        private String email;
        private Integer age;
        private String country;
        private boolean active;
        private TestAddress address;

        public TestPerson() {
        }

        public TestPerson(String name, String email, Integer age, String country, boolean active) {
            this.name = name;
            this.email = email;
            this.age = age;
            this.country = country;
            this.active = active;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public TestAddress getAddress() { return address; }
        public void setAddress(TestAddress address) { this.address = address; }
    }

    static class TestAddress {
        private String city;
        private String country;

        public TestAddress(String city, String country) {
            this.city = city;
            this.country = country;
        }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }

    static class TestPersonDTO {
        private String name;
        private String email;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    private List<TestPerson> createTestPersons() {
        List<TestPerson> persons = new ArrayList<>();
        persons.add(new TestPerson("John", "john@mail.com", 30, "USA", true));
        persons.add(new TestPerson("Jane", "jane@mail.com", 25, "Canada", true));
        persons.add(new TestPerson("Bob", "bob@mail.com", 35, "USA", false));
        persons.add(new TestPerson("Alice", "alice@mail.com", 28, "Canada", true));
        return persons;
    }

    /**
     * Test mapProperty - extracts properties from collection
     */
    @Test
    public void testMapProperty() {
        List<TestPerson> persons = createTestPersons();
        List<String> names = ObjectOperations.mapProperty(persons, "name");

        assertEquals(4, names.size());
        assertEquals("John", names.get(0));
        assertEquals("Jane", names.get(1));
        assertEquals("Bob", names.get(2));
        assertEquals("Alice", names.get(3));
    }

    /**
     * Test mapProperty with typed result
     */
    @Test
    public void testMapPropertyTyped() {
        List<TestPerson> persons = createTestPersons();
        List<Integer> ages = ObjectOperations.mapProperty(persons, "age");

        assertEquals(4, ages.size());
        assertEquals(Integer.valueOf(30), ages.get(0));
        assertEquals(Integer.valueOf(25), ages.get(1));
    }

    /**
     * Test mapProperty with null collection
     */
    @Test
    public void testMapPropertyWithNullCollection() {
        List<String> result = ObjectOperations.mapProperty(null, "name");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test mapToMap - extracts selected properties
     */
    @Test
    public void testMapToMap() {
        TestPerson person = createTestPersons().get(0);
        Map<String, Object> map = ObjectOperations.mapToMap(person, "name", "email", "age");

        assertEquals(3, map.size());
        assertEquals("John", map.get("name"));
        assertEquals("john@mail.com", map.get("email"));
        assertEquals(30, map.get("age"));
    }

    /**
     * Test mapToMap with null bean
     */
    @Test
    public void testMapToMapWithNull() {
        Map<String, Object> map = ObjectOperations.mapToMap(null, "name");
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    /**
     * Test transform - object to DTO
     */
    @Test
    public void testTransform() {
        TestPerson person = createTestPersons().get(0);
        TestPersonDTO dto = ObjectOperations.transform(person, TestPersonDTO.class);

        assertNotNull(dto);
        assertEquals(person.getName(), dto.getName());
        assertEquals(person.getEmail(), dto.getEmail());
    }

    /**
     * Test transform with null source
     */
    @Test
    public void testTransformWithNull() {
        TestPersonDTO dto = ObjectOperations.transform(null, TestPersonDTO.class);
        assertNull(dto);
    }

    /**
     * Test transformAll - collection transformation
     */
    @Test
    public void testTransformAll() {
        List<TestPerson> persons = createTestPersons();
        List<TestPersonDTO> dtos = ObjectOperations.transformAll(persons, TestPersonDTO.class);

        assertEquals(4, dtos.size());
        assertEquals("John", dtos.get(0).getName());
        assertEquals("Jane", dtos.get(1).getName());
    }

    /**
     * Test findByProperty - finds by property value
     */
    @Test
    public void testFindByProperty() {
        List<TestPerson> persons = createTestPersons();
        Optional<TestPerson> found = ObjectOperations.findByProperty(persons, "email", "bob@mail.com");

        assertTrue(found.isPresent());
        assertEquals("Bob", found.get().getName());
    }

    /**
     * Test findByProperty - not found
     */
    @Test
    public void testFindByPropertyNotFound() {
        List<TestPerson> persons = createTestPersons();
        Optional<TestPerson> found = ObjectOperations.findByProperty(persons, "email", "notfound@mail.com");

        assertFalse(found.isPresent());
    }

    /**
     * Test filterByProperty with predicate
     */
    @Test
    public void testFilterByPropertyWithPredicate() {
        List<TestPerson> persons = createTestPersons();
        Predicate<Object> ageFilter = age -> age != null && ((Integer) age) >= 30;
        List<TestPerson> filtered = ObjectOperations.filterByProperty(persons, "age", ageFilter);

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(p -> p.getAge() >= 30));
    }

    /**
     * Test filterByProperty with value
     */
    @Test
    public void testFilterByPropertyWithValue() {
        List<TestPerson> persons = createTestPersons();
        List<TestPerson> usaPersons = ObjectOperations.filterByProperty(persons, "country", "USA");

        assertEquals(2, usaPersons.size());
        assertTrue(usaPersons.stream().allMatch(p -> "USA".equals(p.getCountry())));
    }

    /**
     * Test hasProperty - property exists and not null
     */
    @Test
    public void testHasProperty() {
        TestPerson person = createTestPersons().get(0);

        assertTrue(ObjectOperations.hasProperty(person, "name"));
        assertTrue(ObjectOperations.hasProperty(person, "email"));
        assertFalse(ObjectOperations.hasProperty(person, "nonexistent"));
    }

    /**
     * Test isPropertyNull
     */
    @Test
    public void testIsPropertyNull() {
        TestPerson person = new TestPerson();

        assertTrue(ObjectOperations.isPropertyNull(person, "name"));
        assertTrue(ObjectOperations.isPropertyNull(person, "email"));

        person.setName("Test");
        assertFalse(ObjectOperations.isPropertyNull(person, "name"));
    }

    /**
     * Test matchesProperties - matching criteria
     */
    @Test
    public void testMatchesProperties() {
        TestPerson person = createTestPersons().get(0);

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("name", "John");
        criteria.put("country", "USA");

        assertTrue(ObjectOperations.matchesProperties(person, criteria));

        criteria.put("country", "Canada");
        assertFalse(ObjectOperations.matchesProperties(person, criteria));
    }

    /**
     * Test arePropertiesEqual
     */
    @Test
    public void testArePropertiesEqual() {
        TestPerson person1 = createTestPersons().get(0);
        TestPerson person2 = new TestPerson("John", "different@mail.com", 30, "USA", true);

        assertTrue(ObjectOperations.arePropertiesEqual(person1, person2, "name", "age", "country"));
        assertFalse(ObjectOperations.arePropertiesEqual(person1, person2, "name", "email"));
    }

    /**
     * Test groupBy - groups by property
     */
    @Test
    public void testGroupBy() {
        List<TestPerson> persons = createTestPersons();
        Map<Object, List<TestPerson>> byCountry = ObjectOperations.groupBy(persons, "country");

        assertEquals(2, byCountry.size());
        assertEquals(2, byCountry.get("USA").size());
        assertEquals(2, byCountry.get("Canada").size());
    }

    /**
     * Test countByProperty - counts grouped items
     */
    @Test
    public void testCountByProperty() {
        List<TestPerson> persons = createTestPersons();
        Map<Object, Long> countByCountry = ObjectOperations.countByProperty(persons, "country");

        assertEquals(2, countByCountry.size());
        assertEquals(Long.valueOf(2), countByCountry.get("USA"));
        assertEquals(Long.valueOf(2), countByCountry.get("Canada"));
    }

    /**
     * Test sumProperty - sums numeric properties
     */
    @Test
    public void testSumProperty() {
        List<TestPerson> persons = createTestPersons();
        Number totalAge = ObjectOperations.sumProperty(persons, "age");

        assertEquals(118.0, totalAge.doubleValue(), 0.001);
    }

    /**
     * Test compareByProperty
     */
    @Test
    public void testCompareByProperty() {
        TestPerson person1 = createTestPersons().get(0); // John, age 30
        TestPerson person2 = createTestPersons().get(1); // Jane, age 25

        int result = ObjectOperations.compareByProperty(person1, person2, "age");
        assertTrue(result > 0); // 30 > 25

        result = ObjectOperations.compareByProperty(person2, person1, "age");
        assertTrue(result < 0); // 25 < 30

        result = ObjectOperations.compareByProperty(person1, person1, "age");
        assertEquals(0, result); // Same age
    }

    /**
     * Test compareByProperty with nulls
     */
    @Test
    public void testCompareByPropertyWithNulls() {
        TestPerson person1 = createTestPersons().get(0);

        int result = ObjectOperations.compareByProperty(null, person1, "age");
        assertEquals(-1, result);

        result = ObjectOperations.compareByProperty(person1, null, "age");
        assertEquals(1, result);

        result = ObjectOperations.compareByProperty(null, null, "age");
        assertEquals(0, result);
    }

    /**
     * Test getComparator - creates comparator
     */
    @Test
    public void testGetComparator() {
        List<TestPerson> persons = new ArrayList<>(createTestPersons());
        Comparator<TestPerson> comparator = ObjectOperations.getComparator("age");
        persons.sort(comparator);

        assertEquals("Jane", persons.get(0).getName()); // age 25
        assertEquals("Alice", persons.get(1).getName()); // age 28
        assertEquals("John", persons.get(2).getName()); // age 30
        assertEquals("Bob", persons.get(3).getName()); // age 35
    }

    /**
     * Test getComparatorDesc - descending order
     */
    @Test
    public void testGetComparatorDesc() {
        List<TestPerson> persons = new ArrayList<>(createTestPersons());
        Comparator<TestPerson> comparator = ObjectOperations.getComparatorDesc("age");
        persons.sort(comparator);

        assertEquals("Bob", persons.get(0).getName()); // age 35
        assertEquals("John", persons.get(1).getName()); // age 30
        assertEquals("Alice", persons.get(2).getName()); // age 28
        assertEquals("Jane", persons.get(3).getName()); // age 25
    }

    /**
     * Test ifPropertyPresent - executes action
     */
    @Test
    public void testIfPropertyPresent() {
        TestPerson person = createTestPersons().get(0);
        AtomicReference<String> result = new AtomicReference<>();

        ObjectOperations.ifPropertyPresent(person, "email", value -> result.set(value.toString()));

        assertEquals("john@mail.com", result.get());
    }

    /**
     * Test ifPropertyPresent - no action if null
     */
    @Test
    public void testIfPropertyPresentWithNull() {
        TestPerson person = new TestPerson();
        AtomicReference<String> result = new AtomicReference<>("unchanged");

        ObjectOperations.ifPropertyPresent(person, "email", value -> result.set(value.toString()));

        assertEquals("unchanged", result.get()); // Should not change
    }

    /**
     * Test toFlatMap - flattens bean to map
     */
    @Test
    public void testToFlatMap() {
        TestPerson person = createTestPersons().get(0);
        person.setAddress(new TestAddress("New York", "USA"));

        Map<String, Object> flat = ObjectOperations.toFlatMap(person);

        assertNotNull(flat);
        assertEquals("John", flat.get("name"));
        assertEquals("john@mail.com", flat.get("email"));
        assertEquals(30, flat.get("age"));
        assertEquals("New York", flat.get("address.city"));
        assertEquals("USA", flat.get("address.country"));
    }

    /**
     * Test toFlatMap with null
     */
    @Test
    public void testToFlatMapWithNull() {
        Map<String, Object> flat = ObjectOperations.toFlatMap(null);
        assertNotNull(flat);
        assertTrue(flat.isEmpty());
    }

    /**
     * Test fromFlatMap - reconstructs bean
     */
    @Test
    public void testFromFlatMap() {
        Map<String, Object> flat = new HashMap<>();
        flat.put("name", "John");
        flat.put("email", "john@mail.com");
        flat.put("age", 30);

        TestPerson person = ObjectOperations.fromFlatMap(flat, TestPerson.class);

        assertNotNull(person);
        assertEquals("John", person.getName());
        assertEquals("john@mail.com", person.getEmail());
        assertEquals(Integer.valueOf(30), person.getAge());
    }

    /**
     * Test fromFlatMap with null
     */
    @Test
    public void testFromFlatMapWithNull() {
        TestPerson person = ObjectOperations.fromFlatMap(null, TestPerson.class);
        assertNull(person);
    }

    /**
     * Test streamProperties
     */
    @Test
    public void testStreamProperties() {
        TestPerson person = createTestPersons().get(0);

        List<PropertyValue> properties = ObjectOperations.streamProperties(person)
                .filter(PropertyValue::isPresent)
                .collect(Collectors.toList());

        assertFalse(properties.isEmpty());
        assertTrue(properties.stream().anyMatch(pv -> "name".equals(pv.name())));
        assertTrue(properties.stream().anyMatch(pv -> "email".equals(pv.name())));
    }

    /**
     * Test streamProperties with null
     */
    @Test
    public void testStreamPropertiesWithNull() {
        long count = ObjectOperations.streamProperties(null).count();
        assertEquals(0, count);
    }

    /**
     * Test streamPropertyNames
     */
    @Test
    public void testStreamPropertyNames() {
        List<String> propertyNames = ObjectOperations.streamPropertyNames(TestPerson.class)
                .filter(name -> !name.equals("class"))
                .collect(Collectors.toList());

        assertFalse(propertyNames.isEmpty());
        assertTrue(propertyNames.contains("name"));
        assertTrue(propertyNames.contains("email"));
        assertTrue(propertyNames.contains("age"));
    }

    /**
     * Test streamPropertyNames with null
     */
    @Test
    public void testStreamPropertyNamesWithNull() {
        long count = ObjectOperations.streamPropertyNames(null).count();
        assertEquals(0, count);
    }

    /**
     * Test invokeIfExists - method exists
     */
    @Test
    public void testInvokeIfExists() {
        TestPerson person = createTestPersons().get(0);

        Optional<Object> result = ObjectOperations.invokeIfExists(person, "getName");
        assertTrue(result.isPresent());
        assertEquals("John", result.get());
    }

    /**
     * Test invokeIfExists - method not found
     */
    @Test
    public void testInvokeIfExistsNotFound() {
        TestPerson person = createTestPersons().get(0);

        Optional<Object> result = ObjectOperations.invokeIfExists(person, "nonExistentMethod");
        assertFalse(result.isPresent());
    }

    /**
     * Test hasMethod - method exists
     */
    @Test
    public void testHasMethod() {
        TestPerson person = createTestPersons().get(0);

        assertTrue(ObjectOperations.hasMethod(person, "getName"));
        assertTrue(ObjectOperations.hasMethod(person, "setName", String.class));
        assertFalse(ObjectOperations.hasMethod(person, "nonExistentMethod"));
    }

    /**
     * Test getPropertyType
     */
    @Test
    public void testGetPropertyType() {
        Optional<Class<?>> type = ObjectOperations.getPropertyType(TestPerson.class, "name");

        assertTrue(type.isPresent());
        assertEquals(String.class, type.get());
    }

    /**
     * Test getPropertyType - not found
     */
    @Test
    public void testGetPropertyTypeNotFound() {
        Optional<Class<?>> type = ObjectOperations.getPropertyType(TestPerson.class, "nonexistent");
        assertFalse(type.isPresent());
    }

    /**
     * Test validateProperties - all valid
     */
    @Test
    public void testValidateProperties() {
        TestPerson person = createTestPersons().get(0);

        Map<String, Predicate<Object>> validators = new HashMap<>();
        validators.put("email", email -> email != null && email.toString().contains("@"));
        validators.put("age", age -> age != null && ((Integer) age) >= 18);

        Map<String, Boolean> results = ObjectOperations.validateProperties(person, validators);

        assertEquals(2, results.size());
        assertTrue(results.get("email"));
        assertTrue(results.get("age"));
    }

    /**
     * Test validateProperties - some invalid
     */
    @Test
    public void testValidatePropertiesWithInvalid() {
        TestPerson person = new TestPerson("Test", "invalid-email", 15, "USA", true);

        Map<String, Predicate<Object>> validators = new HashMap<>();
        validators.put("email", email -> email != null && email.toString().contains("@"));
        validators.put("age", age -> age != null && ((Integer) age) >= 18);

        Map<String, Boolean> results = ObjectOperations.validateProperties(person, validators);

        assertEquals(2, results.size());
        assertFalse(results.get("email")); // Invalid
        assertFalse(results.get("age")); // Invalid
    }

    /**
     * Test with - fluent API
     */
    @Test
    public void testWith() {
        TestPerson person = new TestPerson();

        TestPerson result = ObjectOperations.with(person, "name", "John");
        assertEquals(person, result); // Same instance

        ObjectOperations.with(person, "email", "john@mail.com");
        ObjectOperations.with(person, "age", 30);

        assertEquals("John", person.getName());
        assertEquals("john@mail.com", person.getEmail());
        assertEquals(Integer.valueOf(30), person.getAge());
    }

    /**
     * Test copyWith - creates modified copy
     */
    @Test
    public void testCopyWith() {
        TestPerson original = createTestPersons().get(0);

        TestPerson modified = ObjectOperations.copyWith(original, p -> {
            p.setName("Modified Name");
            p.setAge(99);
        });

        assertNotNull(modified);
        assertEquals("Modified Name", modified.getName());
        assertEquals(Integer.valueOf(99), modified.getAge());

        // Original unchanged
        assertEquals("John", original.getName());
        assertEquals(Integer.valueOf(30), original.getAge());
    }

    /**
     * Test copyWith with null
     */
    @Test
    public void testCopyWithNull() {
        TestPerson result = ObjectOperations.copyWith(null, p -> p.setName("Test"));
        assertNull(result);
    }

    /**
     * Test PropertyValue record
     */
    @Test
    public void testPropertyValue() {
        PropertyValue pv = new PropertyValue("name", "John", String.class);

        assertEquals("name", pv.name());
        assertEquals("John", pv.value());
        assertEquals(String.class, pv.type());
        assertTrue(pv.isPresent());
        assertFalse(pv.isNull());
    }

    /**
     * Test PropertyValue with null value
     */
    @Test
    public void testPropertyValueNull() {
        PropertyValue pv = new PropertyValue("name", null, String.class);

        assertNull(pv.value());
        assertFalse(pv.isPresent());
        assertTrue(pv.isNull());
    }

    /**
     * Test PropertyValue getValueAs
     */
    @Test
    public void testPropertyValueGetValueAs() {
        PropertyValue pv = new PropertyValue("age", 30, Integer.class);

        Integer age = pv.getValueAs(Integer.class);
        assertEquals(Integer.valueOf(30), age);
    }
}
