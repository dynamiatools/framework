package mybookstore.actions;

import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.ApplicationGlobalAction;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;

@InstallAction
public class SomeGlobalAction extends ApplicationGlobalAction {

    public SomeGlobalAction() {
        setName("Global Action");
        setImage("check");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        UIMessages.showMessage("This is a Global Action.. Always here!", MessageType.INFO);
    }
}
