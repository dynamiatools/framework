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

package tools.dynamia.modules.saas.services.impl;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.domain.Transferable;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.ApplicationParameters;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.AbstractService;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.CacheManagerUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.saas.AccountConfig;
import tools.dynamia.modules.saas.AccountContext;
import tools.dynamia.modules.saas.AccountSessionHolder;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.saas.api.dto.AccountDTO;
import tools.dynamia.modules.saas.api.dto.AccountLogDTO;
import tools.dynamia.modules.saas.api.dto.AccountPaymentDTO;
import tools.dynamia.modules.saas.api.dto.AccountStatusDTO;
import tools.dynamia.modules.saas.api.enums.AccountStatus;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountFeature;
import tools.dynamia.modules.saas.domain.AccountLog;
import tools.dynamia.modules.saas.domain.AccountPayment;
import tools.dynamia.modules.saas.jpa.AccountParameter;
import tools.dynamia.modules.saas.services.AccountService;
import tools.dynamia.web.util.HttpUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static tools.dynamia.domain.query.QueryConditions.eq;
import static tools.dynamia.domain.util.QueryBuilder.select;

@Service("accountServiceAPI")
@CacheConfig(cacheNames = AccountConfig.CACHE_NAME)
public class AccountServiceAPIImpl extends AbstractService implements AccountServiceAPI, Serializable {

    private final AccountService service;
    private final AccountContext accountContext;
    private final CrudService crudService;
    private final Environment environment;


    public AccountServiceAPIImpl(AccountService service, AccountContext accountContext, CrudService crudService, Environment environment) {
        this.service = service;
        this.accountContext = accountContext;
        this.crudService = crudService;
        this.environment = environment;
    }


    @Override
    @Cacheable(key = "'AccountStatus-'+#accountId")
    public AccountStatus getAccountStatus(Long accountId) {
        try {
            AccountStatusDTO dto = getAccountStatusDetails(accountId);
            return dto.getStatus();
        } catch (Exception e) {
            log("Error getting account status, returning null", e);
            return null;
        }
    }

    @Override
    @Transactional
    @Cacheable(key = "'AccountDTO-'+#accountId")
    public AccountDTO getAccount(Long accountId) {
        AccountDTO dto = null;

        try {
            Account account = crudService.findSingle(Account.class,
                    QueryParameters.with("id", accountId).add("status", QueryConditions.isNotNull()));

            if (account != null) {
                dto = account.toDTO();
            } else {
                log("No account found with id " + accountId);
            }

        } catch (Exception e) {
            log("Error getting account info, returning null", e);
        }
        return dto;
    }

    @Override
    @Cacheable(key = "'SystemAccount'")
    public Long getSystemAccountId() {
        try {
            Account account = crudService.findSingle(Account.class, "name", eq("System"));
            if (account != null) {
                return account.getId();
            } else {
                log("No system account found");
            }
        } catch (Exception e) {
            log("Error getting system account id, returning null", e);
        }
        return null;
    }

    @Override
    public Long getCurrentAccountId() {
        Long id = null;

        try {
            if (HttpUtils.isInWebScope()) {
                var req = HttpUtils.getCurrentRequest();
                if (req != null) {
                    id = (Long) req.getAttribute(CURRENT_ACCOUNT_ID_ATTRIBUTE);
                }

                if (id == null) {
                    if (AccountSessionHolder.get().getCurrent() != null) {
                        id = AccountSessionHolder.get().getCurrent().getId();
                    }
                }
            }
        } catch (Exception e) {
            //ignore
        }


        try {
            if (id == null) {
                id = accountContext.getAccount().getId();
            }
        } catch (Exception e) {
            id = null;
        }

        return id;
    }

    @Override
    public AccountDTO getCurrentAccount() {
        try {
            return accountContext.toDTO();
        } catch (Exception e) {
            log("Error loading current account", e);
            return null;
        }
    }

    @Override
    public AccountDTO setCurrentAccount(Long accountId) {
        if (HttpUtils.isInWebScope()) {
            var current = getCurrentAccount();
            if (current == null || !current.getId().equals(accountId)) {
                var account = service.getAccountById(accountId);
                if (account != null) {
                    AccountSessionHolder.get().setCurrent(account);
                    return AccountSessionHolder.get().toDTO();
                }
            } else {
                return current;
            }
        } else {
            throw new IllegalStateException("Cannot change current Account outside web scope ");
        }
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAccountUsers(Long accountId, long users, long activedUsers) {
        Account account = crudService.find(Account.class, accountId);
        if (account != null) {
            account.setActivedUsers(activedUsers);
            account.setUsers(users);
            account.getIdentification();
            service.computeAccountPaymentValue(account);
            crudService.update(account);
        }
    }

    @Override
    public List<AccountPaymentDTO> getPayments(Long accountId) {
        return crudService().find(AccountPayment.class, QueryParameters.with("accountId", accountId)
                        .add("creationDate", QueryConditions.geqt(DateTimeUtils.createDate(1))))
                .stream().map(Transferable::toDTO)
                .toList();
    }

    @Override
    public List<AccountLogDTO> getLogs(Long accountId, Date startDate, Date endDate) {
        return crudService().find(AccountLog.class, QueryParameters.with("accountId", accountId)
                        .add("creationDate", QueryConditions.between(startDate, endDate)))
                .stream().map(Transferable::toDTO).toList();
    }


    @Override
    public String getParameterValue(String name) {
        return ApplicationParameters.get().getValue(AccountParameter.class, name);
    }

    @Override
    public String getParameterValue(String name, String defaultValue) {
        return ApplicationParameters.get().getValue(AccountParameter.class, name, defaultValue);
    }

    @Override
    public void setParameter(String name, String value) {
        ApplicationParameters.get().setParameter(AccountParameter.class, name, value);
    }

    @Override
    @Cacheable(key = "'AccountFeature-'+#accountId+'-'+#featureId")
    public boolean hasFeature(Long accountId, String featureId) {
        AccountFeature feature = crudService.findSingle(AccountFeature.class, QueryParameters.with("account.id", accountId)
                .add("providerId", eq(featureId)));

        return feature != null && feature.isEnabled();
    }

    @Override
    @Cacheable(key = "'AccountPrintingEnabled-'+#accountId")
    public boolean isPrintingEnabled(Long accountId) {
        Boolean enabled = crudService.executeProjection(Boolean.class, "select a.type.printingSupport from Account a where a.id = :accountId",
                QueryParameters.with("accountId", accountId));

        if (enabled == null) {
            enabled = true;
        }
        return enabled;
    }

    @Override
    @Cacheable(key = "'AccountsByFeatured-'+#featureId")
    public List<Long> findAccountsIdByFeature(String featureId) {
        String jpql = "select af.account.id from AccountFeature af where af.providerId = :feature and af.enabled = true and af.account.status = :status";
        return crudService.executeQuery(jpql, QueryParameters.with("feature", eq(featureId))
                .add("status", AccountStatus.ACTIVE));
    }

    @Override
    public void log(Long accountId, String message) {
        service.log(new Account(accountId), message);
    }

    @Override
    @Cacheable(key = "'AccountsDetails-'+#accountId")
    public AccountStatusDTO getAccountStatusDetails(final Long accountId) {

        List<AccountStatusDTO> result = crudService.executeQuery(select("id", "name", "status", "statusDate",
                "statusDescription", "globalMessage", "showGlobalMessage", "globalMessageType", "balance")
                .from(Account.class, "a").where("id", eq(accountId))
                .resultType(AccountStatusDTO.class));

        var accountStatus = result.stream().findFirst().
                orElse(new AccountStatusDTO(accountId, "unknow",
                        AccountStatus.CANCELED, new Date(),
                        null, null,
                        false, null, BigDecimal.ZERO));

        Long parentAccountId = getParentAccountId(accountId);
        if (parentAccountId != null && !parentAccountId.equals(accountId)) {
            var parentStatus = getAccountStatusDetails(parentAccountId);
            if (parentStatus != null) {
                accountStatus = new AccountStatusDTO(accountStatus.getId(), accountStatus.getName(),
                        parentStatus.getStatus(), parentStatus.getStatusDate(), parentStatus.getStatusDescription(),
                        parentStatus.getGlobalMessage(), parentStatus.isShowGlobalMessage(), parentStatus.getGlobalMessageType(), parentStatus.getBalance());
            }
        }


        return accountStatus;
    }


    @Override
    public Long getParentAccountId(Long accountId) {
        return crudService.executeProjection(Long.class,
                select("a.parentAccount.id").from(Account.class, "a")
                        .where("a.id = :accountId").toString(),
                QueryParameters.with("accountId", accountId));
    }

    @Override
    @Cacheable(key = "'AccountByDomain-'+#domain")
    public Long getAccountIdByDomain(String domain) {
        if (domain == null) {
            return null;
        }


        Long accountId = service.getAccountId(domain);

        if (accountId == null) {
            accountId = service.getAccountIdByCustomDomain(domain);
        }

        if (accountId == null && "true".equals(environment.getProperty("useDefaultAccount"))) {
            var account = service.getDefaultAccount();
            accountId = account.getId();
        }


        return accountId;
    }

    @Override
    public void clearCache() {
        service.clearCache();
    }

    @Override
    public void clearCache(Long accountId, String accountDomain) {
        service.clearCache(accountId, accountDomain);
    }

    @Override
    public List<Long> findAccountsId(Map<String, Object> params) {
        var queryParams = new QueryParameters();
        queryParams.putAll(params);
        var query = select("id").from(Account.class, "a").where(queryParams);
        return crudService.executeQuery(query);
    }

    @Override
    public void initDomainCache() {
        log("Loading subdomain cache");
        var domains = crudService.executeQuery(
                select("id", "subdomain").from(Account.class, "a")
                        .where("a.status", eq(AccountStatus.ACTIVE))
                        .resultType(AccountDTO.class)
        );

        domains.forEach(o -> {
            var dto = (AccountDTO) o;
            CacheManagerUtils.put("saas", "AccountByDomain-" + dto.getSubdomain(), dto.getId());
            log(dto.getId() + "  -> " + dto.getSubdomain());
        });
    }

    @Override
    public void validateAccountStatus(Long accountId) {
        if (accountId != null) {
            var status = Containers.get().findObject(AccountServiceAPI.class).getAccountStatusDetails(accountId);
            if (status != null && status.getStatus() != AccountStatus.ACTIVE) {
                throw new ValidationError("Account " + status.getName() + " is " + status.getStatus() + ": " + status.getGlobalMessage());
            }
        }
    }

}
