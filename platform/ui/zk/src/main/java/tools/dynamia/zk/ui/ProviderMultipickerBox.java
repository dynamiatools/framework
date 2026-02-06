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
import org.zkoss.zul.ListModelList;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.zk.ComponentAliasIndex;

import java.util.Collection;
import java.util.Set;

/**
 * A specialized MultipickerBox that dynamically loads items from Spring container providers.
 * This component allows selecting multiple provider implementations based on a specified class.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * ProviderMultipickerBox picker = new ProviderMultipickerBox();
 * picker.setClassName("com.example.MyProvider");
 * picker.setIdField("id");
 * picker.setNameField("name");
 * picker.setSelected("provider1,provider2");
 * }</pre>
 *
 * @author Mario Serrano
 * @since 5.0.0
 */
public class ProviderMultipickerBox extends MultipickerBox {

    private static final long serialVersionUID = 4710970528102748639L;

    static {
        ComponentAliasIndex.getInstance().add("providermultipickerbox", ProviderMultipickerBox.class);
    }

    private String className;
    private String idField = "id";
    private String nameField = "name";
    private Class<?> providerClass;

    /**
     * Creates a new ProviderMultipickerBox component.
     * Initializes the item renderer to display provider instances.
     */
    public ProviderMultipickerBox() {
        super();
        setItemRenderer((item, data, index) -> {
            try {
                String id = ObjectOperations.invokeGetMethod(data, idField).toString();
                String name = ObjectOperations.invokeGetMethod(data, nameField).toString();

                item.setLabel(StringUtils.capitalize(name));
                item.setValue(id);
            } catch (Exception e) {
                throw new UiException("Error rendering item for " + this, e);
            }
        });

    }
    /**
     * Initializes the model by loading provider implementations from the Spring container.
     * This method is called when the provider class is set or configuration changes.
     */
    protected void initModel() {
        if (providerClass != null) {
            try {
                Collection<?> implementations = Containers.get().findObjects(providerClass);
                setModel(implementations);
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

    @Override
    protected String extractItemValue(Object item) {
        try {
            return ObjectOperations.invokeGetMethod(item, idField).toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void selectItemsByValues(ListModelList<?> model, Set<String> selectedValues) {
        try {
            Collection<?> providers = Containers.get().findObjects(providerClass);
            for (Object provider : providers) {
                String value = extractItemValue(provider);
                if (value != null && selectedValues.contains(value)) {
                    //noinspection unchecked
                    ((ListModelList<Object>) model).addToSelection(provider);
                }
            }
        } catch (Exception e) {
            // Silently ignore errors during selection
        }
    }

}
