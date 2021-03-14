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




package tools.dynamia.zk.viewers;

import org.junit.Before;
import org.junit.Test;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.io.converters.ClassConverter;
import tools.dynamia.io.converters.StringConverter;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorFactory;
import tools.dynamia.viewers.impl.DefaultViewDescriptorFactory;
import tools.dynamia.viewers.impl.YamlViewDescriptorReader;

import static org.junit.Assert.*;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class ViewDescriptorTest {

    @Before
    public void configContainer(){
        var container = new SimpleObjectContainer();
        container.addObject("yml", new YamlViewDescriptorReader());
        container.addObject("cc", new ClassConverter());
        container.addObject("stc", new StringConverter());
        container.addObject("cust", new DefaultFieldCustomizer());
        Containers.get().installObjectContainer(container);

    }

    @Test
    public void shouldBuildADescriptor() {
        ViewDescriptorFactory vdf = new DefaultViewDescriptorFactory();
        ViewDescriptor descriptor = vdf.getDescriptor(MyBean.class, "table");
        assertNotNull(descriptor);
        assertEquals(2, descriptor.getFields().size());
    }

    @Test
    public void shouldBeTheSameDescriptor() {
        ViewDescriptorFactory vdf = new DefaultViewDescriptorFactory();
        ViewDescriptor descriptor1 = vdf.getDescriptor(MyBean.class, "table");
        ViewDescriptor descriptor2 = vdf.getDescriptor(MyBean.class, "table");
        ViewDescriptor descriptor3 = vdf.getDescriptor(MyBean.class, "table");

        assertTrue(descriptor1.equals(descriptor2) && descriptor2.equals(descriptor3) && descriptor3.equals(descriptor1));
    }

    class MyBean {

        private String name;
        private int age;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
