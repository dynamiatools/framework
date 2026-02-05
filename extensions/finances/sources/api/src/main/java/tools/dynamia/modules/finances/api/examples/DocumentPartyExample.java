package tools.dynamia.modules.finances.api.examples;

import tools.dynamia.modules.finances.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Example demonstrating the usage of DocumentParty in financial documents.
 * This class shows how to create parties with various financial characteristics
 * and how they integrate with FinancialDocument.
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public class DocumentPartyExample {

    /**
     * Example: Creating a customer with global discount and tax exemptions.
     */
    public static void example1_CustomerWithDiscount() {
        // Create a customer party with basic information
        DocumentParty customer = DocumentParty.of("CUST-001", "ACME Corporation", "TAX-123456789")
                .type(PartyType.CUSTOMER)
                .email("purchasing@acme.com")
                .phone("+1-555-0100")
                .address("123 Main Street")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .addGlobalDiscount(new BigDecimal("5.0")) // 5% discount on all purchases
                .withTaxExemption("VAT19") // Exempt from VAT
                .creditLimit(new BigDecimal("50000.00"))
                .paymentTermDays(30)
                .preferredCurrency("USD");

        // Use the party in a financial document
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD")
                .documentNumber("INV-2026-001")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(customer.getPaymentTermDays()))
                .party(customer);

        // Add document lines and charges
        invoice.addLine(DocumentLine.of("Product A", new BigDecimal("10"), Money.of("100.00", "USD")));
        invoice.addTax("VAT19", "Value Added Tax 19%", new BigDecimal("19"));

        System.out.println("Created invoice for: " + customer);
        System.out.println("Customer has global discount: " + customer.hasGlobalDiscount());
        System.out.println("Customer is exempt from VAT19: " + customer.isExemptFromTax("VAT19"));
    }

    /**
     * Example: Creating a supplier with auto-withholding.
     */
    public static void example2_SupplierWithWithholding() {
        // Create a supplier party with auto-withholding configuration
        DocumentParty supplier = DocumentParty.of("SUPP-001", "Tech Supplies Inc.", "TAX-987654321")
                .type(PartyType.SUPPLIER)
                .email("sales@techsupplies.com")
                .phone("+1-555-0200")
                .autoWithholder(true) // Enable auto-withholding
                .withAutoWithholding("RET_IVA") // IVA withholding
                .withAutoWithholding("RET_RENTA") // Income tax withholding
                .paymentTermDays(15)
                .preferredCurrency("USD");

        // Create a purchase document
        FinancialDocument purchase = FinancialDocument.of(DocumentType.PURCHASE, "USD")
                .documentNumber("PUR-2026-001")
                .issueDate(LocalDate.now())
                .party(supplier);

        // Add purchase line
        purchase.addLine(DocumentLine.of("Office Supplies", new BigDecimal("50"), Money.of("20.00", "USD")));

        // Add tax and withholdings
        purchase.addTax("VAT19", "Value Added Tax 19%", new BigDecimal("19"));

        // Auto-withholdings should be applied based on supplier configuration
        if (supplier.hasAutoWithholding()) {
            for (String withholdingCode : supplier.getAutoWithholdingCodes()) {
                System.out.println("Applying auto-withholding: " + withholdingCode);
                // In a real implementation, the calculator would apply these automatically
            }
        }

        System.out.println("Created purchase from: " + supplier);
        System.out.println("Supplier has auto-withholding: " + supplier.hasAutoWithholding());
    }

    /**
     * Example: Creating multiple party types.
     */
    public static void example3_DifferentPartyTypes() {
        // Customer
        DocumentParty customer = DocumentParty.of("Customer ABC")
                .type(PartyType.CUSTOMER)
                .taxId("CUST-TAX-001")
                .email("customer@abc.com");

        // Supplier
        DocumentParty supplier = DocumentParty.of("Supplier XYZ")
                .type(PartyType.SUPPLIER)
                .taxId("SUPP-TAX-001")
                .email("supplier@xyz.com");

        // Employee
        DocumentParty employee = DocumentParty.of("John Doe")
                .type(PartyType.EMPLOYEE)
                .taxId("EMP-123456")
                .email("john.doe@company.com");

        // Financial Institution
        DocumentParty bank = DocumentParty.of("National Bank")
                .type(PartyType.FINANCIAL_INSTITUTION)
                .taxId("BANK-999")
                .phone("+1-555-BANK");

        System.out.println("Customer: " + customer);
        System.out.println("Supplier: " + supplier);
        System.out.println("Employee: " + employee);
        System.out.println("Bank: " + bank);
    }

    /**
     * Example: Copying a document with party information.
     */
    public static void example4_CopyDocument() {
        // Original document with party
        DocumentParty originalCustomer = DocumentParty.of("CUST-001", "Original Corp", "TAX-111")
                .addGlobalDiscount(new BigDecimal("10.0"))
                .withTaxExemption("VAT19");

        FinancialDocument original = FinancialDocument.of(DocumentType.SALE, "USD")
                .documentNumber("INV-001")
                .party(originalCustomer);

        original.addLine(DocumentLine.of("Product", new BigDecimal("1"), Money.of("100.00", "USD")));

        // Copy the document (party is deep-copied)
        FinancialDocument copy = original.copyWithNumber("INV-002");

        // Modify the copied party without affecting the original
        copy.getParty().setEmail("newcontact@originalcorp.com");

        System.out.println("Original party email: " + original.getParty().getEmail());
        System.out.println("Copy party email: " + copy.getParty().getEmail());
        System.out.println("Parties are independent: " + (original.getParty() != copy.getParty()));
    }

    /**
     * Example: Party validation.
     */
    public static void example5_PartyValidation() {
        try {
            // Valid party
            DocumentParty validParty = DocumentParty.of("Valid Customer")
                    .addGlobalDiscount(new BigDecimal("5.0"))
                    .creditLimit(new BigDecimal("10000.00"))
                    .paymentTermDays(30);

            validParty.validate();
            System.out.println("Party is valid: " + validParty);

        } catch (IllegalStateException e) {
            System.err.println("Validation failed: " + e.getMessage());
        }

        try {
            // Invalid party - excessive discount
            DocumentParty invalidParty = DocumentParty.of("Invalid Customer")
                    .addGlobalDiscount(new BigDecimal("150.0")); // Over 100%

            invalidParty.validate();

        } catch (IllegalStateException e) {
            System.err.println("Expected validation error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Example 1: Customer with Discount ===");
        example1_CustomerWithDiscount();

        System.out.println("\n=== Example 2: Supplier with Withholding ===");
        example2_SupplierWithWithholding();

        System.out.println("\n=== Example 3: Different Party Types ===");
        example3_DifferentPartyTypes();

        System.out.println("\n=== Example 4: Copy Document ===");
        example4_CopyDocument();

        System.out.println("\n=== Example 5: Party Validation ===");
        example5_PartyValidation();
    }
}
