package tools.dynamia.modules.finances.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FinancialDocument domain model.
 *
 * @author Dynamia Finance Framework
 */
@DisplayName("FinancialDocument Tests")
class FinancialDocumentTest {

    @Test
    @DisplayName("Should create financial document")
    void testCreateDocument() {
        FinancialDocument doc = FinancialDocument.of(DocumentType.SALE, "USD");

        assertEquals(DocumentType.SALE, doc.getType());
        assertEquals("USD", doc.getCurrency());
        assertEquals(DocumentStatus.DRAFT, doc.getStatus());
        assertNotNull(doc.getIssueDate());
    }

    @Test
    @DisplayName("Should add lines to document")
    void testAddLine() {
        FinancialDocument doc = FinancialDocument.of(DocumentType.SALE, "USD");

        DocumentLine line1 = DocumentLine.of("Product A", BigDecimal.ONE, Money.of("100", "USD"));
        DocumentLine line2 = DocumentLine.of("Product B", BigDecimal.ONE, Money.of("200", "USD"));

        doc.addLine(line1);
        doc.addLine(line2);

        assertEquals(2, doc.getLines().size());
        assertEquals(doc, line1.getDocument());
        assertEquals(Integer.valueOf(1), line1.getLineNumber());
        assertEquals(Integer.valueOf(2), line2.getLineNumber());
    }

    @Test
    @DisplayName("Should remove line from document")
    void testRemoveLine() {
        FinancialDocument doc = FinancialDocument.of(DocumentType.SALE, "USD");

        DocumentLine line1 = DocumentLine.of("Product A", BigDecimal.ONE, Money.of("100", "USD"));
        DocumentLine line2 = DocumentLine.of("Product B", BigDecimal.ONE, Money.of("200", "USD"));

        doc.addLine(line1);
        doc.addLine(line2);
        doc.removeLine(line1);

        assertEquals(1, doc.getLines().size());
        assertNull(line1.getDocument());
        assertEquals(Integer.valueOf(1), line2.getLineNumber());
    }

    @Test
    @DisplayName("Should add charges to document")
    void testAddCharge() {
        FinancialDocument doc = FinancialDocument.of(DocumentType.SALE, "USD");

        Charge vat = Charge.percentage("VAT19", "VAT", ChargeType.TAX, new BigDecimal("19"), 20);
        doc.addCharge(vat);

        assertEquals(1, doc.getCharges().size());
        assertTrue(doc.getCharges().contains(vat));
    }

    @Test
    @DisplayName("Should validate document")
    void testValidateDocument() {
        FinancialDocument doc = FinancialDocument.of(DocumentType.SALE, "USD");
        doc.setIssueDate(LocalDate.now());
        doc.addLine(DocumentLine.of("Product A", BigDecimal.ONE, Money.of("100", "USD")));

        assertDoesNotThrow(() -> doc.validate());
    }

    @Test
    @DisplayName("Should throw exception for empty document")
    void testValidateEmptyDocument() {
        FinancialDocument doc = FinancialDocument.of(DocumentType.SALE, "USD");
        doc.setIssueDate(LocalDate.now());

        assertThrows(IllegalStateException.class, () -> doc.validate());
    }

    @Test
    @DisplayName("Should post document")
    void testPostDocument() {
        FinancialDocument doc = FinancialDocument.of(DocumentType.SALE, "USD");

        assertTrue(doc.isDraft());
        assertFalse(doc.isPosted());

        doc.post();

        assertFalse(doc.isDraft());
        assertTrue(doc.isPosted());
        assertEquals(DocumentStatus.POSTED, doc.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when posting non-draft document")
    void testPostNonDraft() {
        FinancialDocument doc = FinancialDocument.of(DocumentType.SALE, "USD");
        doc.post();

        assertThrows(InvalidDocumentStateException.class, () -> doc.post());
    }

    @Test
    @DisplayName("Should cancel document")
    void testCancelDocument() {
        FinancialDocument doc = FinancialDocument.of(DocumentType.SALE, "USD");

        doc.cancel();

        assertTrue(doc.isCancelled());
        assertEquals(DocumentStatus.CANCELLED, doc.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when cancelling already cancelled document")
    void testCancelAlreadyCancelled() {
        FinancialDocument doc = FinancialDocument.of(DocumentType.SALE, "USD");
        doc.cancel();

        assertThrows(InvalidDocumentStateException.class, () -> doc.cancel());
    }

    @Test
    @DisplayName("Should set exchange rate")
    void testSetExchangeRate() {
        ExchangeRate rate = ExchangeRate.of("EUR", "USD", new BigDecimal("1.10"), LocalDate.now());

        FinancialDocument doc = FinancialDocument.of(DocumentType.SALE, "EUR")
                .exchangeRate(rate);

        assertNotNull(doc.getExchangeRate());
        assertEquals(rate, doc.getExchangeRate());
    }

    @Test
    @DisplayName("Should support different document types")
    void testDocumentTypes() {
        FinancialDocument sale = FinancialDocument.of(DocumentType.SALE, "USD");
        FinancialDocument purchase = FinancialDocument.of(DocumentType.PURCHASE, "USD");
        FinancialDocument creditNote = FinancialDocument.of(DocumentType.CREDIT_NOTE, "USD");
        FinancialDocument debitNote = FinancialDocument.of(DocumentType.DEBIT_NOTE, "USD");
        FinancialDocument quote = FinancialDocument.of(DocumentType.QUOTE, "USD");

        assertEquals(DocumentType.SALE, sale.getType());
        assertEquals(DocumentType.PURCHASE, purchase.getType());
        assertEquals(DocumentType.CREDIT_NOTE, creditNote.getType());
        assertEquals(DocumentType.DEBIT_NOTE, debitNote.getType());
        assertEquals(DocumentType.QUOTE, quote.getType());
    }

    @Test
    @DisplayName("Should create complete document using fluent API")
    void testCompleteDocumentWithFluentAPI() {
        // Create a complete invoice using fluent API
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD")
                .id("INV-001")
                .documentNumber("FAC-2026-001")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .party("CUST-12345")
                .reference("ORDER-5678")
                .notes("Urgent delivery required")
                .line(new DocumentLine()
                        .description("Premium Product A")
                        .itemCode("PROD-A")
                        .itemName("Product A")
                        .quantity(new BigDecimal("10"))
                        .unitPrice(Money.of("100", "USD"))
                        .charge(new Charge()
                                .code("VAT19")
                                .name("Value Added Tax 19%")
                                .type(ChargeType.TAX)
                                .rateType(RateType.PERCENTAGE)
                                .value(new BigDecimal("19"))
                                .priority(20)))
                .line(new DocumentLine()
                        .description("Premium Product B")
                        .itemCode("PROD-B")
                        .itemName("Product B")
                        .quantity(new BigDecimal("5"))
                        .unitPrice(Money.of("200", "USD")))
                .charge(new Charge()
                        .code("DISC10")
                        .name("Special Discount 10%")
                        .type(ChargeType.DISCOUNT)
                        .rateType(RateType.PERCENTAGE)
                        .value(new BigDecimal("10"))
                        .appliesTo(ChargeAppliesTo.DOCUMENT)
                        .priority(10));

        // Assertions
        assertEquals("INV-001", invoice.getId());
        assertEquals("FAC-2026-001", invoice.getDocumentNumber());
        assertEquals("CUST-12345", invoice.getParty());
        assertEquals("ORDER-5678", invoice.getReference());
        assertEquals("Urgent delivery required", invoice.getNotes());
        assertEquals(2, invoice.getLines().size());
        assertEquals(1, invoice.getCharges().size());
        assertEquals("Premium Product A", invoice.getLines().get(0).getDescription());
        assertEquals("PROD-A", invoice.getLines().get(0).getItemCode());
        assertEquals(1, invoice.getLines().get(0).getCharges().size());
        assertEquals("Premium Product B", invoice.getLines().get(1).getDescription());
        assertEquals("DISC10", invoice.getCharges().get(0).getCode());
        assertEquals(DocumentStatus.DRAFT, invoice.getStatus());
        assertTrue(invoice.isDraft());
    }

    @Test
    @DisplayName("Should create sales invoice with simplified methods for taxes and charges")
    void testSalesInvoiceWithSimplifiedMethods() {
        // Create sales invoice with multiple items and different VAT rates
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD")
                .id("INV-2026-002")
                .documentNumber("FAC-2026-002")
                .issueDate(LocalDate.of(2026, 2, 5))
                .dueDate(LocalDate.of(2026, 3, 5))
                .party("ACME Corp - Customer 001")
                .reference("PO-9876");

        // Add items with 8% VAT (books, educational materials)
        DocumentLine line1 = DocumentLine.of("Programming Book: Java Fundamentals", new BigDecimal("5"), Money.of("25.00", "USD"))
                .itemCode("BOOK-001")
                .lineNumber(1);
        line1.addTax("VAT8", "Reduced VAT 8%", new BigDecimal("8"));

        DocumentLine line2 = DocumentLine.of("Educational Software License", new BigDecimal("3"), Money.of("50.00", "USD"))
                .itemCode("EDU-002")
                .lineNumber(2);
        line2.addTax("VAT8", "Reduced VAT 8%", new BigDecimal("8"));
        line2.addDiscount("EDU_DISC", "Educational Discount", new BigDecimal("5"));

        // Add items with 19% VAT (standard products)
        DocumentLine line3 = DocumentLine.of("Laptop Computer", new BigDecimal("2"), Money.of("1200.00", "USD"))
                .tax("VAT19", "Standard VAT 19%", new BigDecimal("19"))
                .itemCode("COMP-003")
                .lineNumber(3);

        DocumentLine line4 = DocumentLine.of("Wireless Mouse", new BigDecimal("10"), Money.of("15.00", "USD"))
                .tax("VAT19", "Standard VAT 19%", new BigDecimal("19"))
                .discount("BULK10", "Bulk Discount 10%", new BigDecimal("10"))
                .itemCode("ACC-004")
                .lineNumber(4);

        DocumentLine line5 = DocumentLine.of("USB-C Cable", new BigDecimal("15"), Money.of("8.00", "USD"))
                .tax("VAT19", "Standard VAT 19%", new BigDecimal("19"))
                .fee("HANDLING", "Small Item Handling", new BigDecimal("2.00"))
                .itemCode("ACC-005")
                .lineNumber(5);

        // Add all lines to invoice
        invoice.addLine(line1);
        invoice.addLine(line2);
        invoice.addLine(line3);
        invoice.addLine(line4);
        invoice.addLine(line5);

        // Add document-level charges using simplified methods
        invoice.addDiscount("EARLY_PAY", "Early Payment Discount 5%", new BigDecimal("5"));
        invoice.addFee("SHIP", "Shipping & Handling", new BigDecimal("35.00"));
        invoice.addWithholding("RET_IVA", "IVA Withholding 15%", new BigDecimal("15"));

        // Assertions - Document structure
        assertEquals("INV-2026-002", invoice.getId());
        assertEquals("FAC-2026-002", invoice.getDocumentNumber());
        assertEquals("ACME Corp - Customer 001", invoice.getParty());
        assertEquals("PO-9876", invoice.getReference());
        assertEquals(5, invoice.getLines().size());
        assertEquals(3, invoice.getCharges().size());
        assertTrue(invoice.isDraft());

        // Assertions - Line 1 (Book with 8% VAT)
        assertEquals("Programming Book: Java Fundamentals", line1.getDescription());
        assertEquals("BOOK-001", line1.getItemCode());
        assertEquals(new BigDecimal("5"), line1.getQuantity());
        assertEquals(1, line1.getTaxes().size());
        assertEquals("VAT8", line1.getTaxes().get(0).getCode());
        assertEquals(new BigDecimal("8"), line1.getTaxes().get(0).getValue());

        // Assertions - Line 2 (Educational software with 8% VAT and discount)
        assertEquals("Educational Software License", line2.getDescription());
        assertEquals("EDU-002", line2.getItemCode());
        assertEquals(1, line2.getTaxes().size());
        assertEquals(1, line2.getDiscounts().size());
        assertEquals("VAT8", line2.getTaxes().get(0).getCode());
        assertEquals("EDU_DISC", line2.getDiscounts().get(0).getCode());

        // Assertions - Line 3 (Laptop with 19% VAT)
        assertEquals("Laptop Computer", line3.getDescription());
        assertEquals("COMP-003", line3.getItemCode());
        assertEquals(1, line3.getTaxes().size());
        assertEquals("VAT19", line3.getTaxes().get(0).getCode());
        assertEquals(new BigDecimal("19"), line3.getTaxes().get(0).getValue());

        // Assertions - Line 4 (Mouse with 19% VAT and discount)
        assertEquals("Wireless Mouse", line4.getDescription());
        assertEquals("ACC-004", line4.getItemCode());
        assertEquals(1, line4.getTaxes().size());
        assertEquals(1, line4.getDiscounts().size());
        assertEquals("VAT19", line4.getTaxes().get(0).getCode());
        assertEquals("BULK10", line4.getDiscounts().get(0).getCode());

        // Assertions - Line 5 (Cable with 19% VAT and fee)
        assertEquals("USB-C Cable", line5.getDescription());
        assertEquals("ACC-005", line5.getItemCode());
        assertEquals(1, line5.getTaxes().size());
        assertEquals(1, line5.getFees().size());
        assertEquals("VAT19", line5.getTaxes().get(0).getCode());
        assertEquals("HANDLING", line5.getFees().get(0).getCode());

        // Assertions - Document-level charges
        assertEquals(1, invoice.getDiscounts().size());
        assertEquals(1, invoice.getFees().size());
        assertEquals(1, invoice.getWithholdings().size());
        assertEquals("EARLY_PAY", invoice.getDiscounts().get(0).getCode());
        assertEquals("SHIP", invoice.getFees().get(0).getCode());
        assertEquals("RET_IVA", invoice.getWithholdings().get(0).getCode());

        // Verify all charges are properly configured
        invoice.getCharges().forEach(charge -> {
            assertNotNull(charge.getCode());
            assertNotNull(charge.getName());
            assertNotNull(charge.getType());
            assertNotNull(charge.getRateType());
            assertNotNull(charge.getValue());
            assertNotNull(charge.getPriority());
        });
    }

    @Test
    @DisplayName("Should create a deep copy of financial document")
    void testCopyDocument() {
        // Create original document
        FinancialDocument original = FinancialDocument.of(DocumentType.SALE, "USD")
                .id("DOC-001")
                .documentNumber("INV-100")
                .issueDate(LocalDate.of(2026, 1, 15))
                .dueDate(LocalDate.of(2026, 2, 15))
                .party("Customer XYZ")
                .reference("REF-123")
                .notes("Original document notes");

        // Add exchange rate
        ExchangeRate exchangeRate = ExchangeRate.of("USD", "EUR", new BigDecimal("0.85"), LocalDate.now());
        original.setExchangeRate(exchangeRate);

        // Add lines
        DocumentLine line1 = DocumentLine.of("Product A", new BigDecimal("5"), Money.of("100", "USD"))
                .itemCode("PROD-A")
                .tax("VAT19", "VAT 19%", new BigDecimal("19"));

        DocumentLine line2 = DocumentLine.of("Product B", new BigDecimal("3"), Money.of("50", "USD"))
                .itemCode("PROD-B")
                .tax("VAT8", "VAT 8%", new BigDecimal("8"))
                .discount("DISC5", "Discount 5%", new BigDecimal("5"));

        original.addLine(line1);
        original.addLine(line2);

        // Add document-level charges
        original.addDiscount("EARLY_PAY", "Early Payment", new BigDecimal("10"));
        original.addFee("SHIP", "Shipping", new BigDecimal("25.00"));

        // Post the original
        original.post();

        // Create copy
        FinancialDocument copy = original.copy();

        // Assertions - Copy has reset values
        assertNull(copy.getId(), "Copy should have null ID");
        assertEquals(DocumentStatus.DRAFT, copy.getStatus(), "Copy should be DRAFT");
        assertEquals(LocalDate.now(), copy.getIssueDate(), "Copy should have current date");

        // Assertions - Copy has same basic properties
        assertEquals(original.getType(), copy.getType());
        assertEquals(original.getCurrency(), copy.getCurrency());
        assertEquals(original.getParty(), copy.getParty());
        assertEquals(original.getReference(), copy.getReference());
        assertEquals(original.getNotes(), copy.getNotes());
        assertEquals(original.getDocumentNumber(), copy.getDocumentNumber());
        assertEquals(original.getDueDate(), copy.getDueDate());

        // Assertions - Exchange rate is copied
        assertNotNull(copy.getExchangeRate());
        assertEquals(original.getExchangeRate().getFromCurrency(), copy.getExchangeRate().getFromCurrency());
        assertEquals(original.getExchangeRate().getToCurrency(), copy.getExchangeRate().getToCurrency());
        assertNotSame(original.getExchangeRate(), copy.getExchangeRate(), "Exchange rate should be a new instance");

        // Assertions - Lines are deep copied
        assertEquals(2, copy.getLines().size());
        assertNotSame(original.getLines().get(0), copy.getLines().get(0), "Lines should be new instances");

        DocumentLine copiedLine1 = copy.getLines().get(0);
        assertEquals("Product A", copiedLine1.getDescription());
        assertEquals("PROD-A", copiedLine1.getItemCode());
        assertEquals(new BigDecimal("5"), copiedLine1.getQuantity());
        assertEquals(1, copiedLine1.getTaxes().size());
        assertNotSame(line1, copiedLine1, "Line should be a new instance");

        DocumentLine copiedLine2 = copy.getLines().get(1);
        assertEquals("Product B", copiedLine2.getDescription());
        assertEquals("PROD-B", copiedLine2.getItemCode());
        assertEquals(1, copiedLine2.getTaxes().size());
        assertEquals(1, copiedLine2.getDiscounts().size());

        // Assertions - Charges are deep copied
        assertEquals(2, copy.getCharges().size());
        assertNotSame(original.getCharges().get(0), copy.getCharges().get(0), "Charges should be new instances");

        // Assertions - Totals are reset
        assertNull(copy.getTotals(), "Copy should have null totals");

        // Assertions - Original is not modified
        assertTrue(original.isPosted(), "Original should still be posted");
        assertEquals("DOC-001", original.getId(), "Original ID should not change");
    }

    @Test
    @DisplayName("Should create copy with new document number")
    void testCopyWithNumber() {
        // Create original document
        FinancialDocument original = FinancialDocument.of(DocumentType.SALE, "USD")
                .documentNumber("INV-100")
                .party("Customer ABC");

        DocumentLine line = DocumentLine.of("Item 1", BigDecimal.ONE, Money.of("100", "USD"));
        original.addLine(line);

        // Create copy with new number
        FinancialDocument copy = original.copyWithNumber("INV-200");

        // Assertions
        assertEquals("INV-200", copy.getDocumentNumber());
        assertEquals("INV-100", original.getDocumentNumber());
        assertTrue(copy.isDraft());
        assertEquals(original.getParty(), copy.getParty());
        assertEquals(1, copy.getLines().size());
    }
}
