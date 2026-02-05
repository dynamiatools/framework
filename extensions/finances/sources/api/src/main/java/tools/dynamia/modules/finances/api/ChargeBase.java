package tools.dynamia.modules.finances.api;

/**
 * Enumeration defining the calculation base for applying charges.
 * Determines which amount to use when calculating percentage-based charges.
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public enum ChargeBase {
    /**
     * Net amount - Base amount before any charges
     */
    NET,

    /**
     * Gross amount - Amount including previous charges
     */
    GROSS,

    /**
     * Previous total - Uses the result of the previous calculation step
     * Useful for cascading charges
     */
    PREVIOUS_TOTAL
}
