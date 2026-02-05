package tools.dynamia.modules.finances.api;

/**
 * Enumeration defining where a charge can be applied in the document structure.
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public enum ChargeAppliesTo {
    /**
     * Charge applies at line level - Applied to individual document lines
     */
    LINE,

    /**
     * Charge applies at document level - Applied to the entire document
     */
    DOCUMENT
}
