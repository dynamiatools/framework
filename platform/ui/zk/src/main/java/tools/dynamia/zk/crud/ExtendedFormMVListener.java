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

import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.View;
import tools.dynamia.zk.viewers.mv.MultiView;

public class ExtendedFormMVListener extends AbstractExtendedMVListener {

    @Override
    public void subviewLoaded(MultiView parentView, View subview) {

        if (parentView.getParentView() instanceof CrudView crudView) {
            AbstractEntity parentEntity = (AbstractEntity) parentView.getValue();

            ExtendedFormSubcrudController subcontroller = new ExtendedFormSubcrudController();
            crudView.getController().addSubcrudController(subcontroller);

            AbstractEntity ext = getExtention(parentEntity);
            subcontroller.setEntity(ext);
            //noinspection unchecked
            subview.setValue(ext);
        }

    }

    private AbstractEntity getExtention(AbstractEntity parentEntity) {
        AbstractEntity extention = null;

        if (parentEntity != null && parentEntity.getId() != null) {
            CrudService crudService = Containers.get().findObject(CrudService.class);
            extention = crudService.findSingle(beanClass, beanProperty, parentEntity);

        }

        if (extention == null) {
            extention = ObjectOperations.newInstance(beanClass);
        }

        ObjectOperations.invokeSetMethod(extention, beanProperty, parentEntity);
        return extention;
    }

    @Override
    public void subviewSelected(MultiView parentView, View subview) {
    }

}
