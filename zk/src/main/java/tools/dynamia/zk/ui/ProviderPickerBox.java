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
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;
import tools.dynamia.commons.BeanSorter;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.reflect.ReflectionException;
import tools.dynamia.integration.Containers;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ProviderPickerBox extends Combobox {

    /**
     *
     */
    private static final long serialVersionUID = 4710970528102748639L;

    static {
        ComponentAliasIndex.getInstance().add("providerpickerbox", ProviderPickerBox.class);
        BindingComponentIndex.getInstance().put("selected", ProviderPickerBox.class);
    }

    private String selected;
    private String className;
    private String idField = "id";
    private String nameField = "name";

    private String iconField = "icon";
    private Class<?> providerClass;

    public ProviderPickerBox() {
        setReadonly(true);

        setItemRenderer((item, data, index) -> {

            String id, name, icon;

            try {
                id = BeanUtils.invokeGetMethod(data, idField).toString();
            } catch (ReflectionException | NullPointerException e) {
                throw new UiException("Error loading ID field for " + data, e);
            }

            try {
                name = BeanUtils.invokeGetMethod(data, nameField).toString();
            } catch (ReflectionException | NullPointerException e) {
                name = id;
            }

            try {
                icon = BeanUtils.invokeGetMethod(data, iconField).toString();
            } catch (ReflectionException | NullPointerException e) {
                icon = null;
            }


            if (name == null) {
                name = id;
            }

            item.setLabel(StringUtils.capitalize(name));
            item.setValue(id);
            if (icon != null) {
                item.setIconSclass(icon);
            }

        });
    }

    private void initModel() {
        if (providerClass != null) {
            try {
                Collection<?> implementations = Containers.get().findObjects(providerClass);
                try {
                    @SuppressWarnings("unchecked") List sorted = new ArrayList(implementations);
                    BeanSorter sorter = new BeanSorter(nameField);
                    //noinspection unchecked
                    sorter.sort(sorted);
                    ZKUtil.fillCombobox(this, sorted, true);
                } catch (Exception e) {
                    ZKUtil.fillCombobox(this, implementations, true);
                }
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

    public String getIconField() {
        return iconField;
    }

    public void setIconField(String iconField) {
        this.iconField = iconField;
        initModel();
    }

    public String getSelected() {
        selected = null;
        if (getSelectedItem() != null) {
            selected = getSelectedItem().getValue();
        }
        return selected;
    }

    public void setSelected(String selected) {
        if (selected != this.selected) {
            this.selected = selected;
            try {
                Optional provider = Containers.get().findObjects(providerClass)
                        .stream()
                        .filter(p -> selected.equals(BeanUtils.invokeGetMethod(p, idField)))
                        .findFirst();
                if (provider.isPresent()) {
                    ListModelList model = (ListModelList) getModel();
                    //noinspection unchecked
                    model.addToSelection(provider.get());
                }
            } catch (Exception ignored) {

            }
        }
    }
}
