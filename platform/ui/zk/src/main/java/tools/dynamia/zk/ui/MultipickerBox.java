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
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Bandpopup;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;

import java.io.Serial;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base ZK component for multi-selection functionality using a Bandbox with a popup Listbox.
 * Selected values are stored internally as a comma-separated string.
 * This component provides a foundation for creating specialized multi-picker components.
 *
 * <p>The component renders a readonly Bandbox that displays selected items' labels.
 * When clicked, it shows a popup with a multi-select Listbox.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * MultipickerBox picker = new MultipickerBox();
 * picker.setItemRenderer((item, data, index) -> {
 *     item.setLabel(data.toString());
 *     item.setValue(data.toString());
 * });
 * picker.setModel(new ListModelList<>(Arrays.asList("Option1", "Option2", "Option3")));
 * picker.setSelected("Option1,Option3");
 * }</pre>
 *
 * @author Mario Serrano
 * @since 5.3.0
 */
public class MultipickerBox extends Bandbox {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String SEPARATOR = ",";

    private String selected;
    private final Listbox itemsList;
    private final Bandpopup bandpopup;

    /**
     * Creates a new MultipickerBox component with default configuration.
     * The component is readonly with a visible button for opening the selection popup.
     */
    public MultipickerBox() {
        setReadonly(true);
        setButtonVisible(true);

        bandpopup = new Bandpopup();
        bandpopup.setHflex("min");
        bandpopup.setVflex("min");

        itemsList = new Listbox();
        itemsList.setHeight("200px");
        itemsList.setMultiple(true);
        itemsList.setCheckmark(true);

        itemsList.addEventListener(Events.ON_SELECT, e -> updateLabel());

        bandpopup.appendChild(itemsList);
        appendChild(bandpopup);

        addEventListener(Events.ON_FULFILL, e -> updateLabel());
    }

    /**
     * Updates the Bandbox value with the labels of selected items, separated by commas.
     * If no items are selected, the value is cleared.
     */
    protected void updateLabel() {
        if (itemsList.getSelectedItems().isEmpty()) {
            setValue("");
        } else {
            String label = itemsList.getSelectedItems().stream()
                    .map(Listitem::getLabel)
                    .collect(Collectors.joining(", "));
            setValue(label);
        }
    }

    /**
     * Returns the internal Listbox component used for displaying items.
     * Subclasses can use this to customize the listbox behavior.
     *
     * @return the Listbox component
     */
    protected Listbox getItemsList() {
        return itemsList;
    }

    /**
     * Returns the Bandpopup component containing the listbox.
     * Subclasses can use this to customize the popup behavior.
     *
     * @return the Bandpopup component
     */
    protected Bandpopup getBandpopup() {
        return bandpopup;
    }

    @Override
    public boolean addEventListener(String evtnm, EventListener<? extends Event> listener) {
        if (Events.ON_SELECT.equals(evtnm)) {
            return itemsList.addEventListener(evtnm, listener);
        } else {
            return super.addEventListener(evtnm, listener);
        }
    }

    /**
     * Sets the model for the internal listbox.
     * The model should be a ListModelList with multiple selection enabled.
     *
     * @param model the collection of items to display
     */
    public void setModel(Collection<?> model) {
        ListModelList<?> listModel = new ListModelList<>(model);
        listModel.setMultiple(true);
        itemsList.setModel(listModel);
    }

    /**
     * Sets the item renderer for the internal listbox.
     * The renderer is responsible for creating Listitem components from data objects.
     *
     * @param renderer the item renderer
     */
    public void setItemRenderer(ListitemRenderer<?> renderer) {
        itemsList.setItemRenderer(renderer);
    }

    /**
     * Returns the comma-separated string of selected item values.
     * This method reads the current selection from the listbox.
     *
     * @return comma-separated string of selected values, or null if nothing is selected
     */
    public String getSelected() {
        selected = null;
        if (itemsList.getSelectedItems() != null && !itemsList.getSelectedItems().isEmpty()) {
            selected = itemsList.getSelectedItems().stream()
                    .map(it -> it.getValue().toString())
                    .collect(Collectors.joining(SEPARATOR));
        }
        return selected;
    }

    /**
     * Sets the selected items based on a comma-separated string of values.
     * This method matches values against the listbox model and selects matching items.
     *
     * @param selected comma-separated string of values to select
     */
    public void setSelected(String selected) {
        if (selected != null && selected.equals(this.selected) ||
            (selected == null && this.selected == null)) {
            return;
        }

        this.selected = selected;

        try {
            ListModelList<?> model = (ListModelList<?>) itemsList.getModel();
            if (model == null) {
                return;
            }

            // Clear current selection
            model.clearSelection();

            if (selected == null || selected.trim().isEmpty()) {
                updateLabel();
                return;
            }

            // Parse selected values
            String[] values = selected.split(SEPARATOR);
            Set<String> selectedValues = new HashSet<>();
            for (String value : values) {
                selectedValues.add(value.trim());
            }

            // Select matching items
            selectItemsByValues(model, selectedValues);

            itemsList.renderAll();
            updateLabel();
        } catch (Exception e) {
            throw new UiException("Error setting selected values for " + this, e);
        }
    }

    /**
     * Selects items in the model whose values match the provided set of values.
     * Subclasses can override this method to customize the selection logic.
     *
     * @param model the list model
     * @param selectedValues set of values to select
     */
    protected void selectItemsByValues(ListModelList<?> model, Set<String> selectedValues) {
        for (Object item : model) {
            String itemValue = extractItemValue(item);
            if (itemValue != null && selectedValues.contains(itemValue)) {
                //noinspection unchecked
                ((ListModelList<Object>) model).addToSelection(item);
            }
        }
    }

    /**
     * Extracts the value from a data object to match against selected values.
     * By default, this method returns the toString() of the object.
     * Subclasses should override this method to provide custom value extraction logic.
     *
     * @param item the data object
     * @return the string value to match, or null if the value cannot be extracted
     */
    protected String extractItemValue(Object item) {
        return item != null ? item.toString() : null;
    }

    /**
     * Sets the height of the internal listbox popup.
     *
     * @param height the height (e.g., "200px", "300px")
     */
    public void setListHeight(String height) {
        itemsList.setHeight(height);
    }

    /**
     * Gets the height of the internal listbox popup.
     *
     * @return the height of the listbox
     */
    public String getListHeight() {
        return itemsList.getHeight();
    }

    /**
     * Sets the separator used for joining and splitting selected values.
     * By default, the separator is a comma (",").
     * Note: Changing this value affects how getSelected() and setSelected() work.
     *
     * @return the separator string
     */
    public String getSeparator() {
        return SEPARATOR;
    }
}
