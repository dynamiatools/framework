package tools.dynamia.integration.scheduling;

import tools.dynamia.commons.Callback;

/**
 * Simple task that execute a callback
 */
public class SimpleTask extends Task {

    private final Callback callback;

    public SimpleTask(Callback callback) {
        this.callback = callback;
    }

    public SimpleTask(String name, Callback callback) {
        super(name);
        this.callback = callback;
    }

    @Override
    public void doWork() {
        callback.doSomething();
    }
}
