package tools.dynamia.domain;

import java.math.BigDecimal;

/***
 * Basic API for currency exchange provider. *
 */
public interface CurrencyExchangeProvider {


    /**
     * Convert between currency*
     *
     */
    BigDecimal convert(String from, String to, BigDecimal value);

    /**
     * Get rate
     */
    BigDecimal getRate(String from, String to);

}
