package tools.dynamia.modules.finances.api;

/**
 * Enumeration of charge types that can be applied to financial documents.
 * This unified charge system handles taxes, discounts, withholdings, and fees.
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public enum ChargeType {
    /**
     * Tax charge - VAT, sales tax, excise duties, etc.
     */
    TAX,

    /**
     * Discount - Commercial, promotional, volume-based reductions
     */
    DISCOUNT,

    /**
     * Withholding - Income tax retention, social security deductions
     */
    WITHHOLDING,

    /**
     * Fee - Shipping, handling, environmental charges, etc.
     */
    FEE
}
