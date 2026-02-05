package tools.dynamia.modules.finances.api.examples;

import tools.dynamia.modules.finances.api.Money;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Example demonstrating the new currency utility methods in Money class.
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public class MoneyCurrencyExample {

    static void main(String[] args) {
        System.out.println("=== Money Currency Utility Methods Example ===\n");

        // Example 1: Get all available currencies
        getAllCurrenciesExample();

        // Example 2: Get currency for specific locale
        getCurrencyForLocaleExample();

        // Example 3: Get current currency
        getCurrentCurrencyExample();

        // Example 4: Create Money with locale-based currency
        createMoneyWithLocaleExample();
    }

    /**
     * Example 1: Get list of all available currencies.
     */
    private static void getAllCurrenciesExample() {
        System.out.println("1. Get All Available Currencies:");

        List<String> currencies = Money.getAvailableCurrencies();

        System.out.println("   Total currencies available: " + currencies.size());
        System.out.println("   First 10 currencies:");
        currencies.stream()
            .limit(10)
            .forEach(currency -> System.out.println("   - " + currency));

        System.out.println();
    }

    /**
     * Example 2: Get currency for specific locales.
     */
    private static void getCurrencyForLocaleExample() {
        System.out.println("2. Get Currency for Specific Locale:");

        // United States
        String usCurrency = Money.getCurrencyForLocale(Locale.US);
        System.out.println("   US Locale: " + usCurrency);

        // Germany (Europe)
        String germanCurrency = Money.getCurrencyForLocale(Locale.GERMANY);
        System.out.println("   Germany Locale: " + germanCurrency);

        // Japan
        String japanCurrency = Money.getCurrencyForLocale(Locale.JAPAN);
        System.out.println("   Japan Locale: " + japanCurrency);

        // United Kingdom
        String ukCurrency = Money.getCurrencyForLocale(Locale.UK);
        System.out.println("   UK Locale: " + ukCurrency);

        // Custom locale - Colombia
        String colombiaCurrency = Money.getCurrencyForLocale(new Locale("es", "CO"));
        System.out.println("   Colombia Locale: " + colombiaCurrency);

        System.out.println();
    }

    /**
     * Example 3: Get current system currency.
     */
    private static void getCurrentCurrencyExample() {
        System.out.println("3. Get Current System Currency:");

        String currentCurrency = Money.getCurrentCurrency();
        Locale currentLocale = Locale.getDefault();

        System.out.println("   Current Locale: " + currentLocale.getDisplayName());
        System.out.println("   Current Currency: " + currentCurrency);

        System.out.println();
    }

    /**
     * Example 4: Create Money instances using locale-based currencies.
     */
    private static void createMoneyWithLocaleExample() {
        System.out.println("4. Create Money with Locale-Based Currency:");

        // Create money for US
        String usCurrency = Money.getCurrencyForLocale(Locale.US);
        Money usPrice = Money.of(new BigDecimal("100.00"), usCurrency);
        System.out.println("   US Price: " + usPrice);

        // Create money for Europe
        String euroCurrency = Money.getCurrencyForLocale(Locale.GERMANY);
        Money euroPrice = Money.of(new BigDecimal("85.50"), euroCurrency);
        System.out.println("   Euro Price: " + euroPrice);

        // Create money for current locale
        String currentCurrency = Money.getCurrentCurrency();
        Money localPrice = Money.of(new BigDecimal("50.00"), currentCurrency);
        System.out.println("   Local Price: " + localPrice);

        System.out.println();
    }
}
