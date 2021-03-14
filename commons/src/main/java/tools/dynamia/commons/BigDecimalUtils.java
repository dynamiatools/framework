/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.commons;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;


/**
 * The Class BigDecimalUtils.
 *
 * @author Mario A. Serrano Leones
 */
public class BigDecimalUtils {

    /**
     * Sum.
     *
     * @param field the field
     * @param data  the data
     * @return the big decimal
     */
    @SuppressWarnings("rawtypes")
    public static BigDecimal sum(String field, List data) {
        BigDecimal total = BigDecimal.ZERO;

        try {
            if (data != null && !data.isEmpty()) {
                for (Object object : data) {
                    Object value = BeanUtils.invokeGetMethod(object, field);
                    if (value instanceof Number) {
                        if (value instanceof BigDecimal) {
                            total = total.add((BigDecimal) value);
                        } else {
                            total = total.add(BigDecimal.valueOf(((Number) value).doubleValue()));
                        }
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return total;
    }

    /**
     * a is greater than b. (a > b) Null values are treat like BigDecimal.ZERO
     *
     * @param a the a
     * @param b the b
     * @return true, if successful
     */
    public static boolean gt(BigDecimal a, BigDecimal b) {
        a = safe(a);
        b = safe(b);
        return a.compareTo(b) > 0;
    }

    /**
     * a is greater or equals than b. (a >= b) Null values are treat like
     * BigDecimal.ZERO
     *
     * @param a the a
     * @param b the b
     * @return true, if successful
     */
    public static boolean gte(BigDecimal a, BigDecimal b) {
        a = safe(a);
        b = safe(b);
        return a.compareTo(b) >= 0;
    }

    /**
     * a is less than b. (a < b) Null values are treat like BigDecimal.ZERO
     *
     * @param a the a
     * @param b the b
     * @return true, if successful
     */
    public static boolean lt(BigDecimal a, BigDecimal b) {
        a = safe(a);
        b = safe(b);
        return a.compareTo(b) < 0;
    }

    /**
     * a is less or equal than b. (a <= b) Null values are treat like
     * BigDecimal.ZERO
     *
     * @param a the a
     * @param b the b
     * @return true, if successful
     */
    public static boolean lte(BigDecimal a, BigDecimal b) {
        a = safe(a);
        b = safe(b);
        return a.compareTo(b) <= 0;
    }

    /**
     * Checks if is negative.
     *
     * @param a the a
     * @return true, if is negative
     */
    public static boolean isNegative(BigDecimal a) {
        return lt(a, BigDecimal.ZERO);
    }

    /**
     * Checks if is positive.
     *
     * @param a the a
     * @return true, if is positive
     */
    public static boolean isPositive(BigDecimal a) {

        return gt(a, BigDecimal.ZERO);
    }

    /**
     * Retorn BigDecimal.ZERO if value is null otherwise return value
     *
     * @param value the value
     * @return the big decimal
     */
    public static BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * Evaluate a math expression writen in javascript
     *
     * <pre>
     * <code>
     *  //Example:
     *  evaluate(" (a + b * 2) / c",
     *       MapBuilder.put( "a", new BigDecimail(105.1))
     *                 .put( "b", new BigDecimail(3.1415))
     *                 .put( "c", 65));
     * </code>
     * </pre>
     *
     * @param mathExpression the math expression
     * @param vars           the vars
     * @return the big decimal
     * @throws ScriptException the script exception
     */
    public static BigDecimal evaluate(String mathExpression, Map<String, Number> vars) throws ScriptException {

        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine script = mgr.getEngineByName("JavaScript");
        for (Map.Entry<String, Number> entry : vars.entrySet()) {
            script.put(entry.getKey(), entry.getValue());
        }

        Number result = (Number) script.eval(mathExpression);

        return new BigDecimal(result.toString());
    }

    /**
     * Compute the percent and add to value, percent value should be in percent
     * form. Example 10 for 10%, NOT 0.1
     *
     * @param value
     * @param percent
     * @return
     */
    public static BigDecimal addPercent(BigDecimal value, double percent) {
        return value.add(computePercent(value, percent, false));
    }

    /**
     * Compute the percent, percent value should be in percent form. Example 10
     * for 10%, NOT 0.1. Included equals true means that percent value should be
     * substracted from value. Include = true: value - (value / (1 + percent /
     * 100) ) Include = false value * (p / 100)
     *
     * @param value
     * @param percent
     * @param included
     * @return
     */
    public static BigDecimal computePercent(BigDecimal value, double percent, boolean included) {
        BigDecimal p100 = BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100), MathContext.DECIMAL64);
        if (included) {
            BigDecimal onePointPercent = p100.add(BigDecimal.ONE);
            BigDecimal base = value.divide(onePointPercent, MathContext.DECIMAL64);
            return value.subtract(base);
        } else {
            return value.multiply(p100, MathContext.DECIMAL64);
        }
    }

    /**
     * Financial function that returns the interest rate per period of an annuity. You can use RATE to calculate
     * the periodic interest rate, then multiply as required to derive the annual interest rate.
     * The RATE function calculates by iteration.
     *
     * @param numberPeriods
     * @param paymentByPeriod
     * @param total
     * @return
     */
    public static double rate(long numberPeriods, BigDecimal paymentByPeriod, BigDecimal total) {
        return rate((double) numberPeriods, paymentByPeriod.doubleValue(), total.doubleValue());
    }

    /**
     * Financial function that returns the interest rate per period of an annuity. You can use RATE to calculate
     * the periodic interest rate, then multiply as required to derive the annual interest rate.
     * The RATE function calculates by iteration.
     *
     * @param numberPeriods
     * @param paymentByPeriod
     * @param totalValue
     * @return
     */
    public static double rate(double numberPeriods, double paymentByPeriod, double totalValue) {

        double error = 0.0000001;
        double high = 1.00;
        double low = 0.00;

        double rate = (2.0 * (numberPeriods * paymentByPeriod - totalValue)) / (totalValue * numberPeriods);

        while (true) {
            // Check for error margin
            double calc = Math.pow(1 + rate, numberPeriods);
            calc = (rate * calc) / (calc - 1.0);
            calc -= paymentByPeriod / totalValue;

            if (calc > error) {
                // Guess is too high, lower the guess
                high = rate;
                rate = (high + low) / 2;
            } else if (calc < -error) {
                // Guess is too low, higher the guess.
                low = rate;
                rate = (high + low) / 2;
            } else {
                // Acceptable guess
                break;
            }
        }
        return rate;
    }

    private BigDecimalUtils() {
    }
}
