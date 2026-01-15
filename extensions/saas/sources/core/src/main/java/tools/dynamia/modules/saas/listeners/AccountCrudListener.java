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

package tools.dynamia.modules.saas.listeners;

import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.integration.sterotypes.Listener;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.saas.api.enums.AccountStatus;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.services.AccountService;

/**
 * @author Mario Serrano Leones
 */
@Listener
public class AccountCrudListener extends CrudServiceListenerAdapter<Account> {


    private final AccountService service;

    public AccountCrudListener(AccountService service) {
        this.service = service;
    }

    @Override
    public void afterCreate(Account entity) {
        service.clearCache();
    }

    @Override
    public void afterUpdate(Account entity) {
        service.clearCache(entity.getId(), entity.getSubdomain());
        service.clearCache(entity.getId(), entity.getCustomDomain());
    }

    @Override
    public void beforeQuery(QueryParameters params) {
        if (Account.class.equals(params.getType())) {
            if (params.get("status") == null && params.get("id") == null) {
                params.add("status", QueryConditions.in(AccountStatus.ACTIVE, AccountStatus.NEW, AccountStatus.SUSPENDED));
            }
        }
    }

}
