package tools.dynamia.integration.reactive;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal signal mechanism for tracking and notifying dependencies.
 *
 * <p>Each reactive value (Ref) has an associated Signal that manages its subscribers
 * (effects and computed values that depend on it). When the value changes, the signal
 * triggers all registered subscribers.</p>
 *
 * <p>This class uses a thread-safe collection to support concurrent access patterns.</p>
 *
 * @since 5.0.0
 */
final class Signal {

    private final Set<Runnable> subscribers = ConcurrentHashMap.newKeySet();

    /**
     * Tracks the current effect as a dependency.
     * If an effect is currently executing, it's added to the subscribers set.
     * This is called when a reactive value is accessed.
     */
    void track() {
        var effect = ReactiveRuntime.currentEffect();
        if (effect != null) {
            subscribers.add(effect);
            ReactiveRuntime.trackSignal(this);
        }
    }

    /**
     * Removes an effect from the subscribers set.
     * This is called during effect cleanup to prevent memory leaks.
     *
     * @param effect the effect to unsubscribe
     */
    void untrack(Runnable effect) {
        subscribers.remove(effect);
    }

    /**
     * Triggers all subscribed effects to re-run.
     * This is called when a reactive value changes.
     */
    void trigger() {
        // Create a copy to avoid concurrent modification
        Set<Runnable> effectsToRun = Set.copyOf(subscribers);
        for (var effect : effectsToRun) {
            effect.run();
        }
    }
}
