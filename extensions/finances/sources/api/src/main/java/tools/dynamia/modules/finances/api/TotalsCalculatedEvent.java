package tools.dynamia.modules.finances.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Event published when totals have been calculated for a document or line.
 * Provides access to the calculated totals for auditing and logging.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @EventListener
 * public void onTotalsCalculated(TotalsCalculatedEvent event) {
 *     if (event.isDocumentTotals()) {
 *         DocumentTotals totals = (DocumentTotals) event.getTotals();
 *         log.info("Document totals: Grand Total = {}", totals.getGrandTotal());
 *     }
 * }
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public class TotalsCalculatedEvent implements Serializable {

    private final Object totals;
    private final Object context;
    private final LocalDateTime timestamp;

    /**
     * Creates a new totals calculated event.
     *
     * @param totals the calculated totals (LineTotals or DocumentTotals)
     * @param context the calculation context (document or line)
     */
    public TotalsCalculatedEvent(Object totals, Object context) {
        this.totals = Objects.requireNonNull(totals, "Totals cannot be null");
        this.context = context;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Gets the calculated totals.
     *
     * @return the totals object (LineTotals or DocumentTotals)
     */
    public Object getTotals() {
        return totals;
    }

    /**
     * Gets the calculation context.
     *
     * @return the context (document or line)
     */
    public Object getContext() {
        return context;
    }

    /**
     * Gets the event timestamp.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Checks if the totals are document totals.
     *
     * @return true if totals is DocumentTotals
     */
    public boolean isDocumentTotals() {
        return totals instanceof DocumentTotals;
    }

    /**
     * Checks if the totals are line totals.
     *
     * @return true if totals is LineTotals
     */
    public boolean isLineTotals() {
        return totals instanceof LineTotals;
    }

    @Override
    public String toString() {
        return "TotalsCalculatedEvent{" +
               "totals=" + totals.getClass().getSimpleName() +
               ", timestamp=" + timestamp +
               '}';
    }
}
