package tools.dynamia.integration;

/**
 * An interface that combines ThreadLocalObjectAware and Cloneable.
 */
public interface CloneableThreadLocalObject extends ThreadLocalObjectAware, Cloneable {

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     */
    Object clone();
}
