package tools.dynamia.ui;

import java.util.List;

/**
 * The Interface SelectionEventCallback. Callback interface for multiple item selection events.
 * This functional interface provides a type-safe mechanism for handling selection events
 * where multiple items can be chosen from a collection or list. It's commonly used in UI
 * components like multi-selection lists, checkbox groups, data tables with row selection,
 * and tree components where multiple nodes can be selected simultaneously.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * // Lambda expression for processing multiple selections
 * SelectionEventCallback&lt;Product&gt; callback = selectedItems -> {
 *     selectedItems.forEach(product -> 
 *         System.out.println("Selected: " + product.getName()));
 * };
 * 
 * // Method reference
 * SelectionEventCallback&lt;User&gt; callback = this::processSelectedUsers;
 * 
 * // In a multi-selection list component
 * listBox.onSelectionChange(items -> updateBatchActions(items));
 * </code>
 *
 * @param <T> the type of the selected items
 * @author Mario A. Serrano Leones
 */
@FunctionalInterface
public interface SelectionEventCallback<T> {

    /**
     * Called when items are selected.
     *
     * @param selection the list of selected items
     */
    void onSelect(List<T> selection);
}
