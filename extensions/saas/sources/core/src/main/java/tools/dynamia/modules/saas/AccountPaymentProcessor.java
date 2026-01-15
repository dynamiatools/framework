package tools.dynamia.modules.saas;

import tools.dynamia.modules.saas.domain.AccountPayment;

import java.util.Map;

/**
 * Basic API to implement processing account payments. You should implement
 * your own payment processor and register it using spring annotations
 * registry.
 */
public interface AccountPaymentProcessor {

    String getId();

    String getName();

    Map<String, Object> processPayment(AccountPayment payment);
}
