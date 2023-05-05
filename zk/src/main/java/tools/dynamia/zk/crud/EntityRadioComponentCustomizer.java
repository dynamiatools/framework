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
package tools.dynamia.zk.crud;

import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Radiogroup;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.viewers.ComponentCustomizer;
import tools.dynamia.viewers.Field;

import java.util.List;

/**
 *
 * @author Mario A. Serrano Leones
 */
@Provider
public class EntityRadioComponentCustomizer implements ComponentCustomizer<Radiogroup> {

    @Override
    public void cutomize(Field field, Radiogroup component) {
        if (field != null && field.isVisible() && field.getComponentClass() == Radiogroup.class) {
            if (BeanUtils.isAssignable(field.getFieldClass(), AbstractEntity.class)) {
                if (field.getParams().get("automodel") == Boolean.TRUE) {
                    CrudService crudService = Containers.get().findObject(CrudService.class);
                    List entities = crudService.findAll(field.getFieldClass());
                    //noinspection unchecked
                    component.setModel(new ListModelList(entities, true));
                }
            }
        }
    }
}
