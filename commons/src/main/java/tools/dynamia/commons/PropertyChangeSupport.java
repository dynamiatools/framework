package tools.dynamia.commons;

import tools.dynamia.commons.logger.LoggingService;

import java.util.ArrayList;
import java.util.List;

public class PropertyChangeSupport implements PropertyChangeListenerContainer {

    private final List<PropertyChangeListener> listeners = new ArrayList<>();
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

    /**
     * Add listener to all containers
     */
    public static void onPropertyChange(List<? extends PropertyChangeListenerContainer> containers, PropertyChangeListener listener) {
        containers.forEach(i -> i.addPropertyChangeListener(listener));
    }

    public static void onPropertyChange(PropertyChangeListenerContainer container, PropertyChangeListener listener) {
        container.addPropertyChangeListener(listener);
    }

    public void clearListeners() {
        try {
            listeners.clear();
        } catch (Exception e) {
            LoggingService.get(getClass()).warn("Error clearing listeners: " + e.getMessage());
        }
    }
}
