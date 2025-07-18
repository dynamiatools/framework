package tools.dynamia.ui;

/**
 * The Interface SelectEventCallback. Callback interface for single item selection events.
 * This functional interface provides a type-safe mechanism for handling selection events
 * where a single item is chosen from a collection or list. It's commonly used in UI components
 * like dropdown lists, comboboxes, radio button groups, and single-selection lists where
 * the selected value needs to be processed or validated.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * // Lambda expression with type inference
 * SelectEventCallback&lt;String&gt; callback = selectedValue -> 
 *     System.out.println("Selected: " + selectedValue);
 * 
 * // Method reference
 * SelectEventCallback&lt;User&gt; callback = this::processSelectedUser;
 * 
 * // In a ComboBox component
 * comboBox.onSelect(item -> updateDetails(item));
 * </code>
 *
 * @param <T> the type of the selected value
 * @author Mario A. Serrano Leones
 */
@FunctionalInterface
public interface SelectEventCallback<T> {

    /**
     * Called when a single item is selected.
     *
     * @param value the selected value
     */
    void onSelect(T value);
}
