package tools.dynamia.modules.saas.services.impl;

import org.springframework.stereotype.Service;
import tools.dynamia.domain.query.Parameters;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.AbstractService;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.modules.saas.api.enums.AccountStatus;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountPayment;
import tools.dynamia.modules.saas.domain.AccountReseller;
import tools.dynamia.modules.saas.domain.AccountResellerAgent;
import tools.dynamia.modules.saas.services.AccountResellerService;

import java.util.Date;
import java.util.List;

@Service
public class AccountResellerServiceImpl extends AbstractService implements AccountResellerService {

    public AccountResellerServiceImpl() {
    }

    public AccountResellerServiceImpl(CrudService crudService) {
        super(crudService);
    }

    public AccountResellerServiceImpl(CrudService crudService, Parameters appParams) {
        super(crudService, appParams);
    }

    @Override
    public List<AccountReseller> findAllEnabledResellers() {
        return crudService().find(AccountReseller.class, QueryParameters.with("enabled", true)
                .orderBy("creationDate", true));

    }

    @Override
    public List<AccountResellerAgent> findAgentsByReseller(AccountReseller reseller) {
        if (reseller == null) {
            return List.of();
        }

        return crudService().find(AccountResellerAgent.class, QueryParameters.with("reseller", reseller)
                .orderBy("name", true));
    }

    @Override
    public List<AccountPayment> findPaymentsByReseller(AccountReseller reseller, Date from, Date to) {
        return crudService().find(AccountPayment.class, QueryParameters.with("reseller", reseller)
                .add("creationDate", QueryConditions.between(from, to))
                .add("finished", true));
    }

    @Override
    public List<Account> findAccountsByReseller(AccountReseller reseller, AccountStatus status) {
        return crudService().find(Account.class, QueryParameters.with("reseller", reseller)
                .add("status", status)
                .orderBy("creationDate", true));
    }
}
