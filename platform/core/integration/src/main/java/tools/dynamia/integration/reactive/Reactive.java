  package tools.dynamia.integration.reactive;

import java.util.function.Supplier;

public final class Reactive {

    public static <T> Ref<T> ref(T initialValue) {
        return new Ref<>(initialValue);
    }

    public static void effect(Runnable runnable) {
        Effect.effect(runnable);
    }

    public static <T> Computed<T> computed(Supplier<T> supplier) {
        return Computed.of(supplier);
    }

    private Reactive() {}
}
