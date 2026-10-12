package tools.dynamia.actions.replay;

import java.util.Map;

/**
 * Adds the headless implementation of a UI port to the {@link ReplayUIEnvironment} of a pass. Modules that own the
 * knowledge a port needs (for example {@code crud}, which knows how to fill an entity from submitted values) register one
 * as a bean; {@link ReplayExecutor} asks all of them for every pass.
 * <pre>{@code
 * public Map<Class<?>, Object> ports(ReplaySession session) {
 *     return Map.of(ViewsProvider.class, new HeadlessViews(session));
 * }
 * }</pre>
 */
public interface ReplayPortContributor {

    /**
     * @param session the session of the pass the ports are created for
     * @return the ports to add, by SPI type; a contributed SPI replaces the built-in one of the same type
     */
    Map<Class<?>, Object> ports(ReplaySession session);
}
