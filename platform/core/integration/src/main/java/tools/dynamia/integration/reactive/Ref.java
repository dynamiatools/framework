package tools.dynamia.integration.reactive;

import java.util.Objects;

public final class Ref<T> {

    private T value;
    private final Signal signal;

    Ref(T initialValue) {
        this.value = initialValue;
        this.signal = new Signal();
    }

    public T get() {
        signal.track();
        return value;
    }

    public void set(T newValue) {
        if (Objects.equals(value, newValue)) {
            return;
        }
        this.value = newValue;
        signal.trigger();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
