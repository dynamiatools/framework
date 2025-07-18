package tools.dynamia.commons;

/**
 * Listener for properties change form beans
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
