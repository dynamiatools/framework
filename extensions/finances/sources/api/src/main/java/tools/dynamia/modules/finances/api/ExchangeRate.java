package tools.dynamia.modules.finances.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable value object representing an exchange rate between two currencies.
 * Exchange rates are frozen at a specific date and stored with the document
 * to ensure calculation consistency over time.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * ExchangeRate rate = ExchangeRate.of("USD", "EUR", new BigDecimal("0.85"), LocalDate.now());
 * Money usd = Money.of("100", "USD");
 * Money eur = rate.convert(usd);
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public final class ExchangeRate implements Serializable {

    private final String fromCurrency;
    private final String toCurrency;
    private final BigDecimal rate;
    private final LocalDate date;

    /**
     * Private constructor to enforce immutability.
     *
     * @param fromCurrency the source currency code
     * @param toCurrency the target currency code
     * @param rate the exchange rate
     * @param date the date when the rate applies
     */
    private ExchangeRate(String fromCurrency, String toCurrency, BigDecimal rate, LocalDate date) {
        Objects.requireNonNull(fromCurrency, "From currency cannot be null");
        Objects.requireNonNull(toCurrency, "To currency cannot be null");
        Objects.requireNonNull(rate, "Rate cannot be null");
        Objects.requireNonNull(date, "Date cannot be null");

        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }

        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.date = date;
    }

    /**
     * Creates an ExchangeRate instance.
     *
     * @param fromCurrency the source currency code
     * @param toCurrency the target currency code
     * @param rate the exchange rate
     * @param date the date when the rate applies
     * @return a new ExchangeRate instance
     */
    public static ExchangeRate of(String fromCurrency, String toCurrency, BigDecimal rate, LocalDate date) {
        return new ExchangeRate(fromCurrency, toCurrency, rate, date);
    }

    /**
     * Creates an identity ExchangeRate (rate = 1) for the same currency.
     *
     * @param currency the currency code
     * @param date the date when the rate applies
     * @return a new ExchangeRate instance with rate 1.0
     */
    public static ExchangeRate identity(String currency, LocalDate date) {
        return new ExchangeRate(currency, currency, BigDecimal.ONE, date);
    }

    /**
     * Converts a Money amount using this exchange rate.
     *
     * @param money the Money to convert
     * @return a new Money instance in the target currency
     * @throws InvalidCurrencyOperationException if the Money currency doesn't match fromCurrency
     */
    public Money convert(Money money) {
        if (!money.getCurrencyCode().equals(fromCurrency)) {
            throw new InvalidCurrencyOperationException(
                "Cannot convert " + money.getCurrencyCode() + " using rate from " + fromCurrency
            );
        }

        BigDecimal convertedAmount = money.getAmount().multiply(rate);
        return Money.of(convertedAmount, toCurrency);
    }

    /**
     * Returns the inverse of this exchange rate.
     *
     * @return a new ExchangeRate with inverted currencies and rate
     */
    public ExchangeRate inverse() {
        BigDecimal inverseRate = BigDecimal.ONE.divide(rate, 10, java.math.RoundingMode.HALF_UP);
        return new ExchangeRate(toCurrency, fromCurrency, inverseRate, date);
    }

    /**
     * Checks if this exchange rate is an identity rate (same currency, rate = 1).
     *
     * @return true if this is an identity rate
     */
    public boolean isIdentity() {
        return fromCurrency.equals(toCurrency) && rate.compareTo(BigDecimal.ONE) == 0;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRate that = (ExchangeRate) o;
        return fromCurrency.equals(that.fromCurrency) &&
               toCurrency.equals(that.toCurrency) &&
               rate.compareTo(that.rate) == 0 &&
               date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromCurrency, toCurrency, rate, date);
    }

    @Override
    public String toString() {
        return fromCurrency + "/" + toCurrency + " = " + rate.toPlainString() + " (" + date + ")";
    }

    /**
     * Creates a copy of this ExchangeRate.
     * Since ExchangeRate is immutable, this returns a new instance with the same values.
     *
     * @return a new ExchangeRate instance with the same values
     */
    public ExchangeRate copy() {
        return new ExchangeRate(this.fromCurrency, this.toCurrency, this.rate, this.date);
    }
}
