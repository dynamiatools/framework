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

package tools.dynamia.modules.saas.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.modules.saas.api.enums.AccountStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountStatusDTO implements Serializable {


    private final Long id;
    private final String name;
    private final AccountStatus status;
    private final Date statusDate;
    private String statusDescription;
    private final String globalMessage;
    private final boolean showGlobalMessage;
    private final String globalMessageType;
    private final BigDecimal balance;


    public AccountStatusDTO(Long id, String name, AccountStatus status, Date statusDate, String statusDescription,
                            String globalMessage, boolean showGlobalMessage, String globalMessageType, BigDecimal balance) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.statusDate = statusDate;
        this.statusDescription = statusDescription;
        this.globalMessage = globalMessage;
        this.showGlobalMessage = showGlobalMessage;
        this.globalMessageType = globalMessageType;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Date getStatusDate() {
        return statusDate;
    }

    public String getStatusDescription() {
        if (statusDescription == null) {
            statusDescription = "";
        }
        return statusDescription;
    }

    public String getGlobalMessage() {
        return globalMessage;
    }

    public boolean isShowGlobalMessage() {
        return showGlobalMessage;
    }

    public String getGlobalMessageType() {
        return globalMessageType;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
