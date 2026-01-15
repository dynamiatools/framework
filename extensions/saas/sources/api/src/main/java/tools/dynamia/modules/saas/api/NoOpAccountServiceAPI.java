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

package tools.dynamia.modules.saas.api;


import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.modules.saas.api.dto.AccountDTO;
import tools.dynamia.modules.saas.api.dto.AccountLogDTO;
import tools.dynamia.modules.saas.api.dto.AccountPaymentDTO;
import tools.dynamia.modules.saas.api.dto.AccountTypeDTO;
import tools.dynamia.modules.saas.api.enums.AccountStatus;

import java.util.*;

/**
 * Default No operational {@link AccountServiceAPI} implementation. Useful for testing
 */
public class NoOpAccountServiceAPI implements AccountServiceAPI {

    private final LoggingService logger = new SLF4JLoggingService(NoOpAccountServiceAPI.class);

    private static Long defaultAccountId = 1L;
    private static AccountDTO CURRENT_ACCOUNT;

    private void init() {
        CURRENT_ACCOUNT = new AccountDTO();
        CURRENT_ACCOUNT.setId(defaultAccountId);
        CURRENT_ACCOUNT.setName("NoOp");
        CURRENT_ACCOUNT.setEmail("account@account.com");
        CURRENT_ACCOUNT.setStatus(AccountStatus.ACTIVE);
        CURRENT_ACCOUNT.setTimeZone(TimeZone.getDefault().getID());
        CURRENT_ACCOUNT.setLocale(Locale.getDefault().toString());
        CURRENT_ACCOUNT.setCreationDate(new Date());
        CURRENT_ACCOUNT.setIdentification("111111111");
        CURRENT_ACCOUNT.setAdminUsername("admin");
        CURRENT_ACCOUNT.setMaxUsers(1000);
        CURRENT_ACCOUNT.setInstanceUuid("UUID");
        AccountTypeDTO noOpType = new AccountTypeDTO();
        noOpType.setName("NoOp");
        noOpType.setAllowAdditionalUsers(true);
        noOpType.setMaxUsers(1000);
        CURRENT_ACCOUNT.setType(noOpType);
    }

    @Override
    public AccountStatus getAccountStatus(Long accountId) {
        return AccountStatus.ACTIVE;
    }

    @Override
    public AccountDTO getAccount(Long accountId) {
        return getCurrentAccount();
    }

    @Override
    public Long getSystemAccountId() {
        return defaultAccountId;
    }

    @Override
    public Long getCurrentAccountId() {
        return defaultAccountId;
    }

    @Override
    public AccountDTO setCurrentAccount(Long accountId) {
        if (accountId != null) {
            defaultAccountId = accountId;
            CURRENT_ACCOUNT.setId(defaultAccountId);
            return CURRENT_ACCOUNT;
        }
        return null;
    }

    @Override
    public AccountDTO getCurrentAccount() {
        if (CURRENT_ACCOUNT == null) {
            init();
        }
        return CURRENT_ACCOUNT;
    }

    @Override
    public void updateAccountUsers(Long accountId, long users, long activedUsers) {

    }


    @Override
    public List<AccountPaymentDTO> getPayments(Long accountId) {
        return Collections.emptyList();
    }

    @Override
    public List<AccountLogDTO> getLogs(Long accountId, Date startDate, Date endDate) {
        return Collections.emptyList();
    }


    @Override
    public String getParameterValue(String name) {
        return null;
    }

    @Override
    public String getParameterValue(String name, String defaultValue) {
        return defaultValue;
    }

    @Override
    public void setParameter(String name, String value) {
//do nothin
    }

    @Override
    public List<Long> findAccountsIdByFeature(String featureId) {
        return Collections.emptyList();
    }

    @Override
    public void log(Long accountId, String message) {
        logger.info("[ACCOUNT " + accountId + "]  " + message);
    }

    @Override
    public Long getAccountIdByDomain(String domain) {
        return defaultAccountId;
    }
}
