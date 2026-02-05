package tools.dynamia.modules.finances.api;

/**
 * Enumeration of financial document types supported by the framework.
 * Each type represents a different kind of financial transaction or document.
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public enum DocumentType {
    /**
     * Sales invoice - Customer billing document
     */
    SALE,

    /**
     * Purchase order or invoice - Vendor procurement document
     */
    PURCHASE,

    /**
     * Credit note - Document that reduces the amount owed to the seller
     */
    CREDIT_NOTE,

    /**
     * Debit note - Document that increases the amount owed to the seller
     */
    DEBIT_NOTE,

    /**
     * Adjustment - Document for corrections and modifications
     */
    ADJUSTMENT,

    /**
     * Quote - Pre-sale estimate or proposal
     */
    QUOTE
}
