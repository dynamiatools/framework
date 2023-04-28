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

package tools.dynamia.zk.crud.converters;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.zk.crud.ui.EntityPickerBox;

public class Entity implements Converter<Object, Object, Component> {

    @Override
    public Object coerceToUi(Object beanProp, Component component, BindContext ctx) {
        return coerceToUi(beanProp, component);
    }

    @Override
    public Object coerceToBean(Object compAttr, Component component, BindContext ctx) {
        return coerceToBean(compAttr, component);
    }


    private Object coerceToUi(Object val, Component comp) {
        CrudService crudService = Containers.get().findObject(CrudService.class);

        try {
            if (val != null) {
                EntityPickerBox entityPicker = (EntityPickerBox) comp;
                Long id = Long.valueOf(val.toString());
                return crudService.find(entityPicker.getEntityClass(), id);
            }
        } catch (NumberFormatException e) {
        }

        return null;
    }


    private Object coerceToBean(Object val, Component comp) {
        return DomainUtils.findEntityId(val);
    }

}
