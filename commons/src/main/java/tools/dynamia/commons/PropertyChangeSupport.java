package tools.dynamia.commons;

import java.util.ArrayList;
import java.util.List;

public class PropertyChangeSupport implements PropertyChangeListenerContainer {

    private List<PropertyChangeListener> listeners = new ArrayList<>();
    private Object source;

    public PropertyChangeSupport() {
    }

    public PropertyChangeSupport(Object source) {
        this.source = source;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        final var src = source != null ? source : this;
        listeners.forEach(l -> l.propertyChange(new PropertyChangeEvent(propertyName, src, oldValue, newValue)));
    }
}