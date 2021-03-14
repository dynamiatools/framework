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

package tools.dynamia.zk.ui;

import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Booleanbox extends Combobox {

    private Boolean selected;
    private String trueLabel = "SI";
    private String falseLabel = "NO";

    static {
        BindingComponentIndex.getInstance().put("selected", Booleanbox.class);
        ComponentAliasIndex.getInstance().add("booleanbox", Booleanbox.class);
    }

    public Booleanbox() {
        init();
    }

    public Booleanbox(Boolean value) {
        this.selected = value;
    }

    public void init() {
        setReadonly(true);
        List<BooleanWrapper> model = Arrays.asList(new BooleanWrapper(null), new BooleanWrapper(Boolean.TRUE), new BooleanWrapper(Boolean.FALSE));

        ZKUtil.fillCombobox(this, model, model.get(0), true);

        setItemRenderer((ComboitemRenderer<BooleanWrapper>) (item, data, index) -> {
            item.setValue(data);
            item.setLabel(getLabel(data.value));
        });


    }

    private String getLabel(Boolean data) {
        if (data == null) {
            return "---";
        } else if (data) {
            return trueLabel;
        } else {
            return falseLabel;
        }
    }

    public Boolean getSelected() {
        if (getSelectedItem() != null) {
            this.selected = ((BooleanWrapper) getSelectedItem().getValue()).value;
        }

        return selected;
    }

    public void setSelected(Boolean selected) {

        this.selected = selected;

        if (getModel() instanceof AbstractListModel) {
            AbstractListModel model = (AbstractListModel) getModel();
            Optional result = getItems().stream().filter(c -> Objects.equals(c.getValue(), selected)).map(Comboitem::getValue).findFirst();
            if (result.isPresent()) {
                model.addToSelection(result.get());
            }
        }
    }

    public String getTrueLabel() {
        return trueLabel;
    }

    public void setTrueLabel(String trueLabel) {
        this.trueLabel = trueLabel;
    }

    public String getFalseLabel() {
        return falseLabel;
    }

    public void setFalseLabel(String falseLabel) {
        this.falseLabel = falseLabel;
    }

    class BooleanWrapper {
        Boolean value;

        public BooleanWrapper(Boolean value) {
            this.value = value;
        }


    }


}
