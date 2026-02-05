package tools.dynamia.modules.finances.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates line totals into document totals.
 * Sums up all line amounts and applies document-level charges.
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public class TotalsAggregator {

    private final ChargeCalculator chargeCalculator;

    /**
     * Default constructor.
     */
    public TotalsAggregator() {
        this.chargeCalculator = new ChargeCalculator();
    }

    /**
     * Constructor with custom charge calculator.
     *
     * @param chargeCalculator the charge calculator to use
     */
    public TotalsAggregator(ChargeCalculator chargeCalculator) {
        this.chargeCalculator = chargeCalculator;
    }

    /**
     * Aggregates line totals into document totals.
     *
     * @param document the document to aggregate
     * @return the document totals
     */
    public DocumentTotals aggregate(FinancialDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        String currency = document.getCurrency();

        // Initialize accumulators
        Money subTotal = Money.zero(currency);
        Money discountTotal = Money.zero(currency);
        Money taxTotal = Money.zero(currency);
        Money withholdingTotal = Money.zero(currency);
        Money feeTotal = Money.zero(currency);

        // Sum up all line totals
        for (DocumentLine line : document.getLines()) {
            if (line.getTotals() != null) {
                subTotal = subTotal.add(line.getTotals().getBaseAmount());
                discountTotal = discountTotal.add(line.getTotals().getDiscountTotal());
                taxTotal = taxTotal.add(line.getTotals().getTaxTotal());
                withholdingTotal = withholdingTotal.add(line.getTotals().getWithholdingTotal());
                feeTotal = feeTotal.add(line.getTotals().getFeeTotal());
            }
        }

        // Calculate running total after lines
        Money runningTotal = subTotal
            .subtract(discountTotal)
            .add(taxTotal)
            .add(feeTotal);

        // Apply document-level charges
        List<Charge> documentCharges = new ArrayList<>(document.getCharges());
        documentCharges.removeIf(c -> c.getAppliesTo() == ChargeAppliesTo.LINE);
        documentCharges.sort(new ChargeComparator());

        for (Charge charge : documentCharges) {
            Money chargeAmount = chargeCalculator.applyCharge(charge, getChargeBase(charge, subTotal, runningTotal), document);

            // Accumulate by type
            switch (charge.getType()) {
                case DISCOUNT:
                    discountTotal = discountTotal.add(chargeAmount);
                    runningTotal = runningTotal.subtract(chargeAmount);
                    break;
                case TAX:
                    taxTotal = taxTotal.add(chargeAmount);
                    runningTotal = runningTotal.add(chargeAmount);
                    break;
                case WITHHOLDING:
                    withholdingTotal = withholdingTotal.add(chargeAmount);
                    // Withholdings don't affect grand total
                    break;
                case FEE:
                    feeTotal = feeTotal.add(chargeAmount);
                    runningTotal = runningTotal.add(chargeAmount);
                    break;
            }
        }

        Money grandTotal = runningTotal;
        Money payableTotal = grandTotal.subtract(withholdingTotal);

        // Build and return document totals
        return DocumentTotals.builder()
            .subTotal(subTotal)
            .discountTotal(discountTotal)
            .taxTotal(taxTotal)
            .withholdingTotal(withholdingTotal)
            .feeTotal(feeTotal)
            .grandTotal(grandTotal)
            .payableTotal(payableTotal)
            .build();
    }

    /**
     * Determines the base amount for charge calculation based on charge base setting.
     *
     * @param charge the charge
     * @param subTotal the document subtotal
     * @param runningTotal the current running total
     * @return the base amount to use for calculation
     */
    private Money getChargeBase(Charge charge, Money subTotal, Money runningTotal) {
        if (charge.getBase() == null) {
            return subTotal;
        }

        switch (charge.getBase()) {
            case NET:
                return subTotal;
            case GROSS:
            case PREVIOUS_TOTAL:
                return runningTotal;
            default:
                return subTotal;
        }
    }
}
