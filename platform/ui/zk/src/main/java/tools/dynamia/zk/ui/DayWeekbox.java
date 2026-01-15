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
package tools.dynamia.zk.ui;

import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import tools.dynamia.commons.DayOfWeek;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

/**
 * @author Ing. Mario Serrano
 */
public class DayWeekbox extends Combobox {

    static {
        BindingComponentIndex.getInstance().put("selected", DayWeekbox.class);
        ComponentAliasIndex.getInstance().add("dayweekbox", DayWeekbox.class);
    }

    private DayOfWeek selected = DayOfWeek.today();

    public DayWeekbox() {
        init();
    }

    public DayWeekbox(DayOfWeek selected) {
        this.selected = selected;
        init();
    }

    public DayOfWeek getSelected() {
        if (getSelectedItem() != null) {
            selected = getSelectedItem().getValue();
        }
        return selected;
    }

    public void setSelected(DayOfWeek selected) {
        this.selected = selected;

        if (getModel() instanceof AbstractListModel model) {
            //noinspection unchecked
            model.addToSelection(selected);
        }
    }

    private void init() {
        setItemRenderer((Comboitem item, DayOfWeek data, int index) -> {
            item.setValue(data);
            item.setLabel(data.getDisplayName());

        });

        setReadonly(true);
        initModel();
    }

    private void initModel() {
        ZKUtil.fillCombobox(this, DayOfWeek.valuesList(), selected, true);
    }

}
