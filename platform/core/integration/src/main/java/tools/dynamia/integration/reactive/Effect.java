package tools.dynamia.integration.reactive;

/**
 * Utility class for creating reactive side effects.
 *
 * <p>Effects are functions that run automatically when their reactive dependencies change.
 * They are useful for performing side effects such as logging, UI updates, or API calls
 * in response to state changes.</p>
 *
 * <p>Effects run immediately upon creation and re-run whenever any reactive value
 * accessed within them changes.</p>
 *
 * Example:
 * <pre>{@code
 * Ref<String> status = Reactive.ref("idle");
 *
 * Effect.effect(() -> {
 *     System.out.println("Status changed to: " + status.get());
 * }); // Immediately prints: "Status changed to: idle"
 *
 * status.set("loading"); // Prints: "Status changed to: loading"
 * }</pre>
 *
 * @since 5.0.0
 */
public final class Effect {

    /**
     * Creates and executes a reactive effect that automatically re-runs when dependencies change.
     * The effect runs immediately once and then re-runs whenever any reactive value
     * accessed within it is modified.
     *
     * @param runnable the effect function to execute
     *
     * Example:
     * <pre>{@code
     * Ref<Integer> counter = Reactive.ref(0);
     *
     * Effect.effect(() -> {
     *     if (counter.get() > 10) {
     *         System.out.println("Counter exceeded threshold!");
     *     }
     * });
     * }</pre>
     */
    public static void effect(Runnable runnable) {
        ReactiveRuntime.runEffect(runnable);
    }

    private Effect() {
        // Utility class - prevent instantiation
    }
}
