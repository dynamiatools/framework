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
import org.zkoss.zul.Toolbarbutton;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.actions.Actions;
import tools.dynamia.commons.Messages;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.zk.util.ZKUtil;

import java.util.Map;

public class ToolbarbuttonActionRenderer extends ZKActionRenderer<Toolbarbutton> {

    private boolean showlabels;
    private boolean toggleMode;

    public ToolbarbuttonActionRenderer() {
    }

    public ToolbarbuttonActionRenderer(boolean showlabels) {
        this.showlabels = showlabels;
    }

    public ToolbarbuttonActionRenderer(boolean showlabels, boolean toggleMode) {
        this.showlabels = showlabels;
        this.toggleMode = toggleMode;
    }

    @Override
    public Toolbarbutton render(final Action action, final ActionEventBuilder actionEventBuilder) {
        Toolbarbutton btn = new Toolbarbutton();

        ZKUtil.configureComponentIcon(action.getImage(), btn, IconSize.SMALL);

        String actionName = action.getLocalizedName(Messages.getDefaultLocale());
        String actionDescription = action.getLocalizedDescription(Messages.getDefaultLocale());

        btn.setTooltiptext(action.getName());
        if (toggleMode) {
            btn.setMode("toggle");
        }
        if (actionDescription != null && !actionDescription.isEmpty()) {
            btn.setTooltiptext(actionName + ": " + actionDescription);
        }

        if ((btn.getIconSclass() == null && btn.getImage() == null) || showlabels
                || action.getAttribute("showLabel") == Boolean.TRUE) {
            btn.setLabel(actionName);
        }

        String zclass = (String) action.getAttribute("zclass");
        if (zclass != null) {
            btn.setZclass(zclass);
        }

        btn.addEventListener(Events.ON_CLICK, event -> Actions.run(action, actionEventBuilder, event.getTarget()));


        super.configureProperties(btn, action);
        btn.setAutodisable("self");

        return btn;
    }

    public boolean isShowlabels() {
        return showlabels;
    }

    public void setShowlabels(boolean showlabels) {
        this.showlabels = showlabels;
    }

    public boolean isToggleMode() {
        return toggleMode;
    }

    public void setToggleMode(boolean toggleMode) {
        this.toggleMode = toggleMode;
    }

}
