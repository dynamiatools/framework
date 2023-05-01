package tools.dynamia.domain;

import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.commons.SimpleCache;
import tools.dynamia.commons.StringPojoParser;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

/**
 * This implementation use <a href="https://openexchangerates.org/">...</a> API. Free API key only support USD as base (from) currency
 * Usage subject to terms: <a href="https://openexchangerates.org/terms">...</a>
 * <a href="https://openexchangerates.org/license">...</a>".
 * <p>
 * Its use internal cache to call API just one by day
 */
public class OpenExchangeRatesCurrencyProvider implements CurrencyExchangeProvider {

    private static final SimpleCache<String, BigDecimal> CACHE_RATES = new SimpleCache<>();

    private final String appID;
    private final String endpoint;
    private final boolean freePlan;

    /**
     * When using free plan convertions betweeen currency will be based on USD
     *
     * @param appID    api key
     * @param freePlan true if using free plan
     */
    public OpenExchangeRatesCurrencyProvider(String appID, boolean freePlan) {
        this.appID = appID;
        this.freePlan = freePlan;
        this.endpoint = "https://openexchangerates.org/api/latest.json?app_id=" + appID;
    }

    @Override
    public BigDecimal convert(String from, String to, BigDecimal value) {
        var rate = getRate(from, to);
        return value.multiply(rate,MathContext.DECIMAL64);
    }

    @Override
    public BigDecimal getRate(String from, String to) {
        var cacheKey = buildCacheKey(from, to);
        var rate = CACHE_RATES.get(cacheKey);

        if (rate == null) {
            try {
                var base = freePlan ? "USD" : from;
                var uri = endpoint + "&base=" + base;
                var client = HttpClient.newHttpClient();
                var req = HttpRequest.newBuilder()
                        .uri(new URI(uri))
                        .timeout(Duration.ofSeconds(30))
                        .GET()
                        .build();

                var response = client.send(req, HttpResponse.BodyHandlers.ofString());
                var json = StringPojoParser.parseJsonToMap(response.body());

                if (response.statusCode() == 200) {
                    rate = getRateFromResponse(from, to, json);
                    CACHE_RATES.add(cacheKey, rate);
                } else {
                    throw new ValidationError(json.get("description").toString());
                }

            } catch (Exception e) {
                throw new RuntimeException("Error invocking API. ", e);
            }
        }


        return rate;
    }

    protected BigDecimal getRateFromResponse(String from, String to, Map<String, Object> json) {
        var rates = (Map) json.get("rates");

        if (freePlan) {
            var toRate = BigDecimal.valueOf(Double.parseDouble(rates.get(to.toUpperCase()).toString()));
            if (from.equalsIgnoreCase("USD")) {
                return toRate;
            } else {
                var fromRate = BigDecimal.valueOf(Double.parseDouble(rates.get(from.toUpperCase()).toString()));
                return toRate.divide(fromRate, MathContext.DECIMAL64);
            }
        } else {
            return BigDecimal.valueOf(Double.parseDouble(rates.get(to.toUpperCase()).toString()));
        }
    }

    public boolean isFreePlan() {
        return freePlan;
    }

    public static void clearCache() {
        CACHE_RATES.clear();
    }

    public static SimpleCache<String, BigDecimal> getCacheRates() {
        return CACHE_RATES;
    }

    protected String buildCacheKey(String from, String to) {
        return DateTimeUtils.format(new Date(), "yyyyMMdd") + "_" + from + "-" + to;
    }


}
