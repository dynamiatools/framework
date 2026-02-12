package tools.dynamia.viewers;

import java.io.Serializable;

/**
 * Interface representing a generic label component in a view.
 * <p>
 * This interface defines the contract for label components that display text to users.
 * It provides methods to manage the label's text value, tooltip, styling, and visibility.
 * </p>
 * <p>
 * Label components are typically used to display field names, descriptions, or static
 * text within forms, tables, or other views. Implementations may wrap framework-specific
 * label components to provide a unified interface.
 * </p>
 *
 * Example:
 * <pre>{@code
 * LabelComponent label = field.getLabel();
 * label.setValue("Customer Name:");
 * label.setTooltiptext("Enter the full name of the customer");
 * label.setSclass("required-field");
 * }</pre>
 *
 * @author Dynamia Soluciones IT
 */
public interface LabelComponent extends Serializable {

    /**
     * Gets the text value displayed by the label.
     *
     * @return the label text
     */
    String getValue();

    /**
     * Sets the text value to be displayed by the label.
     *
     * @param value the label text
     */
    void setValue(String value);

    /**
     * Sets the tooltip text that appears when the user hovers over the label.
     *
     * @param tooltiptext the tooltip text
     */
    void setTooltiptext(String tooltiptext);

    /**
     * Gets the tooltip text associated with the label.
     *
     * @return the tooltip text
     */
    String getTooltiptext();

    /**
     * Sets the CSS style class for the label.
     * <p>
     * This allows customization of the label's appearance using CSS classes.
     * </p>
     *
     * @param sclass the CSS style class
     */
    void setSclass(String sclass);

    /**
     * Gets the CSS style class applied to the label.
     *
     * @return the CSS style class
     */
    String getSclass();

    /**
     * Sets the visibility of the label.
     *
     * @param visible {@code true} to make the label visible, {@code false} to hide it
     */
    void setVisible(boolean visible);

    /**
     * Checks if the label is visible.
     *
     * @return {@code true} if the label is visible, {@code false} otherwise
     */
    boolean isVisible();
}
