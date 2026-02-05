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
        FinancialDocument doc = FinancialDocument.of(DocumentType.SALE, "EUR");

        ExchangeRate rate = ExchangeRate.of("EUR", "USD", new BigDecimal("1.10"), LocalDate.now());
        doc.setExchangeRate(rate);

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
}
