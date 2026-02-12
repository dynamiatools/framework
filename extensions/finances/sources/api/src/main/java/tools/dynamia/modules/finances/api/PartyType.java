package tools.dynamia.modules.finances.api;

/**
 * Enumeration of party types in financial documents.
 * Indicates the role of the party in the transaction.
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public enum PartyType {
    /**
     * Customer or client (buyer).
     */
    CUSTOMER,

    /**
     * Supplier or vendor (seller).
     */
    SUPPLIER,

    /**
     * Employee.
     */
    EMPLOYEE,

    /**
     * Partner or associate.
     */
    PARTNER,

    /**
     * Government or tax authority.
     */
    GOVERNMENT,

    /**
     * Financial institution (bank, etc.).
     */
    FINANCIAL_INSTITUTION,

    /**
     * Other party type.
     */
    OTHER
}
