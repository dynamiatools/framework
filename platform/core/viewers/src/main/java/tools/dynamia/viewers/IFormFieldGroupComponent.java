package tools.dynamia.viewers;

import java.io.Serializable;
import java.util.List;

/**
 * Generic interface representing a group of form field components.
 *
 * @param <C> the type of group component
 */
public interface IFormFieldGroupComponent<C> extends Serializable {

    /**
     * Gets the name of the group.
     *
     * @return the group name
     */
    String getGroupName();

    /**
     * Gets the group component.
     *
     * @return the group component
     */
    C getGroupComponent();

    /**
     * Gets the list of field components in the group.
     *
     * @return the list of field components
     */
    List<? extends IFormFieldComponent<C>> getFieldsComponents();

    /**
     * Hides the group component.
     */
    void hide();

    /**
     * Shows the group component.
     */
    void show();

    /**
     * Removes the group component.
     */
    void remove();
}
