package tools.dynamia.viewers;

import java.util.List;

/**
 * Generic interface that represent a form field group
 *
 * @param <C>
 */
public interface GenericFormFieldGroupComponent<C> {

    String getGroupName();

    C getGroupComponent();

    List<? extends GenericFormFieldComponent<C>> getFieldsComponents();

    void hide();

    void show();

    void remove();
}
