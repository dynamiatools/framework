package tools.dynamia.modules.finances.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies charges using the appropriate strategy based on rate type.
 * Maintains a registry of strategies and delegates calculation to them.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * ChargeCalculator calculator = new ChargeCalculator();
 * Money chargeAmount = calculator.applyCharge(charge, baseAmount, context);
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public class ChargeCalculator {

    private final Map<RateType, ChargeStrategy> strategies;

    /**
     * Default constructor with standard strategies.
     */
    public ChargeCalculator() {
        this.strategies = new HashMap<>();
        registerDefaultStrategies();
    }

    /**
     * Registers the default charge strategies.
     */
    private void registerDefaultStrategies() {
        registerStrategy(new PercentageChargeStrategy());
        registerStrategy(new FixedChargeStrategy());
        registerStrategy(new FormulaChargeStrategy());
    }

    /**
     * Registers a custom charge strategy.
     *
     * @param strategy the strategy to register
     */
    public void registerStrategy(ChargeStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }

        // Register for all supported rate types
        for (RateType rateType : RateType.values()) {
            if (strategy.supports(rateType)) {
                strategies.put(rateType, strategy);
            }
        }
    }

    /**
     * Applies a charge to a base amount using the appropriate strategy.
     *
     * @param charge the charge to apply
     * @param base the base amount
     * @param context the calculation context (document or line)
     * @return the calculated charge amount
     * @throws FinancialCalculationException if no strategy is found
     */
    public Money applyCharge(Charge charge, Money base, Object context) {
        if (charge == null || base == null) {
            return Money.zero(base != null ? base.getCurrencyCode() : "USD");
        }

        ChargeStrategy strategy = strategies.get(charge.getRateType());
        if (strategy == null) {
            throw new FinancialCalculationException(
                "No strategy found for rate type: " + charge.getRateType()
            );
        }

        return strategy.calculate(charge, base, context);
    }

    /**
     * Gets the registered strategy for a rate type.
     *
     * @param rateType the rate type
     * @return the strategy, or null if not found
     */
    public ChargeStrategy getStrategy(RateType rateType) {
        return strategies.get(rateType);
    }
}
