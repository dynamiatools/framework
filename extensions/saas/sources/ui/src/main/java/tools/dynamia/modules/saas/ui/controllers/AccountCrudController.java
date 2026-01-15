package tools.dynamia.modules.saas.ui.controllers;

import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.services.AccountService;
import tools.dynamia.zk.crud.CrudController;

public class AccountCrudController extends CrudController<Account> {


    private boolean newAccount;

    @Override
    protected void beforeSave() {
        newAccount = getEntity().getId() == null;
    }

    @Override
    protected void afterSave() {
        if (newAccount) {
            var service = Containers.get().findObject(AccountService.class);
            service.initAccount(getEntity());
        }
    }
}
