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

package tools.dynamia.zk.viewers;

import org.junit.Before;
import org.junit.Test;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Textbox;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.io.IOUtils;
import tools.dynamia.io.Resource;
import tools.dynamia.io.converters.ClassConverter;
import tools.dynamia.io.converters.StringConverter;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorReader;
import tools.dynamia.viewers.impl.YamlViewDescriptorReader;
import tools.dynamia.viewers.util.ViewDescriptorReaderUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.Assert.*;

/**
 * @author Mario A. Serrano Leones
 */
public class ViewDescriptorReaderTest {

    @Before
    public void configContainer() {
        var container = new SimpleObjectContainer();
        container.addObject("yml", new YamlViewDescriptorReader());
        container.addObject("cc", new ClassConverter());
        container.addObject("stc", new StringConverter());
        container.addObject("cust", new DefaultFieldCustomizer());
        Containers.get().installObjectContainer(container);
    }

    @Test
    public void testYamlSupportedReader() {
        ViewDescriptorReader vdr = ViewDescriptorReaderUtils.getReaderFor("yml");
        assertNotNull(vdr);
    }

    @Test
    public void testYamlReader() throws IOException {
        ViewDescriptorReader vdr = ViewDescriptorReaderUtils.getReaderFor("yml");
        assertNotNull(vdr);

        Resource ymlfile = IOUtils.getResource("classpath:META-INF/descriptors/DummyBeanForm.yml");
        assertNotNull(ymlfile);
        assertTrue(ymlfile.exists());
        InputStream is = ymlfile.getInputStream();

        Reader reader = new InputStreamReader(is);
        ViewDescriptor descriptor = vdr.read(ymlfile, reader, ViewDescriptorReaderUtils.getCustomizers(vdr));

        assertNotNull(descriptor);
        assertEquals(DummyBean.class, descriptor.getBeanClass());

        Field nameField = descriptor.getField("name");
        assertNotNull(nameField);
        assertEquals(6, descriptor.getFields().size());

        Field ageField = descriptor.getField("age");
        assertFalse(ageField.isVisible());

        assertTrue(descriptor.getLayout().getParams().containsKey("columns"));
        assertEquals("2", descriptor.getLayout().getParams().get("columns").toString());

        assertEquals(2, descriptor.getFieldGroups().size());
        assertEquals("group1", descriptor.getFieldGroups().get(0).getName());

        assertEquals(2, descriptor.getFieldGroups().get(1).getFields().size());

        assertNotNull(descriptor.getViewCustomizerClass());
        assertEquals(DummyViewCustomizer.class, descriptor.getViewCustomizerClass());

        assertEquals(true, descriptor.getField("lastName").getParams().get("multiline"));
        assertEquals("23px", descriptor.getField("lastName").getParams().get("width"));
        assertEquals(Textbox.class, descriptor.getField("lastName").getComponentClass());
        assertEquals(Intbox.class, descriptor.getField("ownField").getComponentClass());

        for (Field field : descriptor.getFields()) {
        }

    }
}
