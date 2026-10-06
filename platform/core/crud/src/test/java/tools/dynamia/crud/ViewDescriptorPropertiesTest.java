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
package tools.dynamia.crud;

import org.junit.jupiter.api.Test;
import tools.dynamia.viewers.ViewDescriptor;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tools.dynamia.viewers.ViewDescriptorBuilder.field;
import static tools.dynamia.viewers.ViewDescriptorBuilder.viewDescriptor;

public class ViewDescriptorPropertiesTest {

    private ViewDescriptor descriptor() {
        return viewDescriptor("table", TestEntity.class, false)
                .fields(
                        field("name"),
                        field("subentity"),
                        field("subentity.name").path("sub.name"),
                        field("subentity.name")
                )
                .build();
    }

    @Test
    public void propertiesIncludeIdAndEveryDescriptorFieldOnce() {
        String[] props = ViewDescriptorProperties.propertiesOf(descriptor());
        assertEquals(Arrays.asList("id", "name", "subentity", "subentity.name"), Arrays.asList(props));
    }

    @Test
    public void usesFieldNamesNotQueryPaths() {
        String[] props = ViewDescriptorProperties.propertiesOf(descriptor());
        assertEquals(false, Arrays.asList(props).contains("sub.name"));
    }

    private ViewDescriptor bindingsDescriptor() {
        return viewDescriptor("table", TestEntity.class, false)
                .fields(
                        field("name"),
                        // virtual field (not a property of the entity) rendered from its bindings
                        field("productos").component("coollabel").params("bindings", java.util.Map.of(
                                "imageURL", "foto1.thumbnailUrl",
                                "title", "name",
                                "description", "subentity.name",
                                "subtitle", java.util.Map.of("value", "parent.name", "converter", "x"))),
                        field("notes").params("bind", "description")
                )
                .build();
    }

    @Test
    public void includesBindingPaths() {
        var props = Arrays.asList(ViewDescriptorProperties.propertiesOf(bindingsDescriptor()));

        assertEquals(true, props.containsAll(Arrays.asList(
                "id", "name", "foto1.thumbnailUrl", "subentity.name", "parent.name", "description")), props.toString());
    }

    @Test
    public void includesRootObjectOfNestedPathsForDottedExpressions() {
        // ZK evaluates the binding as bean.subentity.name, so the root object must be a key of the BeanMap
        var props = Arrays.asList(ViewDescriptorProperties.propertiesOf(bindingsDescriptor()));

        assertEquals(true, props.containsAll(Arrays.asList("foto1", "subentity", "parent")), props.toString());
    }

    @Test
    public void doesNotRepeatProperties() {
        var props = Arrays.asList(ViewDescriptorProperties.propertiesOf(bindingsDescriptor()));
        assertEquals(props.size(), props.stream().distinct().count());
    }

    @Test
    public void ignoresVirtualFieldNameAndNonPathExpressions() {
        var descriptor = viewDescriptor("table", TestEntity.class, false)
                .fields(field("virtual").params("bindings", java.util.Map.of(
                        "a", "name + ' ' + description",
                        "b", "#{fn:foo(name)}",
                        "c", "bean.notes")))
                .build();

        var props = Arrays.asList(ViewDescriptorProperties.propertiesOf(descriptor));

        assertEquals(false, props.contains("virtual"));
        assertEquals(true, props.contains("notes"));
        assertEquals(false, props.stream().anyMatch(p -> p.contains(" ") || p.contains("#") || p.contains("(")));
    }
}
