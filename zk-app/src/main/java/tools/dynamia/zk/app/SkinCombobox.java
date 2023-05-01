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
package tools.dynamia.zk.app;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.ListModelList;
import tools.dynamia.app.CurrentTemplate;
import tools.dynamia.app.template.ApplicationTemplates;
import tools.dynamia.app.template.Skin;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

import java.util.Optional;

public class SkinCombobox extends Combobox {

    /**
     *
     */
    private static final long serialVersionUID = 5668280738598675701L;
    private String selectedSkin;

    static {
        ComponentAliasIndex.getInstance().add("skincombobox", SkinCombobox.class);
        BindingComponentIndex.getInstance().put("selected", SkinCombobox.class);
    }

    public SkinCombobox() {
        setReadonly(true);
        setItemRenderer(new SkinItemRenderer());
        initModel();
        addEventListener(Events.ON_SELECT, (SelectEvent e) -> {
            Skin skin = (Skin) e.getSelectedObjects().stream().findFirst().get();
            this.selectedSkin = skin.getId();
        });
    }

    public String getSelected() {
        return selectedSkin;
    }

    public void setSelected(String skinId) {
        if (skinId != null) {
            try {
                this.selectedSkin = null;
                Optional<Skin> skin = CurrentTemplate.get().getTemplate().getSkins()
                        .stream()
                        .filter(p -> p.getId().equals(skinId))
                        .findFirst();
                if (skin.isPresent()) {
                    ListModelList model = (ListModelList) getModel();
                    //noinspection unchecked
                    model.addToSelection(skin.get());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setSelectedIndex(-1);
        }
    }

    private void initModel() {
        ZKUtil.fillCombobox(this, ApplicationTemplates.getAllSkins(CurrentTemplate.get().getTemplate()));
    }

    static class SkinItemRenderer implements ComboitemRenderer<Skin> {

        @Override
        public void render(Comboitem item, Skin data, int index) {
            item.setValue(data.getId());
            item.setLabel(data.getName());
            item.setDescription(data.getDescription());

        }
    }

}
