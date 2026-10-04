package tools.dynamia.modules.entityfile.ui.actions;

import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.cfg.AbstractConfigPageAction;
import tools.dynamia.modules.entityfile.EntityFileCache;
import tools.dynamia.ui.UIMessages;

@InstallAction
public class ClearEntityFileCacheAction extends AbstractConfigPageAction {

    private final EntityFileCache cache;

    public ClearEntityFileCacheAction(EntityFileCache cache) {
        this.cache = cache;
        setName("Clear Cache");
        setApplicableConfig("EntityFileCFG");
        setType("secondary");
    }


    @Override
    public void actionPerformed(ActionEvent evt) {
        cache.clear();
        UIMessages.showMessage("Clearing entity files cache");
    }
}