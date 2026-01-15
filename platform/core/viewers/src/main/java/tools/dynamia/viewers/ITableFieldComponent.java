package tools.dynamia.viewers;

import java.io.Serializable;

/**
 * Interface for table field components.
 *
 * @param <T> the type of component
 */
public interface ITableFieldComponent<T> extends Serializable {

    /**
     * Gets the name of the field.
     *
     * @return the field name
     */
    String getFieldName();

    /**
     * Gets the table field component.
     *
     * @return the component
     */
    T getComponent();
}
