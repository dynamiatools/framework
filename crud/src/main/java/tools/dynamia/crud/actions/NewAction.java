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
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.PrimaryAction;
import tools.dynamia.commons.BeanMessages;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.crud.GenericCrudView;

/**
 * @author Mario A. Serrano Leones
 */
@InstallAction
@PrimaryAction
public class NewAction extends AbstractCrudAction {

    public NewAction() {
        setName(Messages.get(NewAction.class, "new"));
        setImage("add");
        setGroup(ActionGroup.get("CRUD"));
        setPosition(1);
    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.READ);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {

        GenericCrudView view = evt.getCrudView();
        BeanMessages messages = new BeanMessages(view.getController().getEntityClass());
        view.getController().doCreate();
        view.setState(CrudState.CREATE);
        view.setValue(view.getController().getEntity());
        view.setTitle(getName() + " " + messages.getLocalizedName());

    }

}
