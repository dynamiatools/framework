
/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tools.dynamia.modules.saas;

import org.springframework.stereotype.Component;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.dto.AccountDTO;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.services.AccountService;
import tools.dynamia.web.util.HttpUtils;

import java.util.List;

/**
 * @author Mario Serrano Leones
 */
@Component("accountContext")
public class AccountContext {

    private final LoggingService logger = new SLF4JLoggingService(AccountContext.class);

    private final List<AccountResolver> resolvers;

    public AccountContext(List<AccountResolver> resolvers) {
        this.resolvers = resolvers;
    }


    public static AccountContext getCurrent() {
        return Containers.get().findObject(AccountContext.class);
    }

    /**
     * Returns the account of the code that is running now. The account bound with {@link AccountTenants} (administration
     * of another account, background work) wins over the session and the resolvers, so it always matches the tenant
     * Hibernate filters by.
     *
     * @return the current account, or null when there is none
     */
    public Account getAccount() {
        Account account = accountBoundToTenant();
        if (account != null) {
            return account;
        }

        try {
            account = AccountSessionHolder.get().getCurrent();
            if (account != null) {
                return account;
            }
        } catch (Exception e) {
            //no session holder
        }


        for (AccountResolver resolver : resolvers) {
            account = resolver.resolve();
            if (account != null) {
                break;
            }
        }


        if (account != null && HttpUtils.isInWebScope()) {
            AccountSessionHolder.get().setCurrent(account);
            account = AccountSessionHolder.get().getCurrent();
        }
        return account;
    }

    public AccountDTO toDTO() {
        Account bound = accountBoundToTenant();
        if (bound != null) {
            return bound.toDTO();
        }
        AccountDTO dto = null;
        if (HttpUtils.isInWebScope()) {
            dto = AccountSessionHolder.get().toDTO();
        }

        if (dto == null) {
            Account account = getAccount();
            if (account != null) {
                dto = account.toDTO();
            }
        }
        return dto;
    }

    public boolean isAdminAccount() {
        Account account = getAccount();
        if (account != null) {
            return account.getType().getName().equalsIgnoreCase("admin");
        }
        return false;
    }



    /**
     * The account bound with {@code AccountTenants.runAs/with}. The root tenant and the no-tenant sentinel are not
     * accounts, so they resolve to none.
     */
    private Account accountBoundToTenant() {
        Long tenantId = AccountTenants.forcedTenantId();
        if (tenantId == null || tenantId <= 0) {
            return null;
        }
        var service = Containers.get().findObject(AccountService.class);
        return service != null ? service.getAccountById(tenantId) : null;
    }
}
