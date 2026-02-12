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

package tools.dynamia.modules.entityfile.ui.actions;

import tools.dynamia.commons.ApplicableClass;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.ui.EntityFileController;

public abstract class AbstractEntityFileAction extends AbstractCrudAction {

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        EntityFileController controller = (EntityFileController) evt.getController();
        actionPerformed(new EntityFileActionEvent(controller.getTargetEntity(), evt.getData(), evt.getSource(), evt.getCrudView(), controller));
    }

    public abstract void actionPerformed(EntityFileActionEvent evt);

    @Override
    public ApplicableClass[] getApplicableClasses() {
        return ApplicableClass.get(EntityFile.class);
    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.READ);
    }

}
