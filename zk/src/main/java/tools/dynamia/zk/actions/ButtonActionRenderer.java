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
import org.zkoss.zul.Button;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.actions.Actions;
import tools.dynamia.commons.Messages;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.zk.util.ZKUtil;

public class ButtonActionRenderer extends ZKActionRenderer<Button> {

    private boolean showLabels = true;

    @Override
    public Button render(final Action action, final ActionEventBuilder actionEventBuilder) {
        Button btn = new Button();
        super.configureProperties(btn, action);
        String actionName = action.getLocalizedName(Messages.getDefaultLocale());
        ZKUtil.configureComponentIcon(action.getImage(), btn, IconSize.SMALL);
        btn.setLabel(actionName);

        if (action.getAttribute("showLabel") == Boolean.FALSE || !showLabels) {
            btn.setLabel("");
            showLabels = false;
        }

        if ((btn.getIconSclass() == null && btn.getImage() == null) || showLabels
                || action.getAttribute("showLabel") == Boolean.TRUE) {
            btn.setLabel(actionName);
        }
        String description = action.getLocalizedDescription(Messages.getDefaultLocale());
        if (description != null && !description.isEmpty()) {
            btn.setTooltiptext(description);
        }

        String zclass = (String) action.getAttribute("zclass");
        if (zclass != null && !zclass.isBlank()) {
            btn.setZclass(zclass);
        }

        String sclass = (String) action.getAttribute("sclass");
        if (sclass != null && !sclass.isBlank()) {
            btn.addSclass(sclass);
        }

        btn.addEventListener(Events.ON_CLICK, event -> Actions.run(action, actionEventBuilder, event.getTarget()));

        btn.setAutodisable("self");

        return btn;
    }

    public boolean isShowLabels() {
        return showLabels;
    }

    public void setShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
    }

}
