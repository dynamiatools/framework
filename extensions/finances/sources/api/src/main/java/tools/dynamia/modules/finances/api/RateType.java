package tools.dynamia.modules.finances.api;

/**
 * Enumeration of rate types for charge calculations.
 * Defines how the charge value should be interpreted and applied.
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public enum RateType {
    /**
     * Percentage rate - Value represents a percentage to apply
     * Example: 19 means 19%
     */
    PERCENTAGE,

    /**
     * Fixed amount - Value represents a fixed monetary amount
     * Example: 50 means 50 in the document currency
     */
    FIXED,

    /**
     * Formula-based calculation - Value is calculated using a custom formula
     * The formula implementation is provided by a strategy
     */
    FORMULA
}
