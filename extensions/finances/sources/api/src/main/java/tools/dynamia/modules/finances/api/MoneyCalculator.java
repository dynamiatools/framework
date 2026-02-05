package tools.dynamia.modules.finances.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Utility class for common monetary calculations and operations.
 * Provides static methods for rounding, currency operations, and formatting.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Money rounded = MoneyCalculator.round(money, 2);
 * Money sum = MoneyCalculator.sum(money1, money2, money3);
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public final class MoneyCalculator {

    private MoneyCalculator() {
        // Utility class, no instantiation
    }

    /**
     * Rounds a Money to the specified scale with HALF_UP rounding mode.
     *
     * @param money the Money to round
     * @param scale the number of decimal places
     * @return the rounded Money
     */
    public static Money round(Money money, int scale) {
        if (money == null) {
            throw new IllegalArgumentException("Money cannot be null");
        }
        return money.round(scale);
    }

    /**
     * Rounds a Money according to its currency's default fraction digits.
     *
     * @param money the Money to round
     * @return the rounded Money
     */
    public static Money roundToCurrency(Money money) {
        if (money == null) {
            throw new IllegalArgumentException("Money cannot be null");
        }
        return money.roundToCurrency();
    }

    /**
     * Sums multiple Money amounts. All amounts must have the same currency.
     *
     * @param amounts the Money amounts to sum
     * @return the sum of all amounts
     * @throws InvalidCurrencyOperationException if currencies don't match
     */
    public static Money sum(Money... amounts) {
        if (amounts == null || amounts.length == 0) {
            throw new IllegalArgumentException("At least one amount is required");
        }

        Money result = amounts[0];
        for (int i = 1; i < amounts.length; i++) {
            result = result.add(amounts[i]);
        }
        return result;
    }

    /**
     * Calculates the percentage of a Money amount.
     *
     * @param money the base Money
     * @param percentage the percentage to calculate (e.g., 19 for 19%)
     * @return the percentage amount
     */
    public static Money percentage(Money money, BigDecimal percentage) {
        if (money == null || percentage == null) {
            throw new IllegalArgumentException("Money and percentage cannot be null");
        }
        return money.percentage(percentage);
    }

    /**
     * Gets the default fraction digits for a currency code.
     *
     * @param currencyCode the ISO 4217 currency code
     * @return the default fraction digits, or 2 if currency is unknown
     */
    public static int getCurrencyScale(String currencyCode) {
        try {
            Currency currency = Currency.getInstance(currencyCode);
            return currency.getDefaultFractionDigits();
        } catch (IllegalArgumentException e) {
            return 2; // Default to 2 decimal places
        }
    }

    /**
     * Checks if two Money amounts are equal within a tolerance.
     *
     * @param m1 first Money
     * @param m2 second Money
     * @param tolerance the tolerance amount
     * @return true if amounts are equal within tolerance
     */
    public static boolean equals(Money m1, Money m2, Money tolerance) {
        if (m1 == null || m2 == null || tolerance == null) {
            return false;
        }

        Money diff = m1.subtract(m2).abs();
        return diff.compareTo(tolerance) <= 0;
    }

    /**
     * Returns the maximum of two Money amounts.
     *
     * @param m1 first Money
     * @param m2 second Money
     * @return the maximum Money
     */
    public static Money max(Money m1, Money m2) {
        if (m1 == null) return m2;
        if (m2 == null) return m1;
        return m1.compareTo(m2) >= 0 ? m1 : m2;
    }

    /**
     * Returns the minimum of two Money amounts.
     *
     * @param m1 first Money
     * @param m2 second Money
     * @return the minimum Money
     */
    public static Money min(Money m1, Money m2) {
        if (m1 == null) return m2;
        if (m2 == null) return m1;
        return m1.compareTo(m2) <= 0 ? m1 : m2;
    }
}
