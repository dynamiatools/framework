package tools.dynamia.viewers;

import java.io.Serializable;

/**
 * Interface representing a header component for a table view column.
 * <p>
 * This interface defines the contract for header components that display column titles
 * and metadata at the top of table columns. Headers typically show the field name or label
 * and may include interactive elements such as sorting controls.
 * </p>
 * <p>
 * Header components are associated with specific fields (columns) and can be customized
 * to change their display text or behavior.
 * </p>
 *
 * Example:
 * <pre>{@code
 * TableViewHeaderComponent header = tableView.getHeader("customerName");
 * header.setLabel("Customer Full Name");
 * }</pre>
 *
 * @author Dynamia Soluciones IT
 */
public interface TableViewHeaderComponent extends Serializable {

    /**
     * Gets the field descriptor associated with this header.
     *
     * @return the field descriptor
     */
    Field getField();

    /**
     * Sets the field descriptor for this header.
     *
     * @param field the field descriptor
     */
    void setField(Field field);

    /**
     * Gets the label text displayed in the header.
     *
     * @return the header label text
     */
    String getLabel();

    /**
     * Sets the label text to be displayed in the header.
     * <p>
     * This allows customization of the column title shown to users.
     * </p>
     *
     * @param label the header label text
     */
    void setLabel(String label);


}
