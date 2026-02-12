package tools.dynamia.integration.reactive;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal runtime management for the reactive system.
 * Manages the current effect context using ThreadLocal to track which effect
 * is currently executing and maintains dependency relationships.
 *
 * <p>This class is internal and should not be used directly by application code.
 * It manages the dependency tracking mechanism that allows reactive values to
 * know which effects depend on them.</p>
 *
 * @since 5.0.0
 */
final class ReactiveRuntime {

    private static final ThreadLocal<EffectContext> CURRENT_CONTEXT = new ThreadLocal<>();
    private static final Set<Signal> ALL_SIGNALS = ConcurrentHashMap.newKeySet();

    /**
     * Internal context for tracking effect execution and dependencies.
     */
    private static class EffectContext {
        final Runnable effect;
        final Set<Signal> dependencies = ConcurrentHashMap.newKeySet();

        EffectContext(Runnable effect) {
            this.effect = effect;
        }
    }

    /**
     * Runs an effect within the reactive context.
     * Sets the current effect before execution and clears it afterwards.
     * This allows reactive values to track which effect is accessing them.
     *
     * @param effect the effect to run
     */
    static void runEffect(Runnable effect) {
        EffectContext oldContext = CURRENT_CONTEXT.get();
        EffectContext context = new EffectContext(effect);

        try {
            CURRENT_CONTEXT.set(context);

            // Clear old dependencies before running
            cleanupDependencies(effect);

            // Run the effect, which will track new dependencies
            effect.run();
        } finally {
            CURRENT_CONTEXT.set(oldContext);
        }
    }

    /**
     * Gets the currently executing effect, if any.
     * Used by reactive values to register themselves as dependencies.
     *
     * @return the current effect, or null if no effect is executing
     */
    static Runnable currentEffect() {
        EffectContext context = CURRENT_CONTEXT.get();
        return context != null ? context.effect : null;
    }

    /**
     * Registers a signal as being tracked by the current effect.
     *
     * @param signal the signal to register
     */
    static void trackSignal(Signal signal) {
        EffectContext context = CURRENT_CONTEXT.get();
        if (context != null) {
            context.dependencies.add(signal);
            ALL_SIGNALS.add(signal);
        }
    }

    /**
     * Cleans up dependencies for an effect before it re-runs.
     * This ensures that dynamic dependencies are properly tracked.
     *
     * @param effect the effect to cleanup
     */
    private static void cleanupDependencies(Runnable effect) {
        for (Signal signal : ALL_SIGNALS) {
            signal.untrack(effect);
        }
    }

    private ReactiveRuntime() {
        // Utility class - prevent instantiation
    }
}
