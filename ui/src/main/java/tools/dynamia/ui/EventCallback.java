package tools.dynamia.ui;

/**
 * The Interface EventCallback. Simple event callback for event listeners where the event object is not important.
 * This functional interface provides a lightweight mechanism for handling UI events when the specific
 * event details are not needed. It's commonly used for simple button clicks, menu selections,
 * timer callbacks, and other straightforward event scenarios where only the occurrence of the
 * event matters, not the event's payload or context.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * // Lambda expression
 * EventCallback callback = () -> System.out.println("Button clicked!");
 * 
 * // Method reference
 * EventCallback callback = this::handleClick;
 * 
 * // Anonymous class
 * EventCallback callback = new EventCallback() {
 *     public void onEvent() {
 *         performAction();
 *     }
 * };
 * </code>
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
