package tools.dynamia.actions;

import org.springframework.transaction.annotation.Transactional;

/**
 * Executes actions inside a transactional context using Spring's {@link Transactional} annotation.
 * <p>
 * This class extends {@link DefaultActionRunner} and ensures that the {@link #run(Action, ActionEvent)} method
 * is executed within a database transaction. This is useful for actions that require atomicity and consistency,
 * such as those that modify persistent data.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     ActionRunner runner = new TransactionalDefaultActionRunner();
 *     runner.run(myAction, myEvent); // executed within a transaction
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
@Transactional
public class TransactionalDefaultActionRunner extends DefaultActionRunner {

    /**
     * Executes the given action within a transactional context.
     * <p>
     * This method delegates to {@link DefaultActionRunner#run(Action, ActionEvent)},
     * but is guaranteed to run inside a transaction due to the {@link Transactional} annotation.
     * </p>
     *
     * @param action the action to execute
     * @param evt the event context for the action
     */
    @Override
    public void run(Action action, ActionEvent evt) {
        super.run(action, evt);
    }
}
