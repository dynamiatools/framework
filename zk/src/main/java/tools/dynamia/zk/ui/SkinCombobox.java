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

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.ListModelList;
import tools.dynamia.templates.ApplicationTemplate;
import tools.dynamia.templates.ApplicationTemplateHolder;
import tools.dynamia.templates.ApplicationTemplateSkin;
import tools.dynamia.templates.ApplicationTemplates;
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
            ApplicationTemplateSkin applicationSkin = (ApplicationTemplateSkin) e.getSelectedObjects().stream().findFirst().get();
            this.selectedSkin = applicationSkin.getId();
        });
    }

    public String getSelected() {
        return selectedSkin;
    }

    public void setSelected(String skinId) {
        if (skinId != null) {
            try {
                this.selectedSkin = null;
                ApplicationTemplateSkin skin = null;
                ApplicationTemplate template = ApplicationTemplateHolder.get().getTemplate();
                if (template != null) {
                    skin = template.getSkins()
                            .stream()
                            .filter(p -> p.getId().equals(skinId))
                            .findFirst().orElse(null);
                }

                if (skin != null) {
                    ListModelList model = (ListModelList) getModel();
                    //noinspection unchecked
                    model.addToSelection(skin);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setSelectedIndex(-1);
        }
    }

    private void initModel() {
        ZKUtil.fillCombobox(this, ApplicationTemplates.getAllSkins(ApplicationTemplateHolder.get().getTemplate()));
    }

    static class SkinItemRenderer implements ComboitemRenderer<ApplicationTemplateSkin> {

        @Override
        public void render(Comboitem item, ApplicationTemplateSkin data, int index) {
            item.setValue(data.getId());
            item.setLabel(data.getName());
            item.setDescription(data.getDescription());

        }
    }

}
