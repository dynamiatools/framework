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
class MoveEntityFileLocalToRemoteStorageAction extends AbstractConfigPageAction {

    public static final String STORAGE_INFO = "storageInfo";
    private final CrudService crudService;
    private final LocalEntityFileStorage localStorage;

    private final LoggingService logger = new SLF4JLoggingService(MoveEntityFileLocalToRemoteStorageAction.class);

    public MoveEntityFileLocalToRemoteStorageAction(LocalEntityFileStorage localStorage, CrudService crudService) {
        this.crudService = crudService;
        this.localStorage = localStorage;
        setName("Move Local to Remote");
        setApplicableConfig("EntityFileCFG");
        setType("warning");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        var otherStorages = Containers.get().findObjects(EntityFileStorage.class, s -> !LocalEntityFileStorage.ID.equals(s.getId()));

        ZKUtil.showListboxSelector("Select new Storage", new ArrayList(otherStorages), event -> {
            var otherStorage = (EntityFileStorage) event.getSelectedObjects().stream().findFirst().orElse(null);
            if (otherStorage == null) {
                return;
            }

            List<Long> accounts = findFilesAccounts();
            UIMessages.showQuestion("Are you sure want to move files from " + accounts.size() + " accounts to " + otherStorage,
                    () -> move(otherStorage, accounts, "Moving files to " + otherStorage));
        });
    }

    public void move(EntityFileStorage otherStorage, List<Long> accounts, String title) {
        var monitor = new ProgressMonitor();

        var longOp = LongOperation.create()
                .execute(() -> moveFiles(accounts, monitor, otherStorage))
                .onFinish(() -> UIMessages.showMessage("Moving files completed"))
                .onException(e -> UIMessages.showMessage("Error: " + e.getMessage()))
                .start();

        LongOperationMonitorWindow.show(title, longOp, monitor)
                .setMessageTemplate("Moving files to " + otherStorage.getName() + ": {0} / {1}");
    }


    private void moveFiles(List<Long> accounts, ProgressMonitor monitor, EntityFileStorage otherStorage) {
        otherStorage.reloadParams();

        accounts.forEach(accountId -> {
            List<EntityFile> files = findAllLocalFiles(accountId);

            monitor.setCurrent(0);
            monitor.setMax(files.size());

            logger.info("Moving " + files.size() + " files to " + otherStorage + " account id = " + accountId);
            files.forEach(entityFile -> {
                if (monitor.isStopped()) {
                    throw new ValidationError("Moving files stoped manually");
                }

                try {
                    monitor.setCurrent(monitor.getCurrent() + 1);
                    localStorage.copy(entityFile, otherStorage);
                    monitor.setMessage("Account " + accountId + ": " + entityFile.getName() + " moved");
                    crudService.executeWithinTransaction(() -> crudService.updateField(entityFile, STORAGE_INFO, otherStorage.getId()));
                } catch (Exception e) {
                    e.printStackTrace();
                    monitor.setMessage("Error moving entity file " + entityFile);
                }
            });
            logger.info("MOVING FILES COMPLETED - Account " + accountId);

        });
    }


    private List<EntityFile> findAllLocalFiles(Long accountId) {

        return crudService.find(EntityFile.class, QueryParameters.with(STORAGE_INFO, QueryConditions.eq(LocalEntityFileStorage.ID))
                .add("state", EntityFileState.VALID)
                .add("accountId", accountId));
    }

    private List<Long> findFilesAccounts() {
        return crudService.executeQuery(QueryBuilder
                .select("distinct e.accountId")
                .from(EntityFile.class, "e")
                .where(QueryParameters.with(STORAGE_INFO, QueryConditions.eq(LocalEntityFileStorage.ID))
                        .add("state", EntityFileState.VALID)
                        .add("accountId", QueryConditions.isNotNull())));
    }
}
