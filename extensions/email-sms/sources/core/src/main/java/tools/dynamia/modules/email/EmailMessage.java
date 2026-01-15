
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

package tools.dynamia.modules.email;

import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.contraints.EmailValidator;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.scheduling.SchedulerUtil;
import tools.dynamia.modules.email.domain.EmailAccount;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.email.services.EmailService;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents an email message that can be sent with various recipients, subject, content, and attachments.
 * The email message can also be associated with an email account, email template, and other additional properties.
 */
public class EmailMessage implements Serializable {


    public static boolean isEmailAddress(String email) {
        return email != null && !email.isBlank() && new EmailValidator().isValid(email);
    }


    /**
     *
     */
    private static final long serialVersionUID = -7780849220746433667L;
    private final Set<String> tos = new HashSet<>();
    private final Set<String> ccs = new HashSet<>();
    private final Set<String> bccs = new HashSet<>();
    private String to;
    private String subject;
    private String content;
    private String plainText;
    private final List<EmailAttachment> attachments = new ArrayList<>();
    private EmailAccount mailAccount;
    private EmailTemplate template;
    private String templateName;
    private String source;
    private String replyTo;
    private String tag;
    private Map<String, Object> templateModel = new HashMap<>();
    private boolean sended;
    private boolean templateOptional;
    private Long accountId;

    private boolean notification;
    private String notificationUuid;

    /**
     * Represents an email message that can be sent.
     */
    public EmailMessage() {
    }

    /**
     * Represents an email message that can be sent.
     *
     * @param template the email template to be used for creating the message
     */
    public EmailMessage(EmailTemplate template) {
        super();
        this.template = template;
    }

    /**
     * Represents an email message that can be sent.
     *
     * @param to      the email address of the recipient
     * @param subject the subject of the email
     * @param content the content of the email
     */
    public EmailMessage(String to, String subject, String content) {
        this.to = to;
        this.subject = subject;
        this.content = content;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTemplateName() {
        return templateName;
    }

    /**
     * Sets the template name for the email message.
     *
     * @param templateName the name of the template to be used for creating the email message
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Map<String, Object> getTemplateModel() {
        return templateModel;
    }

    public void setTemplateModel(Map<String, Object> templateModel) {
        this.templateModel = templateModel;
    }

    public EmailAccount getMailAccount() {
        return mailAccount;
    }

    /**
     * Sets the mail account for the email message.
     *
     * @param emailAccount the email account to be set as the sender of the email message
     */
    public void setMailAccount(EmailAccount emailAccount) {
        this.mailAccount = emailAccount;
    }

    public EmailTemplate getTemplate() {
        return template;
    }

    public void setTemplate(EmailTemplate template) {
        this.template = template;
    }

    public String getPlainText() {
        return plainText;
    }

    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTo() {
        return to;
    }

    public void addTo(String to) {
        tos.add(to);
    }

    public void addCc(String cc) {
        ccs.add(cc);
    }

    public void addBcc(String bcc) {
        bccs.add(bcc);
    }

    public void addAttachment(File file) {
        attachments.add(new EmailAttachment(file));
    }

    public void addAttachment(EmailAttachment attachment) {
        attachments.add(attachment);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<String> getTos() {
        return tos;
    }

    public String[] getTosAsArray() {
        return getTos().toArray(new String[tos.size()]);
    }

    public Set<String> getCcs() {
        return ccs;
    }

    public String[] getCcsAsArray() {
        return getCcs().toArray(new String[ccs.size()]);
    }

    public Set<String> getBccs() {
        return bccs;
    }

    public String[] getBccsAsArray() {
        return getBccs().toArray(new String[bccs.size()]);
    }

    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    @Override
    public String toString() {

        return String.format("To: %s, tos: %s, Subject: %s", to, tos.toString(), subject);
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    /**
     * Utility method for sendind this email message. It use @{@link EmailService} to send the message
     */
    public void send() {
        EmailService emailService = Containers.get().findObject(EmailService.class);
        if (emailService == null) {
            throw new ValidationError("No email service found");
        }

        emailService.send(this);
    }

    /**
     * Utility method for sendind this email message in a new thread. It use @{@link EmailService} to send the message
     */
    public void sendAsync() {
        SchedulerUtil.run(this::send);
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setSended(boolean sended) {
        this.sended = sended;
    }

    public boolean isSended() {
        return sended;
    }

    public boolean isTemplateOptional() {
        return templateOptional;
    }

    public void setTemplateOptional(boolean templateOptional) {
        this.templateOptional = templateOptional;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public String getNotificationUuid() {
        return notificationUuid;
    }

    public void setNotificationUuid(String notificationUuid) {
        this.notificationUuid = notificationUuid;
    }

    public Set<String> loadAllAddresses() {
        Set<String> all = new HashSet<>();
        all.addAll(getTos());
        all.addAll(getCcs());
        all.addAll(getBccs());
        return all;
    }
}
