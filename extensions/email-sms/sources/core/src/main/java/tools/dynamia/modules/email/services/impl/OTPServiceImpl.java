package tools.dynamia.modules.email.services.impl;

import tools.dynamia.integration.scheduling.SchedulerUtil;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.email.EmailMessage;
import tools.dynamia.modules.email.OTPMessage;
import tools.dynamia.modules.email.OTPSendResult;
import tools.dynamia.modules.email.SMSMessage;
import tools.dynamia.modules.email.domain.EmailAccount;
import tools.dynamia.modules.email.services.EmailService;
import tools.dynamia.modules.email.services.OTPService;
import tools.dynamia.modules.email.services.SMSService;

import java.util.concurrent.CompletableFuture;

@Service
public class OTPServiceImpl implements OTPService {

    private final EmailService emailService;
    private final SMSService smsService;

    public OTPServiceImpl(EmailService emailService, SMSService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    @Override
    public CompletableFuture<OTPSendResult> send(OTPMessage message) {
        final var emailAccount = message.getAccountId() != null ? emailService.getPreferredEmailAccount(message.getAccountId()) : emailService.getPreferredEmailAccount();
        final var emailMessage = buildEmailMessage(emailAccount, message);
        final var smsMessage = buildSMSMessage(emailAccount, message);

        return SchedulerUtil.runWithResult(() -> {
            boolean smsSended = false;
            boolean emailSended = false;
            String smsId = null;
            if (smsMessage != null) {
                smsId = smsService.send(smsMessage);
                smsSended = true;
            }

            if (emailMessage != null) {
                var emailResult = emailService.sendAndWait(emailMessage);
                emailSended = emailResult.isSended();
            }

            final boolean sended = smsSended || emailSended;


            return new OTPSendResult(message, sended, smsId, "Sended");
        });
    }

    private SMSMessage buildSMSMessage(EmailAccount emailAccount, OTPMessage message) {
        if (message.isSendSMS() && message.getTargetPhoneNumber() != null) {
            var sms = new SMSMessage(message.getTargetPhoneNumber(), message.getContent(), emailAccount.getSmsSenderID(), false);
            sms.setCredentials(emailAccount.getSmsUsername(), emailAccount.getSmsPassword(), emailAccount.getSmsRegion());
            if (emailAccount.getSmsDefaultPrefix() != null && !emailAccount.getSmsDefaultPrefix().isEmpty()) {
                String prefix = emailAccount.getSmsDefaultPrefix();
                if (!sms.getPhoneNumber().startsWith("+") && !sms.getPhoneNumber().startsWith(prefix)) {
                    sms.setPhoneNumber(prefix + sms.getPhoneNumber());
                }
            }
            sms.setAccountId(message.getAccountId());
            if (sms.getAccountId() == null) {
                sms.setAccountId(emailAccount.getId());
            }
            return sms;
        } else {
            return null;
        }
    }

    private EmailMessage buildEmailMessage(EmailAccount emailAccount, OTPMessage message) {
        if (message.isSendEmail() && message.getTargetEmail() != null && message.getEmailSubject() != null) {
            var msg = new EmailMessage(message.getTargetEmail(), message.getEmailSubject(), message.getContent());
            msg.setMailAccount(emailAccount);
            msg.setAccountId(message.getAccountId());
            if (msg.getAccountId() == null) {
                msg.setAccountId(emailAccount.getAccountId());
            }
            return msg;
        }
        return null;
    }
}
