package tools.dynamia.ui;

/**
 * Simple event callback for event listener where the event object is not important
 */
@FunctionalInterface
public interface EventCallback {

    void onEvent();
}
