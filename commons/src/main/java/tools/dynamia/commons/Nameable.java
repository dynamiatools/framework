package tools.dynamia.commons;

/**
 * The Interface Nameable. Represents objects that can provide a name representation.
 *
 * @author Mario A. Serrano Leones
 */
public interface Nameable {

    /**
     * Returns the name representation of this object.
     *
     * @return the name as string
     */
    default String toName() {
        return toString();
    }

    /**
     * Sets the name of this object.
     *
     * @param name the name to set
     */
    default void name(String name) {

    }
}
