package tools.dynamia.actions.replay;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Runs one pass of a replayed action in a transaction that is only committed when {@code commit} says so.
 * <p>
 * A pass that ends waiting for the user must leave no trace, because the action will run again from the start when
 * the answer arrives. The application (the {@code app} module, with Spring) provides the implementation; without it
 * the passes run with no transaction, which is fine for actions that only talk to the user.
 */
public interface ReplayTransactions {

    /**
     * @param work   one pass of the action
     * @param commit asked after {@code work} returned: {@code true} commits, {@code false} rolls back. When
     *               {@code work} throws, the transaction is rolled back whatever it answers.
     * @param <T>    result of the pass
     * @return what {@code work} returned
     */
    <T> T run(Supplier<T> work, BooleanSupplier commit);

    /**
     * Runs {@code work} with the transaction of the pass suspended, so the work manages its own transactions. This is the
     * rule of a {@code UIProgress} task in ZK too: it does not run inside the transaction of whoever started it.
     *
     * @param work the work
     * @param <T>  its result
     * @return what {@code work} returns
     */
    default <T> T runOutside(Supplier<T> work) {
        return work.get();
    }

    /** @return the transactions of the application, or {@link #NONE} when there are none */
    static ReplayTransactions current() {
        var found = tools.dynamia.integration.Containers.get().findObject(ReplayTransactions.class);
        return found != null ? found : NONE;
    }

    /** No transaction at all. */
    ReplayTransactions NONE = new ReplayTransactions() {
        @Override
        public <T> T run(Supplier<T> work, BooleanSupplier commit) {
            return work.get();
        }
    };
}
