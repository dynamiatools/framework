  package tools.dynamia.integration.reactive;

import java.util.function.Supplier;

/**
 * Main entry point for the reactive programming system.
 * Provides a React/Vue-inspired reactive framework for Java applications.
 *
 * <p>This framework allows you to create reactive data structures where changes
 * automatically propagate to dependent computations and side effects.</p>
 *
 * <p>Key concepts:</p>
 * <ul>
 *   <li><b>Ref:</b> A reactive reference that holds a mutable value</li>
 *   <li><b>Computed:</b> A derived value that automatically updates when its dependencies change</li>
 *   <li><b>Effect:</b> A side effect that automatically re-runs when its dependencies change</li>
 * </ul>
 *
 * Example usage:
 * <pre>{@code
 * // Create reactive references
 * Ref<Integer> count = Reactive.ref(0);
 * Ref<Integer> multiplier = Reactive.ref(2);
 *
 * // Create computed value that depends on both refs
 * Computed<Integer> doubled = Reactive.computed(() -> count.get() * multiplier.get());
 *
 * // Create effect that runs when dependencies change
 * Reactive.effect(() -> {
 *     System.out.println("Doubled value: " + doubled.get());
 * });
 *
 * // Changing count triggers the effect
 * count.set(5); // Prints: "Doubled value: 10"
 * }</pre>
 *
 * @since 5.0.0
 */
public final class Reactive {

    /**
     * Creates a reactive reference with an initial value.
     * Changes to the reference will trigger all dependent effects and computed values.
     *
     * @param <T> the type of value stored in the reference
     * @param initialValue the initial value to store
     * @return a new reactive reference
     *
     * Example:
     * <pre>{@code
     * Ref<String> name = Reactive.ref("John");
     * System.out.println(name.get()); // "John"
     * name.set("Jane");
     * System.out.println(name.get()); // "Jane"
     * }</pre>
     */
    public static <T> Ref<T> ref(T initialValue) {
        return new Ref<>(initialValue);
    }

    /**
     * Creates a side effect that automatically re-runs when its reactive dependencies change.
     * The effect runs immediately once upon creation and then re-runs whenever any
     * reactive value accessed within it changes.
     *
     * @param runnable the effect function to execute
     *
     * Example:
     * <pre>{@code
     * Ref<Integer> count = Reactive.ref(0);
     *
     * Reactive.effect(() -> {
     *     System.out.println("Count is: " + count.get());
     * }); // Immediately prints: "Count is: 0"
     *
     * count.set(1); // Prints: "Count is: 1"
     * }</pre>
     */
    public static void effect(Runnable runnable) {
        Effect.effect(runnable);
    }

    /**
     * Creates a computed value that automatically updates when its reactive dependencies change.
     * The supplier function is re-executed whenever any reactive value accessed within it changes.
     *
     * @param <T> the type of value computed
     * @param supplier the function that computes the value
     * @return a new computed reactive value
     *
     * Example:
     * <pre>{@code
     * Ref<Integer> width = Reactive.ref(10);
     * Ref<Integer> height = Reactive.ref(20);
     *
     * Computed<Integer> area = Reactive.computed(() -> width.get() * height.get());
     *
     * System.out.println(area.get()); // 200
     * width.set(15);
     * System.out.println(area.get()); // 300
     * }</pre>
     */
    public static <T> Computed<T> computed(Supplier<T> supplier) {
        return Computed.of(supplier);
    }

    private Reactive() {
        // Utility class - prevent instantiation
    }
}
