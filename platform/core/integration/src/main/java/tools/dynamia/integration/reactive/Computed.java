package tools.dynamia.integration.reactive;

import java.util.function.Supplier;

/**
 * A computed reactive value that automatically updates when its dependencies change.
 *
 * <p>Computed values are derived from other reactive values (refs or other computed values).
 * They cache their result and only recompute when one of their dependencies changes.</p>
 *
 * <p>Computed values are lazily evaluated - the supplier function is executed once on
 * creation and then only when dependencies change.</p>
 *
 * Example:
 * <pre>{@code
 * Ref<Integer> price = Reactive.ref(100);
 * Ref<Double> taxRate = Reactive.ref(0.15);
 *
 * Computed<Double> totalPrice = Reactive.computed(() ->
 *     price.get() * (1 + taxRate.get())
 * );
 *
 * System.out.println(totalPrice.get()); // 115.0
 * price.set(200);
 * System.out.println(totalPrice.get()); // 230.0
 * }</pre>
 *
 * @param <T> the type of computed value
 *
 * @since 5.0.0
 */
public final class Computed<T> {

    private final Ref<T> ref;

    /**
     * Creates a new computed value with the given supplier function.
     * The supplier is executed immediately to compute the initial value.
     *
     * @param supplier function that computes the value based on reactive dependencies
     */
    private Computed(Supplier<T> supplier) {
        this.ref = new Ref<>(supplier.get());
        Effect.effect(() -> ref.set(supplier.get()));
    }

    /**
     * Factory method to create a computed value.
     *
     * @param <T> the type of computed value
     * @param supplier function that computes the value
     * @return a new computed reactive value
     *
     * Example:
     * <pre>{@code
     * Ref<String> firstName = Reactive.ref("John");
     * Ref<String> lastName = Reactive.ref("Doe");
     *
     * Computed<String> fullName = Computed.of(() ->
     *     firstName.get() + " " + lastName.get()
     * );
     * }</pre>
     */
    public static <T> Computed<T> of(Supplier<T> supplier) {
        return new Computed<>(supplier);
    }

    /**
     * Gets the current computed value.
     * If called within an effect or another computed context, this computed value
     * becomes a dependency of that context.
     *
     * @return the current computed value
     */
    public T get() {
        return ref.get();
    }

    /**
     * Returns a string representation of the current computed value.
     *
     * @return string representation of the value
     */
    @Override
    public String toString() {
        return ref.toString();
    }
}
