/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import tools.dynamia.commons.Messages;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Toolbarbutton;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.zk.util.ZKUtil;

import java.util.List;

public class ComboboxToolbarbuttonActionRenderer extends ZKActionRenderer<Component> {

    private boolean showlabels;
    private boolean toggleMode;
    private final List comboboxModel;

    public ComboboxToolbarbuttonActionRenderer(List comboboxModel) {
        this.comboboxModel = comboboxModel;
    }

    public ComboboxToolbarbuttonActionRenderer(boolean showlabels, List comboboxModel) {
        this.showlabels = showlabels;
        this.comboboxModel = comboboxModel;
    }

    public ComboboxToolbarbuttonActionRenderer(boolean showlabels, boolean toggleMode, List comboboxModel) {
        this.showlabels = showlabels;
        this.toggleMode = toggleMode;
        this.comboboxModel = comboboxModel;
    }

    @Override
    public Component render(final Action action, final ActionEventBuilder actionEventBuilder) {
        Hlayout htl = new Hlayout();
        htl.setStyle("display:inline");
        final Combobox combo = new Combobox();
        combo.setParent(htl);
        combo.setReadonly(true);
        ZKUtil.fillCombobox(combo, comboboxModel);

        Toolbarbutton btn = new Toolbarbutton();
        btn.setParent(htl);
        ZKUtil.configureComponentIcon(action.getImage(), btn, IconSize.NORMAL);

        btn.setTooltiptext(action.getLocalizedName(Messages.getDefaultLocale()));
        if (toggleMode) {
            btn.setMode("toggle");
        }
        if (action.getDescription() != null && !action.getDescription().isEmpty()) {
            btn.setTooltiptext(action.getLocalizedName(Messages.getDefaultLocale()) + ": " + action.getLocalizedDescription(Messages.getDefaultLocale()));
        }

        if (btn.getImage() == null || showlabels || action.getAttribute("showLabel") == Boolean.TRUE) {
            btn.setLabel(action.getLocalizedName(Messages.getDefaultLocale()));
        }

        btn.addEventListener(Events.ON_CLICK, event -> {
            if (combo.getSelectedItem() != null) {
                ActionEvent evt = new ActionEvent(combo.getSelectedItem().getValue(), actionEventBuilder);
                action.actionPerformed(evt);
            }
        });
        super.configureProperties(htl, action);

        return htl;
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
