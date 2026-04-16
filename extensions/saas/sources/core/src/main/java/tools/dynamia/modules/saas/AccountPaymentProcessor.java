package tools.dynamia.modules.saas;

/**
 * Basic API to implement processing account payments. You should implement
 * your own payment processor and register it using spring annotations
 * registry.
 */
public interface AccountPaymentProcessor {

    String getId();

    String getName();

    default String getExtra0Label() {
        return "Extra 0";
    }

    default String getExtra1Label() {
        return "Extra 1";
    }

    default String getExtra2Label() {
        return "Extra 2";
    }

    default String getExtra3Label() {
        return "Extra 3";
    }

}
