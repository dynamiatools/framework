package tools.dynamia.integration.reactive;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * A reactive reference that holds a mutable value.
 *
 * <p>When the value changes (via {@link #set(Object)}), all dependent effects and
 * computed values are automatically notified and re-executed.</p>
 *
 * <p>Refs track their dependencies through the internal signal system. When a ref's
 * value is read during effect or computed execution, that effect/computed is registered
 * as a subscriber.</p>
 *
 * Example:
 * <pre>{@code
 * Ref<String> message = Reactive.ref("Hello");
 *
 * Reactive.effect(() -> {
 *     System.out.println(message.get());
 * }); // Prints: "Hello"
 *
 * message.set("World"); // Prints: "World"
 * }</pre>
 *
 * @param <T> the type of value stored in this reference
 *
 * @since 5.0.0
 */
public final class Ref<T> {

    private T value;
    private final Signal signal;

    /**
     * Creates a new reactive reference with the specified initial value.
     *
     * @param initialValue the initial value to store
     */
    Ref(T initialValue) {
        this.value = initialValue;
        this.signal = new Signal();
    }

    /**
     * Gets the current value of this reference.
     * If called within an effect or computed context, this ref becomes a dependency
     * of that context.
     *
     * @return the current value
     */
    public T get() {
        signal.track();
        return value;
    }

    /**
     * Sets a new value for this reference.
     * If the new value differs from the current value (determined by {@link Objects#equals}),
     * all dependent effects and computed values are triggered.
     *
     * @param newValue the new value to set
     */
    public void set(T newValue) {
        if (Objects.equals(value, newValue)) {
            return;
        }
        this.value = newValue;
        signal.trigger();
    }

    /**
     * Updates the value by applying a transformation function to the current value.
     * This is useful for updating complex objects or performing operations based on
     * the previous value.
     *
     * @param updater function that takes the current value and returns the new value
     *
     * Example:
     * <pre>{@code
     * Ref<Integer> count = Reactive.ref(0);
     * count.update(n -> n + 1); // Increments by 1
     * }</pre>
     */
    public void update(UnaryOperator<T> updater) {
        set(updater.apply(value));
    }

    /**
     * Returns a string representation of the current value.
     * Note: This does NOT track dependencies.
     *
     * @return string representation of the value
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
