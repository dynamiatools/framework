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
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.GenericCrudView;

/**
 *
 * @author Mario A. Serrano Leones
 */
@InstallAction
public class SaveAndEditAction extends SaveAction {

    public SaveAndEditAction() {
        setName(Messages.get(getClass(), "save_edit"));
        setImage("save-edit");
        setGroup(ActionGroup.get("CRUD"));
        setPosition(2);
        setAttribute("showLabel",true);
        setType("default");
        setSclass("hidden-xs");

    }

    @Override
    protected void afterSave(Object entity, GenericCrudView crud) {
        EditAction edit = new EditAction();
        edit.actionPerformed(new CrudActionEvent(entity, null, crud, crud.getController()));
    }
}
