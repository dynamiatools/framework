package tools.dynamia.viewers;

import java.io.Serializable;

/**
 * Interface representing a table field component in a table view.
 * <p>
 * This interface defines the contract for components that represent individual fields
 * (columns) within a table row. Each table field component is associated with a specific
 * field name and provides access to its underlying UI component.
 * </p>
 * <p>
 * Table field components are used to access and manipulate individual cells in a table,
 * allowing for dynamic customization of cell content, styling, or behavior based on
 * row data or field properties.
 * </p>
 *
 * Example:
 * <pre>{@code
 * ITableFieldComponent<Label> statusField = tableView.getTableFieldComponent("status", 0);
 * Label label = statusField.getComponent();
 * label.setSclass("badge badge-success");
 * }</pre>
 *
 * @param <T> the type of UI component (e.g., Label, Textbox, Button)
 * @author Dynamia Soluciones IT
 */
public interface ITableFieldComponent<T> extends Serializable {

    /**
     * Gets the name of the field represented by this component.
     * <p>
     * The field name corresponds to the column name in the table view descriptor
     * and typically matches the property name of the displayed entity.
     * </p>
     *
     * @return the field name
     */
    String getFieldName();

    /**
     * Gets the UI component that represents this table field.
     * <p>
     * This method returns the actual UI component (e.g., Label, Textbox, Button)
     * that is rendered in the table cell.
     * </p>
     *
     * @return the UI component
     */
    T getComponent();
}
