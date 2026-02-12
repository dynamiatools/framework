package tools.dynamia.viewers;

import java.io.Serializable;

/**
 * Generic interface for table view header
 */
public interface TableViewHeaderComponent extends Serializable {

    Field getField();

    void setField(Field field);

    String getLabel();

    void setLabel(String label);


}
