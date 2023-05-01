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
package tools.dynamia.zk.actions;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menuitem;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.actions.Actions;
import tools.dynamia.commons.Messages;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.zk.util.ZKUtil;

public class MenuitemActionRenderer extends ZKActionRenderer<Menuitem> {

    @Override
    public Menuitem render(final Action action, final ActionEventBuilder actionEventBuilder) {
        Menuitem menu = new Menuitem();
        ZKUtil.configureComponentIcon(action.getImage(), menu, IconSize.SMALL);
        menu.setLabel(action.getLocalizedName(Messages.getDefaultLocale()));
        menu.setAttribute("ACTION", action);
        action.setAttribute("COMPONENT", menu);
        menu.addEventListener(Events.ON_CLICK, event ->
                Actions.run(action, actionEventBuilder, event.getTarget()));


        return menu;
    }

}
