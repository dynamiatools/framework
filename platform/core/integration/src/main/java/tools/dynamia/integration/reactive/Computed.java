package tools.dynamia.integration.reactive;

import java.util.function.Supplier;

public final class Computed<T> {

    private final Ref<T> ref;

    private Computed(Supplier<T> supplier) {
        this.ref = new Ref<>(supplier.get());
        Effect.effect(() -> ref.set(supplier.get()));
    }

    public static <T> Computed<T> of(Supplier<T> supplier) {
        return new Computed<>(supplier);
    }

    public T get() {
        return ref.get();
    }

    @Override
    public String toString() {
        return ref.toString();
    }
}
