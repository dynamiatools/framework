package tools.dynamia.modules.finances.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculator for individual document lines.
 * Applies all charges in priority order and produces line totals.
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public class LineCalculator {

    private final ChargeCalculator chargeCalculator;

    /**
     * Default constructor.
     */
    public LineCalculator() {
        this.chargeCalculator = new ChargeCalculator();
    }

    /**
     * Constructor with custom charge calculator.
     *
     * @param chargeCalculator the charge calculator to use
     */
    public LineCalculator(ChargeCalculator chargeCalculator) {
        this.chargeCalculator = chargeCalculator;
    }

    /**
     * Calculates the totals for a document line.
     *
     * @param line the line to calculate
     */
    public void calculate(DocumentLine line) {
        if (line == null) {
            throw new IllegalArgumentException("Line cannot be null");
        }

        // Get base amount (quantity × unit price)
        Money baseAmount = line.getBaseAmount();
        String currency = baseAmount.getCurrencyCode();

        // Initialize accumulators
        Money discountTotal = Money.zero(currency);
        Money taxTotal = Money.zero(currency);
        Money withholdingTotal = Money.zero(currency);
        Money feeTotal = Money.zero(currency);
        Money runningTotal = baseAmount;

        // Get all charges and sort by priority
        List<Charge> charges = new ArrayList<>(line.getAllCharges());
        charges.sort(new ChargeComparator());

        // Apply each charge in order
        for (Charge charge : charges) {
            Money chargeAmount = chargeCalculator.applyCharge(charge, getChargeBase(charge, baseAmount, runningTotal), line);

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
                    // Withholdings don't affect running total
                    break;
                case FEE:
                    feeTotal = feeTotal.add(chargeAmount);
                    runningTotal = runningTotal.add(chargeAmount);
                    break;
            }
        }

        // Build and set totals
        LineTotals totals = LineTotals.builder()
            .baseAmount(baseAmount)
            .discountTotal(discountTotal)
            .taxTotal(taxTotal)
            .withholdingTotal(withholdingTotal)
            .feeTotal(feeTotal)
            .netTotal(runningTotal)
            .build();

        line.setTotals(totals);
    }

    /**
     * Determines the base amount for charge calculation based on charge base setting.
     *
     * @param charge the charge
     * @param baseAmount the line base amount
     * @param runningTotal the current running total
     * @return the base amount to use for calculation
     */
    private Money getChargeBase(Charge charge, Money baseAmount, Money runningTotal) {
        if (charge.getBase() == null) {
            return baseAmount;
        }

        switch (charge.getBase()) {
            case NET:
                return baseAmount;
            case GROSS:
            case PREVIOUS_TOTAL:
                return runningTotal;
            default:
                return baseAmount;
        }
    }
}
