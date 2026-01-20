package tools.dynamia.integration.reactive;

import java.util.LinkedHashSet;
import java.util.Set;

final class Signal {

    private final Set<Runnable> subscribers = new LinkedHashSet<>();

    void track() {
        var effect = ReactiveRuntime.currentEffect();
        if (effect != null) {
            subscribers.add(effect);
        }
    }

    void trigger() {
        for (var effect : subscribers) {
            effect.run();
        }
    }
}
