package tools.dynamia.modules.finances.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DefaultFinancialCalculator.
 * Tests complete document calculations with various scenarios.
 *
 * @author Dynamia Finance Framework
 */
@DisplayName("FinancialCalculator Tests")
class FinancialCalculatorTest {

    private FinancialCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DefaultFinancialCalculator();
    }

    @Test
    @DisplayName("Should calculate simple invoice with one line")
    void testSimpleInvoice() {
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-001");
        invoice.setIssueDate(LocalDate.now());

        DocumentLine line = DocumentLine.of("Product A", new BigDecimal("10"), Money.of("100", "USD"));
        invoice.addLine(line);

        calculator.calculateDocument(invoice);

        DocumentTotals totals = invoice.getTotals();
        assertNotNull(totals);
        assertEquals(Money.of("1000", "USD"), totals.getSubTotal());
        assertEquals(Money.of("1000", "USD"), totals.getGrandTotal());
        assertEquals(Money.of("1000", "USD"), totals.getPayableTotal());
    }

    @Test
    @DisplayName("Should calculate invoice with multiple lines")
    void testMultipleLines() {
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-002");
        invoice.setIssueDate(LocalDate.now());

        invoice.addLine(DocumentLine.of("Product A", new BigDecimal("5"), Money.of("100", "USD")));
        invoice.addLine(DocumentLine.of("Product B", new BigDecimal("3"), Money.of("200", "USD")));
        invoice.addLine(DocumentLine.of("Product C", new BigDecimal("2"), Money.of("150", "USD")));

        calculator.calculateDocument(invoice);

        DocumentTotals totals = invoice.getTotals();
        assertEquals(Money.of("1400", "USD"), totals.getSubTotal()); // 500 + 600 + 300
        assertEquals(Money.of("1400", "USD"), totals.getGrandTotal());
    }

    @Test
    @DisplayName("Should calculate invoice with line-level VAT")
    void testInvoiceWithLineLevelVAT() {
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-003");
        invoice.setIssueDate(LocalDate.now());

        // Add VAT at document level (applies to all lines)
        Charge vat = Charge.percentage("VAT19", "VAT 19%", ChargeType.TAX, new BigDecimal("19"), 20);
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        vat.setBase(ChargeBase.NET);
        invoice.addCharge(vat);

        invoice.addLine(DocumentLine.of("Product A", new BigDecimal("10"), Money.of("100", "USD")));
        invoice.addLine(DocumentLine.of("Product B", new BigDecimal("5"), Money.of("200", "USD")));

        calculator.calculateDocument(invoice);

        DocumentTotals totals = invoice.getTotals();
        assertEquals(Money.of("2000", "USD"), totals.getSubTotal()); // 1000 + 1000
        assertEquals(Money.of("380", "USD"), totals.getTaxTotal()); // 19% of 2000
        assertEquals(Money.of("2380", "USD"), totals.getGrandTotal());
    }

    @Test
    @DisplayName("Should calculate invoice with document-level charges")
    void testInvoiceWithDocumentLevelCharges() {
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-004");
        invoice.setIssueDate(LocalDate.now());

        invoice.addLine(DocumentLine.of("Product A", new BigDecimal("5"), Money.of("50", "USD")));
        invoice.addLine(DocumentLine.of("Product B", new BigDecimal("3"), Money.of("75", "USD")));

        // Line-level VAT
        Charge vat = Charge.percentage("VAT19", "VAT 19%", ChargeType.TAX, new BigDecimal("19"), 20);
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        invoice.addCharge(vat);

        // Document-level shipping fee
        Charge shipping = Charge.fixed("SHIP", "Shipping", ChargeType.FEE, new BigDecimal("50"), 100);
        shipping.setAppliesTo(ChargeAppliesTo.DOCUMENT);
        invoice.addCharge(shipping);

        calculator.calculateDocument(invoice);

        DocumentTotals totals = invoice.getTotals();
        assertEquals(Money.of("475", "USD"), totals.getSubTotal()); // 250 + 225
        assertEquals(Money.of("90.25", "USD"), totals.getTaxTotal()); // 19% of 475
        assertEquals(Money.of("50", "USD"), totals.getFeeTotal());
        assertEquals(Money.of("615.25", "USD"), totals.getGrandTotal()); // 475 + 90.25 + 50
    }

    @Test
    @DisplayName("Should calculate invoice with discounts")
    void testInvoiceWithDiscounts() {
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-005");
        invoice.setIssueDate(LocalDate.now());

        // 10% discount
        Charge discount = Charge.percentage("DISC10", "Discount 10%", ChargeType.DISCOUNT, new BigDecimal("10"), 10);
        discount.setAppliesTo(ChargeAppliesTo.LINE);
        discount.setBase(ChargeBase.NET);
        invoice.addCharge(discount);

        // VAT after discount
        Charge vat = Charge.percentage("VAT19", "VAT 19%", ChargeType.TAX, new BigDecimal("19"), 20);
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        vat.setBase(ChargeBase.PREVIOUS_TOTAL);
        invoice.addCharge(vat);

        invoice.addLine(DocumentLine.of("Product A", new BigDecimal("10"), Money.of("100", "USD")));

        calculator.calculateDocument(invoice);

        DocumentTotals totals = invoice.getTotals();
        assertEquals(Money.of("1000", "USD"), totals.getSubTotal());
        assertEquals(Money.of("100", "USD"), totals.getDiscountTotal()); // 10% of 1000
        assertEquals(Money.of("171", "USD"), totals.getTaxTotal()); // 19% of 900
        assertEquals(Money.of("1071", "USD"), totals.getGrandTotal()); // 1000 - 100 + 171
    }

    @Test
    @DisplayName("Should calculate invoice with withholdings")
    void testInvoiceWithWithholdings() {
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-006");
        invoice.setIssueDate(LocalDate.now());

        // VAT
        Charge vat = Charge.percentage("VAT19", "VAT 19%", ChargeType.TAX, new BigDecimal("19"), 20);
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        invoice.addCharge(vat);

        // Withholding
        Charge withholding = Charge.percentage("WHOLD", "Withholding 2.5%", ChargeType.WITHHOLDING, new BigDecimal("2.5"), 30);
        withholding.setAppliesTo(ChargeAppliesTo.LINE);
        withholding.setBase(ChargeBase.PREVIOUS_TOTAL);
        invoice.addCharge(withholding);

        invoice.addLine(DocumentLine.of("Service", new BigDecimal("1"), Money.of("1000", "USD")));

        calculator.calculateDocument(invoice);

        DocumentTotals totals = invoice.getTotals();
        assertEquals(Money.of("1000", "USD"), totals.getSubTotal());
        assertEquals(Money.of("190", "USD"), totals.getTaxTotal());
        assertEquals(Money.of("29.75", "USD"), totals.getWithholdingTotal()); // 2.5% of 1190
        assertEquals(Money.of("1190", "USD"), totals.getGrandTotal());
        assertEquals(Money.of("1160.25", "USD"), totals.getPayableTotal()); // Grand total - withholding
    }

    @Test
    @DisplayName("Should calculate complex invoice with all charge types")
    void testComplexInvoice() {
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-007");
        invoice.setIssueDate(LocalDate.now());

        // Discount 10% (priority 10)
        Charge discount = Charge.percentage("DISC10", "Discount", ChargeType.DISCOUNT, new BigDecimal("10"), 10);
        discount.setAppliesTo(ChargeAppliesTo.LINE);
        invoice.addCharge(discount);

        // VAT 19% (priority 20)
        Charge vat = Charge.percentage("VAT19", "VAT", ChargeType.TAX, new BigDecimal("19"), 20);
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        vat.setBase(ChargeBase.PREVIOUS_TOTAL);
        invoice.addCharge(vat);

        // Withholding 2.5% (priority 30)
        Charge withholding = Charge.percentage("WHOLD", "Withholding", ChargeType.WITHHOLDING, new BigDecimal("2.5"), 30);
        withholding.setAppliesTo(ChargeAppliesTo.LINE);
        withholding.setBase(ChargeBase.PREVIOUS_TOTAL);
        invoice.addCharge(withholding);

        // Shipping fee at document level
        Charge shipping = Charge.fixed("SHIP", "Shipping", ChargeType.FEE, new BigDecimal("50"), 100);
        shipping.setAppliesTo(ChargeAppliesTo.DOCUMENT);
        invoice.addCharge(shipping);

        invoice.addLine(DocumentLine.of("Product A", new BigDecimal("10"), Money.of("100", "USD")));
        invoice.addLine(DocumentLine.of("Product B", new BigDecimal("5"), Money.of("200", "USD")));

        calculator.calculateDocument(invoice);

        DocumentTotals totals = invoice.getTotals();
        assertNotNull(totals);
        assertTrue(totals.getSubTotal().isPositive());
        assertTrue(totals.getGrandTotal().isPositive());
        assertTrue(totals.getPayableTotal().isPositive());
    }

    @Test
    @DisplayName("Should throw exception when calculating posted document")
    void testCalculatePostedDocument() {
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-008");
        invoice.setIssueDate(LocalDate.now());
        invoice.addLine(DocumentLine.of("Product A", new BigDecimal("1"), Money.of("100", "USD")));

        // Calculate first time
        calculator.calculateDocument(invoice);

        // Post the document
        invoice.post();

        // Try to calculate again
        assertThrows(InvalidDocumentStateException.class, () -> calculator.calculateDocument(invoice));
    }

    @Test
    @DisplayName("Should recalculate draft document")
    void testRecalculateDraft() {
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-009");
        invoice.setIssueDate(LocalDate.now());
        invoice.addLine(DocumentLine.of("Product A", new BigDecimal("10"), Money.of("100", "USD")));

        calculator.calculateDocument(invoice);
        Money firstTotal = invoice.getTotals().getGrandTotal();

        // Modify document
        invoice.addLine(DocumentLine.of("Product B", new BigDecimal("5"), Money.of("50", "USD")));

        // Recalculate
        calculator.recalculate(invoice);
        Money secondTotal = invoice.getTotals().getGrandTotal();

        assertNotEquals(firstTotal, secondTotal);
        assertEquals(Money.of("1250", "USD"), secondTotal);
    }

    @Test
    @DisplayName("Should throw exception for empty document")
    void testEmptyDocument() {
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-010");
        invoice.setIssueDate(LocalDate.now());

        assertThrows(IllegalStateException.class, () -> calculator.calculateDocument(invoice));
    }

    @Test
    @DisplayName("Should calculate credit note")
    void testCreditNote() {
        FinancialDocument creditNote = FinancialDocument.of(DocumentType.CREDIT_NOTE, "USD");
        creditNote.setDocumentNumber("CN-001");
        creditNote.setIssueDate(LocalDate.now());

        creditNote.addLine(DocumentLine.of("Return Product A", new BigDecimal("5"), Money.of("100", "USD")));

        Charge vat = Charge.percentage("VAT19", "VAT 19%", ChargeType.TAX, new BigDecimal("19"), 20);
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        creditNote.addCharge(vat);

        calculator.calculateDocument(creditNote);

        DocumentTotals totals = creditNote.getTotals();
        assertEquals(Money.of("500", "USD"), totals.getSubTotal());
        assertEquals(Money.of("95", "USD"), totals.getTaxTotal());
        assertEquals(Money.of("595", "USD"), totals.getGrandTotal());
    }

    @Test
    @DisplayName("Should calculate purchase order")
    void testPurchaseOrder() {
        FinancialDocument purchaseOrder = FinancialDocument.of(DocumentType.PURCHASE, "USD");
        purchaseOrder.setDocumentNumber("PO-001");
        purchaseOrder.setIssueDate(LocalDate.now());

        purchaseOrder.addLine(DocumentLine.of("Raw Material A", new BigDecimal("100"), Money.of("10", "USD")));

        calculator.calculateDocument(purchaseOrder);

        DocumentTotals totals = purchaseOrder.getTotals();
        assertEquals(Money.of("1000", "USD"), totals.getSubTotal());
    }
}
