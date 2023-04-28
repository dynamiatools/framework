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

import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProviderMultipickerBox extends Bandbox {

    /**
     *
     */
    private static final long serialVersionUID = 4710970528102748639L;

    static {
        ComponentAliasIndex.getInstance().add("providermultipickerbox", ProviderMultipickerBox.class);
        BindingComponentIndex.getInstance().put("selected", ProviderMultipickerBox.class);
    }

    private String selected;
    private String className;
    private String idField = "id";
    private String nameField = "name";
    private Class<?> providerClass;
    private final Listbox itemsList;

    public ProviderMultipickerBox() {
        setReadonly(true);
        setButtonVisible(true);
        Bandpopup bandpopup = new Bandpopup();
        bandpopup.setHflex("min");
        bandpopup.setVflex("min");

        itemsList = new Listbox();

        itemsList.setHeight("200px");
        itemsList.setMultiple(true);
        itemsList.setCheckmark(true);

        itemsList.setItemRenderer((item, data, index) -> {

            try {
                String id = BeanUtils.invokeGetMethod(data, idField).toString();
                String name = BeanUtils.invokeGetMethod(data, nameField).toString();

                item.setLabel(StringUtils.capitalize(name));
                item.setValue(id);

            } catch (Exception e) {
                throw new UiException("Error rendering item for " + this, e);
            }
        });
        itemsList.addEventListener(Events.ON_SELECT, e -> updateLabel());


        bandpopup.appendChild(itemsList);
        appendChild(bandpopup);

        addEventListener(Events.ON_FULFILL, e -> updateLabel());


    }


    private void updateLabel() {

        if (itemsList.getSelectedItems().isEmpty()) {
            setValue("");
        } else {
            String label = itemsList.getSelectedItems().stream().map(Listitem::getLabel).collect(Collectors.joining(", "));
            setValue(label);
        }
    }

    @Override
    public boolean addEventListener(String evtnm, EventListener<? extends Event> listener) {
        if (Events.ON_SELECT.equals(evtnm)) {
            return itemsList.addEventListener(evtnm, listener);
        } else {
            return super.addEventListener(evtnm, listener);
        }
    }

    private void initModel() {
        if (providerClass != null) {
            try {
                Collection<?> implementations = Containers.get().findObjects(providerClass);
                ListModelList model = new ListModelList<>(implementations);
                model.setMultiple(true);
                itemsList.setModel(model);

                itemsList.setMultiple(true);

            } catch (Exception e) {
                throw new UiException("Cannot init model for " + this + ". Provider class name: " + className, e);
            }
        }

    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
        try {
            this.providerClass = Class.forName(className);
            initModel();
        } catch (ClassNotFoundException e) {
            throw new UiException("Invalid class name for " + this, e);
        }
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
        initModel();
    }

    public String getNameField() {
        return nameField;
    }

    public void setNameField(String nameField) {
        this.nameField = nameField;
        initModel();
    }

    public String getSelected() {
        selected = null;
        if (itemsList.getSelectedItems() != null && !itemsList.getSelectedItems().isEmpty()) {
            selected = itemsList.getSelectedItems().stream().map(it -> it.getValue().toString()).collect(Collectors.joining(","));
        }
        return selected;
    }

    public void setSelected(String selected) {
        if (selected != this.selected) {

            this.selected = selected;
            try {

                String[] values = null;

                if (selected != null) {
                    if (selected.contains(",")) {
                        values = selected.split(",");
                    } else {
                        values = new String[]{selected};
                    }
                }

                if (values != null) {
                    ListModelList model = (ListModelList) itemsList.getModel();
                    for (String value : values) {
                        Optional provider = Containers.get().findObjects(providerClass)
                                .stream()
                                .filter(p -> value.trim().equals(BeanUtils.invokeGetMethod(p, idField)))
                                .findFirst();

                        if (provider.isPresent()) {
                            model.addToSelection(provider.get());
                        }
                    }
                }
                itemsList.renderAll();
                updateLabel();
            } catch (Exception e) {

            }
        }
    }

}
