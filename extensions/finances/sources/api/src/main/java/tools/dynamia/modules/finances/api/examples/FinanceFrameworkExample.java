package tools.dynamia.modules.finances.api.examples;

import tools.dynamia.modules.finances.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Complete example demonstrating the Dynamia Finance Framework usage.
 * This example creates a sales invoice with multiple lines, applies various charges,
 * and calculates the final totals.
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public class FinanceFrameworkExample {

    public static void main(String[] args) {
        // Example 1: Simple Sales Invoice
        simpleInvoiceExample();

        // Example 2: Invoice with Multiple Charges
        complexInvoiceExample();

        // Example 3: Document with Document-Level Charges
        documentLevelChargesExample();

        // Example 4: Multi-Currency Invoice
        multiCurrencyExample();
    }

    /**
     * Example 1: Simple sales invoice with one line and VAT.
     */
    private static void simpleInvoiceExample() {
        System.out.println("\n=== Example 1: Simple Sales Invoice ===");

        // Create a sales invoice
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-001");
        invoice.setIssueDate(LocalDate.now());
        invoice.setParty(DocumentParty.of("Customer"));

        // Add a product line
        DocumentLine line = DocumentLine.of(
                "Product A - Premium Widget",
                10,
                Money.of("100.00", "USD")
        );

        // Add VAT 19%
        Charge vat = Charge.percentage(
                "VAT19",
                "Value Added Tax 19%",
                ChargeType.TAX,
                new BigDecimal("19"),
                20
        );
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        vat.setBase(ChargeBase.NET);

        line.addCharge(vat);
        invoice.addLine(line);

        // Calculate the invoice
        FinancialCalculator calculator = new DefaultFinancialCalculator();
        calculator.calculateDocument(invoice);

        // Print results
        printInvoice(invoice);
    }

    /**
     * Example 2: Invoice with discount, tax, and withholding.
     */
    private static void complexInvoiceExample() {
        System.out.println("\n=== Example 2: Complex Invoice with Multiple Charges ===");

        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-002");
        invoice.setIssueDate(LocalDate.now());
        invoice.setParty("Customer XYZ Corp");

        // Line 1: Product with discount, tax, and withholding
        DocumentLine line1 = DocumentLine.of(
                "Enterprise Software License",
                1,
                Money.of("5000.00", "USD")
        );

        // Commercial discount 10% (priority 10 - applied first)
        Charge discount = Charge.percentage(
                "DISC10",
                "Commercial Discount 10%",
                ChargeType.DISCOUNT,
                new BigDecimal("10"),
                10
        );
        discount.setAppliesTo(ChargeAppliesTo.LINE);
        discount.setBase(ChargeBase.NET);

        // VAT 19% (priority 20 - applied after discount)
        Charge vat = Charge.percentage(
                "VAT19",
                "Value Added Tax 19%",
                ChargeType.TAX,
                new BigDecimal("19"),
                20
        );
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        vat.setBase(ChargeBase.PREVIOUS_TOTAL);

        // Income withholding 2.5% (priority 30 - applied last)
        Charge withholding = Charge.percentage(
                "WHOLD25",
                "Income Tax Withholding 2.5%",
                ChargeType.WITHHOLDING,
                new BigDecimal("2.5"),
                30
        );
        withholding.setAppliesTo(ChargeAppliesTo.LINE);
        withholding.setBase(ChargeBase.PREVIOUS_TOTAL);

        line1.addCharge(discount);
        line1.addCharge(vat);
        line1.addCharge(withholding);
        invoice.addLine(line1);

        // Line 2: Consulting services
        DocumentLine line2 = DocumentLine.of(
                "Professional Consulting Services",
                20,
                Money.of("150.00", "USD")
        );

        // Same charges apply
        line2.addCharge(discount);
        line2.addCharge(vat);
        line2.addCharge(withholding);
        invoice.addLine(line2);

        // Calculate
        FinancialCalculator calculator = new DefaultFinancialCalculator();
        calculator.calculateDocument(invoice);

        // Print results
        printInvoice(invoice);
    }

    /**
     * Example 3: Invoice with document-level charges (shipping fee).
     */
    private static void documentLevelChargesExample() {
        System.out.println("\n=== Example 3: Invoice with Document-Level Charges ===");

        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-003");
        invoice.setIssueDate(LocalDate.now());
        invoice.setParty("Customer DEF Ltd");

        // Add product lines
        DocumentLine line1 = DocumentLine.of(
                "Product A",
                5,
                Money.of("50.00", "USD")
        );
        invoice.addLine(line1);

        DocumentLine line2 = DocumentLine.of(
                "Product B",
                3,
                Money.of("75.00", "USD")
        );
        invoice.addLine(line2);

        // Add VAT at line level
        Charge vat = Charge.percentage(
                "VAT19",
                "VAT 19%",
                ChargeType.TAX,
                new BigDecimal("19"),
                20
        );
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        invoice.addCharge(vat);

        // Add shipping fee at document level
        Charge shipping = Charge.fixed(
                "SHIP",
                "Shipping Cost",
                ChargeType.FEE,
                new BigDecimal("50.00"),
                100
        );
        shipping.setAppliesTo(ChargeAppliesTo.DOCUMENT);
        invoice.addCharge(shipping);

        // Calculate
        FinancialCalculator calculator = new DefaultFinancialCalculator();
        calculator.calculateDocument(invoice);

        // Print results
        printInvoice(invoice);
    }

    /**
     * Example 4: Multi-currency invoice with exchange rate.
     */
    private static void multiCurrencyExample() {
        System.out.println("\n=== Example 4: Multi-Currency Invoice ===");

        // Create invoice in EUR
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "EUR");
        invoice.setDocumentNumber("INV-004");
        invoice.setIssueDate(LocalDate.now());
        invoice.setParty("International Customer");

        // Set exchange rate (EUR to USD for reference)
        ExchangeRate rate = ExchangeRate.of("EUR", "USD", new BigDecimal("1.10"), LocalDate.now());
        invoice.setExchangeRate(rate);

        // Add product
        DocumentLine line = DocumentLine.of(
                "International Product",
                10,
                Money.of("100.00", "EUR")
        );

        // Add VAT
        Charge vat = Charge.percentage(
                "VAT21",
                "VAT 21%",
                ChargeType.TAX,
                new BigDecimal("21"),
                20
        );
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        line.addCharge(vat);
        invoice.addLine(line);

        // Calculate
        FinancialCalculator calculator = new DefaultFinancialCalculator();
        calculator.calculateDocument(invoice);

        // Print results
        printInvoice(invoice);

        // Convert to USD
        Money eurTotal = invoice.getTotals().getGrandTotal();
        Money usdTotal = rate.convert(eurTotal);
        System.out.println("Converted to USD: " + usdTotal);
    }

    /**
     * Prints invoice details.
     */
    private static void printInvoice(FinancialDocument invoice) {
        System.out.println("\nInvoice: " + invoice.getDocumentNumber());
        System.out.println("Customer: " + invoice.getParty());
        System.out.println("Date: " + invoice.getIssueDate());
        System.out.println("Currency: " + invoice.getCurrency());
        System.out.println("Status: " + invoice.getStatus());

        System.out.println("\nLines:");
        for (DocumentLine line : invoice.getLines()) {
            System.out.println("  " + line.getDescription());
            System.out.println("    Quantity: " + line.getQuantity());
            System.out.println("    Unit Price: " + line.getUnitPrice());
            System.out.println("    Base Amount: " + line.getBaseAmount());
            if (line.getTotals() != null) {
                System.out.println("    Discount Total: " + line.getTotals().getDiscountTotal());
                System.out.println("    Tax Total: " + line.getTotals().getTaxTotal());
                System.out.println("    Withholding Total: " + line.getTotals().getWithholdingTotal());
                System.out.println("    Net Total: " + line.getTotals().getNetTotal());
            }
        }

        if (invoice.getTotals() != null) {
            System.out.println("\nDocument Totals:");
            System.out.println("  Subtotal: " + invoice.getTotals().getSubTotal());
            System.out.println("  Discount Total: " + invoice.getTotals().getDiscountTotal());
            System.out.println("  Tax Total: " + invoice.getTotals().getTaxTotal());
            System.out.println("  Fee Total: " + invoice.getTotals().getFeeTotal());
            System.out.println("  Withholding Total: " + invoice.getTotals().getWithholdingTotal());
            System.out.println("  Grand Total: " + invoice.getTotals().getGrandTotal());
            System.out.println("  Payable Total: " + invoice.getTotals().getPayableTotal());
        }

        System.out.println("\n" + "=".repeat(60));
    }
}
