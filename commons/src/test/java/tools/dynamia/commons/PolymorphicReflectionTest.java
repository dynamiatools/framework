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

import org.junit.Assert;
import org.junit.Test;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

public class PolymorphicReflectionTest {

    @Test
    public void testPolymorphicGetter() {
        ParentBean pbean = new ParentBean();
        ParentBean cBean = new ChildBean();

        String result = (String) BeanUtils.invokeGetMethod(pbean, "name");
        Assert.assertEquals("mario", result);

        result = (String) BeanUtils.invokeGetMethod(cBean, "name");
        Assert.assertEquals("alejandro", result);

        result = (String) BeanUtils.invokeGetMethod(cBean, "lastName");
        Assert.assertEquals("serrano", result);

    }

    @Test
    public void testPolymorphicFieldFromChildToParent() throws NoSuchFieldException {
        Field field = BeanUtils.getField(ChildBean.class, "name");
        Assert.assertNotNull(field);
    }

    @Test(expected = NoSuchFieldException.class)
    public void testPolymorphicFieldFromParentToChield() throws NoSuchFieldException {
        BeanUtils.getField(ParentBean.class, "age");
    }

    @Test
    public void testInstropector() throws IntrospectionException {
        BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(ParentBean.class);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
        }
        Assert.assertTrue(true);
    }

    @Test
    public void testInstropectorFromChild() throws IntrospectionException {
        BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(ChildBean.class);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
        }

        Assert.assertTrue(true);
    }

    class ParentBean {

        private String name = "mario";

        public String getName() {
            return name;
        }

        public String getAlgo() {
            return "Hay" + "   algo";
        }

        public String getLastName() {
            return "serrano";
        }

    }

    class ChildBean extends ParentBean {

        @Override
        public String getName() {
            return "alejandro";
        }

        public int getAge() {
            int age = 11;
            return age;
        }

        public String getZorro() {
            return "THE ZORRO";
        }
    }

}
