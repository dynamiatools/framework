package tools.dynamia.commons;

/**
 * Property change event
 */
public class PropertyChangeEvent {

    private String propertyName;

    private Object source;

    private Object oldValue;
    private Object newValue;


    public PropertyChangeEvent(String propertyName, Object source, Object oldValue, Object newValue) {
        this.propertyName = propertyName;
        this.source = source;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Object getSource() {
        return source;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Object getOldValue() {
        return oldValue;
    }
}
