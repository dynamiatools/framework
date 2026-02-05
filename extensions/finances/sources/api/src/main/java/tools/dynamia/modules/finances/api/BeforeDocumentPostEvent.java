package tools.dynamia.modules.finances.api;

/**
 * Event published before a document is posted.
 * Can be used for final validation or authorization checks.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @EventListener
 * public void beforePost(BeforeDocumentPostEvent event) {
 *     FinancialDocument doc = event.getDocument();
 *     // Validate before posting
 *     if (doc.getTotals() == null) {
 *         throw new IllegalStateException("Cannot post uncalculated document");
 *     }
 * }
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public class BeforeDocumentPostEvent extends DocumentEvent {

    /**
     * Creates a new before post event.
     *
     * @param document the document about to be posted
     */
    public BeforeDocumentPostEvent(FinancialDocument document) {
        super(document);
    }
}
