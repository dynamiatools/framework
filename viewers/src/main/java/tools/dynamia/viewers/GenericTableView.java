package tools.dynamia.viewers;


import java.util.List;

/**
 * Generic interface to implement Table Views
 *
 * @param <T>
 */
public interface GenericTableView<T> extends DataSetView<List<T>> {

    /**
     * Find and get a table field using filed name and row index
     *
     * @param fieldName
     * @param rowIndex
     * @param <C>
     * @return
     */
    <C> GenericTableFieldComponent<C> getTableFieldComponent(String fieldName, int rowIndex);

    /**
     * Find and get a table field using filed name and item component
     *
     * @param fieldName
     * @param item
     * @param <C>
     * @return
     */
    <C> GenericTableFieldComponent<C> getTableFieldComponent(String fieldName, Object item);

    /**
     * Clear table
     */
    void clear();

    /**
     * Show row numbers
     *
     * @param showRowNumber
     */
    void setShowRowNumber(boolean showRowNumber);

    /**
     * check is row numbers is shown
     *
     * @return
     */
    boolean isShowRowNumber();

    /**
     * Check if item is selected
     *
     * @return
     */
    boolean isListitemSelected();

    /**
     * Order by attribute
     *
     * @return
     */
    String getOrderBy();

    /**
     * Update order by attribute
     *
     * @param orderBy
     */
    void setOrderBy(String orderBy);

    /**
     * Find a footer by field name
     *
     * @param fieldName
     * @return
     */
    GenericTableViewFooter getFooter(String fieldName);

    /**
     * Find a header by field name
     *
     * @param fieldName
     * @return
     */
    GenericTableViewHeader getHeader(String fieldName);


    /**
     * Update view
     */
    void updateUI();

    Object getSource();

    void setSource(Object source);

    int getMaxResults();

    void setMaxResults(int maxResults);

    boolean isProjection();

    void setProjection(boolean projection);

    boolean isReadonly();

    void setReadonly(boolean readonly);

    void updateSelectedItem();
}
