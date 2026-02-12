
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

package tools.dynamia.modules.email.services.impl;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.commons.SimpleCache;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.contraints.EmailValidator;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.scheduling.SchedulerUtil;
import tools.dynamia.integration.scheduling.TaskException;
import tools.dynamia.integration.scheduling.TaskWithResult;
import tools.dynamia.modules.email.EmailAttachment;
import tools.dynamia.modules.email.EmailMessage;
import tools.dynamia.modules.email.EmailSendResult;
import tools.dynamia.modules.email.EmailServiceException;
import tools.dynamia.modules.email.EmailServiceListener;
import tools.dynamia.modules.email.EmailTemplateModelProvider;
import tools.dynamia.modules.email.domain.EmailAccount;
import tools.dynamia.modules.email.domain.EmailAddress;
import tools.dynamia.modules.email.domain.EmailMessageLog;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.email.services.EmailService;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.templates.TemplateEngine;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author Mario Serrano Leones
 */
@Service
public class EmailServiceImpl implements EmailService {

    private final SimpleCache<Long, MailSender> MAIL_SENDERS = new SimpleCache<>();


    private final TemplateEngine templateEngine;
    private final CrudService crudService;
    private final AccountServiceAPI accountServiceAPI;

    private EmailValidator emailValidator = new EmailValidator();

    public EmailServiceImpl(TemplateEngine templateEngine, CrudService crudService, AccountServiceAPI accountServiceAPI) {
        this.templateEngine = templateEngine;
        this.crudService = crudService;
        this.accountServiceAPI = accountServiceAPI;
    }

    private final LoggingService logger = new SLF4JLoggingService(EmailService.class);

    @Override
    public CompletableFuture<EmailSendResult> send(String to, String subject, String content) {
        return send(new EmailMessage(to, subject, content));
    }

    public CompletableFuture<EmailSendResult> send(final EmailMessage mailMessage) {
        try {

            loadEmailAccount(mailMessage);
            return SchedulerUtil.runWithResult(new TaskWithResult<>() {
                @Override
                public EmailSendResult doWorkWithResult() {
                    return sendAndWait(mailMessage);
                }
            });

        } catch (ValidationError e) {
            return CompletableFuture.completedFuture(new EmailSendResult(mailMessage, false, e.getMessage()));
        }
    }

    @Override
    public EmailSendResult sendAndWait(final EmailMessage mailMessage) {
        EmailSendResult result = null;

        try {
            EmailAccount emailAccount = loadEmailAccount(mailMessage);

            if (mailMessage.getTemplate() == null && mailMessage.getTemplateName() != null
                    && !mailMessage.getTemplateName().isEmpty()) {
                mailMessage.setTemplate(getTemplateByName(mailMessage.getTemplateName(), true, emailAccount.getAccountId()));
            }

            if (mailMessage.getTemplate() != null && !mailMessage.getTemplate().isEnabled()) {
                if (mailMessage.isTemplateOptional()) {
                    mailMessage.setTemplate(null);
                } else {
                    String msg = "Template " + mailMessage.getTemplate().getName() + " is not Enabled";
                    logger.warn(msg);
                    result = new EmailSendResult(mailMessage, false, msg);
                }
            }

            if (result == null) {
                logger.info("Sending e-mail " + mailMessage);
                result = processAndSendEmail(mailMessage, emailAccount);
            }
        } catch (ValidationError e) {
            result = new EmailSendResult(mailMessage, false, e.getMessage());
        } catch (TaskException e) {
            logger.error("Error sending email task", e);
            result = new EmailSendResult(mailMessage, e);
        }
        logEmailResult(result);
        return result;
    }

    private void logEmailResult(EmailSendResult result) {
        var log = new EmailMessageLog(result.getMessage());
        log.save();

    }

    private EmailAccount loadEmailAccount(EmailMessage mailMessage) {
        if (mailMessage.getAccountId() == null) {
            mailMessage.setAccountId(accountServiceAPI.getCurrentAccountId());
        }

        EmailAccount emailAccount = mailMessage.getMailAccount();
        if (emailAccount == null && mailMessage.isNotification()) {
            emailAccount = mailMessage.getAccountId() != null ? getNotificationEmailAccount(mailMessage.getAccountId()) : getNotificationEmailAccount();
        }

        if (emailAccount == null) {
            emailAccount = mailMessage.getAccountId() != null ? getPreferredEmailAccount(mailMessage.getAccountId()) : getPreferredEmailAccount();
        }

        if (emailAccount == null) {
            throw new ValidationError("No email account to send: " + mailMessage);
        } else {
            mailMessage.setMailAccount(emailAccount);
        }
        return emailAccount;
    }


    private EmailSendResult processAndSendEmail(EmailMessage mailMessage, EmailAccount emailAccount) {
        try {
            logger.info("Proccesing email message using account: " + emailAccount);
            if (mailMessage.getTemplate() != null) {
                processTemplate(mailMessage);
            }

            fireOnMailProcessing(mailMessage);

            JavaMailSenderImpl jmsi = (JavaMailSenderImpl) createMailSender(emailAccount);
            MimeMessage mimeMessage = jmsi.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            String[] tosAsArray = validateEmails(mailMessage.getTosAsArray());
            if (mailMessage.getTo() != null && !mailMessage.getTo().isEmpty()) {
                mailMessage.setTo(mailMessage.getTo().replace(";", ","));
                helper.setTo(mailMessage.getTo().split(","));
            } else {
                if (!mailMessage.getTos().isEmpty())
                    helper.setTo(tosAsArray[0].toString());
            }

            if (!mailMessage.getTos().isEmpty()) {
                helper.setTo(validateEmails(tosAsArray));
            }
            String from = emailAccount.getFromAddress();
            String personal = emailAccount.getName();
            if (from != null && personal != null) {
                helper.setFrom(from, personal);
            }

            if (!mailMessage.getBccs().isEmpty()) {
                helper.setBcc(validateEmails(mailMessage.getBccsAsArray()));
            }

            if (!mailMessage.getCcs().isEmpty()) {
                helper.setCc(validateEmails(mailMessage.getCcsAsArray()));
            }

            helper.setSubject(mailMessage.getSubject());
            if (mailMessage.getPlainText() != null && mailMessage.getContent() != null) {
                helper.setText(mailMessage.getPlainText(), mailMessage.getContent());
            } else {
                helper.setText(mailMessage.getContent(), true);
            }

            if (mailMessage.getReplyTo() != null && !mailMessage.getReplyTo().isEmpty()) {
                helper.setReplyTo(mailMessage.getReplyTo());
            }

            for (EmailAttachment archivo : mailMessage.getAttachments()) {
                helper.addAttachment(archivo.getName(), archivo.getFile());
            }

            fireOnMailSending(mailMessage);

            jmsi.send(mimeMessage);

            logger.info("Email sended succesfull!");
            mailMessage.setSended(true);
            mailMessage.setMailAccount(emailAccount);
            fireOnMailSended(mailMessage);
            logEmailAddress(emailAccount, mailMessage);
            return new EmailSendResult(mailMessage, true, "ok");
        } catch (Exception me) {
            me.printStackTrace();
            logger.error("Error sending e-mail " + mailMessage, me);
            fireOnMailSendFail(mailMessage, me);
            return new EmailSendResult(mailMessage, new EmailServiceException("Error sending mail message " + mailMessage, me));
        }
    }

    private String[] validateEmails(String[] bccsAsArray) {
        String[] array = Arrays.asList(bccsAsArray).stream().flatMap(e -> Arrays.stream(e.split(",")))
                .map(String::trim).filter(e -> emailValidator.isValid(e, null)).toArray(String[]::new);
        return array;
    }

    @Override
    public EmailAccount getPreferredEmailAccount() {
        return getPreferredEmailAccount(accountServiceAPI.getCurrentAccountId());
    }

    @Override
    public EmailAccount getPreferredEmailAccount(Long accountId) {
        QueryParameters params = QueryParameters.with("preferred", true);
        if (accountId != null) {
            params.add("accountId", accountId);
        }

        EmailAccount account = crudService.findSingle(EmailAccount.class, params);
        if (account == null) {
            logger.warn("There is not a preferred email account, trying to get System Account email account ");
            Long systemAccountId = accountServiceAPI.getSystemAccountId();
            if (systemAccountId != null) {
                account = crudService.findSingle(EmailAccount.class,
                        QueryParameters.with("accountId", systemAccountId).add("preferred", true));
            }
        }
        return account;
    }


    @Override
    public EmailAccount getNotificationEmailAccount() {
        return getNotificationEmailAccount(accountServiceAPI.getCurrentAccountId());
    }

    @Override
    public EmailAccount getNotificationEmailAccount(Long accountId) {
        QueryParameters params = QueryParameters.with("notifications", true);
        if (accountId != null) {
            params.add("accountId", accountId);
        }

        EmailAccount account = crudService.findSingle(EmailAccount.class, params);
        if (account == null) {
            logger.warn("There is not a notifications email account, trying to get System Account email account ");
            Long systemAccountId = accountServiceAPI.getSystemAccountId();
            if (systemAccountId != null) {
                account = crudService.findSingle(EmailAccount.class,
                        QueryParameters.with("accountId", systemAccountId).add("notifications", true));
            }
        }
        return account;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setPreferredEmailAccount(EmailAccount account) {
        crudService.batchUpdate(EmailAccount.class, "preferred", false, QueryParameters.with("accountId", account.getAccountId()));
        crudService.updateField(account, "preferred", true);
    }

    @Override
    public EmailTemplate getTemplateByName(String name, boolean autocreate) {
        return getTemplateByName(name, autocreate, accountServiceAPI.getCurrentAccountId());
    }

    @Override
    public EmailTemplate getTemplateByName(String name, boolean autocreate, Long accountId) {
        EmailTemplate template = crudService.findSingle(EmailTemplate.class, QueryParameters.with("accountId", accountId)
                .add("name", QueryConditions.eq(name)));

        if (template == null) {
            logger.warn("There is not a template with name " + name + ", trying to get System Account template ");
            Long systemAccountId = accountServiceAPI.getSystemAccountId();
            if (systemAccountId != null) {
                template = crudService.findSingle(EmailTemplate.class,
                        QueryParameters.with("accountId", systemAccountId)
                                .add("name", QueryConditions.eq(name)));
            }
        }

        if (template == null && autocreate) {
            template = new EmailTemplate();
            template.setName(name);
            template.setAccountId(accountId);
            template.setEnabled(false);
            template.setContent("<empty>");
            template.setSubject(name);
            template.setDescription("autocreated template");
            template = crudService.create(template);
        }

        return template;
    }

    @Override
    public EmailTemplate getTemplateByName(String name) {
        return getTemplateByName(name, true);
    }

    private MailSender createMailSender(EmailAccount account) {
        JavaMailSenderImpl mailSender = (JavaMailSenderImpl) MAIL_SENDERS.get(account.getId());
        if (mailSender == null) {
            logger.info("Creating Mail Sender for: " + account);
            mailSender = new JavaMailSenderImpl();
            mailSender.setHost(account.getServerAddress());
            mailSender.setPort(account.getPort());
            mailSender.setUsername(account.getUsername());
            mailSender.setPassword(account.getPassword());
            mailSender.setProtocol("smtp");
            if (account.getEnconding() != null && !account.getEnconding().isEmpty()) {
                mailSender.setDefaultEncoding(account.getEnconding());
            }

            Properties jmp = new Properties();
            jmp.setProperty("mail.smtp.auth", String.valueOf(account.isLoginRequired()));
            jmp.setProperty("mail.smtp.from", account.getFromAddress());
            jmp.setProperty("mail.smtp.port", String.valueOf(account.getPort()));
            if (account.isUseTTLS()) {
                jmp.setProperty("mail.smtp.starttls.enable", "true");
                jmp.setProperty("mail.smtp.starttls.required", "true");
                jmp.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
            } else if (account.isUseSSL()) {
                jmp.setProperty("mail.smtp.ssl.enable", "true");
                jmp.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
            }
            jmp.setProperty("mail.smtp.host", account.getServerAddress());
            jmp.setProperty("mail.from", account.getFromAddress());
            jmp.setProperty("mail.personal", account.getName());

            mailSender.setJavaMailProperties(jmp);

            MAIL_SENDERS.add(account.getId(), mailSender);
        }

        return mailSender;

    }

    public void processTemplate(EmailMessage message) {


        if (message.getTemplate() == null) {
            throw new EmailServiceException(message + " has no template to process");
        }

        EmailTemplate template = message.getTemplate();
        logger.info("Processing template " + template);

        Map<String, Object> context = new HashMap<>();

        // Load model from providers
        if (message.getSource() != null && !message.getSource().isEmpty()) {
            Containers.get().findObjects(EmailTemplateModelProvider.class, object -> object.getSource().equals(message.getSource()))
                    .forEach(p -> {
                        Map<String, Object> model = p.getModel(message);
                        if (model != null) {
                            context.putAll(model);
                        }
                    });
            ;
        }

        // Load message models, can override providers models
        if (message.getTemplateModel() != null) {
            for (Entry<String, Object> entry : message.getTemplateModel().entrySet()) {
                context.put(entry.getKey(), entry.getValue());
            }
        }

        message.setSubject(parse(template.getSubject(), context));

        String content = parse(template.getContent(), context);
        if (template.getParent() != null) {
            context.put("TEMPLATE_CONTENT", content);
            content = parse(template.getParent().getContent(), context);
        }

        message.setContent(content);

        String[] tos = parseDestination(template.getTo(), context);
        if (tos != null) {
            for (String to : tos) {
                if (to != null && !to.isEmpty()) {
                    message.addTo(to);
                }
            }
        }

        String[] ccs = parseDestination(template.getCc(), context);
        if (ccs != null) {
            for (String cc : ccs) {
                if (cc != null && !cc.isEmpty()) {
                    message.addCc(cc);
                }
            }
        }

        String[] bccs = parseDestination(template.getBcc(), context);
        if (bccs != null) {
            for (String bcc : bccs) {
                if (bcc != null && !bcc.isEmpty()) {
                    message.addBcc(bcc);
                }
            }
        }

        String replyTo = parse(template.getReplyTo(), context);
        if (replyTo != null && !replyTo.isBlank()) {
            message.setReplyTo(replyTo);
        }
    }

    private String parse(String templateString, Map<String, Object> context) {
        if (templateString != null && !templateString.isBlank()) {
            return templateEngine.evaluate(templateString, context);
        } else {
            return templateString;
        }
    }

    private String[] parseDestination(String destination, Map<String, Object> context) {
        if (destination != null && !destination.isBlank()) {
            destination = parse(destination, context);
            if (destination.contains(",")) {
                return StringUtils.split(destination, ",");
            } else {
                return new String[]{destination};
            }
        }
        return null;
    }

    private void fireOnMailProcessing(EmailMessage message) {
        Collection<EmailServiceListener> listeners = Containers.get().findObjects(EmailServiceListener.class);
        for (EmailServiceListener listener : listeners) {
            listener.onMailProcessing(message);
        }
    }

    private void fireOnMailSending(EmailMessage message) {
        Collection<EmailServiceListener> listeners = Containers.get().findObjects(EmailServiceListener.class);
        for (EmailServiceListener listener : listeners) {
            listener.onMailSending(message);
        }
    }

    private void fireOnMailSended(EmailMessage message) {
        Collection<EmailServiceListener> listeners = Containers.get().findObjects(EmailServiceListener.class);
        for (EmailServiceListener listener : listeners) {
            listener.onMailSended(message);
        }
    }

    private void fireOnMailSendFail(EmailMessage message, Throwable cause) {
        Collection<EmailServiceListener> listeners = Containers.get().findObjects(EmailServiceListener.class);
        for (EmailServiceListener listener : listeners) {
            listener.onMailSendFail(message, cause);
        }
    }



    @Override
    public void logEmailAddress(EmailAccount emailAccount, EmailMessage message) {
        try {
            crudService.executeWithinTransaction(() -> {
                Set<String> addresses = new HashSet<>();
                addresses.add(message.getTo());
                addresses.addAll(message.getTos());
                addresses.addAll(message.getBccs());
                addresses.addAll(message.getCcs());

                addresses.forEach(a -> logEmailAddress(emailAccount, a, message.getTag()));
            });
        } catch (Exception e) {
            logger.error("Error logging email addresses: " + message);
            e.printStackTrace();
        }

    }

    @Override
    @Transactional
    public void logEmailAddress(EmailAccount account, String address, String tag) {
        try {
            EmailAddress emailAddress = getEmailAddress(address);
            if (emailAddress == null) {
                emailAddress = new EmailAddress(address);
                emailAddress.setSendCount(1);
                emailAddress.setTag(tag);
                emailAddress.setAccountId(account.getAccountId());
                emailAddress.save();
                logger.info("Logging Email Address: " + address);
            } else {
                crudService.increaseCounter(emailAddress, "sendCount");
                logger.info("Updating Send count Email Address: " + address);
            }
        } catch (Exception e) {
            logger.error("Error loggin email address; " + address, e);
        }
    }

    @Override
    public EmailAddress getEmailAddress(String address) {
        return crudService.findSingle(EmailAddress.class, "email", QueryConditions.eq(address));
    }

    @Override
    public void clearCache(EmailAccount account) {
        logger.info("Removing mail sender cache for " + account);
        MAIL_SENDERS.remove(account.getId());
    }
}
