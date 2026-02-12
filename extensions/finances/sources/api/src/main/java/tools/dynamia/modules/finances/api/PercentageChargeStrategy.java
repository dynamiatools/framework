package tools.dynamia.modules.finances.api;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Strategy for calculating percentage-based charges.
 * Applies a percentage rate to the base amount.
 *
 * <p>Example: A 19% VAT on $100 results in $19</p>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public class PercentageChargeStrategy implements ChargeStrategy {

    @Override
    public Money calculate(Charge charge, Money base, Object context) {
        if (charge.getValue() == null || base == null) {
            return Money.zero(base != null ? base.getCurrencyCode() : "USD");
        }

        // Calculate percentage: base × (value / 100)
        BigDecimal percentage = charge.getValue()
            .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);

        Money result = base.multiply(percentage);

        // Round to currency precision
        return result.roundToCurrency();
    }

    @Override
    public boolean supports(RateType rateType) {
        return rateType == RateType.PERCENTAGE;
    }
}
