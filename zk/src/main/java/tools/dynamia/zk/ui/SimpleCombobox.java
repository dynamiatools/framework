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
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.ui.model.SimpleItem;
import tools.dynamia.zk.util.ZKUtil;

import java.util.List;
import java.util.Map;

public class SimpleCombobox extends Combobox {

    static {
        ComponentAliasIndex.getInstance().put("simplecombobox", SimpleCombobox.class);
        BindingComponentIndex.getInstance().put("selected", SimpleCombobox.class);
    }

    private List<SimpleItem> simpleModel;
    private SimpleItem selected;


    public SimpleCombobox() {
        setItemRenderer((item, data, index) -> {
            item.setValue(data);
            if (data instanceof SimpleItem) {
                item.setLabel(((SimpleItem) data).getLabel().replace("_", " "));
            } else {
                item.setLabel(BeanUtils.getInstanceName(data));
            }
        });

        setReadonly(true);
    }

    public void setModelMap(Map<String, Object> model) {
        List<SimpleItem> items = SimpleItem.parse(model);
        setSimpleModel(items);
    }

    public void setSimpleModel(List<SimpleItem> simpleModel) {
        this.simpleModel = simpleModel;
        ZKUtil.fillCombobox(this, simpleModel, true);
    }

    public List<SimpleItem> getSimpleModel() {
        return simpleModel;
    }

    public Object getSelected() {
        if (getSelectedItem() != null) {
            selected = getSelectedItem().getValue();
        }
        return selected.getValue();
    }

    public void setSelected(SimpleItem selected) {
        this.selected = selected;
        if (getModel() instanceof AbstractListModel) {
            AbstractListModel model = (AbstractListModel) getModel();
            model.addToSelection(selected);
        }
    }

    public void setSelected(Object value) {
        SimpleItem item = simpleModel.stream().filter(s -> s.getValue().equals(value)).findFirst().orElse(null);

        setSelected(item);
    }

}
