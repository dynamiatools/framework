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
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorFactory;
import tools.dynamia.viewers.impl.DefaultViewDescriptorFactory;
import tools.dynamia.viewers.impl.YamlViewDescriptorReader;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static tools.dynamia.zk.viewers.table.TableViewDescriptorBuilder.*;

/**
 * @author Mario Serrano Leones
 */
public class TableViewDescriptorBuilderTest {

    private ViewDescriptorFactory factory;

    @Before
    public void initFactory() {
        factory = new DefaultViewDescriptorFactory();
        var container = new SimpleObjectContainer();
        container.addObject("yml", new YamlViewDescriptorReader());
        container.addObject("cc", new ClassConverter());


        Containers.get().installObjectContainer(container);
    }

    @Test
    public void shouldBeEquals() {
        ViewDescriptor vdReaded = factory.getDescriptor(DummyBean.class, "table");
        ViewDescriptor vdBuilded = tableViewDescriptor(DummyBean.class, false)
                .fields(
                        column("name")
                                .label("First Name")
                                .description("The first name"),
                        column("lastName")
                                .label("Last Name")
                                .header(h()
                                        .align("center")
                                        .width("100px"))
                                .footer(f()
                                        .function("count"))
                )
                .orderBy("name")
                .build();

        assertEquals(vdReaded.getViewTypeName(), vdBuilded.getViewTypeName());
        assertEquals(vdReaded.getBeanClass(), vdBuilded.getBeanClass());
        assertEquals(vdReaded.getFields().size(), vdBuilded.getFields().size());
        assertEquals(vdReaded.getField("name") != null, vdBuilded.getField("name") != null);
        assertEquals(vdReaded.getParams().get("orderBy"), vdBuilded.getParams().get("orderBy"));

        Field lastNameExpected = vdReaded.getField("lastName");
        Field lastName = vdBuilded.getField("lastName");

        assertEquals(lastNameExpected.getLabel(), lastName.getLabel());

        Map<String, Object> headerExpected = (Map<String, Object>) lastNameExpected.getParams().get("header");
        Map<String, Object> header = (Map<String, Object>) lastName.getParams().get("header");

        assertEquals(headerExpected.get("align"), header.get("align"));
        assertEquals(headerExpected.get("width"), header.get("width"));

        Map<String, Object> footerExpected = (Map<String, Object>) lastNameExpected.getParams().get("footer");
        Map<String, Object> footer = (Map<String, Object>) lastName.getParams().get("footer");

        assertEquals(footerExpected.get("function"), footer.get("function"));

    }

}
