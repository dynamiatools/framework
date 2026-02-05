package tools.dynamia.modules.finances.api;

/**
 * Event published after a document has been posted successfully.
 * Can be used for accounting integration, notifications, or workflow triggers.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @EventListener
 * public void afterPost(AfterDocumentPostEvent event) {
 *     FinancialDocument doc = event.getDocument();
 *     // Create accounting entries
 *     accountingService.createJournalEntries(doc);
 *     // Send notification
 *     notificationService.notifyDocumentPosted(doc);
 * }
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public class AfterDocumentPostEvent extends DocumentEvent {

    /**
     * Creates a new after post event.
     *
     * @param document the document that was posted
     */
    public AfterDocumentPostEvent(FinancialDocument document) {
        super(document);
    }
}
