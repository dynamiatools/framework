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
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ComboitemRenderer;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.actions.Actions;
import tools.dynamia.commons.Messages;
import tools.dynamia.zk.util.ZKUtil;

import java.util.List;

public class ComboboxActionRenderer extends ZKActionRenderer<Combobox> {

    private final List model;
    private boolean readonly = true;
    private Object defaultValue;
    private ComboitemRenderer comboitemRenderer;

    public ComboboxActionRenderer(List model) {
        super();
        this.model = model;
    }

    public ComboboxActionRenderer(List model, Object defaultValue) {
        super();
        this.model = model;
        this.defaultValue = defaultValue;
    }

    @Override
    public Combobox render(Action action, ActionEventBuilder actionEventBuilder) {
        Combobox combobox = new Combobox();
        ZKUtil.fillCombobox(combobox, model, defaultValue, true);
        configureProperties(combobox, action);
        combobox.setReadonly(isReadonly());
        combobox.setTooltiptext(action.getLocalizedName(Messages.getDefaultLocale()));

        combobox.addEventListener(Events.ON_SELECT, e -> Actions.run(action, actionEventBuilder, combobox,
                (Object) combobox.getSelectedItem().getValue()));

        if (comboitemRenderer != null) {
            combobox.setItemRenderer(comboitemRenderer);
        }

        return combobox;
    }

    public boolean isReadonly() {
        return readonly;
    }

    /**
     * Sets the readonly. By default is true
     *
     * @param readonly the new readonly
     */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public ComboitemRenderer getComboitemRenderer() {
        return comboitemRenderer;
    }

    public void setComboitemRenderer(ComboitemRenderer comboitemRenderer) {
        this.comboitemRenderer = comboitemRenderer;
    }
}
