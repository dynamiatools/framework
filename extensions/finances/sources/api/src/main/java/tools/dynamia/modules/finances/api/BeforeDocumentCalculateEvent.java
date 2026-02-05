package tools.dynamia.modules.finances.api;

/**
 * Event published before a document is calculated.
 * Can be used for validation or preprocessing logic.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @EventListener
 * public void beforeCalculate(BeforeDocumentCalculateEvent event) {
 *     FinancialDocument doc = event.getDocument();
 *     // Perform validation or preprocessing
 *     if (doc.getLines().isEmpty()) {
 *         throw new IllegalStateException("Cannot calculate empty document");
 *     }
 * }
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public class BeforeDocumentCalculateEvent extends DocumentEvent {

    /**
     * Creates a new before calculate event.
     *
     * @param document the document about to be calculated
     */
    public BeforeDocumentCalculateEvent(FinancialDocument document) {
        super(document);
    }
}
