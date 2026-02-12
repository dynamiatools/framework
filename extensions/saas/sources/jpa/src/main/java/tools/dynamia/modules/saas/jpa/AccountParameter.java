
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

package tools.dynamia.modules.saas.jpa;

import tools.dynamia.domain.jpa.JpaParameter;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.AccountAware;
import tools.dynamia.modules.saas.api.AccountServiceAPI;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * @author Mario Serrano Leones
 */
@Entity
public class AccountParameter extends JpaParameter implements AccountAware {


    public static AccountParameter find(String param, Long accountId) {
        return DomainUtils.lookupCrudService().findSingle(AccountParameter.class, QueryParameters.with("accountId", accountId)
                .add("name", QueryConditions.eq(param)));
    }

    private Long accountId;

    public static AccountParameter create(String name, String value, Long accountId) {
        AccountParameter parameter = new AccountParameter();
        parameter.setName(name);
        parameter.setValue(value);
        parameter.setAccountId(accountId);
        return parameter;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    @Override
    public String identifier() {
        if (accountId != null) {
            return "Account" + accountId;
        } else {
            AccountServiceAPI service = Containers.get().findObject(AccountServiceAPI.class);
            return "Account" + service.getCurrentAccountId();
        }
    }


}
