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

import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.viewers.ComponentCustomizer;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.FieldCustomizer;
import tools.dynamia.zk.ui.SimpleCombobox;
import tools.dynamia.zk.ui.model.SimpleItem;

import java.util.List;
import java.util.Map;

@Provider
public class SimpleComboboxFieldCustomizer implements FieldCustomizer, ComponentCustomizer<SimpleCombobox> {

    @Override
    public void customize(String viewTypeName, Field field) {

        if ("simplecombobox".equalsIgnoreCase(field.getComponent()) || field.getComponentClass() == SimpleCombobox.class) {
            Object items = field.getParams().get("items");
            if (items instanceof Map) {
                field.setComponentCustomizer(SimpleComboboxFieldCustomizer.class.getName());
                List<SimpleItem> itemsList = SimpleItem.parse((Map) items);
                field.getParams().put("simpleModel", itemsList);
            }
        }
    }

    @Override
    public void cutomize(Field field, SimpleCombobox component) {
        List<SimpleItem> itemList = (List<SimpleItem>) field.getParams().get("simpleModel");
        component.setSimpleModel(itemList);
    }
}
