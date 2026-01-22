
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

import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.integration.sterotypes.Listener;
import tools.dynamia.modules.saas.api.AccountAware;
import tools.dynamia.modules.saas.api.AccountServiceAPI;

/**
 * @author Mario Serrano Leones
 */
@Listener
public class AccountAwareCrudServiceListener extends CrudServiceListenerAdapter<AccountAware> {

    private static final String ACCOUNT_ID = "accountId";
    private final AccountServiceAPI serviceAPI;

    public AccountAwareCrudServiceListener(AccountServiceAPI serviceAPI) {
        this.serviceAPI = serviceAPI;
    }

    @Override
    public void beforeCreate(AccountAware entity) {
        if (entity.getAccountId() == null) {
            var accountId = serviceAPI.getCurrentAccountId();
            if (accountId != null) {
                entity.setAccountId(accountId);
            }
        }
    }

    @Override
    public void beforeQuery(QueryParameters params) {
        if (params != null && (!params.containsKey(ACCOUNT_ID) || params.get(ACCOUNT_ID) == null || params.get(ACCOUNT_ID).equals(0L))) {
            Class paramsType = params.getType();
            if (paramsType != null && ObjectOperations.isAssignable(paramsType, AccountAware.class)) {
                var accountId = serviceAPI.getCurrentAccountId();
                if (accountId != null) {
                    params.add(ACCOUNT_ID, accountId);
                }
            }
        }
    }


}
