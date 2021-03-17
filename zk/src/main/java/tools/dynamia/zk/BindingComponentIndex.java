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
package tools.dynamia.zk;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.HeaderElement;
import org.zkoss.zul.impl.InputElement;
import org.zkoss.zul.impl.LabelElement;
import tools.dynamia.commons.collect.HashSetMultiMap;
import tools.dynamia.zk.ui.Calculator;
import tools.dynamia.zk.ui.DateRangebox;
import tools.dynamia.zk.ui.DecimalboxCalculator;

/**
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"rawtypes"})
public class BindingComponentIndex extends HashSetMultiMap<String, String> {

    private final static BindingComponentIndex instance = new BindingComponentIndex();

    static {
        instance.put("label", Listcell.class, Column.class, HeaderElement.class, LabelElement.class);
        instance.put("value", Label.class, Textbox.class, InputElement.class, Progressmeter.class, DateRangebox.class, DecimalboxCalculator.class, Calculator.class);
        instance.put("selectedItem", Combobox.class, Listbox.class, Radiogroup.class);
        instance.put("checked", Checkbox.class);
        instance.put("src", Image.class);
        instance.put("model", Chart.class);

    }

    public static BindingComponentIndex getInstance() {
        return instance;
    }

    public void put(String key, Class value) {
        if (getKey(value.getName()) != null) {
            throw new RuntimeException(value + " is already indexed");
        }
        super.put(key, value.getName());
    }

    public void put(String key, Class... value) {
        if (value != null && value.length > 0) {
            for (Class clazz : value) {
                put(key, clazz);
            }
        }
    }

    private String getAttributeForSuperClass(Class superClass) {
        if (superClass == null) {
            return null;
        }

        String component = getKey(superClass.getName());
        if (component == null) {
            component = getAttributeForSuperClass(superClass.getSuperclass());
        }

        return component;
    }

    public String getAttribute(Class<? extends Component> componentClass) {
        String attr = getKey(componentClass.getName());
        if (attr == null) {
            attr = getAttributeForSuperClass(componentClass.getSuperclass());
        }
        return attr;
    }
}
