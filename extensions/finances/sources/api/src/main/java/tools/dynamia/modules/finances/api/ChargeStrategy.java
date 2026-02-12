package tools.dynamia.modules.finances.api;

/**
 * Strategy interface for calculating different types of charges.
 * Implementations handle specific rate types (percentage, fixed, formula).
 *
 * <p>This allows the framework to be extended with custom calculation logic
 * without modifying the core calculation engine.</p>
 *
 * <p>Example implementation:</p>
 * <pre>{@code
 * public class CustomChargeStrategy implements ChargeStrategy {
 *     @Override
 *     public Money calculate(Charge charge, Money base, FinancialDocument document) {
 *         // Custom calculation logic
 *         return base.multiply(charge.getValue());
 *     }
 *
 *     @Override
 *     public boolean supports(RateType rateType) {
 *         return rateType == RateType.FORMULA;
 *     }
 * }
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public interface ChargeStrategy {

    /**
     * Calculates the charge amount based on the provided base amount.
     *
     * @param charge the charge to calculate
     * @param base the base amount to apply the charge to
     * @param context the calculation context (document or line)
     * @return the calculated charge amount
     */
    Money calculate(Charge charge, Money base, Object context);

    /**
     * Checks if this strategy supports the given rate type.
     *
     * @param rateType the rate type to check
     * @return true if this strategy can handle the rate type
     */
    boolean supports(RateType rateType);
}
