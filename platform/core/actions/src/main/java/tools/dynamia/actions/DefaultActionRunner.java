package tools.dynamia.actions;

/**
 * Very simple action runner
 */
public class DefaultActionRunner implements ActionRunner {

    @Override
    public void run(Action action, ActionEvent evt) {
        if(action instanceof LocalAction localAction) {
            localAction.actionPerformed(evt);
        }
    }
}
