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
package tools.dynamia.viewers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static tools.dynamia.viewers.ViewDescriptorBuilder.field;
import static tools.dynamia.viewers.ViewDescriptorBuilder.viewDescriptor;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class ViewDescriptorBuilderTest {

    @Test
    public void testViewDescriptorBuilder() {
        ViewDescriptor vd = viewDescriptor("form", SomeBean.class)
                .sortFields("name", "age", "url", "email", "date", "number")
                .customizer(SomeViewCustomizer.class)
                .fields(
                        field("name").label("Nombre").component("label"),
                        field("age").fieldClass(int.class).label("Edad").component("intbox").params("converter", "converters.Number"))
                .build();

        assertEquals(6, vd.getFields().size());
        assertEquals("intbox", vd.getField("age").getComponent());
        assertEquals(SomeViewCustomizer.class, vd.getViewCustomizerClass());

    }
}
