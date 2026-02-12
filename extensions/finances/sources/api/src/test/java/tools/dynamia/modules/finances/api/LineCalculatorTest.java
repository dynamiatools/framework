package tools.dynamia.modules.finances.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LineCalculator.
 * Tests line-level calculations with various charge combinations.
 *
 * @author Dynamia Finance Framework
 */
@DisplayName("LineCalculator Tests")
class LineCalculatorTest {

    private LineCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new LineCalculator();
    }

    @Test
    @DisplayName("Should calculate simple line without charges")
    void testSimpleLine() {
        DocumentLine line = DocumentLine.of("Product A", 10, Money.of("100", "USD"));

        calculator.calculate(line);

        LineTotals totals = line.getTotals();
        assertNotNull(totals);
        assertEquals(Money.of("1000", "USD"), totals.getBaseAmount());
        assertEquals(Money.zero("USD"), totals.getDiscountTotal());
        assertEquals(Money.zero("USD"), totals.getTaxTotal());
        assertEquals(Money.of("1000", "USD"), totals.getNetTotal());
    }

    @Test
    @DisplayName("Should calculate line with VAT")
    void testLineWithTax() {
        DocumentLine line = DocumentLine.of("Product A", 10, Money.of("100", "USD"));

        Charge vat = Charge.percentage("VAT19", "VAT 19%", ChargeType.TAX, new BigDecimal("19"), 20);
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        vat.setBase(ChargeBase.NET);
        line.addCharge(vat);

        calculator.calculate(line);

        LineTotals totals = line.getTotals();
        assertEquals(Money.of("1000", "USD"), totals.getBaseAmount());
        assertEquals(Money.of("190", "USD"), totals.getTaxTotal());
        assertEquals(Money.of("1190", "USD"), totals.getNetTotal());
    }

    @Test
    @DisplayName("Should calculate line with discount")
    void testLineWithDiscount() {
        DocumentLine line = DocumentLine.of("Product A", 10, Money.of("100", "USD"));

        Charge discount = Charge.percentage("DISC10", "Discount 10%", ChargeType.DISCOUNT, new BigDecimal("10"), 10);
        discount.setAppliesTo(ChargeAppliesTo.LINE);
        discount.setBase(ChargeBase.NET);
        line.addCharge(discount);

        calculator.calculate(line);

        LineTotals totals = line.getTotals();
        assertEquals(Money.of("1000", "USD"), totals.getBaseAmount());
        assertEquals(Money.of("100", "USD"), totals.getDiscountTotal());
        assertEquals(Money.of("900", "USD"), totals.getNetTotal());
    }

    @Test
    @DisplayName("Should calculate line with discount and tax")
    void testLineWithDiscountAndTax() {
        DocumentLine line = DocumentLine.of("Product A", 10, Money.of("100", "USD"));

        // Discount applied first (priority 10)
        Charge discount = Charge.percentage("DISC10", "Discount 10%", ChargeType.DISCOUNT, new BigDecimal("10"), 10);
        discount.setAppliesTo(ChargeAppliesTo.LINE);
        discount.setBase(ChargeBase.NET);
        line.addCharge(discount);

        // Tax applied after discount (priority 20)
        Charge vat = Charge.percentage("VAT19", "VAT 19%", ChargeType.TAX, new BigDecimal("19"), 20);
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        vat.setBase(ChargeBase.PREVIOUS_TOTAL);
        line.addCharge(vat);

        calculator.calculate(line);

        LineTotals totals = line.getTotals();
        assertEquals(Money.of("1000", "USD"), totals.getBaseAmount());
        assertEquals(Money.of("100", "USD"), totals.getDiscountTotal());

        // Tax should be 19% of 900 (after discount)
        assertEquals(Money.of("171", "USD"), totals.getTaxTotal());
        assertEquals(Money.of("1071", "USD"), totals.getNetTotal());
    }

    @Test
    @DisplayName("Should calculate line with withholding")
    void testLineWithWithholding() {
        DocumentLine line = DocumentLine.of("Service", 1, Money.of("1000", "USD"));

        Charge withholding = Charge.percentage("WHOLD", "Withholding 2.5%", ChargeType.WITHHOLDING, new BigDecimal("2.5"), 30);
        withholding.setAppliesTo(ChargeAppliesTo.LINE);
        withholding.setBase(ChargeBase.NET);
        line.addCharge(withholding);

        calculator.calculate(line);

        LineTotals totals = line.getTotals();
        assertEquals(Money.of("1000", "USD"), totals.getBaseAmount());
        assertEquals(Money.of("25", "USD"), totals.getWithholdingTotal());
        // Withholding doesn't affect net total
        assertEquals(Money.of("1000", "USD"), totals.getNetTotal());
    }

    @Test
    @DisplayName("Should calculate complex line with all charge types")
    void testComplexLine() {
        DocumentLine line = DocumentLine.of("Premium Service", 1, Money.of("5000", "USD"));

        // Discount 10% (priority 10)
        Charge discount = Charge.percentage("DISC10", "Discount", ChargeType.DISCOUNT, new BigDecimal("10"), 10);
        discount.setAppliesTo(ChargeAppliesTo.LINE);
        discount.setBase(ChargeBase.NET);
        line.addCharge(discount);

        // Tax 19% (priority 20)
        Charge vat = Charge.percentage("VAT19", "VAT", ChargeType.TAX, new BigDecimal("19"), 20);
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        vat.setBase(ChargeBase.PREVIOUS_TOTAL);
        line.addCharge(vat);

        // Withholding 2.5% (priority 30)
        Charge withholding = Charge.percentage("WHOLD", "Withholding", ChargeType.WITHHOLDING, new BigDecimal("2.5"), 30);
        withholding.setAppliesTo(ChargeAppliesTo.LINE);
        withholding.setBase(ChargeBase.PREVIOUS_TOTAL);
        line.addCharge(withholding);

        calculator.calculate(line);

        LineTotals totals = line.getTotals();
        assertEquals(Money.of("5000", "USD"), totals.getBaseAmount());
        assertEquals(Money.of("500", "USD"), totals.getDiscountTotal()); // 10% of 5000
        assertEquals(Money.of("855", "USD"), totals.getTaxTotal()); // 19% of 4500
        assertEquals(Money.of("133.88", "USD").round(2), totals.getWithholdingTotal().round(2)); // 2.5% of 5355
        assertEquals(Money.of("5355", "USD"), totals.getNetTotal()); // 5000 - 500 + 855
    }

    @Test
    @DisplayName("Should handle fractional quantities")
    void testFractionalQuantity() {
        DocumentLine line = DocumentLine.of("Item", 2.5, Money.of("100", "USD"));

        calculator.calculate(line);

        LineTotals totals = line.getTotals();
        assertEquals(Money.of("250", "USD"), totals.getBaseAmount());
    }
}
