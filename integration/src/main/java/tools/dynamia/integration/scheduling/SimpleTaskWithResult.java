package tools.dynamia.integration.scheduling;

import java.util.function.Supplier;

/**
 * Simple task with result
 *
 * @param <R> Result type
 */
public class SimpleTaskWithResult<R> extends TaskWithResult<R> {

    private final Supplier<R> supplier;

    public SimpleTaskWithResult(Supplier<R> supplier) {
        this.supplier = supplier;
    }

    public SimpleTaskWithResult(String name, Supplier<R> supplier) {
        super(name);
        this.supplier = supplier;
    }

    @Override
    public R doWorkWithResult() {
        return supplier.get();
    }
}
