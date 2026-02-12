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

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.sterotypes.Listener;
import tools.dynamia.modules.email.domain.EmailAccount;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.email.services.SMSService;
import tools.dynamia.templates.TemplateEngine;

@Listener
public class AutoSendSMSFromEmailServiceListener extends EmailServiceListenerAdapter {


    private final SMSService smsService;
    private final TemplateEngine templateEngine;

    private LoggingService logger = new SLF4JLoggingService(AutoSendSMSFromEmailServiceListener.class);

    public AutoSendSMSFromEmailServiceListener(SMSService smsService, TemplateEngine templateEngine) {
        this.smsService = smsService;
        this.templateEngine = templateEngine;
    }

    @Override
    public void onMailSended(EmailMessage message) {

        if (message.getTemplate() != null && message.getTemplate().isSendSMS() && message.getMailAccount() != null && message.getMailAccount().isSmsEnabled()) {

            EmailTemplate template = message.getTemplate();
            EmailAccount account = message.getMailAccount();

            SMSMessage sms = new SMSMessage();
            sms.setPhoneNumber(parse(message, template.getSmsNumber()));
            sms.setText(parse(message, template.getSmsText()));
            sms.setCredentials(account.getSmsUsername(), account.getSmsPassword(), account.getSmsRegion());
            sms.setSenderID(account.getSmsSenderID());
            sms.setAccountId(account.getAccountId());

            if (account.getSmsDefaultPrefix() != null && !account.getSmsDefaultPrefix().isEmpty()) {
                String prefix = account.getSmsDefaultPrefix();
                if (!sms.getPhoneNumber().startsWith("+") && !sms.getPhoneNumber().startsWith(prefix)) {
                    sms.setPhoneNumber(prefix + sms.getPhoneNumber());
                }
            }
            try {
                if (sms.getUsername() != null && sms.getPassword() != null) {
                    smsService.send(sms);
                }
            } catch (Exception e) {
                logger.error("Error sending sms message from email " + message + " --> " + e.getMessage());
            }
        }
    }

    private String parse(EmailMessage message, String text) {
        return templateEngine.evaluate(text, message.getTemplateModel());
    }
}
