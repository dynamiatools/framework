package tools.dynamia.viewers;


import java.util.List;

/**
 * Interface for table view components that display data in a tabular format.
 * <p>
 * This interface extends {@link DataSetView} to provide specialized behavior for table views,
 * including access to individual table cells, headers, footers, and table-specific operations
 * such as sorting, row numbering, and selection management.
 * </p>
 * <p>
 * Table view components are responsible for rendering collections of data as rows and columns,
 * providing rich interaction capabilities and customization options for each cell, header, and footer.
 * </p>
 *
 * Example:
 * <pre>{@code
 * TableViewComponent<Customer> tableView = ...;
 * tableView.setValue(customers);
 * tableView.setShowRowNumber(true);
 * tableView.setOrderBy("name");
 *
 * // Access a specific cell
 * ITableFieldComponent<Label> nameCell = tableView.getTableFieldComponent("name", 0);
 * Label label = nameCell.getComponent();
 * label.setSclass("highlight");
 * }</pre>
 *
 * @param <T> the type of items displayed in the table
 * @author Dynamia Soluciones IT
 */
public interface TableViewComponent<T> extends DataSetView<List<T>> {

    /**
     * Finds and gets a table field component using field name and row index.
     * <p>
     * This method allows access to individual table cells for customization or data retrieval.
     * </p>
     *
     * @param fieldName the name of the field (column)
     * @param rowIndex the zero-based index of the row
     * @param <C> the type of the cell component
     * @return the table field component for the specified cell
     */
    <C> ITableFieldComponent<C> getTableFieldComponent(String fieldName, int rowIndex);

    /**
     * Finds and gets a table field component using field name and item object.
     * <p>
     * This method allows access to table cells by specifying the data item displayed in the row.
     * </p>
     *
     * @param fieldName the name of the field (column)
     * @param item the data item representing the row
     * @param <C> the type of the cell component
     * @return the table field component for the specified cell
     */
    <C> ITableFieldComponent<C> getTableFieldComponent(String fieldName, Object item);

    /**
     * Clears all data from the table.
     * <p>
     * This method removes all rows from the table view, effectively resetting it to an empty state.
     * </p>
     */
    void clear();

    /**
     * Sets whether row numbers should be displayed.
     * <p>
     * When enabled, the table displays a column with sequential row numbers.
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
     * Checks if any row is currently selected in the table.
     *
     * @return {@code true} if a row is selected, {@code false} otherwise
     */
    boolean isListitemSelected();

    /**
     * Gets the current order-by attribute for table sorting.
     * <p>
     * This typically represents the field name used for sorting the table data.
     * </p>
     *
     * @return the order-by attribute, or {@code null} if no ordering is set
     */
    String getOrderBy();

    /**
     * Sets the order-by attribute for table sorting.
     * <p>
     * This method updates the sorting criteria for the table data.
     * </p>
     *
     * @param orderBy the field name to use for sorting
     */
    void setOrderBy(String orderBy);

    /**
     * Finds and returns a footer component by field name.
     * <p>
     * Table footers typically display aggregate information such as totals or counts.
     * </p>
     *
     * @param fieldName the name of the field (column)
     * @return the footer component for the specified field
     */
    TableViewFooterComponent getFooter(String fieldName);

    /**
     * Finds and returns a header component by field name.
     * <p>
     * Table headers display column titles and may include sorting controls.
     * </p>
     *
     * @param fieldName the name of the field (column)
     * @return the header component for the specified field
     */
    TableViewHeaderComponent getHeader(String fieldName);

    /**
     * Updates the table UI to reflect changes in the data or configuration.
     * <p>
     * This method forces the table to refresh its display.
     * </p>
     */
    void updateUI();

    /**
     * Gets the source object associated with this table view.
     * <p>
     * The source may represent the data provider or query used to populate the table.
     * </p>
     *
     * @return the source object
     */
    Object getSource();

    /**
     * Sets the source object for this table view.
     *
     * @param source the source object
     */
    void setSource(Object source);

    /**
     * Gets the maximum number of results (rows) to display in the table.
     *
     * @return the maximum number of results
     */
    int getMaxResults();

    /**
     * Sets the maximum number of results (rows) to display in the table.
     * <p>
     * This can be used to implement pagination or limit the table size.
     * </p>
     *
     * @param maxResults the maximum number of results
     */
    void setMaxResults(int maxResults);

    /**
     * Checks if the table is using projection mode.
     * <p>
     * Projection mode typically indicates that only specific fields are loaded from the data source.
     * </p>
     *
     * @return {@code true} if projection mode is enabled, {@code false} otherwise
     */
    boolean isProjection();

    /**
     * Sets whether the table should use projection mode.
     *
     * @param projection {@code true} to enable projection mode, {@code false} to disable
     */
    void setProjection(boolean projection);

    /**
     * Checks if the table is in read-only mode.
     * <p>
     * Read-only mode prevents user modifications to the table data.
     * </p>
     *
     * @return {@code true} if the table is read-only, {@code false} otherwise
     */
    boolean isReadonly();

    /**
     * Sets whether the table should be read-only.
     *
     * @param readonly {@code true} to make the table read-only, {@code false} to allow editing
     */
    void setReadonly(boolean readonly);

    /**
     * Updates the UI to reflect changes to the currently selected item.
     * <p>
     * This method is useful when the selected item's data has been modified externally.
     * </p>
     */
    void updateSelectedItem();
}
