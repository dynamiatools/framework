package tools.dynamia.viewers;

import java.io.Serializable;

/**
 * Interface representing a footer component for a table view column.
 * <p>
 * This interface defines the contract for footer components that display aggregate information
 * at the bottom of table columns, such as totals, counts, averages, or other summary data.
 * </p>
 * <p>
 * Footer components are associated with specific fields (columns) and can display calculated
 * values based on the data in that column.
 * </p>
 *
 * Example:
 * <pre>{@code
 * TableViewFooterComponent footer = tableView.getFooter("amount");
 * footer.setValue("Total: $1,234.56");
 * }</pre>
 *
 * @author Dynamia Soluciones IT
 */
public interface TableViewFooterComponent extends Serializable {

    /**
     * Gets the current value displayed in the footer.
     *
     * @return the footer value
     */
    Object getValue();

    /**
     * Sets the value to be displayed in the footer.
     * <p>
     * This is typically an aggregate value such as a sum, count, or average.
     * </p>
     *
     * @param value the footer value
     */
    void setValue(Object value);

    /**
     * Clears the footer content.
     * <p>
     * This method removes any displayed value from the footer.
     * </p>
     */
    void clear();

    /**
     * Gets the field descriptor associated with this footer.
     *
     * @return the field descriptor
     */
    Field getField();

    /**
     * Sets the field descriptor for this footer.
     *
     * @param field the field descriptor
     */
    void setField(Field field);

}
