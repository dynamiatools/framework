package tools.dynamia.modules.email.ui.actions;

import org.springframework.beans.factory.annotation.Autowired;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.email.migration.EmailTemplateMigrator;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;

/**
 * Puts a migrated template back as it was before the migration. When the design was edited after the migration the
 * user is asked again, because reverting loses those edits.
 */
@InstallAction
@RunsOn(ActionRuntime.HEADLESS)
public class RevertEmailTemplateAction extends AbstractCrudAction {

    @Autowired
    private EmailTemplateMigrator migrator;
    @Autowired
    private CrudService crudService;

    public RevertEmailTemplateAction() {
        setName("Revert migration");
        setImage("undo");
        setApplicableClass(EmailTemplate.class);
        setMenuSupported(true);
        setApplicableStates(CrudState.get(CrudState.READ));
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        var template = (EmailTemplate) evt.getData();
        if (template == null || template.getId() == null) {
            UIMessages.showMessage("Select a saved template", MessageType.WARNING);
        } else if (!template.hasBackup()) {
            UIMessages.showMessage("This template has no migration to revert", MessageType.INFO);
        } else {
            UIMessages.showQuestion("Revert '" + template.getName() + "' to Velocity?", () -> revert(evt, template, false));
        }
    }

    private void revert(CrudActionEvent evt, EmailTemplate template, boolean confirmed) {
        var result = migrator.revert(template, confirmed);
        if (result.reverted()) {
            crudService.save(template);
            evt.getController().doQuery();
            UIMessages.showMessage("Migration reverted");
        } else if (result.designEdited()) {
            UIMessages.showQuestion("The design was edited after the migration. Reverting loses those edits. Revert anyway?",
                    () -> revert(evt, template, true));
        } else {
            UIMessages.showMessage(result.reason(), MessageType.WARNING);
        }
    }
}
