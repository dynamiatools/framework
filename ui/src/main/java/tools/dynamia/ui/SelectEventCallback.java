package tools.dynamia.ui;

/**
 * The Interface SelectEventCallback. Callback interface for single item selection events.
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
