package tools.dynamia.viewers;

/**
 * Interface representing a tree field component in a tree view.
 * <p>
 * This interface defines the contract for components that represent individual fields
 * within a tree node. Each tree field component is associated with a specific field name
 * and provides access to its underlying UI component.
 * </p>
 * <p>
 * Tree field components are used to access and manipulate individual cells in tree nodes,
 * allowing for dynamic customization of node content, styling, or behavior based on
 * node data or field properties.
 * </p>
 *
 * Example:
 * <pre>{@code
 * ITreeFieldComponent<Label> nameField = treeView.getTreeFieldComponent("name", node);
 * Label label = nameField.getComponent();
 * label.setStyle("font-weight: bold");
 * }</pre>
 *
 * @param <T> the type of UI component (e.g., Label, Textbox, Image)
 * @author Dynamia Soluciones IT
 */
public interface ITreeFieldComponent<T> {

    /**
     * Gets the name of the field represented by this component.
     * <p>
     * The field name corresponds to the column name in the tree view descriptor
     * and typically matches the property name of the displayed entity.
     * </p>
     *
     * @return the field name
     */
    String getFieldName();

    /**
     * Gets the UI component that represents this tree field.
     * <p>
     * This method returns the actual UI component (e.g., Label, Textbox, Image)
     * that is rendered in the tree node cell.
     * </p>
     *
     * @return the UI component
     */
    T getComponent();
}
