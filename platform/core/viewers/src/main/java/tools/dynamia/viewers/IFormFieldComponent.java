package tools.dynamia.viewers;

import java.io.Serializable;

/**
 * Interface representing a form field component in a view.
 * <p>
 * This interface defines the contract for components that represent individual form fields,
 * including their input components, labels, and visibility management. Form field components
 * are typically used within form views to render and manage user input for specific fields.
 * </p>
 * <p>
 * Implementations provide access to the main input component, an optional alternative input
 * component, and the associated label. They also support operations such as showing, hiding,
 * and removing the field from the form.
 * </p>
 *
 * Example:
 * <pre>{@code
 * IFormFieldComponent<Textbox> nameField = formView.getFieldComponent("name");
 * nameField.updateLabel("Full Name:");
 * nameField.getInputComponent().setPlaceholder("Enter your name");
 * nameField.show();
 * }</pre>
 *
 * @param <C> the type of input component (e.g., Textbox, Datebox, Combobox)
 * @author Dynamia Soluciones IT
 */
public interface IFormFieldComponent<C> extends Serializable {

    /**
     * Gets the name of the field.
     * <p>
     * The field name typically corresponds to the property name of the domain object
     * being edited in the form.
     * </p>
     *
     * @return the field name
     */
    String getFieldName();

    /**
     * Gets the main input component for this field.
     * <p>
     * This is the primary UI component used for user input, such as a textbox,
     * datebox, or combobox.
     * </p>
     *
     * @return the input component
     */
    C getInputComponent();

    /**
     * Gets the label component associated with this field.
     * <p>
     * The label typically displays the field name or description to the user.
     * </p>
     *
     * @return the label component
     */
    LabelComponent getLabel();

    /**
     * Gets an alternative input component for this field.
     * <p>
     * Some field implementations may provide an alternative input mechanism,
     * such as a button to open a selection dialog alongside a text input.
     * </p>
     *
     * @return the alternative input component, or {@code null} if not available
     */
    C getInputComponentAlt();

    /**
     * Updates the label text for this field.
     * <p>
     * This method allows dynamic modification of the field's label display text.
     * </p>
     *
     * @param label the new label text
     */
    void updateLabel(String label);

    /**
     * Removes the field component from the form.
     * <p>
     * This operation permanently removes the field from the view's component tree.
     * </p>
     */
    void remove();

    /**
     * Hides the field component.
     * <p>
     * This operation makes the field invisible to the user but keeps it in the
     * component tree for potential later display.
     * </p>
     */
    void hide();

    /**
     * Shows the field component.
     * <p>
     * This operation makes the field visible to the user if it was previously hidden.
     * </p>
     */
    void show();


}
