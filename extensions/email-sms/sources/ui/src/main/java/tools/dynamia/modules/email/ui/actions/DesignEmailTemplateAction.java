package tools.dynamia.modules.email.ui.actions;

import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.email.ui.EmailEditorLocation;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.UINavigation;

/**
 * Opens the block editor on the selected template in a new window. A template that still uses Velocity shows the migration
 * options inside the editor page. The editor talks to the REST API with the session of the user, so it can only touch
 * the templates of the current account.
 */
@InstallAction
@RunsOn(ActionRuntime.HEADLESS)
public class DesignEmailTemplateAction extends AbstractCrudAction {

    public DesignEmailTemplateAction() {
        setName("Design");
        setImage("edit");
        setApplicableClass(EmailTemplate.class);
        setMenuSupported(true);
        setApplicableStates(CrudState.get(CrudState.READ, CrudState.UPDATE));
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        var template = (EmailTemplate) evt.getData();
        if (template == null || template.getId() == null) {
            UIMessages.showMessage("Select a saved template", MessageType.WARNING);
            return;
        }
        if (!EmailEditorLocation.isInstalled()) {
            UIMessages.showMessage("The block editor is not included in this build (build the email UI module with -Peditor)", MessageType.WARNING);
            return;
        }
        UINavigation.openInNewWindow(EmailEditorLocation.url("", template.getId(), null));
    }
}
