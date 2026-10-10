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
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.DelegateActionRender;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.ReadableOnly;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudFiltersProvider;
import tools.dynamia.crud.CrudState;
import tools.dynamia.ui.UIFacades;

/**
 * Opens the panel where the user builds the filters of the CRUD query. The panel belongs to the front end: this action only
 * names its renderer ({@value #RENDERER}, an {@code ActionRenderProvider} bean) and asks the {@link CrudFiltersProvider} of
 * the environment to toggle it. Where there is none (a front end that implements filters on the client) it does nothing.
 *
 * @author Mario A. Serrano Leones
 */
@InstallAction
@RunsOn(ActionRuntime.ZK_ONLY)
public class FiltersAction extends AbstractCrudAction implements ReadableOnly {

    /** Name of the {@code ActionRenderProvider} that draws the toggle button. */
    public static final String RENDERER = "filters";

    public FiltersAction() {
        setName(Messages.get(getClass(), "filters"));
        setImage("filter");
        setGroup(ActionGroup.get("CRUD_FIND"));
        setPosition(2);
    }

    @Override
    public CrudState[] getApplicableStates() {
        return new CrudState[]{CrudState.READ};
    }

    @Override
    public void actionPerformed(final CrudActionEvent evt) {
        CrudFiltersProvider provider = UIFacades.find(CrudFiltersProvider.class);
        if (provider != null) {
            provider.toggle(this, evt);
        }
    }

    @Override
    public ActionRenderer getRenderer() {
        return new DelegateActionRender(RENDERER);
    }
}
