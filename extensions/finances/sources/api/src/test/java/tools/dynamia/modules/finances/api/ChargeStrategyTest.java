package tools.dynamia.modules.finances.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for charge calculation strategies.
 *
 * @author Dynamia Finance Framework
 */
@DisplayName("ChargeStrategy Tests")
class ChargeStrategyTest {

    @Test
    @DisplayName("PercentageChargeStrategy should calculate percentage correctly")
    void testPercentageStrategy() {
        PercentageChargeStrategy strategy = new PercentageChargeStrategy();

        Charge vat = new Charge();
        vat.setCode("VAT19");
        vat.setValue(new BigDecimal("19"));
        vat.setRateType(RateType.PERCENTAGE);

        Money base = Money.of("1000.00", "USD");
        Money result = strategy.calculate(vat, base, null);

        assertEquals(new BigDecimal("190.00"), result.getAmount());
        assertEquals("USD", result.getCurrencyCode());
    }

    @Test
    @DisplayName("FixedChargeStrategy should return fixed amount")
    void testFixedStrategy() {
        FixedChargeStrategy strategy = new FixedChargeStrategy();

        Charge shipping = new Charge();
        shipping.setCode("SHIP");
        shipping.setValue(new BigDecimal("50.00"));
        shipping.setRateType(RateType.FIXED);

        Money base = Money.of("1000.00", "USD");
        Money result = strategy.calculate(shipping, base, null);

        assertEquals(new BigDecimal("50.00"), result.getAmount());
        assertEquals("USD", result.getCurrencyCode());
    }

    @Test
    @DisplayName("FormulaChargeStrategy should return zero by default")
    void testFormulaStrategy() {
        FormulaChargeStrategy strategy = new FormulaChargeStrategy();

        Charge custom = new Charge();
        custom.setCode("CUSTOM");
        custom.setRateType(RateType.FORMULA);

        Money base = Money.of("1000.00", "USD");
        Money result = strategy.calculate(custom, base, null);

        assertEquals(BigDecimal.ZERO, result.getAmount());
    }

    @Test
    @DisplayName("Should support correct rate types")
    void testStrategySupports() {
        PercentageChargeStrategy percentageStrategy = new PercentageChargeStrategy();
        FixedChargeStrategy fixedStrategy = new FixedChargeStrategy();
        FormulaChargeStrategy formulaStrategy = new FormulaChargeStrategy();

        assertTrue(percentageStrategy.supports(RateType.PERCENTAGE));
        assertFalse(percentageStrategy.supports(RateType.FIXED));

        assertTrue(fixedStrategy.supports(RateType.FIXED));
        assertFalse(fixedStrategy.supports(RateType.PERCENTAGE));

        assertTrue(formulaStrategy.supports(RateType.FORMULA));
        assertFalse(formulaStrategy.supports(RateType.PERCENTAGE));
    }

    @Test
    @DisplayName("Should handle small percentage calculations correctly")
    void testSmallPercentage() {
        PercentageChargeStrategy strategy = new PercentageChargeStrategy();

        Charge discount = new Charge();
        discount.setCode("DISC");
        discount.setValue(new BigDecimal("2.5"));
        discount.setRateType(RateType.PERCENTAGE);

        Money base = Money.of("100.00", "USD");
        Money result = strategy.calculate(discount, base, null);

        assertEquals(new BigDecimal("2.50"), result.getAmount());
    }

    @Test
    @DisplayName("Should handle large percentage calculations correctly")
    void testLargePercentage() {
        PercentageChargeStrategy strategy = new PercentageChargeStrategy();

        Charge tax = new Charge();
        tax.setCode("TAX");
        tax.setValue(new BigDecimal("100"));
        tax.setRateType(RateType.PERCENTAGE);

        Money base = Money.of("1000.00", "USD");
        Money result = strategy.calculate(tax, base, null);

        assertEquals(new BigDecimal("1000.00"), result.getAmount());
    }
}
