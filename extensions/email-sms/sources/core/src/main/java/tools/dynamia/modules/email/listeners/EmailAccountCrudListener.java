package tools.dynamia.modules.email.listeners;

import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.integration.sterotypes.Listener;
import tools.dynamia.modules.email.domain.EmailAccount;
import tools.dynamia.modules.email.services.EmailService;

@Listener
public class EmailAccountCrudListener extends CrudServiceListenerAdapter<EmailAccount> {

    private final EmailService emailService;

    public EmailAccountCrudListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void afterUpdate(EmailAccount entity) {
        if (entity != null) {
            emailService.clearCache(entity);
        }
    }
}
