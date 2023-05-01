package tools.dynamia.commons;

/**
 * Property change event
 */
public record PropertyChangeEvent(String propertyName, Object source, Object oldValue, Object newValue) {


    public String getPropertyName() {
        return propertyName;
    }


    public Object getSource() {
        return source;
    }


    public Object getOldValue() {
        return oldValue;
    }


    public Object getNewValue() {
        return newValue;
    }
}
