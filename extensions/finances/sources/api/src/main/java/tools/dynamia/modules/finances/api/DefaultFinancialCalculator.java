package tools.dynamia.modules.finances.api;

/**
 * Default implementation of the FinancialCalculator interface.
 * Provides complete calculation logic for financial documents and lines.
 *
 * <p>This calculator is stateless and can be safely shared across threads.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * FinancialCalculator calculator = new DefaultFinancialCalculator();
 *
 * FinancialDocument invoice = new FinancialDocument();
 * invoice.setType(DocumentType.SALE);
 * invoice.setCurrency("USD");
 *
 * DocumentLine line = DocumentLine.of("Product A",
 *                                     new BigDecimal("10"),
 *                                     Money.of("100", "USD"));
 * invoice.addLine(line);
 *
 * calculator.calculateDocument(invoice);
 *
 * System.out.println("Grand Total: " + invoice.getTotals().getGrandTotal());
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public class DefaultFinancialCalculator implements FinancialCalculator {

    private final LineCalculator lineCalculator;
    private final TotalsAggregator totalsAggregator;

    /**
     * Default constructor with standard calculators.
     */
    public DefaultFinancialCalculator() {
        ChargeCalculator chargeCalculator = new ChargeCalculator();
        this.lineCalculator = new LineCalculator(chargeCalculator);
        this.totalsAggregator = new TotalsAggregator(chargeCalculator);
    }

    /**
     * Constructor with custom calculators.
     *
     * @param lineCalculator the line calculator to use
     * @param totalsAggregator the totals aggregator to use
     */
    public DefaultFinancialCalculator(LineCalculator lineCalculator, TotalsAggregator totalsAggregator) {
        this.lineCalculator = lineCalculator;
        this.totalsAggregator = totalsAggregator;
    }

    @Override
    public void calculateDocument(FinancialDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        // Validate document before calculation
        document.validate();

        // Check if document can be calculated
        if (document.isPosted()) {
            throw new InvalidDocumentStateException(
                "Cannot calculate POSTED document. Use recalculate() or change status to DRAFT."
            );
        }

        // Calculate all lines
        for (DocumentLine line : document.getLines()) {
            calculateLine(line);
        }

        // Aggregate totals
        DocumentTotals totals = totalsAggregator.aggregate(document);
        document.setTotals(totals);
    }

    @Override
    public void calculateLine(DocumentLine line) {
        if (line == null) {
            throw new IllegalArgumentException("Line cannot be null");
        }

        // Validate line before calculation
        line.validate();

        // Calculate line totals
        lineCalculator.calculate(line);
    }

    @Override
    public void recalculate(FinancialDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        if (!document.isDraft()) {
            throw new InvalidDocumentStateException(
                "Only DRAFT documents can be recalculated. Current status: " + document.getStatus()
            );
        }

        // Recalculate is the same as calculate for DRAFT documents
        calculateDocument(document);
    }
}
