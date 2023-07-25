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
package tools.dynamia.crud.actions;

import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.Callback;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.crud.GenericCrudView;
import tools.dynamia.domain.ValidationError;

/**
 * @author Mario A. Serrano Leones
 */
@InstallAction
public class SaveAction extends AbstractCrudAction {

    public SaveAction() {
        setName(Messages.get(getClass(), "save"));
        setImage("save");
        setGroup(ActionGroup.get("CRUD"));
        setPosition(1);
        setShowLabel(true);
        setType("primary");
    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.CREATE, CrudState.UPDATE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(CrudActionEvent evt) {
        GenericCrudView crud = evt.getCrudView();
        crud.getController().setEntity(evt.getData());
        try {
            crud.getController().doSave();
        } catch (ValidationError e) {
            crud.handleValidationError(e);
        }

        Callback afterSave = () -> {
            if (crud.getController().isSaved()) {
                crud.setState(CrudState.READ);
                afterSave(evt.getData(), crud);
            }
        };

        if (crud.getController().isConfirmBeforeSave()) {
            crud.getController().onSave(afterSave);
        } else {
            afterSave.doSomething();
        }

    }

    protected void afterSave(Object entity, GenericCrudView crud) {

    }
}
