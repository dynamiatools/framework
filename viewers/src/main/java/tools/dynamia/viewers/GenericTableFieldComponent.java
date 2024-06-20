package tools.dynamia.viewers;

/**
 * Interface to table field components
 * @param <T>
 */
public interface GenericTableFieldComponent<T> {

    String getFieldName();


    T getComponent();
}
