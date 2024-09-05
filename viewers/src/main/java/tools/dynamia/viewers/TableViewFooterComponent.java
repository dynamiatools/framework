package tools.dynamia.viewers;

import java.io.Serializable;

/**
 * Generic Interface for Table View Footer
 */
public interface TableViewFooterComponent extends Serializable {

    Object getValue();

    void setValue(Object value);

    void clear();

    Field getField();

    void setField(Field field);

}
