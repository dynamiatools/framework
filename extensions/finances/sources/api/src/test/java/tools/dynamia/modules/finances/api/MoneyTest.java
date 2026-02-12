package tools.dynamia.modules.finances.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Money value object.
 * Tests all arithmetic operations, currency handling, and utility methods.
 *
 * @author Dynamia Finance Framework
 */
@DisplayName("Money Tests")
class MoneyTest {

    @Test
    @DisplayName("Should create Money with BigDecimal amount")
    void testCreateWithBigDecimal() {
        Money money = Money.of(new BigDecimal("100.50"), "USD");

        assertEquals(new BigDecimal("100.50"), money.getAmount());
        assertEquals("USD", money.getCurrencyCode());
    }

    @Test
    @DisplayName("Should create Money with String amount")
    void testCreateWithString() {
        Money money = Money.of("100.50", "EUR");

        assertEquals(new BigDecimal("100.50"), money.getAmount());
        assertEquals("EUR", money.getCurrencyCode());
    }

    @Test
    @DisplayName("Should create zero Money")
    void testCreateZero() {
        Money money = Money.zero("GBP");

        assertEquals(BigDecimal.ZERO, money.getAmount());
        assertEquals("GBP", money.getCurrencyCode());
        assertTrue(money.isZero());
    }

    @Test
    @DisplayName("Should add two Money amounts with same currency")
    void testAdd() {
        Money money1 = Money.of("100.00", "USD");
        Money money2 = Money.of("50.50", "USD");

        Money result = money1.add(money2);

        assertEquals(new BigDecimal("150.50"), result.getAmount());
        assertEquals("USD", result.getCurrencyCode());
    }

    @Test
    @DisplayName("Should throw exception when adding different currencies")
    void testAddDifferentCurrencies() {
        Money usd = Money.of("100.00", "USD");
        Money eur = Money.of("50.00", "EUR");

        assertThrows(InvalidCurrencyOperationException.class, () -> usd.add(eur));
    }

    @Test
    @DisplayName("Should subtract two Money amounts")
    void testSubtract() {
        Money money1 = Money.of("100.00", "USD");
        Money money2 = Money.of("30.50", "USD");

        Money result = money1.subtract(money2);

        assertEquals(new BigDecimal("69.50"), result.getAmount());
        assertEquals("USD", result.getCurrencyCode());
    }

    @Test
    @DisplayName("Should multiply Money by BigDecimal")
    void testMultiplyByBigDecimal() {
        Money money = Money.of("100.00", "USD");

        Money result = money.multiply(new BigDecimal("2.5"));

        assertEquals(0, new BigDecimal("250.00").compareTo(result.getAmount()));
        assertEquals("USD", result.getCurrencyCode());
    }

    @Test
    @DisplayName("Should multiply Money by double")
    void testMultiplyByDouble() {
        Money money = Money.of("100.00", "USD");

        Money result = money.multiply(1.5);

        assertEquals(0, new BigDecimal("150.00").compareTo(result.getAmount()));
    }

    @Test
    @DisplayName("Should calculate percentage")
    void testPercentage() {
        Money money = Money.of("1000.00", "USD");

        Money result = money.percentage(new BigDecimal("19"));

        assertEquals(0, new BigDecimal("190.00").compareTo(result.getAmount()));
    }

    @Test
    @DisplayName("Should round to specified scale")
    void testRound() {
        Money money = Money.of("100.567", "USD");

        Money result = money.round(2);

        assertEquals(new BigDecimal("100.57"), result.getAmount());
    }

    @Test
    @DisplayName("Should round to currency default")
    void testRoundToCurrency() {
        Money money = Money.of("100.567", "USD");

        Money result = money.roundToCurrency();

        assertEquals(new BigDecimal("100.57"), result.getAmount());
    }

    @Test
    @DisplayName("Should return absolute value")
    void testAbs() {
        Money negative = Money.of("-100.00", "USD");

        Money result = negative.abs();

        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertTrue(result.isPositive());
    }

    @Test
    @DisplayName("Should negate Money")
    void testNegate() {
        Money positive = Money.of("100.00", "USD");

        Money result = positive.negate();

        assertEquals(new BigDecimal("-100.00"), result.getAmount());
        assertTrue(result.isNegative());
    }

    @Test
    @DisplayName("Should check if Money is zero")
    void testIsZero() {
        Money zero = Money.zero("USD");
        Money nonZero = Money.of("1.00", "USD");

        assertTrue(zero.isZero());
        assertFalse(nonZero.isZero());
    }

    @Test
    @DisplayName("Should check if Money is positive")
    void testIsPositive() {
        Money positive = Money.of("100.00", "USD");
        Money negative = Money.of("-100.00", "USD");
        Money zero = Money.zero("USD");

        assertTrue(positive.isPositive());
        assertFalse(negative.isPositive());
        assertFalse(zero.isPositive());
    }

    @Test
    @DisplayName("Should check if Money is negative")
    void testIsNegative() {
        Money positive = Money.of("100.00", "USD");
        Money negative = Money.of("-100.00", "USD");
        Money zero = Money.zero("USD");

        assertFalse(positive.isNegative());
        assertTrue(negative.isNegative());
        assertFalse(zero.isNegative());
    }

    @Test
    @DisplayName("Should compare Money amounts")
    void testCompareTo() {
        Money money1 = Money.of("100.00", "USD");
        Money money2 = Money.of("50.00", "USD");
        Money money3 = Money.of("100.00", "USD");

        assertTrue(money1.compareTo(money2) > 0);
        assertTrue(money2.compareTo(money1) < 0);
        assertEquals(0, money1.compareTo(money3));
    }

    @Test
    @DisplayName("Should test Money equality")
    void testEquals() {
        Money money1 = Money.of("100.00", "USD");
        Money money2 = Money.of("100.00", "USD");
        Money money3 = Money.of("100.00", "EUR");
        Money money4 = Money.of("50.00", "USD");

        assertEquals(money1, money2);
        assertNotEquals(money1, money3);
        assertNotEquals(money1, money4);
    }

    @Test
    @DisplayName("Should get available currencies")
    void testGetAvailableCurrencies() {
        var currencies = Money.getAvailableCurrencies();

        assertNotNull(currencies);
        assertFalse(currencies.isEmpty());
        assertTrue(currencies.contains("USD"));
        assertTrue(currencies.contains("EUR"));
        assertTrue(currencies.contains("GBP"));
    }

    @Test
    @DisplayName("Should get currency for locale")
    void testGetCurrencyForLocale() {
        String usCurrency = Money.getCurrencyForLocale(java.util.Locale.US);
        String germanCurrency = Money.getCurrencyForLocale(java.util.Locale.GERMANY);
        String japanCurrency = Money.getCurrencyForLocale(java.util.Locale.JAPAN);

        assertEquals("USD", usCurrency);
        assertEquals("EUR", germanCurrency);
        assertEquals("JPY", japanCurrency);
    }

    @Test
    @DisplayName("Should get current currency")
    void testGetCurrentCurrency() {
        // Save the current default locale
        Locale originalLocale = java.util.Locale.getDefault();

        try {
            // Set a locale with a valid country code for testing
            java.util.Locale.setDefault(java.util.Locale.US);

            String currency = Money.getCurrentCurrency();

            assertNotNull(currency);
            assertFalse(currency.isEmpty());
            assertEquals("USD", currency);
        } finally {
            // Restore the original locale
            java.util.Locale.setDefault(originalLocale);
        }
    }

    @Test
    @DisplayName("Should throw exception for null amount")
    void testNullAmount() {
        assertThrows(NullPointerException.class, () -> Money.of((BigDecimal) null, "USD"));
    }

    @Test
    @DisplayName("Should throw exception for null currency")
    void testNullCurrency() {
        assertThrows(NullPointerException.class, () -> Money.of("100", null));
    }
}
