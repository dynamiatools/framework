package tools.dynamia.commons;

/**
 * Listener for properties change form beans
 */
@FunctionalInterface
public interface PropertyChangeListener {

    void propertyChange(PropertyChangeEvent evt);
}
