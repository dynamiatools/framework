package tools.dynamia.modules.finances.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Immutable value object representing a monetary amount with its currency.
 * Prevents accidental operations between different currencies and ensures
 * proper rounding according to currency precision.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Money price = Money.of("100.50", "USD");
 * Money quantity = Money.of("10", "USD");
 * Money total = price.multiply(quantity);
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public final class Money implements Serializable, Comparable<Money> {

    private final BigDecimal amount;
    private final String currencyCode;

    /**
     * Private constructor to enforce immutability.
     *
     * @param amount       the monetary amount
     * @param currencyCode the ISO 4217 currency code
     */
    private Money(BigDecimal amount, String currencyCode) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currencyCode, "Currency code cannot be null");

        this.amount = amount;
        this.currencyCode = currencyCode;
    }

    /**
     * Creates a Money instance from a BigDecimal amount and currency code.
     *
     * @param amount       the monetary amount
     * @param currencyCode the ISO 4217 currency code
     * @return a new Money instance
     */
    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, currencyCode);
    }

    /**
     * Creates a Money instance from a string amount and currency code.
     *
     * @param amount       the monetary amount as string
     * @param currencyCode the ISO 4217 currency code
     * @return a new Money instance
     */
    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), currencyCode);
    }

    /**
     * Creates a Money instance from a numeric amount and currency code.
     *
     * @param amount       the monetary amount
     * @param currencyCode the ISO 4217 currency code
     * @return a new Money instance
     */
    public static Money of(Number amount, String currencyCode) {
        return new Money(new BigDecimal(amount.toString()), currencyCode);
    }

    /**
     * Creates a Money instance from a double amount and currency code.
     *
     * @param amount       the monetary amount
     * @param currencyCode the ISO 4217 currency code
     * @return a new Money instance
     */
    public static Money of(double amount, String currencyCode) {
        return new Money(BigDecimal.valueOf(amount), currencyCode);
    }

    /**
     * Creates a Money instance from a long amount and currency code.
     *
     * @param amount       the monetary amount
     * @param currencyCode the ISO 4217 currency code
     * @return a new Money instance
     */
    public static Money of(long amount, String currencyCode) {
        return new Money(BigDecimal.valueOf(amount), currencyCode);
    }

    /**
     * Creates a Money instance with the specified amount in the current locale's currency.
     *
     * @param amount the monetary amount as string
     * @return a new Money instance
     */
    public static Money of(long amount) {
        return new Money(BigDecimal.valueOf(amount), getCurrentCurrency());
    }

    /**
     * Creates a Money instance with the specified amount in the current locale's currency.
     *
     * @param amount the monetary amount
     * @return a new Money instance
     */
    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount), getCurrentCurrency());
    }

    /**
     * Creates a Money instance with the specified amount in the current locale's currency.
     *
     * @param amount the monetary amount
     * @return a new Money instance
     */
    public static Money of(BigDecimal amount) {
        return new Money(amount, getCurrentCurrency());
    }

    /**
     * Creates a Money instance with the specified amount in the current locale's currency.
     *
     * @param amount the monetary amount
     * @return a new Money instance
     */
    public static Money of(Number amount) {
        return new Money(new BigDecimal(amount.toString()), getCurrentCurrency());
    }


    /**
     * Creates a Money instance with zero amount in the specified currency.
     *
     * @param currencyCode the ISO 4217 currency code
     * @return a new Money instance with zero amount
     */
    public static Money zero(String currencyCode) {
        return new Money(BigDecimal.ZERO, currencyCode);
    }

    /**
     * Gets a list of all available currency codes (ISO 4217).
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * List<String> currencies = Money.getAvailableCurrencies();
     * // Returns: ["USD", "EUR", "GBP", "JPY", ...]
     * }</pre>
     *
     * @return list of currency codes
     */
    public static List<String> getAvailableCurrencies() {
        return Currency.getAvailableCurrencies().stream()
                .map(Currency::getCurrencyCode)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Gets the currency code for a specific locale.
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * String usCurrency = Money.getCurrencyForLocale(Locale.US);
     * // Returns: "USD"
     *
     * String germanCurrency = Money.getCurrencyForLocale(Locale.GERMANY);
     * // Returns: "EUR"
     * }</pre>
     *
     * @param locale the locale to get the currency for
     * @return the currency code for the locale
     * @throws IllegalArgumentException if the locale has no currency
     */
    public static String getCurrencyForLocale(Locale locale) {
        Objects.requireNonNull(locale, "Locale cannot be null");
        try {
            Currency currency = Currency.getInstance(locale);
            return currency.getCurrencyCode();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "No currency found for locale: " + locale.getDisplayName(), e
            );
        }
    }

    /**
     * Gets the currency code for the current default locale.
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * String currency = Money.getCurrentCurrency();
     * // Returns "USD" in US locale, "EUR" in European locales, etc.
     * }</pre>
     *
     * @return the currency code for the current default locale
     * @throws IllegalArgumentException if the current locale has no currency
     */
    public static String getCurrentCurrency() {
        return getCurrencyForLocale(Locale.getDefault());
    }

    /**
     * Calculates the base amount from a price that includes tax.
     * This is useful in countries like Colombia where prices are displayed with tax included.
     *
     * <p>Formula: baseAmount = priceWithTax / (1 + taxRate/100)</p>
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * Money priceWithTax = Money.of("20000", "COP"); // Lunch price with 19% VAT
     * Money baseAmount = priceWithTax.extractBase(new BigDecimal("19"));
     * // baseAmount = COP 16806.72 (base price before VAT)
     * }</pre>
     *
     * @param taxPercentage the tax percentage included in the price (e.g., 19 for 19%)
     * @return the base amount without tax
     */
    public Money extractBase(BigDecimal taxPercentage) {
        // baseAmount = priceWithTax / (1 + taxRate/100)
        BigDecimal divisor = BigDecimal.ONE.add(
            taxPercentage.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP)
        );
        BigDecimal baseAmount = this.amount.divide(divisor, 10, RoundingMode.HALF_UP);
        return new Money(baseAmount, this.currencyCode);
    }

    /**
     * Calculates the tax amount from a price that includes tax.
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * Money priceWithTax = Money.of("20000", "COP");
     * Money taxAmount = priceWithTax.extractTax(new BigDecimal("19"));
     * // taxAmount = COP 3193.28 (the VAT portion)
     * }</pre>
     *
     * @param taxPercentage the tax percentage included in the price
     * @return the tax amount
     */
    public Money extractTax(BigDecimal taxPercentage) {
        Money baseAmount = extractBase(taxPercentage);
        return this.subtract(baseAmount);
    }

    /**
     * Calculates price with tax from a base amount.
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * Money basePrice = Money.of("16806.72", "COP");
     * Money priceWithTax = basePrice.addTax(new BigDecimal("19"));
     * // priceWithTax = COP 20000.00
     * }</pre>
     *
     * @param taxPercentage the tax percentage to add
     * @return the price including tax
     */
    public Money addTax(BigDecimal taxPercentage) {
        Money taxAmount = this.percentage(taxPercentage);
        return this.add(taxAmount);
    }


    /**
     * Adds another Money to this Money.
     * Both Money instances must have the same currency.
     *
     * @param other the Money to add
     * @return a new Money instance with the sum
     * @throws InvalidCurrencyOperationException if currencies don't match
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currencyCode);
    }

    /**
     * Subtracts another Money from this Money.
     * Both Money instances must have the same currency.
     *
     * @param other the Money to subtract
     * @return a new Money instance with the difference
     * @throws InvalidCurrencyOperationException if currencies don't match
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currencyCode);
    }

    /**
     * Multiplies this Money by a BigDecimal factor.
     *
     * @param factor the multiplication factor
     * @return a new Money instance with the product
     */
    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currencyCode);
    }

    /**
     * Multiplies this Money by a numeric factor.
     *
     * @param factor the multiplication factor
     * @return a new Money instance with the product
     */
    public Money multiply(double factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    /**
     * Divides this Money by a BigDecimal divisor.
     *
     * @param divisor      the division divisor
     * @param scale        the scale for the result
     * @param roundingMode the rounding mode to apply
     * @return a new Money instance with the quotient
     */
    public Money divide(BigDecimal divisor, int scale, RoundingMode roundingMode) {
        return new Money(this.amount.divide(divisor, scale, roundingMode), this.currencyCode);
    }

    /**
     * Applies a percentage to this Money.
     *
     * @param percentage the percentage to apply (e.g., 19 for 19%)
     * @return a new Money instance with the percentage amount
     */
    public Money percentage(BigDecimal percentage) {
        return multiply(percentage.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));
    }

    /**
     * Rounds this Money to the specified scale with HALF_UP rounding mode.
     *
     * @param scale the number of decimal places
     * @return a new Money instance with the rounded amount
     */
    public Money round(int scale) {
        return new Money(amount.setScale(scale, RoundingMode.HALF_UP), currencyCode);
    }

    /**
     * Rounds this Money according to the currency's default fraction digits.
     *
     * @return a new Money instance with the rounded amount
     */
    public Money roundToCurrency() {
        try {
            Currency currency = Currency.getInstance(currencyCode);
            return round(currency.getDefaultFractionDigits());
        } catch (IllegalArgumentException e) {
            return round(2); // Default to 2 decimal places
        }
    }

    /**
     * Returns the absolute value of this Money.
     *
     * @return a new Money instance with the absolute amount
     */
    public Money abs() {
        return new Money(amount.abs(), currencyCode);
    }

    /**
     * Returns the negated value of this Money.
     *
     * @return a new Money instance with the negated amount
     */
    public Money negate() {
        return new Money(amount.negate(), currencyCode);
    }

    /**
     * Checks if this Money is zero.
     *
     * @return true if the amount is zero
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Checks if this Money is positive.
     *
     * @return true if the amount is greater than zero
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks if this Money is negative.
     *
     * @return true if the amount is less than zero
     */
    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Validates that another Money has the same currency as this Money.
     *
     * @param other the Money to validate
     * @throws InvalidCurrencyOperationException if currencies don't match
     */
    private void validateSameCurrency(Money other) {
        if (!this.currencyCode.equals(other.currencyCode)) {
            throw new InvalidCurrencyOperationException(
                    "Cannot perform operation between different currencies: " +
                            this.currencyCode + " and " + other.currencyCode
            );
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0 &&
                currencyCode.equals(money.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currencyCode);
    }

    @Override
    public String toString() {
        return currencyCode + " " + amount.toPlainString();
    }

    @Override
    public int compareTo(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    /**
     * Formats the Money amount with the currency symbol.
     *
     * @return formatted string with currency symbol and amount
     */
    public String format() {
        Currency currency = Currency.getInstance(currencyCode);
        return String.format("%s %.2f", currency.getSymbol(), amount);
    }

    /**
     * Creates a copy of this Money instance.
     *
     * @return a new Money instance with the same amount and currency
     */
    public Money copy() {
        return new Money(this.amount, this.currencyCode);
    }
}
