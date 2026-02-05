package tools.dynamia.modules.finances.api;

/**
 * Enumeration of financial document status in its lifecycle.
 * Controls the state transitions and calculation behavior.
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public enum DocumentStatus {
    /**
     * Draft status - Document is editable and can be recalculated
     */
    DRAFT,

    /**
     * Posted status - Document is finalized, calculations are frozen
     */
    POSTED,

    /**
     * Cancelled status - Document has no financial impact
     */
    CANCELLED
}
