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
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A ZK Combobox component that discovers and lists implementations of a given
 * provider interface or base class from the application's IoC container.
 *
 * <p>The component populates its items by querying {@link Containers#get()}
 * for all objects assignable to the configured provider class. Each item is
 * rendered using configurable field names for the identifier, display name,
 * and optional icon. By default the fields are {@code id}, {@code name} and
 * {@code icon} respectively.</p>
 *
 * <p>The combobox is configured as read-only and will show the provider's
 * display name while the underlying value stored for selection is the provider
 * identifier (as resolved by {@link #idField}). The component registers the
 * alias {@code providerpickerbox} for use in ZUL files and supports binding
 * via the {@code selected} property.</p>
 *
 * <h3>Example ZUL usage</h3>
 * <pre>{@code
 * <providerpickerbox className="com.example.MyProvider"
 *                    idField="code"
 *                    nameField="label"
 *                    iconField="iconSclass"
 *                    selected="@bind(vm.selectedProvider)" />
 * }</pre>
 */
public class ProviderPickerBox extends Combobox {

    /**
     * Serialization id.
     */
    @Serial
    private static final long serialVersionUID = 4710970528102748639L;

    static {
        ComponentAliasIndex.getInstance().add("providerpickerbox", ProviderPickerBox.class);
        BindingComponentIndex.getInstance().put("selected", ProviderPickerBox.class);
    }

    /**
     * The currently selected provider identifier. This value corresponds to the
     * field defined by {@link #idField} for the selected provider instance.
     */
    private String selected;

    /**
     * Fully-qualified class name of the provider interface or base class used to
     * discover implementations in the IoC container.
     */
    private String className;

    /**
     * Name of the provider field used as unique identifier. Defaults to {@code "id"}.
     */
    private String idField = "id";

    /**
     * Name of the provider field used for display label. Defaults to {@code "name"}.
     */
    private String nameField = "name";

    /**
     * Name of the provider field used as an icon CSS class. Defaults to {@code "icon"}.
     */
    private String iconField = "icon";

    /**
     * Resolved provider {@link Class} corresponding to {@link #className}. May be {@code null}
     * until {@link #setClassName(String)} is called with a valid class name.
     */
    private Class<?> providerClass;

    /**
     * Constructs a new ProviderPickerBox.
     *
     * <p>The combobox is set to read-only and an item renderer is installed that
     * resolves the item's id, name and icon using the configured field names.
     * If the id cannot be resolved for an item an {@link UiException} is thrown
     * during rendering.</p>
     */
    public ProviderPickerBox() {
        setReadonly(true);

        setItemRenderer((item, data, index) -> {

            String id = getItemId(item);
            if (id == null) {
                throw new UiException(item + " has no id field named [" + idField + "]");
            }
            String name = getItemName(item);
            String icon = getItemIcon(item);


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

    /**
     * Initializes or refreshes the combobox model by locating all provider
     * implementations from the IoC container and filling the component model.
     *
     * <p>The list is attempted to be sorted by {@link #nameField} using
     * {@link BeanSorter}; if sorting fails the raw collection is used.</p>
     *
     * @throws UiException if the lookup or model population fails
     */
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

    /**
     * Returns the fully-qualified class name configured to discover provider
     * implementations.
     *
     * @return the configured provider class name, or {@code null} if none set
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the fully-qualified provider class name and immediately attempts to
     * resolve the class and initialize the component model.
     *
     * @param className fully-qualified provider interface or base class name
     * @throws UiException if the class cannot be found on the classpath
     */
    public void setClassName(String className) {
        this.className = className;
        try {
            this.providerClass = Class.forName(className);
            initModel();
        } catch (ClassNotFoundException e) {
            throw new UiException("Invalid class name for " + this, e);
        }
    }

    /**
     * Returns the name of the field used as the provider identifier.
     *
     * @return the id field name
     */
    public String getIdField() {
        return idField;
    }

    /**
     * Sets the name of the field used as the provider identifier and refreshes
     * the component model.
     *
     * @param idField the identifier field name (getter must be available on provider objects)
     */
    public void setIdField(String idField) {
        this.idField = idField;
        initModel();
    }

    /**
     * Returns the name of the field used as the provider display label.
     *
     * @return the display name field
     */
    public String getNameField() {
        return nameField;
    }

    /**
     * Sets the name of the field used as the provider display label and
     * refreshes the component model.
     *
     * @param nameField the display name field
     */
    public void setNameField(String nameField) {
        this.nameField = nameField;
        initModel();
    }

    /**
     * Returns the name of the field used as the provider icon CSS class.
     *
     * @return the icon field name
     */
    public String getIconField() {
        return iconField;
    }

    /**
     * Sets the name of the field used as the provider icon CSS class and
     * refreshes the component model.
     *
     * @param iconField the icon field name
     */
    public void setIconField(String iconField) {
        this.iconField = iconField;
        initModel();
    }

    /**
     * Returns the identifier of the currently selected provider item, or
     * {@code null} if nothing is selected.
     *
     * @return selected provider id or {@code null}
     */
    public String getSelected() {
        selected = null;
        if (getSelectedItem() != null) {
            selected = getSelectedItem().getValue();
        }
        return selected;
    }

    /**
     * Programmatically selects the item whose identifier matches the provided
     * {@code selected} value. If a matching item is found it is added to the
     * selection of the underlying {@link ListModelList}.
     *
     * @param selected provider identifier to select
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setSelected(String selected) {
        if (!Objects.equals(selected, this.selected)) {
            this.selected = selected;
            if (getModel() instanceof ListModelList model) {
                model.stream().filter(item -> Objects.equals(getItemId(item), selected))
                        .findFirst()
                        .ifPresent(model::addToSelection);
            }
        }
    }

    /**
     * Returns the provider item's identifier by reading the configured {@code field}
     * from the given {@code item} using reflection utilities.
     *
     * @param item provider instance to inspect
     * @return identifier as string or {@code null} when it cannot be resolved
     */
    protected String getItemId(Object item) {
        return getItemField(item, idField);
    }

    /**
     * Returns the provider item's display name by reading the configured
     * {@link #nameField} from the given {@code item}.
     *
     * @param item provider instance to inspect
     * @return display name as string or {@code null} when it cannot be resolved
     */
    protected String getItemName(Object item) {
        return getItemField(item, nameField);
    }

    /**
     * Returns the provider item's icon CSS class by reading the configured
     * {@link #iconField} from the given {@code item}.
     *
     * @param item provider instance to inspect
     * @return icon css class as string or {@code null} when it cannot be resolved
     */
    protected String getItemIcon(Object item) {
        return getItemField(item, iconField);
    }

    /**
     * Generic helper that reads a named field value from an object using
     * {@link ObjectOperations#invokeGetMethod(Object, String)} and returns
     * its string representation when present.
     *
     * @param item  object to inspect
     * @param field field name to read (getter must exist)
     * @return field value as string or {@code null} if not found or not accessible
     */
    protected String getItemField(Object item, String field) {
        try {
            if (item != null) {
                Object fieldValue = ObjectOperations.invokeGetMethod(item, field);
                if (fieldValue != null) {
                    return fieldValue.toString();
                }
            } else {
                return null;
            }
        } catch (Exception _) {
        }
        return null;
    }
}
