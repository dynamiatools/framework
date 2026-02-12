package tools.dynamia.viewers;


/**
 * Interface for tree view components that display hierarchical data.
 * <p>
 * This interface extends {@link DataSetView} to provide specialized behavior for tree views,
 * including access to individual tree node cells, row numbering, and tree-specific operations
 * such as sorting and selection management.
 * </p>
 * <p>
 * Tree view components are responsible for rendering hierarchical data structures where items
 * can have parent-child relationships, providing rich interaction capabilities and customization
 * options for each node cell.
 * </p>
 *
 * Example:
 * <pre>{@code
 * TreeViewComponent<Category> treeView = ...;
 * treeView.setValue(categories);
 * treeView.setShowRowNumber(true);
 * treeView.setOrderBy("name");
 *
 * // Access a specific tree node cell
 * ITreeFieldComponent<Label> nameCell = treeView.getTreeFieldComponent("name", categoryNode);
 * Label label = nameCell.getComponent();
 * label.setStyle("font-weight: bold");
 * }</pre>
 *
 * @param <T> the type of items displayed in the tree
 * @author Dynamia Soluciones IT
 */
public interface TreeViewComponent<T> extends DataSetView<T> {


    /**
     * Finds and gets a tree field component using field name and item object.
     * <p>
     * This method allows access to individual tree node cells for customization or data retrieval.
     * </p>
     *
     * @param fieldName the name of the field (column)
     * @param item the data item representing the tree node
     * @param <C> the type of the cell component
     * @return the tree field component for the specified cell
     */
    <C> ITreeFieldComponent<C> getTreeFieldComponent(String fieldName, Object item);

    /**
     * Clears all data from the tree.
     * <p>
     * This method removes all nodes from the tree view, effectively resetting it to an empty state.
     * </p>
     */
    void clear();

    /**
     * Sets whether row numbers should be displayed.
     * <p>
     * When enabled, the tree displays a column with sequential row numbers for each visible node.
     * </p>
     *
     * @param showRowNumber {@code true} to show row numbers, {@code false} to hide them
     */
    void setShowRowNumber(boolean showRowNumber);

    /**
     * Checks if row numbers are currently shown.
     *
     * @return {@code true} if row numbers are shown, {@code false} otherwise
     */
    boolean isShowRowNumber();


    /**
     * Gets the current order-by attribute for tree node sorting.
     * <p>
     * This typically represents the field name used for sorting the tree nodes at each level.
     * </p>
     *
     * @return the order-by attribute, or {@code null} if no ordering is set
     */
    String getOrderBy();

    /**
     * Sets the order-by attribute for tree node sorting.
     * <p>
     * This method updates the sorting criteria for tree nodes at each level.
     * </p>
     *
     * @param orderBy the field name to use for sorting
     */
    void setOrderBy(String orderBy);

    /**
     * Gets the source object associated with this tree view.
     * <p>
     * The source may represent the data provider or query used to populate the tree.
     * </p>
     *
     * @return the source object
     */
    Object getSource();

    /**
     * Sets the source object for this tree view.
     *
     * @param source the source object
     */
    void setSource(Object source);
}
