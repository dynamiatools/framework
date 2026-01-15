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

package tools.dynamia.modules.email.services;

import tools.dynamia.modules.email.EmailMessage;
import tools.dynamia.modules.email.EmailSendResult;
import tools.dynamia.modules.email.domain.EmailAccount;
import tools.dynamia.modules.email.domain.EmailAddress;
import tools.dynamia.modules.email.domain.EmailTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * Email service for sending emails and managing email-related resources.
 * <p>
 * This service defines the contract to send emails synchronously and asynchronously, resolve
 * preferred/notification accounts, manage templates, log addresses, and control internal caches.
 * Implementations may rely on Spring's async and scheduling infrastructure.
 * </p>
 *
 * @author Mario Serrano Leones
 */
public interface EmailService {

    /**
     * Sends an email message asynchronously.
     * <p>
     * Default implementations are expected to use Spring's {@code @EnableAsync} and scheduling facilities.
     * Ensure your application has {@link org.springframework.scheduling.annotation.EnableAsync} and
     * {@link org.springframework.scheduling.annotation.EnableScheduling} enabled so the task executor handles
     * background execution properly.
     * </p>
     *
     * @param message Fully built {@link EmailMessage} to be sent, including recipients, subject, content,
     *                attachments, and any headers.
     * @return a {@link CompletableFuture} that completes with an {@link EmailSendResult} indicating success or failure.
     * The future is completed exceptionally if an unrecoverable error occurs during dispatch.
     */
    CompletableFuture<EmailSendResult> send(EmailMessage message);

    /**
     * Builds and sends an email message asynchronously based on simple inputs.
     * <p>
     * This is a convenience method that internally creates an {@link EmailMessage} with a single recipient,
     * subject, and content, and then delegates to {@link #send(EmailMessage)}.
     * </p>
     *
     * @param to      Recipient email address (e.g., "user@example.com"). Must be a valid RFC 5322 address.
     * @param subject Email subject line.
     * @param content Email body content. Implementations may treat this as plain text or HTML depending on configuration.
     * @return a {@link CompletableFuture} that completes with an {@link EmailSendResult} when sending finishes.
     */
    CompletableFuture<EmailSendResult> send(String to, String subject, String content);

    /**
     * Returns the default notification {@link EmailAccount} to be used for system-generated emails
     * in the current SaaS context.
     *
     * @return the configured notification email account, or {@code null} if none is configured.
     */
    EmailAccount getNotificationEmailAccount();

    /**
     * Returns the notification {@link EmailAccount} associated with the given SaaS account id.
     *
     * @param accountId SaaS account identifier.
     * @return the notification email account for the provided {@code accountId}, or {@code null} if not found.
     */
    EmailAccount getNotificationEmailAccount(Long accountId);

    /**
     * Sets the preferred {@link EmailAccount} for the current SaaS account.
     * Implementations should persist this preference so subsequent calls to
     * {@link #getPreferredEmailAccount()} return this account.
     *
     * @param account The {@link EmailAccount} to set as preferred. Must be a valid, enabled account.
     */
    void setPreferredEmailAccount(EmailAccount account);

    /**
     * Sends an email message synchronously and waits for the result.
     * <p>
     * Use this method when you need immediate feedback about delivery status. Consider timeouts and
     * potential blocking behavior in your calling thread.
     * </p>
     *
     * @param mailMessage Fully built {@link EmailMessage} to be sent.
     * @return the {@link EmailSendResult} containing delivery details, success flag, and error information if any.
     */
    EmailSendResult sendAndWait(EmailMessage mailMessage);

    /**
     * Returns the preferred {@link EmailAccount} for the current SaaS account.
     *
     * @return the preferred account, or {@code null} if none has been configured.
     */
    EmailAccount getPreferredEmailAccount();

    /**
     * Returns the preferred {@link EmailAccount} for the specified SaaS account id.
     *
     * @param accountId SaaS account identifier.
     * @return the preferred email account for {@code accountId}, or {@code null} if not configured.
     */
    EmailAccount getPreferredEmailAccount(Long accountId);

    /**
     * Finds an {@link EmailTemplate} by name within the current SaaS account.
     * If {@code autocreate} is {@code true} and the template does not exist, a new blank template will be created
     * and returned.
     *
     * @param name       Template name to search for.
     * @param autocreate Whether to create a new template if one is not found.
     * @return the existing or newly created {@link EmailTemplate}, or {@code null} if not found and {@code autocreate}
     * is {@code false}.
     */
    EmailTemplate getTemplateByName(String name, boolean autocreate);

    /**
     * Finds an {@link EmailTemplate} by name for a specific SaaS account id.
     * If {@code autocreate} is {@code true} and the template does not exist, a new blank template will be created
     * under that account.
     *
     * @param name       Template name to search for.
     * @param autocreate Whether to create a new template if one is not found.
     * @param accountId  SaaS account identifier.
     * @return the existing or newly created {@link EmailTemplate}, or {@code null} if not found and {@code autocreate}
     * is {@code false}.
     */
    EmailTemplate getTemplateByName(String name, boolean autocreate, Long accountId);

    /**
     * Finds an {@link EmailTemplate} by name using default lookup rules for the current SaaS account.
     *
     * @param name Template name to search for.
     * @return the matching {@link EmailTemplate}, or {@code null} if none exists.
     */
    EmailTemplate getTemplateByName(String name);

    /**
     * Logs all email addresses present in the given {@link EmailMessage}.
     * <p>
     * Implementations should inspect TO, CC, BCC, REPLY-TO (and possibly FROM) fields and persist or update
     * a registry of known {@link EmailAddress} entries associated with the supplied {@link EmailAccount}.
     * </p>
     *
     * @param account The {@link EmailAccount} context in which addresses will be logged.
     * @param message The {@link EmailMessage} whose addresses should be extracted and logged.
     */
    void logEmailAddress(EmailAccount account, EmailMessage message);

    /**
     * Logs a single email address with an optional tag for classification.
     *
     * @param account The {@link EmailAccount} context used to associate the address entry.
     * @param address A valid email address string to log.
     * @param tag     A marker or label (e.g., "customer", "supplier", "notification") to categorize the address.
     */
    void logEmailAddress(EmailAccount account, String address, String tag);

    /**
     * Retrieves a previously logged {@link EmailAddress} by its string representation.
     *
     * @param address Email address string to look up.
     * @return the {@link EmailAddress} entity if found; otherwise {@code null}.
     */
    EmailAddress getEmailAddress(String address);

    /**
     * Clears the internal mail-sender cache for the specified {@link EmailAccount}.
     * <p>
     * Implementations that cache mail sender instances (e.g., JavaMailSender) should discard and recreate them
     * after configuration changes like credentials, host, or port updates.
     * </p>
     *
     * @param account The email account whose cache entries should be invalidated. Must not be {@code null}.
     */
    void clearCache(EmailAccount account);
}
