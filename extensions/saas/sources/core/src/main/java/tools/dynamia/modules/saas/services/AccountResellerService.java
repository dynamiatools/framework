package tools.dynamia.modules.saas.services;

import tools.dynamia.modules.saas.api.enums.AccountStatus;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountPayment;
import tools.dynamia.modules.saas.domain.AccountReseller;
import tools.dynamia.modules.saas.domain.AccountResellerAgent;

import java.util.Date;
import java.util.List;

/**
 * Service for managing account resellers in a SaaS application.
 * This service is typically used to handle operations related to resellers and their agents.
 * It may include methods for retrieving, updating, and managing reseller information.
 */
public interface AccountResellerService {

    /**
     * Finds all enabled resellers in the system.
     *
     * @return A list of AccountReseller entities that are enabled.
     */
    List<AccountReseller> findAllEnabledResellers();

    /**
     * Finds all agents associated with a specific reseller.
     *
     * @param reseller The AccountReseller for which to find agents.
     * @return A list of AccountResellerAgent entities associated with the specified reseller.
     */
    List<AccountResellerAgent> findAgentsByReseller(AccountReseller reseller);

    /**
     * Finds all payments associated with a specific reseller.
     *
     * @param reseller The AccountReseller for which to find payments.
     * @param from     The start date for filtering payments.
     * @param to       The end date for filtering payments.
     * @return A list of AccountPayment entities associated with the specified reseller.
     */
    List<AccountPayment> findPaymentsByReseller(AccountReseller reseller, Date from, Date to);

    /**
     * Finds all  accounts associated with a specific reseller.
     *
     * @param reseller The AccountReseller for which to find accounts.
     * @return A list of Account entities associated with the specified reseller.
     */
    List<Account> findAccountsByReseller(AccountReseller reseller, AccountStatus status);


}
