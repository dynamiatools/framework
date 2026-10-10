package tools.dynamia.app.replay;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionTemplate;
import tools.dynamia.actions.replay.ReplayTransactions;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * One transaction per pass of a replayed action, rolled back when the pass stopped at a question.
 * <p>
 * An action may catch an exception of the service and show a message, as the delete action does; the transaction is then
 * already marked rollback-only and Spring reports an {@link UnexpectedRollbackException} at commit. The pass did
 * finish and told the user what happened, so the result is returned as it is.
 */
@Component
public class SpringReplayTransactions implements ReplayTransactions {

    private final ObjectProvider<PlatformTransactionManager> transactionManager;

    public SpringReplayTransactions(ObjectProvider<PlatformTransactionManager> transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public <T> T runOutside(Supplier<T> work) {
        var manager = transactionManager.getIfAvailable();
        if (manager == null) {
            return work.get();
        }
        var template = new TransactionTemplate(manager);
        template.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        return template.execute(status -> work.get());
    }

    @Override
    public <T> T run(Supplier<T> work, BooleanSupplier commit) {
        var manager = transactionManager.getIfAvailable();
        if (manager == null) {
            return work.get();
        }
        var result = new AtomicReference<T>();
        try {
            new TransactionTemplate(manager).executeWithoutResult(status -> {
                result.set(work.get());
                if (!commit.getAsBoolean()) {
                    status.setRollbackOnly();
                }
            });
        } catch (UnexpectedRollbackException e) {
            // the action reported the failure itself, see the class comment
        }
        return result.get();
    }
}
