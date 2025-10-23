package tools.dynamia.zk.actions;

import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.MapBuilder;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.ViewAction;
import tools.dynamia.zk.util.ZKBindingUtil;

import static tools.dynamia.commons.Lambdas.ifValid;
import static tools.dynamia.commons.Lambdas.ifValidElse;

/**
 * Action to call a global command in the current view
 */
@InstallAction
public class CallZKGlobalCommandViewAction extends ViewAction {

    public CallZKGlobalCommandViewAction() {
        setId("call-zk-global-command");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {

        ifValidElse(getStringAttribute("command"), command -> {
            var args = MapBuilder.of("data", evt.getData());
            args.putAll(getAttributes());
            ZKBindingUtil.postGlobalCommand(command, args);

            ifValid(getStringAttribute("successMessage"), message -> UIMessages.showLocalizedMessage(message, MessageType.INFO));


        }, () -> UIMessages.showLocalizedMessage("No command specified to call.", MessageType.WARNING));
    }
}
