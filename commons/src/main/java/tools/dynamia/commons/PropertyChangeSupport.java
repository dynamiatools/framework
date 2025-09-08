package tools.dynamia.commons;

import tools.dynamia.commons.logger.LoggingService;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides support for managing and notifying {@link PropertyChangeListener}s about property changes.
 * This class can be used to implement the observer pattern for property changes in Java objects.
 * It allows listeners to be registered and notified when a property value changes.
 * <p>
 * Example usage:
 * <pre>
 * PropertyChangeSupport support = new PropertyChangeSupport(this);
 * support.addPropertyChangeListener(evt -> {
 *     // handle property change
 * });
 * support.firePropertyChange("propertyName", oldValue, newValue);
 * </pre>
 * </p>
 */
public class PropertyChangeSupport implements PropertyChangeListenerContainer {

    /**
     * List of registered property change listeners.
     */
    private final List<PropertyChangeListener> listeners = new ArrayList<>();
    /**
     * The source object for property change events. If not set, defaults to this instance.
     */
    private Object source;

    /**
     * Constructs a new {@code PropertyChangeSupport} with no source object.
     */
    public PropertyChangeSupport() {
    }

    /**
     * Constructs a new {@code PropertyChangeSupport} with the specified source object.
     *
     * @param source the source object for property change events
     */
    public PropertyChangeSupport(Object source) {
        this.source = source;
    }

    /**
     * Adds a {@link PropertyChangeListener} to this container.
     * The listener will be notified of property changes.
     *
     * @param listener the listener to add
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a {@link PropertyChangeListener} from this container.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners of a property change event.
     *
     * @param propertyName the name of the property that changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     */
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        final var src = source != null ? source : this;
        listeners.forEach(l -> l.propertyChange(new PropertyChangeEvent(propertyName, src, oldValue, newValue)));
    }

    /**
     * Adds a {@link PropertyChangeListener} to all containers in the given list.
     *
     * @param containers the list of containers to add the listener to
     * @param listener   the listener to add
     */
    public static void onPropertyChange(List<? extends PropertyChangeListenerContainer> containers, PropertyChangeListener listener) {
        containers.forEach(i -> i.addPropertyChangeListener(listener));
    }

    /**
     * Adds a {@link PropertyChangeListener} to the specified container.
     *
     * @param container the container to add the listener to
     * @param listener  the listener to add
     */
    public static void onPropertyChange(PropertyChangeListenerContainer container, PropertyChangeListener listener) {
        container.addPropertyChangeListener(listener);
    }

    /**
     * Removes all registered property change listeners from this container.
     * If an error occurs during removal, a warning is logged.
     */
    public void clearListeners() {
        try {
            listeners.clear();
        } catch (Exception e) {
            LoggingService.get(getClass()).warn("Error clearing listeners: " + e.getMessage());
        }
    }
}
