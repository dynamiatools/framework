package tools.dynamia.commons;

import java.util.concurrent.atomic.AtomicReference;

public class AtomicString extends AtomicReference<String> {

    public static AtomicString of(String string) {
        return new AtomicString(string);
    }

    public static AtomicString empty() {
        return new AtomicString("");
    }


    public AtomicString() {
        super(null);
    }

    public AtomicString(String initialValue) {
        super(initialValue);
    }

    public String append(String string) {
        return updateAndGet(current -> current == null ? string : current + string);
    }
}