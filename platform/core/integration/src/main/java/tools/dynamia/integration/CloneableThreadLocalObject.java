package tools.dynamia.integration;

/**
 * An interface that combines ThreadLocalObjectAware and Cloneable.
 *
 * @deprecated still honoured (adapter in {@code ObjectsContext#capture()}), but new code should use a {@link tools.dynamia.integration.context.ContextCapturer} that captures an immutable snapshot.
 */
@Deprecated
public interface CloneableThreadLocalObject extends ThreadLocalObjectAware, Cloneable {

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     */
    Object clone();
}
