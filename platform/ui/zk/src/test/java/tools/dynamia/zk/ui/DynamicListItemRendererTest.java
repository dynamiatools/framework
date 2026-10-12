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
package tools.dynamia.zk.ui;

import org.junit.jupiter.api.Test;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import tools.dynamia.commons.BeanMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The renderer must produce the same cells for an entity (Open Persistence In View) and for its BeanMap row
 * (without OSIV).
 */
public class DynamicListItemRendererTest {

    public static class Person {
        private final String name;
        private final Customer customer = new Customer();

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Customer getCustomer() {
            return customer;
        }

        @Override
        public String toString() {
            return "Person " + name;
        }
    }

    public static class Customer {
        public String getCode() {
            return "C-1";
        }
    }

    private static DynamicListItemRenderer renderer(String... fields) {
        DynamicListItemRenderer renderer = new DynamicListItemRenderer();
        renderer.setFields(fields);
        return renderer;
    }

    private static String[] texts(Listitem item) {
        return item.getChildren().stream().map(c -> ((Listcell) c).getLabel()).toArray(String[]::new);
    }

    private static BeanMap row(Object id) {
        BeanMap row = new BeanMap();
        row.setBeanClass(Person.class);
        row.setId(id);
        row.put("name", "Ana");
        row.put("customer.code", "C-1");
        return row;
    }

    @Test
    public void beanMapRowRendersIdAndValuesFromTheMap() {
        Listitem item = new Listitem();
        BeanMap row = row(5L);

        renderer("name", "customer.code").render(item, row, 0);

        assertArrayEquals(new String[]{"5", "Ana", "C-1"}, texts(item));
        assertSame(row, item.getValue());
    }

    @Test
    public void beanMapWithoutIdHasNoIdCell() {
        Listitem item = new Listitem();

        renderer("name").render(item, row(null), 0);

        assertArrayEquals(new String[]{"Ana"}, texts(item));
    }

    @Test
    public void beanMapMissingAFieldRendersAnEmptyCell() {
        Listitem item = new Listitem();

        renderer("name", "unknown").render(item, row(1L), 0);

        assertArrayEquals(new String[]{"1", "Ana", ""}, texts(item));
    }

    @Test
    public void plainBeansKeepReadingPropertiesByReflection() {
        Listitem item = new Listitem();

        renderer("name", "customer.code").render(item, new Person("Ana"), 0);

        assertArrayEquals(new String[]{"Ana", "C-1"}, texts(item));
    }

    @Test
    public void withoutFieldsTheInstanceNameIsRendered() {
        Listitem item = new Listitem();
        BeanMap row = row(1L);
        row.setStringRepresentation("Ana García");

        renderer().render(item, row, 0);

        assertArrayEquals(new String[]{"1", "Ana García"}, texts(item));
    }
}
