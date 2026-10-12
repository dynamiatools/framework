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
import tools.dynamia.actions.HeadlessCapable;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.actions.ReadableOnly;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.LazyLoadable;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.UIViews;
import tools.dynamia.ui.ViewOptions;

import java.io.Serializable;

/**
 * @author Mario A. Serrano Leones
 */
@InstallAction
@RunsOn(ActionRuntime.HEADLESS)
public class ViewDataAction extends AbstractCrudAction implements ReadableOnly, HeadlessCapable {


    public ViewDataAction() {
        setName(Messages.get(ViewDataAction.class, "viewData"));
        setImage("info");
        setGroup(ActionGroup.get("CRUD"));
        setPosition(1.01f);
        setMenuSupported(true);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        if (evt.getData() != null) {
            reloadAndView(evt.getData());
        }
    }

    /**
     * Reload entity data within transaction a show the data
     *
     * @param data
     */
    public void reloadAndView(final Object data) {
        crudService().executeWithinTransaction(() -> {
            Object entity = data;
            Serializable id = DomainUtils.findEntityId(entity);
            if (id != null) {
                entity = crudService().load(entity.getClass(), id);
            }

            view(entity);
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void view(Object data) {
        if (data != null) {
            final Object entity = data;

            if (entity instanceof LazyLoadable) {
                ((LazyLoadable) entity).lazyLoad();
            }

            UIViews.showView(ViewOptions.of(ObjectOperations.getInstanceName(entity), (Class) entity.getClass(), entity));
        } else {
            UIMessages.showMessage(Messages.get(ViewDataAction.class, "select_row"), MessageType.ERROR);
        }
    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.READ);
    }

}
