
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

import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.domain.jpa.BaseEntityUuid;
import tools.dynamia.modules.saas.api.AccountAware;

/**
 *
 * @author Mario Serrano Leones
 */
@MappedSuperclass
public abstract class BaseEntityUuidSaaS extends BaseEntityUuid implements AccountAware {

    @NotNull
    private Long accountId;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

}
