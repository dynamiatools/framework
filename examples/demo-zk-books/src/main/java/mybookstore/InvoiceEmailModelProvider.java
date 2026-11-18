package mybookstore;

import org.springframework.stereotype.Component;
import tools.dynamia.modules.email.EmailMessage;
import tools.dynamia.modules.email.EmailTemplateModelProvider;
import tools.dynamia.modules.email.TemplateVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Model of the "invoice" email templates. The variables below are what the block editor lists in its
 * variables panel and uses to fill the preview.
 */
@Component
public class InvoiceEmailModelProvider implements EmailTemplateModelProvider {

    public static final String SOURCE = "invoice";

    @Override
    public String getSource() {
        return SOURCE;
    }

    @Override
    public Map<String, Object> getModel(EmailMessage message) {
        // the sender puts the invoice in the message's template params, see EmailMessage.getTemplateModel()
        return Map.of();
    }

    @Override
    public List<TemplateVariable> getVariables() {
        return List.of(
                new TemplateVariable("customer.firstName", "Customer name", "Ada"),
                new TemplateVariable("invoice.number", "Invoice number", "INV-0001"),
                new TemplateVariable("invoice.total", "Invoice total", BigDecimal.valueOf(125)),
                new TemplateVariable("invoice.details", "Invoice lines", List.of(
                        Map.of("book", "Clean Code", "quantity", 1, "subtotal", 75),
                        Map.of("book", "Dune", "quantity", 2, "subtotal", 50))));
    }
}
