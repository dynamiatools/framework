package tools.dynamia.viewers;

/**
 * Interface to table field components
 * @param <T>
 */
public interface ITreeFieldComponent<T> {

    String getFieldName();


    T getComponent();
}
