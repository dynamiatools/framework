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

import tools.dynamia.modules.saas.api.dto.AccountDTO;
import tools.dynamia.modules.saas.api.dto.AccountLogDTO;
import tools.dynamia.modules.saas.api.dto.AccountPaymentDTO;
import tools.dynamia.modules.saas.api.dto.AccountStatusDTO;
import tools.dynamia.modules.saas.api.enums.AccountStatus;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Core service API for managing SaaS accounts in a multi-tenant environment.
 * This interface provides comprehensive account management capabilities including
 * status tracking, payment processing, logging, feature management, and configuration.
 * <p>
 * Implementations of this interface handle the business logic for account operations,
 * tenant isolation, and account lifecycle management in a SaaS architecture.
 * <p>
 * Example usage:
 * <pre>{@code
 * AccountServiceAPI accountService = Containers.get().findObject(AccountServiceAPI.class);
 * AccountDTO account = accountService.getCurrentAccount();
 * if (accountService.hasFeature(account.getId(), "advanced-reports")) {
 *     // Enable advanced reporting features
 * }
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public interface AccountServiceAPI {

    /**
     * Attribute name used to store the current account ID in the session or context.
     */
    String CURRENT_ACCOUNT_ID_ATTRIBUTE = "currentAccountId";

    /**
     * Retrieves the current status of the specified account.
     *
     * @param accountId the unique identifier of the account
     * @return the current {@link AccountStatus} of the account
     */
    AccountStatus getAccountStatus(Long accountId);

    /**
     * Retrieves the complete account information for the specified account ID.
     *
     * @param accountId the unique identifier of the account
     * @return an {@link AccountDTO} containing all account details
     */
    AccountDTO getAccount(Long accountId);

    /**
     * Returns the system account ID. The system account represents the main
     * administrative account with special privileges.
     *
     * @return the unique identifier of the system account
     */
    Long getSystemAccountId();

    /**
     * Retrieves the ID of the account currently active in the session or execution context.
     *
     * @return the unique identifier of the current account, or null if no account is set
     */
    Long getCurrentAccountId();

    /**
     * Retrieves the complete information of the currently active account.
     *
     * @return an {@link AccountDTO} with the current account details
     */
    AccountDTO getCurrentAccount();

    /**
     * Sets the specified account as the current active account in the session or context.
     * This is typically used for account switching in multi-tenant scenarios.
     *
     * @param accountId the unique identifier of the account to set as current
     * @return the {@link AccountDTO} of the newly set current account
     */
    AccountDTO setCurrentAccount(Long accountId);

    /**
     * Updates the user count statistics for the specified account.
     *
     * @param accountId the unique identifier of the account
     * @param users the total number of users registered in the account
     * @param activedUsers the number of currently active users in the account
     */
    void updateAccountUsers(Long accountId, long users, long activedUsers);

    /**
     * Retrieves all payment records associated with the specified account.
     *
     * @param accountId the unique identifier of the account
     * @return a list of {@link AccountPaymentDTO} objects representing payment history
     */
    List<AccountPaymentDTO> getPayments(Long accountId);

    /**
     * Retrieves activity logs for the specified account within a date range.
     *
     * @param accountId the unique identifier of the account
     * @param startDate the beginning of the date range for log retrieval
     * @param endDate the end of the date range for log retrieval
     * @return a list of {@link AccountLogDTO} objects representing account activities
     */
    List<AccountLogDTO> getLogs(Long accountId, Date startDate, Date endDate);

    /**
     * Retrieves a configuration parameter value by name for the current account.
     *
     * @param name the name of the parameter to retrieve
     * @return the parameter value as a string, or null if not found
     */
    String getParameterValue(String name);

    /**
     * Retrieves a configuration parameter value by name, with a default fallback.
     *
     * @param name the name of the parameter to retrieve
     * @param defaultValue the default value to return if the parameter is not found
     * @return the parameter value, or the default value if not found
     */
    String getParameterValue(String name, String defaultValue);

    /**
     * Sets a configuration parameter for the current account.
     *
     * @param name the name of the parameter to set
     * @param value the value to assign to the parameter
     */
    void setParameter(String name, String value);

    /**
     * Checks whether the specified account has access to a particular feature.
     * This method is used for feature flagging and permission management in multi-tenant scenarios.
     *
     * @param accountId the unique identifier of the account
     * @param featureId the identifier of the feature to check
     * @return true if the account has access to the feature, false otherwise
     */
    default boolean hasFeature(Long accountId, String featureId) {
        return false;
    }

    /**
     * Determines whether printing functionality is enabled for the specified account.
     * This can be used to control access to document generation and printing features.
     *
     * @param accountId the unique identifier of the account
     * @return true if printing is enabled, false otherwise
     */
    default boolean isPrintingEnabled(Long accountId) {
        return true;
    }

    /**
     * Finds all account IDs that have access to the specified feature.
     *
     * @param featureId the identifier of the feature to search for
     * @return a list of account IDs that have access to the feature
     */
    List<Long> findAccountsIdByFeature(String featureId);

    /**
     * Records a log message for the specified account.
     * This method is used for audit trails and activity tracking.
     *
     * @param accountId the unique identifier of the account
     * @param message the log message to record
     */
    void log(Long accountId, String message);

    /**
     * Retrieves detailed status information for the specified account.
     * This includes status, messages, and balance information.
     *
     * @param accountId the unique identifier of the account
     * @return an {@link AccountStatusDTO} containing comprehensive status details
     */
    default AccountStatusDTO getAccountStatusDetails(Long accountId) {
        AccountDTO dto = getAccount(accountId);
        return new AccountStatusDTO(dto.getId(), dto.getName(), dto.getStatus(), dto.getStatusDate(),
                dto.getStatusDescription(), dto.getGlobalMessage(), dto.isShowGlobalMessage(), dto.getGlobalMessageType(), BigDecimal.ZERO);
    }

    /**
     * Retrieves the parent account ID for the specified account in a hierarchical account structure.
     *
     * @param accountId the unique identifier of the account
     * @return the parent account ID, or null if the account has no parent
     */
    default Long getParentAccountId(Long accountId) {
        return null;
    }

    /**
     * Finds the account ID associated with the specified domain name.
     * This is used in multi-tenant scenarios where each account has a unique domain or subdomain.
     *
     * @param domain the domain name to search for
     * @return the account ID associated with the domain, or null if not found
     */
    Long getAccountIdByDomain(String domain);

    /**
     * Clears all cached account data.
     * This method should be called when account information needs to be refreshed globally.
     */
    default void clearCache() {
        //do nothing
    }

    /**
     * Clears cached data for a specific account and its associated domain.
     *
     * @param accountId the unique identifier of the account
     * @param accountDomain the domain associated with the account
     */
    default void clearCache(Long accountId, String accountDomain) {
        //do nothing
    }

    /**
     * Finds account IDs matching the specified search criteria.
     * The parameters map can contain various filters such as status, type, or other account attributes.
     *
     * @param params a map of search parameters and their values
     * @return a list of account IDs matching the criteria, or an empty list if none found
     */
    default List<Long> findAccountsId(Map<String, Object> params) {
        return Collections.emptyList();
    }

    /**
     * Finds all active accounts that require payment.
     * This is a convenience method that filters accounts by ACTIVE status and payment requirement.
     *
     * @return a list of account IDs for active accounts requiring payment
     */
    default List<Long> findActivePaymentRequiredAccounts() {
        return findAccountsId(Map.of("status", AccountStatus.ACTIVE, "type.paymentRequired", true));
    }

    /**
     * Initializes the domain cache for improved performance in domain-to-account lookups.
     * This method should be called during application startup or when the domain mapping changes.
     */
    default void initDomainCache() {
        //do nothing
    }

    /**
     * Validates that the specified account is in ACTIVE status.
     * If the account is not active, this method throws a validation error.
     *
     * @param accountId the unique identifier of the account to validate
     * @throws RuntimeException if the account is not active
     */
    default void validateAccountStatus(Long accountId) {
        //do nothing
    }
}
