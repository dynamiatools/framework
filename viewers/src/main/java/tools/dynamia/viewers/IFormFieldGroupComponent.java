package tools.dynamia.viewers;

import java.io.Serializable;
import java.util.List;

/**
 * Generic interface that represent a form field group
 *
 * @param <C>
 */
public interface IFormFieldGroupComponent<C>  extends Serializable {

    String getGroupName();

    C getGroupComponent();

    List<? extends IFormFieldComponent<C>> getFieldsComponents();

    void hide();

    void show();

    void remove();
}
