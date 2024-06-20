package tools.dynamia.viewers;

/**
 * Generic Interface for Table View Footer
 */
public interface GenericTableViewFooter {

    Object getValue();

    void setValue(Object value);

    void clear();

    Field getField();

    void setField(Field field);

}
