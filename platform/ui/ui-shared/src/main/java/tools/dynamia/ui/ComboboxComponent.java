package tools.dynamia.ui;

import java.util.List;

/**
 * The Interface ComboboxComponent. Represents a UI combobox component for item selection.
 * This interface defines the functionality for dropdown list components that allow users
 * to select a single item from a predefined list of options. Comboboxes are commonly used
 * in forms, filters, and configuration interfaces where users need to choose from a limited
 * set of values. The component supports custom item rendering, flexible layout properties,
 * and programmatic selection control.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * ComboboxComponent&lt;Country&gt; countryCombo = uiFactory.createCombobox();
 * countryCombo.setData(countryList);
 * countryCombo.setSelected(defaultCountry);
 * countryCombo.setHflex("1");
 * countryCombo.setItemRenderer(country -> country.getName());
 * </code>
 *
 * @param <T> the type of items in the combobox
 * @author Mario A. Serrano Leones
 */
public interface ComboboxComponent<T> extends UIComponent {

    /**
     * Sets the data items for the combobox.
     *
     * @param items the list of items
     */
    void setData(List<T> items);

    /**
     * Gets the data items from the combobox.
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
     * Clears the combobox selection and data.
     */
    void clear();

    /**
     * Refreshes the combobox display.
     */
    void refresh();

    /**
     * Sets the vertical flex property.
     *
     * @param vflex the vertical flex value
     */
    void setVflex(String vflex);

    /**
     * Gets the vertical flex property.
     *
     * @return the vertical flex value
     */
    String getVflex();

    /**
     * Sets the horizontal flex property.
     *
     * @param hflex the horizontal flex value
     */
    void setHflex(String hflex);

    /**
     * Gets the horizontal flex property.
     *
     * @return the horizontal flex value
     */
    String getHflex();

    /**
     * Sets the item renderer for custom item display.
     *
     * @param itemRenderer the item renderer object
     */
    void setItemRenderer(Object itemRenderer);
}
