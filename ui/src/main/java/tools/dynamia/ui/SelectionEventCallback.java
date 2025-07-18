package tools.dynamia.ui;

import java.util.List;

/**
 * The Interface SelectionEventCallback. Callback interface for multiple item selection events.
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
