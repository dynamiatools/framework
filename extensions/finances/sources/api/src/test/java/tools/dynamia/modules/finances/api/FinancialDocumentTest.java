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
}
