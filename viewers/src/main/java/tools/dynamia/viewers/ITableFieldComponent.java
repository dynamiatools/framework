package tools.dynamia.viewers;

import java.io.Serializable;

/**
 * Interface to table field components
 * @param <T>
 */
public interface ITableFieldComponent<T>  extends Serializable {

    String getFieldName();


    T getComponent();
}
