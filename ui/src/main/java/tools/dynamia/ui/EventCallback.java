package tools.dynamia.ui;

/**
 * Simple event callback for event listener where the event object is not important
 *
 * @author Mario A. Serrano Leones
 */
@FunctionalInterface
public interface EventCallback {

    /**
     * Called when the event occurs.
     */
    void onEvent();
}
