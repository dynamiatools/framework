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
import org.zkoss.zul.A;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.actions.Actions;
import tools.dynamia.commons.Messages;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.zk.util.ZKUtil;

public class LinkActionRenderer extends ZKActionRenderer<A> {

    @Override
    public A render(Action action, ActionEventBuilder actionEventBuilder) {
        A link = new A();
        link.setLabel(action.getLocalizedName(Messages.getDefaultLocale()));
        link.setTooltiptext(action.getLocalizedDescription(Messages.getDefaultLocale()));
        if (action.getImage() != null) {
            ZKUtil.configureComponentIcon(link.getImage(), link, IconSize.SMALL);
        }
        link.addEventListener(Events.ON_CLICK, evt ->
                Actions.run(action, actionEventBuilder, evt.getTarget()));

        super.configureProperties(link, action);

        return link;
    }

}
