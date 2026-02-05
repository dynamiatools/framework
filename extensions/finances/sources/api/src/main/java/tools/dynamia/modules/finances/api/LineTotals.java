package tools.dynamia.modules.finances.api;

import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable value object representing the calculated totals for a document line.
 * Contains all relevant amounts after applying charges (taxes, discounts, etc.).
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * LineTotals totals = LineTotals.builder()
 *     .baseAmount(Money.of("1000", "USD"))
 *     .discountTotal(Money.of("100", "USD"))
 *     .taxTotal(Money.of("171", "USD"))
 *     .netTotal(Money.of("1071", "USD"))
 *     .build();
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public final class LineTotals implements Serializable {

    private final Money baseAmount;
    private final Money discountTotal;
    private final Money taxTotal;
    private final Money withholdingTotal;
    private final Money feeTotal;
    private final Money netTotal;

    private LineTotals(Builder builder) {
        this.baseAmount = builder.baseAmount;
        this.discountTotal = builder.discountTotal;
        this.taxTotal = builder.taxTotal;
        this.withholdingTotal = builder.withholdingTotal;
        this.feeTotal = builder.feeTotal;
        this.netTotal = builder.netTotal;
    }

    /**
     * Creates a new builder for LineTotals.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates LineTotals with all values set to zero in the specified currency.
     *
     * @param currency the currency code
     * @return a new LineTotals instance with zero amounts
     */
    public static LineTotals zero(String currency) {
        return builder()
            .baseAmount(Money.zero(currency))
            .discountTotal(Money.zero(currency))
            .taxTotal(Money.zero(currency))
            .withholdingTotal(Money.zero(currency))
            .feeTotal(Money.zero(currency))
            .netTotal(Money.zero(currency))
            .build();
    }

    public Money getBaseAmount() {
        return baseAmount;
    }

    public Money getDiscountTotal() {
        return discountTotal;
    }

    public Money getTaxTotal() {
        return taxTotal;
    }

    public Money getWithholdingTotal() {
        return withholdingTotal;
    }

    public Money getFeeTotal() {
        return feeTotal;
    }

    public Money getNetTotal() {
        return netTotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineTotals that = (LineTotals) o;
        return Objects.equals(baseAmount, that.baseAmount) &&
               Objects.equals(discountTotal, that.discountTotal) &&
               Objects.equals(taxTotal, that.taxTotal) &&
               Objects.equals(withholdingTotal, that.withholdingTotal) &&
               Objects.equals(feeTotal, that.feeTotal) &&
               Objects.equals(netTotal, that.netTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseAmount, discountTotal, taxTotal, withholdingTotal, feeTotal, netTotal);
    }

    @Override
    public String toString() {
        return "LineTotals{" +
               "base=" + baseAmount +
               ", discount=" + discountTotal +
               ", tax=" + taxTotal +
               ", withholding=" + withholdingTotal +
               ", fee=" + feeTotal +
               ", net=" + netTotal +
               '}';
    }

    /**
     * Builder class for creating LineTotals instances.
     */
    public static class Builder {
        private Money baseAmount;
        private Money discountTotal;
        private Money taxTotal;
        private Money withholdingTotal;
        private Money feeTotal;
        private Money netTotal;

        private Builder() {
        }

        public Builder baseAmount(Money baseAmount) {
            this.baseAmount = baseAmount;
            return this;
        }

        public Builder discountTotal(Money discountTotal) {
            this.discountTotal = discountTotal;
            return this;
        }

        public Builder taxTotal(Money taxTotal) {
            this.taxTotal = taxTotal;
            return this;
        }

        public Builder withholdingTotal(Money withholdingTotal) {
            this.withholdingTotal = withholdingTotal;
            return this;
        }

        public Builder feeTotal(Money feeTotal) {
            this.feeTotal = feeTotal;
            return this;
        }

        public Builder netTotal(Money netTotal) {
            this.netTotal = netTotal;
            return this;
        }

        public LineTotals build() {
            Objects.requireNonNull(baseAmount, "Base amount is required");
            Objects.requireNonNull(discountTotal, "Discount total is required");
            Objects.requireNonNull(taxTotal, "Tax total is required");
            Objects.requireNonNull(withholdingTotal, "Withholding total is required");
            Objects.requireNonNull(feeTotal, "Fee total is required");
            Objects.requireNonNull(netTotal, "Net total is required");

            return new LineTotals(this);
        }
    }
}
