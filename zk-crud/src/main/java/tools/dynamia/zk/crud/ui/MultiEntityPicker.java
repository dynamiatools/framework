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
package tools.dynamia.zk.crud.ui;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.ui.icons.IconsTheme;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class MultiEntityPicker extends Div {

    /**
     *
     */
    private static final long serialVersionUID = 4761200963821487965L;
    private EntityPickerBox entityPicker;
    private Listbox listbox;
    private List<AbstractEntity> selectedEntities;

    static {
        BindingComponentIndex.getInstance().put("selectedEntities", MultiEntityPicker.class);
        ComponentAliasIndex.getInstance().add(MultiEntityPicker.class);
    }

    public MultiEntityPicker() {
        buildLayout();
    }

    private void buildLayout() {
        Vlayout box = new Vlayout();
        box.setWidth("100%");
        box.setParent(this);

        entityPicker = new EntityPickerBox();
        entityPicker.setParent(box);
        entityPicker.setWidth("100%");
        entityPicker.addEventListener(Events.ON_SELECT, event -> {
            addEntity(entityPicker.getSelected());
            entityPicker.setSelected(null);

        });

        listbox = new Listbox();
        listbox.setWidth("100%");
        listbox.setParent(box);
        {
            new Listhead().setParent(listbox);
            addColumn("Id", "50px");
            addColumn("Seleccionados", null);
            addColumn("", "40px");
        }
        listbox.setItemRenderer(new ListitemRenderer() {

            @Override
            public void render(Listitem item, Object data, int index) {
                renderEntity(item, data);

            }
        });
    }

    private void addEntity(Object selected) {
        if (selectedEntities == null) {
            selectedEntities = new ArrayList<>();
        }
        if (!selectedEntities.contains(selected)) {
            selectedEntities.add((AbstractEntity) selected);
            renderItems();
            Events.postEvent(new Event(Events.ON_SELECT, this, selected));
        }
    }

    public void setListbox(Listbox listbox) {
        if (listbox != null) {
            this.listbox = listbox;
        }
    }

    private Listheader addColumn(String label, String width) {
        Listheader header = new Listheader(label);
        if (width != null) {
            header.setWidth(width);
        }
        header.setParent(listbox.getListhead());
        return header;
    }

    public void setEntityClass(String entityClass) {
        entityPicker.setEntityClass(entityClass);
    }

    public Class getEntityClass() {
        return entityPicker.getEntityClass();
    }

    public void setFields(String fields) {
        entityPicker.setFields(fields);
    }

    public List<AbstractEntity> getSelectedEntities() {
        return selectedEntities;
    }

    public void setSelectedEntities(List<AbstractEntity> selectedEntities) {
        this.selectedEntities = selectedEntities;
        renderItems();
    }

    private void renderItems() {
        ZKUtil.fillListbox(listbox, selectedEntities, true);
    }

    private void renderEntity(final Listitem item, Object data) {
        AbstractEntity entity = (AbstractEntity) data;
        if (entity == null || entity.getId() == null) {
            return;
        }

        item.setValue(entity);

        String id = entity.getId().toString();

        new Listcell(id).setParent(item);
        new Listcell(data.toString()).setParent(item);

        Listcell cellBtn = new Listcell();
        cellBtn.setParent(item);
        {
            Toolbarbutton removeBtn = new Toolbarbutton();
            removeBtn.setParent(cellBtn);
            String icon = IconsTheme.get().getIcon("delete").getRealPath(IconSize.SMALL);
            if (icon == null) {
                removeBtn.setLabel("Borrar");
            } else {
                removeBtn.setImage(icon);
            }

            removeBtn.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    item.detach();
                    selectedEntities.remove(item.getValue());
                }
            });
        }
    }
}
