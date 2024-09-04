package tools.dynamia.viewers;


/**
 * Generic interface to implement Table Views
 *
 * @param <T>
 */
public interface TreeViewComponent<T> extends DataSetView<T> {


    /**
     * Find and get a table field using filed name and item component
     *
     * @param fieldName
     * @param item
     * @param <C>
     * @return
     */
    <C> ITreeFieldComponent<C> getTreeFieldComponent(String fieldName, Object item);

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


    Object getSource();

    void setSource(Object source);
}
