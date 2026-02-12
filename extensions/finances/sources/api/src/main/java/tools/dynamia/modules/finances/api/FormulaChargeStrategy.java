package tools.dynamia.modules.finances.api;

/**
 * Strategy for calculating formula-based charges.
 * This is a placeholder for custom formulas that can be implemented by extending this class.
 *
 * <p>Subclasses should override the calculate method to implement specific formula logic.</p>
 *
 * <p>Example implementation:</p>
 * <pre>{@code
 * public class ProgressiveDiscountStrategy extends FormulaChargeStrategy {
 *     @Override
 *     public Money calculate(Charge charge, Money base, Object context) {
 *         // Progressive discount based on amount
 *         if (base.getAmount().compareTo(new BigDecimal("1000")) > 0) {
 *             return base.multiply(0.10);
 *         } else if (base.getAmount().compareTo(new BigDecimal("500")) > 0) {
 *             return base.multiply(0.05);
 *         }
 *         return Money.zero(base.getCurrencyCode());
 *     }
 * }
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public class FormulaChargeStrategy implements ChargeStrategy {

    @Override
    public Money calculate(Charge charge, Money base, Object context) {
        // Default implementation returns zero
        // Subclasses should override this method with specific formula logic
        return Money.zero(base != null ? base.getCurrencyCode() : "USD");
    }

    @Override
    public boolean supports(RateType rateType) {
        return rateType == RateType.FORMULA;
    }
}
