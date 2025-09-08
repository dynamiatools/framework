package tools.dynamia.viewers;

import java.io.Serializable;

/**
 * Interface representing a form field component.
 *
 * @param <C> the type of input component
 */
public interface IFormFieldComponent<C> extends Serializable {

    /**
     * Gets the name of the field.
     *
     * @return the field name
     */
    String getFieldName();

    /**
     * Gets the main input component.
     *
     * @return the input component
     */
    C getInputComponent();

    /**
     * Gets the label component associated with the field.
     *
     * @return the label component
     */
    LabelComponent getLabel();

    /**
     * Gets an alternative input component.
     *
     * @return the alternative input component
     */
    C getInputComponentAlt();

    /**
     * Updates the label text.
     *
     * @param label the new label text
     */
    void updateLabel(String label);

    /**
     * Removes the field component.
     */
    void remove();

    /**
     * Hides the field component.
     */
    void hide();

    /**
     * Shows the field component.
     */
    void show();


}
