package tools.dynamia.modules.finances.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base class for all financial document domain events.
 * Events are immutable and contain a timestamp for audit purposes.
 *
 * <p>This event system is compatible with Spring's ApplicationEventPublisher
 * but does not require Spring as a dependency.</p>
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public abstract class DocumentEvent implements Serializable {

    private final FinancialDocument document;
    private final LocalDateTime timestamp;

    /**
     * Creates a new document event.
     *
     * @param document the document associated with this event
     */
    protected DocumentEvent(FinancialDocument document) {
        this.document = Objects.requireNonNull(document, "Document cannot be null");
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Gets the document associated with this event.
     *
     * @return the financial document
     */
    public FinancialDocument getDocument() {
        return document;
    }

    /**
     * Gets the timestamp when this event was created.
     *
     * @return the event timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
               "document=" + document.getId() +
               ", timestamp=" + timestamp +
               '}';
    }
}
