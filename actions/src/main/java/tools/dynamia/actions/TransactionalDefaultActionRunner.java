package tools.dynamia.actions;

import org.springframework.transaction.annotation.Transactional;

/**
 * Execute actions inside a transaction
 */
@Transactional
public class TransactionalDefaultActionRunner extends DefaultActionRunner {

    @Override
    public void run(Action action, ActionEvent evt) {
        super.run(action, evt);
    }
}
