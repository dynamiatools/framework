package tools.dynamia.domain;

import org.junit.Test;

import java.math.BigDecimal;

public class CurrencyExchangeProviderTest {


    @Test
    public void shouldConvertBetweenCurrencies() {
        String appId = System.getProperty("openExchangeApiKey");
        if (appId != null) {

            var provider = new OpenExchangeRatesCurrencyProvider(appId, true);


            System.out.println("$1 USD o COP: " + provider.convert("USD", "COP", BigDecimal.valueOf(1)));
            System.out.println("$1 EUR o COP: " + provider.convert("EUR", "COP", BigDecimal.valueOf(1)));

            System.out.println("$150.000 COP to USD: " + provider.convert("COP", "USD", BigDecimal.valueOf(150000)));

            System.out.println("$150.000 COP to EUR: " + provider.convert("COP", "EUR", BigDecimal.valueOf(150000)));

            System.out.println("$29 USD to COP: " + provider.convert("USD", "COP", BigDecimal.valueOf(29)));
            System.out.println("$29 USD to EUR: " + provider.convert("USD", "EUR", BigDecimal.valueOf(29)));

            System.out.println("$35.000 ARS to USD: " + provider.convert("COP", "USD", BigDecimal.valueOf(35000)));
            System.out.println("\n\nCACHE--> " + OpenExchangeRatesCurrencyProvider.getCacheRates());
        } else {
            System.err.println("No API key, pass property -DopenExchangeApiKey");
        }
    }
}
