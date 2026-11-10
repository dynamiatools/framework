package tools.dynamia.modules.email.ui.actions;

import org.springframework.beans.factory.annotation.Autowired;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.email.migration.EmailTemplateMigrator;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;

import java.util.stream.Collectors;

/**
 * Migrates the selected template from Velocity to the block editor engine after showing what would change. A template
 * that cannot be migrated automatically is left untouched and the reasons are shown.
 */
@InstallAction
public class MigrateEmailTemplateAction extends AbstractCrudAction {

    @Autowired
    private EmailTemplateMigrator migrator;
    @Autowired
    private CrudService crudService;

    public MigrateEmailTemplateAction() {
        setName("Migrate to editor");
        setImage("upgrade");
        setApplicableClass(EmailTemplate.class);
        setMenuSupported(true);
        setApplicableStates(CrudState.get(CrudState.READ));
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        var template = (EmailTemplate) evt.getData();
        if (template == null || template.getId() == null) {
            UIMessages.showMessage("Select a saved template", MessageType.WARNING);
            return;
        }
        var report = migrator.analyze(template);
        if (report.alreadyMigrated()) {
            UIMessages.showMessage("This template already uses the block editor engine", MessageType.INFO);
        } else if (!report.portable()) {
            var reasons = report.issues().stream()
                    .map(i -> i.field() + ":" + i.line() + " " + i.snippet() + " - " + i.reason())
                    .collect(Collectors.joining("\n"));
            UIMessages.showMessage("This template cannot be migrated automatically and stays as it is:\n" + reasons, MessageType.WARNING);
        } else {
            var warnings = String.join("\n", report.warnings());
            UIMessages.showQuestion("Migrate '" + template.getName() + "'? Its look does not change and you can revert it.\n" + warnings, () -> {
                var result = migrator.migrate(template);
                if (result.applied()) {
                    crudService.save(template);
                    evt.getController().doQuery();
                    UIMessages.showMessage("Template migrated");
                }
            });
        }
    }
}
