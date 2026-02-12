package tools.dynamia.modules.finances.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MoneyCalculator utility class.
 *
 * @author Dynamia Finance Framework
 */
@DisplayName("MoneyCalculator Tests")
class MoneyCalculatorTest {

    @Test
    @DisplayName("Should round money to specified scale")
    void testRound() {
        Money money = Money.of("100.567", "USD");

        Money rounded = MoneyCalculator.round(money, 2);

        assertEquals(new BigDecimal("100.57"), rounded.getAmount());
    }

    @Test
    @DisplayName("Should round money to currency")
    void testRoundToCurrency() {
        Money money = Money.of("100.567", "USD");

        Money rounded = MoneyCalculator.roundToCurrency(money);

        assertEquals(new BigDecimal("100.57"), rounded.getAmount());
    }

    @Test
    @DisplayName("Should sum multiple money amounts")
    void testSum() {
        Money m1 = Money.of("100.00", "USD");
        Money m2 = Money.of("50.50", "USD");
        Money m3 = Money.of("25.25", "USD");

        Money result = MoneyCalculator.sum(m1, m2, m3);

        assertEquals(new BigDecimal("175.75"), result.getAmount());
    }

    @Test
    @DisplayName("Should calculate percentage")
    void testPercentage() {
        Money base = Money.of("1000.00", "USD");

        Money result = MoneyCalculator.percentage(base, new BigDecimal("19"));

        assertEquals(0, new BigDecimal("190.00").compareTo(result.getAmount()));
    }

    @Test
    @DisplayName("Should get currency scale")
    void testGetCurrencyScale() {
        int usdScale = MoneyCalculator.getCurrencyScale("USD");
        int jpyScale = MoneyCalculator.getCurrencyScale("JPY");

        assertEquals(2, usdScale);
        assertEquals(0, jpyScale);
    }

    @Test
    @DisplayName("Should check money equality within tolerance")
    void testEquals() {
        Money m1 = Money.of("100.00", "USD");
        Money m2 = Money.of("100.01", "USD");
        Money tolerance = Money.of("0.02", "USD");

        assertTrue(MoneyCalculator.equals(m1, m2, tolerance));
    }

    @Test
    @DisplayName("Should return maximum money")
    void testMax() {
        Money m1 = Money.of("100.00", "USD");
        Money m2 = Money.of("150.00", "USD");

        Money max = MoneyCalculator.max(m1, m2);

        assertEquals(m2, max);
    }

    @Test
    @DisplayName("Should return minimum money")
    void testMin() {
        Money m1 = Money.of("100.00", "USD");
        Money m2 = Money.of("150.00", "USD");

        Money min = MoneyCalculator.min(m1, m2);

        assertEquals(m1, min);
    }

    @Test
    @DisplayName("Should handle null in max")
    void testMaxWithNull() {
        Money m1 = Money.of("100.00", "USD");

        Money max = MoneyCalculator.max(m1, null);

        assertEquals(m1, max);
    }

    @Test
    @DisplayName("Should handle null in min")
    void testMinWithNull() {
        Money m1 = Money.of("100.00", "USD");

        Money min = MoneyCalculator.min(m1, null);

        assertEquals(m1, min);
    }

    @Test
    @DisplayName("Should throw exception for null money in sum")
    void testSumWithNull() {
        assertThrows(IllegalArgumentException.class, () -> MoneyCalculator.sum());
    }
}
