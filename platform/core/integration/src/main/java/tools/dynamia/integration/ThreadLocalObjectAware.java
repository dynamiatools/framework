package tools.dynamia.integration;

/**
 * Marker interface to indicate that an object is aware of ThreadLocalObjectContainer.
 *
 * @deprecated still honoured (adapter in {@code ObjectsContext#capture()}), but new code should use a bean that carries state with {@link tools.dynamia.integration.context.ContextCapturer}, or bind objects with {@link tools.dynamia.integration.context.ObjectsContext#with(Object...)}.
 */
@Deprecated
public interface ThreadLocalObjectAware {
}
