package tools.dynamia.modules.finances.api;

/**
 * Strategy for calculating fixed amount charges.
 * Returns the charge value as a fixed amount regardless of the base.
 *
 * <p>Example: A $50 shipping fee is always $50</p>
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public class FixedChargeStrategy implements ChargeStrategy {

    @Override
    public Money calculate(Charge charge, Money base, Object context) {
        if (charge.getValue() == null || base == null) {
            return Money.zero(base != null ? base.getCurrencyCode() : "USD");
        }

        // Return fixed amount in the document currency
        Money result = Money.of(charge.getValue(), base.getCurrencyCode());

        // Round to currency precision
        return result.roundToCurrency();
    }

    @Override
    public boolean supports(RateType rateType) {
        return rateType == RateType.FIXED;
    }
}
