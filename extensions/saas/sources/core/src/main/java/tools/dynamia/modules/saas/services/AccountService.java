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

package tools.dynamia.modules.saas.services;

import jakarta.servlet.http.HttpServletRequest;
import tools.dynamia.modules.saas.api.AccountStats;
import tools.dynamia.modules.saas.api.dto.AccountDTO;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountPayment;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for managing accounts in a SaaS application.
 * Provides methods for retrieving, initializing, updating, and managing account payments and statistics.
 * This service is typically used in multi-tenant applications where each tenant has its own account.
 * It includes methods for handling account payments, checking account status, and managing account statistics.
 * @author Mario Serrano Leones
 */
public interface AccountService {

    /**
     * Gets the account associated with the specified subdomain.
     * @param subdomain The tenant's subdomain.
     * @return The corresponding Account entity, or null if not found.
     */
    Account getAccount(String subdomain);

    /**
     * Gets the account associated with the current HTTP request.
     * @param request The HTTP request object.
     * @return The corresponding Account entity, or null if not found.
     */
    Account getAccount(HttpServletRequest request);

    /**
     * Gets the account ID associated with the specified subdomain.
     * @param subdomain The tenant's subdomain.
     * @return The account ID, or null if not found.
     */
    Long getAccountId(String subdomain);

    /**
     * Gets the account associated with a custom domain.
     * @param domain The custom domain.
     * @return The corresponding Account entity, or null if not found.
     */
    Account getAccountByCustomDomain(String domain);

    /**
     * Sets the default account for the system.
     * @param account The account to set as default.
     */
    void setDefaultAccount(Account account);

    /**
     * Gets the default account for the system.
     * @return The default Account entity.
     */
    Account getDefaultAccount();

    /**
     * Initializes a new account with default values.
     * @return A new initialized Account entity.
     */
    Account initAccount();

    /**
     * Calculates and updates the payment value for the account.
     * @param account The account to compute the payment value for.
     */
    void computeAccountPaymentValue(Account account);

    /**
     * Initializes the Account entity with default or required values.
     * @param entity The Account entity to initialize.
     */
    void initAccount(Account entity);

    /**
     * Initializes the account from an AccountDTO.
     * @param accountDTO The DTO containing account data.
     */
    void initAccount(AccountDTO accountDTO);

    /**
     * Updates the statistics associated with the account.
     * @param a The account whose statistics will be updated.
     */
    void updateStats(Account a);

    /**
     * Updates the statistics associated with all accounts in the system.
     */
    void updateAllAccountsStats();

    /**
     * Updates the provided statistics for the given account.
     * @param a The account whose statistics will be updated.
     * @param stats List of statistics to update.
     */
    void updateStats(Account a, List<AccountStats> stats);

    /**
     * Finds all accounts that are eligible for payment processing.
     * @return List of payable accounts.
     */
    List<Account> findPayableAccounts();

    /**
     * Finds the last payment made for the specified account.
     * @param account The account to search payments for.
     * @return The last AccountPayment, or null if none exists.
     */
    AccountPayment findLastPayment(Account account);

    /**
     * Checks if the specified account is overdue.
     * @param account The account to check.
     * @return True if the account is overdue, false otherwise.
     */
    boolean isOverdue(Account account);

    /**
     * Determines if the specified account should be suspended.
     * @param account The account to check.
     * @return True if the account should be suspended, false otherwise.
     */
    boolean shouldBeSuspended(Account account);

    /**
     * Attempts to charge the specified account.
     * @param account The account to charge.
     * @return True if the charge was successful, false otherwise.
     */
    boolean chargeAccount(Account account);

    /**
     * Computes and updates the expiration date for the specified account.
     * @param account The account to update.
     */
    void computeExpirationDate(Account account);

    /**
     * Checks the validity and status of the given payment.
     * @param payment The payment to check.
     */
    void checkPayment(AccountPayment payment);

    /**
     * Computes and updates the balance for the specified account.
     * @param account The account to compute the balance for.
     */
    void computeBalance(Account account);

    /**
     * Checks if the specified account is about to expire.
     * @param account The account to check.
     * @return True if the account is about to expire, false otherwise.
     */
    boolean isAboutToExpire(Account account);

    /**
     * Gets the payment value for the specified account.
     * @param account The account to retrieve the payment value for.
     * @return The payment value as a BigDecimal.
     */
    BigDecimal getPaymentValue(Account account);

    /**
     * Gets the account associated with the specified account ID.
     * @param accountId The account ID.
     * @return The corresponding Account entity, or null if not found.
     */
    Account getAccountById(Long accountId);

    /**
     * Gets the account ID associated with a custom domain.
     * @param domain The custom domain.
     * @return The account ID, or null if not found.
     */
    Long getAccountIdByCustomDomain(String domain);

    /**
     * Gets the account associated with the specified name.
     * @param name The name of the account.
     * @return The corresponding Account entity, or null if not found.
     */
    Account getAccountByName(String name);

    /**
     * Logs a message for the specified account.
     * @param account The account to log the message for.
     * @param message The message to log.
     */
    void log(Account account, String message);

    /**
     * Finds all payments associated with the specified account.
     * @param account The account to retrieve payments for.
     * @return List of AccountPayment entities.
     */
    List<AccountPayment> findAllPayments(Account account);

    /**
     * Clears the cache for all accounts.
     */
    void clearCache();

    /**
     * Clears the cache for a specific account identified by accountId and accountDomain.
     * @param accountId The ID of the account.
     * @param accountDomain The domain of the account.
     */
    void clearCache(Long accountId, String accountDomain);

    /**
     * Clears the cache for the specified account.
     * @param account The account to clear the cache for.
     */
    void clearCache(Account account);
}
