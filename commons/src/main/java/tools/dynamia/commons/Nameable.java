package tools.dynamia.commons;

/**
 * The Interface Nameable. Represents objects that can provide a name representation.
 * This interface is commonly used by entities, components, and configuration objects that need
 * to expose a human-readable name. It provides a standardized way to get and set displayable
 * names for objects, particularly useful in UI components, logging, and serialization scenarios.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * public class Category implements Nameable {
 *     private String name;
 *     
 *     public String toName() {
 *         return name != null ? name : "Unnamed Category";
 *     }
 *     
 *     public void name(String name) {
 *         this.name = name;
 *     }
 * }
 * </code>
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
