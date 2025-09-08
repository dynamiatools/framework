package tools.dynamia.viewers;

/**
 * Interface for tree field components.
 *
 * @param <T> the type of component
 */
public interface ITreeFieldComponent<T> {

    /**
     * Gets the name of the field.
     *
     * @return the field name
     */
    String getFieldName();

    /**
     * Gets the tree field component.
     *
     * @return the component
     */
    T getComponent();
}
