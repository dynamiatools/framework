package tools.dynamia.modules.finances.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LineTotals and DocumentTotals value objects.
 *
 * @author Dynamia Finance Framework
 */
@DisplayName("Totals Tests")
class TotalsTest {

    @Test
    @DisplayName("Should create LineTotals with builder")
    void testLineTotalsBuilder() {
        LineTotals totals = LineTotals.builder()
            .baseAmount(Money.of("1000", "USD"))
            .discountTotal(Money.of("100", "USD"))
            .taxTotal(Money.of("171", "USD"))
            .withholdingTotal(Money.of("25", "USD"))
            .feeTotal(Money.of("10", "USD"))
            .netTotal(Money.of("1071", "USD"))
            .build();

        assertNotNull(totals);
        assertEquals(Money.of("1000", "USD"), totals.getBaseAmount());
        assertEquals(Money.of("100", "USD"), totals.getDiscountTotal());
        assertEquals(Money.of("171", "USD"), totals.getTaxTotal());
        assertEquals(Money.of("25", "USD"), totals.getWithholdingTotal());
        assertEquals(Money.of("10", "USD"), totals.getFeeTotal());
        assertEquals(Money.of("1071", "USD"), totals.getNetTotal());
    }

    @Test
    @DisplayName("Should create zero LineTotals")
    void testZeroLineTotals() {
        LineTotals totals = LineTotals.zero("USD");

        assertTrue(totals.getBaseAmount().isZero());
        assertTrue(totals.getDiscountTotal().isZero());
        assertTrue(totals.getTaxTotal().isZero());
        assertTrue(totals.getWithholdingTotal().isZero());
        assertTrue(totals.getFeeTotal().isZero());
        assertTrue(totals.getNetTotal().isZero());
    }

    @Test
    @DisplayName("Should throw exception for incomplete LineTotals")
    void testIncompleteLineTotals() {
        assertThrows(NullPointerException.class, () ->
            LineTotals.builder()
                .baseAmount(Money.of("1000", "USD"))
                .build()
        );
    }

    @Test
    @DisplayName("Should create DocumentTotals with builder")
    void testDocumentTotalsBuilder() {
        DocumentTotals totals = DocumentTotals.builder()
            .subTotal(Money.of("10000", "USD"))
            .discountTotal(Money.of("1000", "USD"))
            .taxTotal(Money.of("1710", "USD"))
            .withholdingTotal(Money.of("200", "USD"))
            .feeTotal(Money.of("50", "USD"))
            .grandTotal(Money.of("10760", "USD"))
            .payableTotal(Money.of("10560", "USD"))
            .build();

        assertNotNull(totals);
        assertEquals(Money.of("10000", "USD"), totals.getSubTotal());
        assertEquals(Money.of("1000", "USD"), totals.getDiscountTotal());
        assertEquals(Money.of("1710", "USD"), totals.getTaxTotal());
        assertEquals(Money.of("200", "USD"), totals.getWithholdingTotal());
        assertEquals(Money.of("50", "USD"), totals.getFeeTotal());
        assertEquals(Money.of("10760", "USD"), totals.getGrandTotal());
        assertEquals(Money.of("10560", "USD"), totals.getPayableTotal());
    }

    @Test
    @DisplayName("Should create zero DocumentTotals")
    void testZeroDocumentTotals() {
        DocumentTotals totals = DocumentTotals.zero("EUR");

        assertTrue(totals.getSubTotal().isZero());
        assertTrue(totals.getDiscountTotal().isZero());
        assertTrue(totals.getTaxTotal().isZero());
        assertTrue(totals.getWithholdingTotal().isZero());
        assertTrue(totals.getFeeTotal().isZero());
        assertTrue(totals.getGrandTotal().isZero());
        assertTrue(totals.getPayableTotal().isZero());
    }

    @Test
    @DisplayName("Should test LineTotals equality")
    void testLineTotalsEquality() {
        LineTotals totals1 = LineTotals.builder()
            .baseAmount(Money.of("1000", "USD"))
            .discountTotal(Money.zero("USD"))
            .taxTotal(Money.of("190", "USD"))
            .withholdingTotal(Money.zero("USD"))
            .feeTotal(Money.zero("USD"))
            .netTotal(Money.of("1190", "USD"))
            .build();

        LineTotals totals2 = LineTotals.builder()
            .baseAmount(Money.of("1000", "USD"))
            .discountTotal(Money.zero("USD"))
            .taxTotal(Money.of("190", "USD"))
            .withholdingTotal(Money.zero("USD"))
            .feeTotal(Money.zero("USD"))
            .netTotal(Money.of("1190", "USD"))
            .build();

        assertEquals(totals1, totals2);
    }

    @Test
    @DisplayName("Should test DocumentTotals equality")
    void testDocumentTotalsEquality() {
        DocumentTotals totals1 = DocumentTotals.builder()
            .subTotal(Money.of("1000", "USD"))
            .discountTotal(Money.zero("USD"))
            .taxTotal(Money.of("190", "USD"))
            .withholdingTotal(Money.zero("USD"))
            .feeTotal(Money.zero("USD"))
            .grandTotal(Money.of("1190", "USD"))
            .payableTotal(Money.of("1190", "USD"))
            .build();

        DocumentTotals totals2 = DocumentTotals.builder()
            .subTotal(Money.of("1000", "USD"))
            .discountTotal(Money.zero("USD"))
            .taxTotal(Money.of("190", "USD"))
            .withholdingTotal(Money.zero("USD"))
            .feeTotal(Money.zero("USD"))
            .grandTotal(Money.of("1190", "USD"))
            .payableTotal(Money.of("1190", "USD"))
            .build();

        assertEquals(totals1, totals2);
    }
}
