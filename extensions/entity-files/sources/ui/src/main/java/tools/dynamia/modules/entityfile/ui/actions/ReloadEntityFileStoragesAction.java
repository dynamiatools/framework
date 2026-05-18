package tools.dynamia.modules.entityfile.ui.actions;


import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.crud.cfg.AbstractConfigPageAction;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.modules.entityfile.EntityFileStorage;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.domain.enums.EntityFileState;
import tools.dynamia.modules.entityfile.local.LocalEntityFileStorage;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.ui.LongOperationMonitorWindow;
import tools.dynamia.zk.util.LongOperation;
import tools.dynamia.zk.util.ZKUtil;

import java.util.ArrayList;
import java.util.List;

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
