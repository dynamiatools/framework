package tools.dynamia.modules.finances.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ExchangeRate value object.
 *
 * @author Dynamia Finance Framework
 */
@DisplayName("ExchangeRate Tests")
class ExchangeRateTest {

    @Test
    @DisplayName("Should create exchange rate")
    void testCreateExchangeRate() {
        ExchangeRate rate = ExchangeRate.of("USD", "EUR", new BigDecimal("0.85"), LocalDate.now());

        assertEquals("USD", rate.getFromCurrency());
        assertEquals("EUR", rate.getToCurrency());
        assertEquals(new BigDecimal("0.85"), rate.getRate());
        assertNotNull(rate.getDate());
    }

    @Test
    @DisplayName("Should create identity exchange rate")
    void testIdentityRate() {
        ExchangeRate rate = ExchangeRate.identity("USD", LocalDate.now());

        assertEquals("USD", rate.getFromCurrency());
        assertEquals("USD", rate.getToCurrency());
        assertEquals(BigDecimal.ONE, rate.getRate());
        assertTrue(rate.isIdentity());
    }

    @Test
    @DisplayName("Should convert money using exchange rate")
    void testConvert() {
        ExchangeRate rate = ExchangeRate.of("USD", "EUR", new BigDecimal("0.85"), LocalDate.now());
        Money usd = Money.of("100.00", "USD");

        Money eur = rate.convert(usd);

        assertEquals(0, new BigDecimal("85.00").compareTo(eur.getAmount()));
        assertEquals("EUR", eur.getCurrencyCode());
    }

    @Test
    @DisplayName("Should throw exception when converting wrong currency")
    void testConvertWrongCurrency() {
        ExchangeRate rate = ExchangeRate.of("USD", "EUR", new BigDecimal("0.85"), LocalDate.now());
        Money gbp = Money.of("100.00", "GBP");

        assertThrows(InvalidCurrencyOperationException.class, () -> rate.convert(gbp));
    }

    @Test
    @DisplayName("Should create inverse exchange rate")
    void testInverse() {
        ExchangeRate rate = ExchangeRate.of("USD", "EUR", new BigDecimal("0.85"), LocalDate.now());

        ExchangeRate inverse = rate.inverse();

        assertEquals("EUR", inverse.getFromCurrency());
        assertEquals("USD", inverse.getToCurrency());
        assertTrue(inverse.getRate().compareTo(new BigDecimal("1.17")) > 0);
    }

    @Test
    @DisplayName("Should check if rate is identity")
    void testIsIdentity() {
        ExchangeRate identity = ExchangeRate.identity("USD", LocalDate.now());
        ExchangeRate normal = ExchangeRate.of("USD", "EUR", new BigDecimal("0.85"), LocalDate.now());

        assertTrue(identity.isIdentity());
        assertFalse(normal.isIdentity());
    }

    @Test
    @DisplayName("Should throw exception for negative rate")
    void testNegativeRate() {
        assertThrows(IllegalArgumentException.class, () ->
            ExchangeRate.of("USD", "EUR", new BigDecimal("-0.85"), LocalDate.now())
        );
    }

    @Test
    @DisplayName("Should throw exception for zero rate")
    void testZeroRate() {
        assertThrows(IllegalArgumentException.class, () ->
            ExchangeRate.of("USD", "EUR", BigDecimal.ZERO, LocalDate.now())
        );
    }

    @Test
    @DisplayName("Should test equality")
    void testEquals() {
        LocalDate date = LocalDate.now();
        ExchangeRate rate1 = ExchangeRate.of("USD", "EUR", new BigDecimal("0.85"), date);
        ExchangeRate rate2 = ExchangeRate.of("USD", "EUR", new BigDecimal("0.85"), date);
        ExchangeRate rate3 = ExchangeRate.of("USD", "GBP", new BigDecimal("0.85"), date);

        assertEquals(rate1, rate2);
        assertNotEquals(rate1, rate3);
    }
}
