package tools.dynamia.actions;

/**
 * Marks an {@link Action} written against the UI-neutral ports ({@code UIMessages}, {@code CrudControllerAPI},
 * {@code CrudViewComponent}) and nothing UI specific (no ZK, no browser): the same class can run in a ZK desktop and
 * in a headless runtime that serves a REST client, which answers the questions of the action with the replay
 * protocol of {@link tools.dynamia.actions.replay.ReplayExecutor}.
 * <p>
 * <p>
 * <b>Deprecated:</b> declare {@code @RunsOn(ActionRuntime.HEADLESS)} on the concrete class instead. While this marker exists it
 * counts only when the concrete class lists it in its own {@code implements}; it is never inherited, and
 * {@link #headlessSupported()} still works as a dynamic veto.
 * <p>
 * An action that implements it must keep the code that runs <em>before</em> an interaction free of side effects that
 * are not database writes: in a headless run the action is executed again from the start every time the user
 * answers, and only the last run is committed.
 */
@Deprecated
public interface HeadlessCapable {

    /**
     * Whether this class, and not only a superclass it inherits the marker from, runs headless. A subclass that adds
     * something that needs a screen ({@code SaveAndEditAction} opens the form again) answers {@code false}.
     *
     * @return {@code true} by default
     */
    default boolean headlessSupported() {
        return true;
    }

    /**
     * Id of the action for REST clients. By default the class name without the {@code Action} suffix, in lower case
     * ({@code DeleteAction} becomes {@code delete}), which is what the Vue CRUD looks for.
     *
     * @return the id
     */
    default String headlessId() {
        return ActionRuntimes.headlessId(getClass());
    }
}
