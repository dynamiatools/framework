package tools.dynamia.commons;

/**
 * The Interface PropertyChangeListener. Functional interface for listening to property changes from beans.
 * This interface provides a standardized mechanism for observing property modifications in objects,
 * enabling reactive programming patterns and data binding scenarios. It's commonly used in UI frameworks,
 * data validation, auditing, and synchronization between different parts of an application.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * // Lambda expression usage
 * PropertyChangeListener listener = evt -> {
 *     System.out.println("Property " + evt.getPropertyName() + 
 *                       " changed from " + evt.getOldValue() + 
 *                       " to " + evt.getNewValue());
 * };
 * 
 * // Method reference usage
 * PropertyChangeListener validator = this::validateProperty;
 * </code>
 *
 * @author Mario A. Serrano Leones
 */
@FunctionalInterface
public interface PropertyChangeListener {

    /**
     * Called when a property changes.
     *
     * @param evt the property change event
     */
    void propertyChange(PropertyChangeEvent evt);
}
