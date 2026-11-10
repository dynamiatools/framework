package mybookstore;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.security.services.SecurityService;
import tools.dynamia.modules.saas.domain.AccountType;
import tools.dynamia.modules.saas.api.enums.AccountPeriodicity;
import tools.dynamia.modules.saas.api.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * Creates the customer account of the demo (subdomain "demo": open http://demo.localhost:8484) and its email
 * template. The "System" account that the saas module creates is only for administrators and the template editor
 * never changes its templates, so the sample data lives in this account.
 */
@Component
@Order(1)
public class InitDemoAccountCommandLineRunner implements CommandLineRunner {

    public static final String SUBDOMAIN = "demo";

    private final CrudService crudService;
    private final AccountServiceAPI accounts;
    private final SecurityService security;

    public InitDemoAccountCommandLineRunner(CrudService crudService, AccountServiceAPI accounts, SecurityService security) {
        this.security = security;
        this.crudService = crudService;
        this.accounts = accounts;
    }

    @Override
    public void run(String... args) {
        var type = new AccountType();
        type.setActive(true);
        type.setName("demo");
        type.setPublicType(true);
        type.setPeriodicity(AccountPeriodicity.UNLIMITED);
        type.setPrice(BigDecimal.ZERO);
        type.setDescription("Demo account type");
        type = crudService.save(type);

        var account = new Account();
        account.setType(type);
        account.setName("Demo Bookstore");
        account.setSubdomain(SUBDOMAIN);
        account.setEmail("demo@mybookstore.test");
        account.setTimeZone(TimeZone.getDefault().getID());
        account.setStatus(AccountStatus.ACTIVE);
        account.setStatusDate(LocalDateTime.now());
        account.setIdentification("DEMO-1");
        account.setAutoInit(true);
        account = crudService.save(account);

        var accountId = account.getId();
        // the default admin user and profiles are created per account (admin / adminadmin)
        accounts.runAsAccount(accountId, security::checkAccountDefaultsSettings);
        accounts.runAsAccount(accounts.getSystemAccountId(), security::checkAccountDefaultsSettings);
        accounts.runAsAccount(accountId, () -> createEmailTemplate(accountId));
    }

    private void createEmailTemplate(Long accountId) {
        // A classic Velocity template: open Email > Templates, use "Migrate to editor" and then "Design"
        var invoiceEmail = new EmailTemplate();
        invoiceEmail.setAccountId(accountId);
        invoiceEmail.setName("Invoice email");
        invoiceEmail.setDescription("Sent to a customer with the invoice. Try Migrate to editor, then Design.");
        invoiceEmail.setSubject("Your invoice $invoice.number");
        invoiceEmail.setContent("<h1>Hello $customer.firstName</h1>"
                + "<p>Thanks for your purchase. Your invoice <b>$invoice.number</b> total is $invoice.total.</p>"
                + "#foreach($line in $invoice.details)<p>$line.quantity x $line.book</p>#end");
        crudService.save(invoiceEmail);
    }
}
