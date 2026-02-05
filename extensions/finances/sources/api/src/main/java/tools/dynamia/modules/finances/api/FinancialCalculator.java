package tools.dynamia.modules.finances.api;

/**
 * Main calculator interface for financial documents.
 * Implementations handle the complete calculation flow for documents and lines.
 *
 * <p>The calculator is stateless and should not modify external state.
 * It only updates the totals within the document and line objects.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * FinancialCalculator calculator = new DefaultFinancialCalculator();
 * calculator.calculateDocument(invoice);
 *
 * System.out.println("Total: " + invoice.getTotals().getGrandTotal());
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public interface FinancialCalculator {

    /**
     * Calculates totals for a complete financial document.
     * This includes calculating all lines and aggregating document-level charges.
     *
     * @param document the document to calculate
     * @throws FinancialCalculationException if calculation fails
     */
    void calculateDocument(FinancialDocument document);

    /**
     * Calculates totals for a single document line.
     * Applies all line-level charges in priority order.
     *
     * @param line the line to calculate
     * @throws FinancialCalculationException if calculation fails
     */
    void calculateLine(DocumentLine line);

    /**
     * Recalculates a document that was previously calculated.
     * Only works if the document is in DRAFT status.
     *
     * @param document the document to recalculate
     * @throws InvalidDocumentStateException if document is not in DRAFT status
     */
    void recalculate(FinancialDocument document);
}
