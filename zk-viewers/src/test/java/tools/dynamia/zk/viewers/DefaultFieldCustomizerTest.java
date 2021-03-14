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

import org.junit.Test;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Textbox;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.FieldCustomizer;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.impl.DefaultViewDescriptor;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class DefaultFieldCustomizerTest {

    @Test
    public void testFieldCustomizerDirectly() {
        DefaultFieldCustomizer fc = new DefaultFieldCustomizer();
        Field field = new Field("name", Date.class);
        fc.customize("form", field);
        assertEquals(Datebox.class, field.getComponentClass());
    }

    @Test
    public void testFieldComponentAlias() {
        DefaultFieldCustomizer fc = new DefaultFieldCustomizer();
        Field field = new Field("name", Date.class);
        field.setComponent("combobox");
        fc.customize("form", field);
        assertEquals(Combobox.class, field.getComponentClass());
    }

    @Test
    public void testFieldEnums() {
        ViewDescriptor vd = new DefaultViewDescriptor(DummyBean.class, "form");
        Field type = vd.getField("type");
        assertEquals(Combobox.class, type.getComponentClass());
        assertEquals(EnumComponentCustomizer.class.getName(), type.getComponentCustomizer());
    }

    @Test
    public void testComboboxReadonly() {
        FieldCustomizer fc = new DefaultFieldCustomizer();
        Field comboField = new Field("test");
        comboField.setComponent("combobox");
        fc.customize("form", comboField);
        assertEquals(Boolean.TRUE, comboField.getParams().get("readonly"));

    }

    @Test
    public void testCustomize() {
        var container = new SimpleObjectContainer();
        container.addObject("customizer",new DefaultFieldCustomizer());
        Containers.get().installObjectContainer(container);
        DefaultViewDescriptor vd = new DefaultViewDescriptor(DummyBean.class, "form");

        {
            Field field = vd.getField("name");
            assertEquals(Textbox.class, field.getComponentClass());
        }

        {
            Field field = vd.getField("lastName");
            assertEquals(Textbox.class, field.getComponentClass());
        }

        {
            Field field = vd.getField("age");
            assertEquals(Intbox.class, field.getComponentClass());
        }

    }
}
