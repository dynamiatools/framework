package tools.dynamia.commons;

/**
 * Property change event
 */
public record PropertyChangeEvent(String propertyName, Object source, Object oldValue, Object newValue) {

}
