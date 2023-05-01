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
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class BeanUtilsTest {


    /**
     * Test of invokeMethod method, of class BeanUtils.
     */
    @Test
    public void testInvokeMethod() {
        Object bean = "Bean";
        String methodName = "length";

        Object[] args = null;
        Object expResult = 4;
        Object result = BeanUtils.invokeMethod(bean, methodName, args);

        assertEquals(expResult, result);
    }

    /**
     * Test of formatGetMethod method, of class BeanUtils.
     */
    @Test
    public void testFormatGetMethod() {
        String propertyName = "text";
        String expResult = "getText";
        String result = BeanUtils.formatGetMethod(propertyName);
        assertEquals(expResult, result);
    }

    /**
     * Test of formatSetMethod method, of class BeanUtils.
     */
    @Test
    public void testFormatSetMethod() {
        String propertyName = "text";
        String expResult = "setText";
        String result = BeanUtils.formatSetMethod(propertyName);
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
        Object result = BeanUtils.invokeGetMethod(bean, propertyName);
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
        Class result = BeanUtils.getFieldGenericType(SomeBean.class, "names");
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
        Field field = BeanUtils.getField(DummyClass.class, "name");
        assertNotNull(field);
    }
    @Test
    public void testGetField_Subfield() throws NoSuchFieldException {
        Field field = BeanUtils.getField(SomeBean.class, "dummy.name");
        assertNotNull(field);
        assertEquals("name", field.getName());
        assertEquals(DummyClass.class, field.getDeclaringClass());
        assertEquals(String.class, field.getType());
    }
    @Test
    public void testGetPropertyInfo() {
        PropertyInfo expected = new PropertyInfo("name", String.class, DummyClass.class, AccessMode.READ_WRITE);

        PropertyInfo result = BeanUtils.getPropertyInfo(DummyClass.class, "name");
        assertEquals(expected.getAccessMode(), result.getAccessMode());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getOwnerClass(), result.getOwnerClass());
        assertEquals(expected.getType(), expected.getType());
    }
    @Test
    public void testGetPropertyInfo_SubProperty() {
        PropertyInfo pinfo = BeanUtils.getPropertyInfo(SomeBean.class, "dummy.name");
        assertNotNull(pinfo);
        assertEquals("name", pinfo.getName());
        assertEquals(DummyClass.class, pinfo.getOwnerClass());
        assertEquals(String.class, pinfo.getType());
    }
    @Test
    public void testGetPropertiesInfo() {
        List<PropertyInfo> infos = BeanUtils.getPropertiesInfo(ChildClass.class);

        for (PropertyInfo propertyInfo : infos) {
        }
        assertEquals(8, infos.size());
    }
    @Test
    public void testSetMethod() {
        ChildClass child = new ChildClass();

        BeanUtils.invokeSetMethod(child, "visible", true);
        assertTrue(child.isVisible());

        SomeBean bean = new SomeBean();
        BeanUtils.invokeSetMethod(bean, "size", 2);

        assertEquals(Integer.valueOf(2), bean.getSize());
    }

    @Test
    public void testSetMethodWithPath() {
        SomeBean bean = new SomeBean();
        BeanUtils.invokeSetMethod(bean, "dummy.name", "TheDummy");
        assertEquals("TheDummy", bean.getDummy().getName());
    }

    @Test
    public void testIsAssignable() {
        assertTrue(BeanUtils.isAssignable(Integer.class, Object.class));
    }

    static class DummyClass extends LinkedList<String> {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        @Deprecated
        private String name;
        private Date date;

        public Class getGenericType() {
            return BeanUtils.getGenericTypeClass(this);
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
