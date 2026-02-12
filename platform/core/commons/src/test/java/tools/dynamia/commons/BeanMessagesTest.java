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

import my.company.ChildDummy;
import my.company.Dummy;
import org.junit.Assert;
import org.junit.Test;
import tools.dynamia.commons.reflect.PropertyInfo;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class BeanMessagesTest {

    @Test
    public void testCanFindBundles() {
        ResourceBundle bundle = ResourceBundle.getBundle(Dummy.class.getName());
        Assert.assertNotNull(bundle);
    }

    @Test
    public void testAllProperties() {
        BeanMessages msg = new BeanMessages(Dummy.class, Locale.of("es"));
        Assert.assertEquals("El idiota", msg.getLocalizedName());
        Assert.assertEquals("nombrecito", msg.getMessage("name"));
        Assert.assertEquals("edad", msg.getMessage("age"));

    }

    @Test
    public void testChild() {
        BeanMessages msg = new BeanMessages(ChildDummy.class, Locale.of("es"));
        List<PropertyInfo> info = ObjectOperations.getPropertiesInfo(ChildDummy.class);

        for (PropertyInfo propertyInfo : info) {
            System.out.println(propertyInfo);
        }
        Assert.assertEquals("El idiota hijo", msg.getLocalizedName());
    }
}
