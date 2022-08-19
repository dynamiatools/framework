package tools.dynamia.domain;

import java.math.BigDecimal;

/***
 * Basic API for currency exchange provider. *
 */
public interface CurrencyExchangeProvider {


    /**
     * Convert between currency*
     *
     * @param from
     * @param to
     * @param value
     * @return
     */
    BigDecimal convert(String from, String to, BigDecimal value);

    /**
     * Get rate
     * @param from
     * @param to
     * @return
     */
    BigDecimal getRate(String from, String to);

}
