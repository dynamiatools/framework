package tools.dynamia.modules.finances.api;

import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable value object representing the consolidated totals for a financial document.
 * Contains all aggregated amounts from all lines plus document-level charges.
 *
 * <p>The payableTotal represents the final amount to be paid after all charges,
 * including withholdings and credits.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * DocumentTotals totals = DocumentTotals.builder()
 *     .subTotal(Money.of("10000", "USD"))
 *     .discountTotal(Money.of("1000", "USD"))
 *     .taxTotal(Money.of("1710", "USD"))
 *     .withholdingTotal(Money.of("200", "USD"))
 *     .grandTotal(Money.of("10710", "USD"))
 *     .payableTotal(Money.of("10510", "USD"))
 *     .build();
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public final class DocumentTotals implements Serializable {

    private final Money subTotal;
    private final Money discountTotal;
    private final Money taxTotal;
    private final Money withholdingTotal;
    private final Money feeTotal;
    private final Money grandTotal;
    private final Money payableTotal;

    private DocumentTotals(Builder builder) {
        this.subTotal = builder.subTotal;
        this.discountTotal = builder.discountTotal;
        this.taxTotal = builder.taxTotal;
        this.withholdingTotal = builder.withholdingTotal;
        this.feeTotal = builder.feeTotal;
        this.grandTotal = builder.grandTotal;
        this.payableTotal = builder.payableTotal;
    }

    /**
     * Creates a new builder for DocumentTotals.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates DocumentTotals with all values set to zero in the specified currency.
     *
     * @param currency the currency code
     * @return a new DocumentTotals instance with zero amounts
     */
    public static DocumentTotals zero(String currency) {
        return builder()
            .subTotal(Money.zero(currency))
            .discountTotal(Money.zero(currency))
            .taxTotal(Money.zero(currency))
            .withholdingTotal(Money.zero(currency))
            .feeTotal(Money.zero(currency))
            .grandTotal(Money.zero(currency))
            .payableTotal(Money.zero(currency))
            .build();
    }

    public Money getSubTotal() {
        return subTotal;
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

    public Money getGrandTotal() {
        return grandTotal;
    }

    /**
     * Gets the payable total, which is the grand total minus withholdings.
     * This represents the actual amount to be paid or received.
     *
     * @return the payable total
     */
    public Money getPayableTotal() {
        return payableTotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentTotals that = (DocumentTotals) o;
        return Objects.equals(subTotal, that.subTotal) &&
               Objects.equals(discountTotal, that.discountTotal) &&
               Objects.equals(taxTotal, that.taxTotal) &&
               Objects.equals(withholdingTotal, that.withholdingTotal) &&
               Objects.equals(feeTotal, that.feeTotal) &&
               Objects.equals(grandTotal, that.grandTotal) &&
               Objects.equals(payableTotal, that.payableTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subTotal, discountTotal, taxTotal, withholdingTotal,
                           feeTotal, grandTotal, payableTotal);
    }

    @Override
    public String toString() {
        return "DocumentTotals{" +
               "subTotal=" + subTotal +
               ", discount=" + discountTotal +
               ", tax=" + taxTotal +
               ", withholding=" + withholdingTotal +
               ", fee=" + feeTotal +
               ", grandTotal=" + grandTotal +
               ", payableTotal=" + payableTotal +
               '}';
    }

    /**
     * Builder class for creating DocumentTotals instances.
     */
    public static class Builder {
        private Money subTotal;
        private Money discountTotal;
        private Money taxTotal;
        private Money withholdingTotal;
        private Money feeTotal;
        private Money grandTotal;
        private Money payableTotal;

        private Builder() {
        }

        public Builder subTotal(Money subTotal) {
            this.subTotal = subTotal;
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

        public Builder grandTotal(Money grandTotal) {
            this.grandTotal = grandTotal;
            return this;
        }

        public Builder payableTotal(Money payableTotal) {
            this.payableTotal = payableTotal;
            return this;
        }

        public DocumentTotals build() {
            Objects.requireNonNull(subTotal, "Subtotal is required");
            Objects.requireNonNull(discountTotal, "Discount total is required");
            Objects.requireNonNull(taxTotal, "Tax total is required");
            Objects.requireNonNull(withholdingTotal, "Withholding total is required");
            Objects.requireNonNull(feeTotal, "Fee total is required");
            Objects.requireNonNull(grandTotal, "Grand total is required");
            Objects.requireNonNull(payableTotal, "Payable total is required");

            return new DocumentTotals(this);
        }
    }
}
