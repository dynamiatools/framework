package testapp;

import java.util.function.Supplier;

/**
 * Stands for application code (outside the framework packages) that opens a session with no account bound, so the
 * call site reported by the resolver can be asserted.
 */
public final class TenantlessCaller {

    private TenantlessCaller() {
    }

    /**
     * Runs the work as the application code that forgot to bind an account.
     *
     * @param work what asks the resolver for the tenant
     * @param <T>  the result type
     * @return what the work returns
     */
    public static <T> T run(Supplier<T> work) {
        return work.get();
    }
}
