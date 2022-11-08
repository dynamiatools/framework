package tools.dynamia.actions;

/**
 * Very simple action runner
 */
public class DefaultActionRunner implements ActionRunner {

    @Override
    public void run(Action action, ActionEvent evt) {
        action.actionPerformed(evt);
    }
}
