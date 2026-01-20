package tools.dynamia.integration.reactive;

public final class Effect {

    public static void effect(Runnable runnable) {
        ReactiveRuntime.runEffect(runnable);
    }

    private Effect() {}
}
