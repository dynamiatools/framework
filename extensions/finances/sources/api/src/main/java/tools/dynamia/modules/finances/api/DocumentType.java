package tools.dynamia.modules.finances.api;

/**
 * Enumeration of financial document types supported by the framework.
 * Each type represents a different kind of financial transaction or document.
 *
 * @author Dynamia Finance Framework
 * @since 26.1
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
    QUOTE,

    /**
     * Receipt - Proof of payment document
     */
    RECEIPT,

    /**
     * Refund - Document for returning funds to a customer
     */
    REFUND,

    /**
     * Delivery Note - Document confirming the delivery of goods
     */
    DELIVERY_NOTE,

    /**
     * Proforma Invoice - Preliminary bill of sale
     */
    PROFORMA_INVOICE,

    /**
     * Work Order - Document authorizing work to be performed
     */
    WORK_ORDER,

    /**
     * Support Document - Document supporting a transaction (common in electronic invoicing)
     */
    SUPPORT_DOCUMENT,

    /**
     * POS Equivalent Document - Point of Sale equivalent document for electronic invoicing
     */
    POS_EQUIVALENT_DOCUMENT
}
