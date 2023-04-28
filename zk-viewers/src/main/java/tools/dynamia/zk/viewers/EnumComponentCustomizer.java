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

import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelArray;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.viewers.ComponentCustomizer;
import tools.dynamia.viewers.Field;

public class EnumComponentCustomizer implements ComponentCustomizer<Combobox> {

    @Override
    public void cutomize(Field field, Combobox component) {

        if (field.getFieldClass() != null && field.getFieldClass().isEnum()) {
            ListModel model = new ListModelArray(field.getFieldClass().getEnumConstants(), true);
            component.setModel(model);
            component.setReadonly(true);
            component.setItemRenderer((item, data, index) -> {
                String name = BeanUtils.getInstanceName(data);
                item.setLabel(name.replace("_", " "));
                item.setValue(data);
            });
        }
    }
}
