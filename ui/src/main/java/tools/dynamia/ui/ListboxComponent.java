package tools.dynamia.ui;

import java.util.List;

/**
 * The Interface ListboxComponent. Represents a UI listbox component for item selection.
 *
 * @param <T> the type of items in the listbox
 * @author Mario A. Serrano Leones
 */
public interface ListboxComponent<T> extends UIComponent {

    /**
     * Sets the data items for the listbox.
     *
     * @param data the list of items
     */
    void setData(List<T> data);

    /**
     * Gets the data items from the listbox.
     *
     * @return the list of items
     */
    List<T> getData();

    /**
     * Gets the currently selected item.
     *
     * @return the selected item
     */
    T getSelected();

    /**
     * Sets the selected item.
     *
     * @param item the item to select
     */
    void setSelected(T item);

    /**
     * Gets the list of selected items (for multiple selection).
     *
     * @return the list of selected items
     */
    List<T> getSelection();

    /**
     * Sets the selected items (for multiple selection).
     *
     * @param items the items to select
     */
    void setSelection(List<T> items);

    /**
     * Gets the index of the selected item.
     *
     * @return the selected index
     */
    int getSelectedIndex();

    /**
     * Sets the selected item by index.
     *
     * @param index the index to select
     */
    void setSelectedIndex(int index);

    /**
     * Clears the listbox selection and data.
     */
    void clear();

    /**
     * Refreshes the listbox display.
     */
    void refresh();

    /**
     * Sets the item renderer for custom item display.
     *
     * @param itemRenderer the item renderer object
     */
    void setItemRenderer(Object itemRenderer);

    /**
     * Sets whether multiple selection is enabled.
     *
     * @param multiple true to enable multiple selection
     */
    void setMultiple(boolean multiple);

    /**
     * Checks if multiple selection is enabled.
     *
     * @return true if multiple selection is enabled
     */
    boolean isMultiple();

    /**
     * Sets the callback for single item selection events.
     *
     * @param onSelect the selection event callback
     */
    void onSelect(SelectEventCallback<T> onSelect);

    /**
     * Sets the callback for multiple item selection events.
     *
     * @param onSelection the selection event callback
     */
    void onSelection(SelectionEventCallback<T> onSelection);

}
