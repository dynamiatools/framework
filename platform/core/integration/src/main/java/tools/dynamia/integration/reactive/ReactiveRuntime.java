package tools.dynamia.integration.reactive;

final class ReactiveRuntime {

    private static final ThreadLocal<Runnable> CURRENT_EFFECT = new ThreadLocal<>();

    static void runEffect(Runnable effect) {
        try {
            CURRENT_EFFECT.set(effect);
            effect.run();
        } finally {
            CURRENT_EFFECT.remove();
        }
    }

    static Runnable currentEffect() {
        return CURRENT_EFFECT.get();
    }

    private ReactiveRuntime() {}
}
