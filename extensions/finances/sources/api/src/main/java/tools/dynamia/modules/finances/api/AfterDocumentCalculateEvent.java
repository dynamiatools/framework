package tools.dynamia.modules.finances.api;

/**
 * Event published after a document has been calculated successfully.
 * Can be used for auditing, logging, or triggering additional processes.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @EventListener
 * public void afterCalculate(AfterDocumentCalculateEvent event) {
 *     FinancialDocument doc = event.getDocument();
 *     log.info("Document calculated: {} - Total: {}",
 *              doc.getDocumentNumber(),
 *              doc.getTotals().getGrandTotal());
 * }
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public class AfterDocumentCalculateEvent extends DocumentEvent {

    /**
     * Creates a new after calculate event.
     *
     * @param document the document that was calculated
     */
    public AfterDocumentCalculateEvent(FinancialDocument document) {
        super(document);
    }
}
