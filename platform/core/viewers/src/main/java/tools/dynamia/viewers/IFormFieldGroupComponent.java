package tools.dynamia.viewers;

import java.io.Serializable;
import java.util.List;

/**
 * Interface representing a group of form field components in a view.
 * <p>
 * This interface defines the contract for components that represent a logical grouping
 * of form fields, typically displayed together under a common heading or within a
 * collapsible panel. Field groups help organize complex forms into manageable sections.
 * </p>
 * <p>
 * Implementations provide access to the group's name, the container component that holds
 * all fields in the group, and a list of the individual field components. They also support
 * operations to show, hide, or remove the entire group at once.
 * </p>
 *
 * Example:
 * <pre>{@code
 * IFormFieldGroupComponent<Div> addressGroup = formView.getFieldGroup("address");
 * addressGroup.show();
 * List<IFormFieldComponent<Div>> fields = (List) addressGroup.getFieldsComponents();
 * for (IFormFieldComponent field : fields) {
 *     field.updateLabel(field.getFieldName().toUpperCase());
 * }
 * }</pre>
 *
 * @param <C> the type of group component (e.g., Div, Groupbox, Panel)
 * @author Dynamia Soluciones IT
 */
public interface IFormFieldGroupComponent<C> extends Serializable {

    /**
     * Gets the name of the group.
     * <p>
     * The group name typically corresponds to the logical section name defined
     * in the view descriptor.
     * </p>
     *
     * @return the group name
     */
    String getGroupName();

    /**
     * Gets the container component that holds all fields in this group.
     * <p>
     * This is the UI component that wraps all field components, such as a
     * div, groupbox, or panel.
     * </p>
     *
     * @return the group component
     */
    C getGroupComponent();

    /**
     * Gets the list of field components contained in this group.
     * <p>
     * Each field component represents an individual form field within the group.
     * </p>
     *
     * @return the list of field components
     */
    List<? extends IFormFieldComponent<C>> getFieldsComponents();

    /**
     * Hides the group component and all its contained fields.
     * <p>
     * This operation makes the entire group invisible to the user but keeps it
     * in the component tree for potential later display.
     * </p>
     */
    void hide();

    /**
     * Shows the group component and all its contained fields.
     * <p>
     * This operation makes the group visible to the user if it was previously hidden.
     * </p>
     */
    void show();

    /**
     * Removes the group component and all its contained fields from the form.
     * <p>
     * This operation permanently removes the group from the view's component tree.
     * </p>
     */
    void remove();
}
