package tools.dynamia.modules.entityfile.ui.actions;


import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.crud.cfg.AbstractConfigPageAction;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.entityfile.EntityFileStorage;
import tools.dynamia.ui.UIMessages;


@InstallAction
class ReloadEntityFileStoragesAction extends AbstractConfigPageAction {


    private final LoggingService logger = new SLF4JLoggingService(ReloadEntityFileStoragesAction.class);

    public ReloadEntityFileStoragesAction() {
        setName("Reload Storages");
        setApplicableConfig("EntityFileCFG");
        setType("secondary");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Containers.get().findObjects(EntityFileStorage.class).forEach(EntityFileStorage::reloadParams);
        UIMessages.showMessage("Reloaded");
    }

}
