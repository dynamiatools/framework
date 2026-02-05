/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.math.MathFunction;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;


/**
 * Utility class that provides helper methods for BigDecimal operations including
 * comparison, calculation, percentage computation, and financial functions.
 * This class simplifies common BigDecimal operations with null-safe methods
 * and convenient comparison utilities.
 *
 * <p>All comparison methods treat null values as BigDecimal.ZERO for safe operations.
 * The class also includes financial calculation functions like interest rate computation.</p>
 *
 * @author Mario A. Serrano Leones
 */
public class BigDecimalUtils {


    private BigDecimalUtils() {
    }

    /**
     * Computes the sum of a numeric field across all objects in the provided list.
     * The method uses reflection to invoke the getter method for the specified field
     * and accumulates the numeric values. Non-numeric values are ignored.
     *
     * @param field the name of the field to sum (must have a corresponding getter method)
     * @param data  the list of objects containing the field to sum
     * @return the total sum as BigDecimal, or BigDecimal.ZERO if the list is empty or null
     *
     * <pre>{@code
     * // Example:
     * List<Product> products = Arrays.asList(
     *     new Product("Item1", new BigDecimal("10.50")),
     *     new Product("Item2", new BigDecimal("25.75"))
     * );
     * BigDecimal total = BigDecimalUtils.sum("price", products);
     * // Returns: 36.25
     * }</pre>
     */
    @SuppressWarnings("rawtypes")
    public static BigDecimal sum(String field, List data) {
        BigDecimal total = BigDecimal.ZERO;

        try {
            if (data != null && !data.isEmpty()) {
                for (Object object : data) {
                    Object value = ObjectOperations.invokeGetMethod(object, field);
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
            LoggingService.get(BigDecimalUtils.class).error(exception.getMessage(), exception);
        }
        return total;
    }

    /**
     * Checks if the first BigDecimal is greater than the second (a &gt; b).
     * Null values are treated as BigDecimal.ZERO for safe comparison.
     *
     * @param a the first value to compare
     * @param b the second value to compare
     * @return true if a is greater than b, false otherwise
     *
     * <pre>{@code
     * // Example:
     * BigDecimal price = new BigDecimal("150.00");
     * BigDecimal limit = new BigDecimal("100.00");
     * boolean overLimit = BigDecimalUtils.gt(price, limit); // Returns: true
     * }</pre>
     */
    public static boolean gt(BigDecimal a, BigDecimal b) {
        a = safe(a);
        b = safe(b);
        return a.compareTo(b) > 0;
    }

    /**
     * Checks if the first BigDecimal is greater than or equal to the second (a &gt;= b).
     * Null values are treated as BigDecimal.ZERO for safe comparison.
     *
     * @param a the first value to compare
     * @param b the second value to compare
     * @return true if a is greater than or equal to b, false otherwise
     *
     * <pre>{@code
     * // Example:
     * BigDecimal balance = new BigDecimal("100.00");
     * BigDecimal minRequired = new BigDecimal("100.00");
     * boolean hasEnough = BigDecimalUtils.gte(balance, minRequired); // Returns: true
     * }</pre>
     */
    public static boolean gte(BigDecimal a, BigDecimal b) {
        a = safe(a);
        b = safe(b);
        return a.compareTo(b) >= 0;
    }

    /**
     * Checks if the first BigDecimal is less than the second (a &lt; b).
     * Null values are treated as BigDecimal.ZERO for safe comparison.
     *
     * @param a the first value to compare
     * @param b the second value to compare
     * @return true if a is less than b, false otherwise
     *
     * <pre>{@code
     * // Example:
     * BigDecimal stock = new BigDecimal("5.00");
     * BigDecimal minStock = new BigDecimal("10.00");
     * boolean needsReorder = BigDecimalUtils.lt(stock, minStock); // Returns: true
     * }</pre>
     */
    public static boolean lt(BigDecimal a, BigDecimal b) {
        a = safe(a);
        b = safe(b);
        return a.compareTo(b) < 0;
    }

    /**
     * Checks if the first BigDecimal is less than or equal to the second (a &lt;= b).
     * Null values are treated as BigDecimal.ZERO for safe comparison.
     *
     * @param a the first value to compare
     * @param b the second value to compare
     * @return true if a is less than or equal to b, false otherwise
     *
     * <pre>{@code
     * // Example:
     * BigDecimal discount = new BigDecimal("15.00");
     * BigDecimal maxDiscount = new BigDecimal("20.00");
     * boolean isValid = BigDecimalUtils.lte(discount, maxDiscount); // Returns: true
     * }</pre>
     */
    public static boolean lte(BigDecimal a, BigDecimal b) {
        a = safe(a);
        b = safe(b);
        return a.compareTo(b) <= 0;
    }

    /**
     * Checks if the given BigDecimal value is negative (less than zero).
     * Null values are treated as BigDecimal.ZERO and will return false.
     *
     * @param value the value to check
     * @return true if the value is negative, false otherwise
     *
     * <pre>{@code
     * // Example:
     * BigDecimal loss = new BigDecimal("-50.00");
     * boolean hasLoss = BigDecimalUtils.isNegative(loss); // Returns: true
     * }</pre>
     */
    public static boolean isNegative(BigDecimal value) {
        return lt(value, BigDecimal.ZERO);
    }

    /**
     * Checks if the given BigDecimal value is positive (greater than zero).
     * Null values are treated as BigDecimal.ZERO and will return false.
     *
     * @param value the value to check
     * @return true if the value is positive, false otherwise
     *
     * <pre>{@code
     * // Example:
     * BigDecimal profit = new BigDecimal("100.00");
     * boolean hasProfit = BigDecimalUtils.isPositive(profit); // Returns: true
     * }</pre>
     */
    public static boolean isPositive(BigDecimal value) {

        return gt(value, BigDecimal.ZERO);
    }

    /**
     * Checks if the given BigDecimal value is exactly zero.
     * This method performs an exact comparison with BigDecimal.ZERO.
     *
     * @param value the value to check
     * @return true if the value is exactly zero, false otherwise (including null)
     *
     * <pre>{@code
     * // Example:
     * BigDecimal amount = new BigDecimal("0.00");
     * boolean isEmpty = BigDecimalUtils.isZero(amount); // Returns: true
     * }</pre>
     */
    public static boolean isZero(BigDecimal value) {
        return BigDecimal.ZERO.equals(value);
    }

    /**
     * Returns a safe BigDecimal value, converting null to BigDecimal.ZERO.
     * This method is useful to avoid NullPointerException when performing operations.
     *
     * @param value the value to make safe (can be null)
     * @return BigDecimal.ZERO if value is null, otherwise returns the original value
     *
     * <pre>{@code
     * // Example:
     * BigDecimal nullValue = null;
     * BigDecimal safeValue = BigDecimalUtils.safe(nullValue); // Returns: 0
     *
     * BigDecimal amount = new BigDecimal("50.00");
     * BigDecimal result = BigDecimalUtils.safe(amount); // Returns: 50.00
     * }</pre>
     */
    public static BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * Evaluates a mathematical expression written in JavaScript syntax.
     * The method replaces variables in the expression with their numeric values
     * and calculates the result using the MathFunction evaluator.
     *
     * @param mathExpression the mathematical expression to evaluate (e.g., "(a + b * 2) / c")
     * @param vars           a map of variable names to their numeric values
     * @return the evaluated result as BigDecimal
     *
     * <pre>{@code
     * // Example:
     * Map<String, Number> variables = new HashMap<>();
     * variables.put("a", new BigDecimal("105.1"));
     * variables.put("b", new BigDecimal("3.1415"));
     * variables.put("c", 65);
     *
     * BigDecimal result = BigDecimalUtils.evaluate("(a + b * 2) / c", variables);
     * // Returns: approximately 1.71355...
     * }</pre>
     */
    public static BigDecimal evaluate(String mathExpression, Map<String, Number> vars) {

        String parsedExpression = mathExpression.trim();
        for (Map.Entry<String, Number> var : vars.entrySet()) {
            parsedExpression = parsedExpression.replace(var.getKey(), String.valueOf(var.getValue()));
        }

        return BigDecimal.valueOf(MathFunction.evaluate(parsedExpression));
    }

    /**
     * Computes the percentage of a value and adds it to the original value.
     * The percent parameter should be in percentage form (e.g., 10 for 10%, not 0.1).
     * This is equivalent to calculating: value + (value * percent / 100)
     *
     * @param value   the base value to which the percentage will be added
     * @param percent the percentage to add (e.g., 10 for 10%, 15.5 for 15.5%)
     * @return the original value plus the computed percentage
     *
     * <pre>{@code
     * // Example:
     * BigDecimal price = new BigDecimal("100.00");
     * BigDecimal priceWithTax = BigDecimalUtils.addPercent(price, 19); // 19% tax
     * // Returns: 119.00 (100 + 19% of 100)
     * }</pre>
     */
    public static BigDecimal addPercent(BigDecimal value, double percent) {
        return value.add(computePercent(value, percent, false));
    }

    /**
     * Computes a percentage of the given value. The percent parameter should be in percentage form
     * (e.g., 10 for 10%, not 0.1).
     *
     * <p>The calculation method depends on the <code>included</code> parameter:</p>
     * <ul>
     *   <li><b>included = false:</b> Calculates the percentage directly: value * (percent / 100)</li>
     *   <li><b>included = true:</b> Extracts the included percentage from the value: value - (value / (1 + percent / 100))</li>
     * </ul>
     *
     * @param value    the base value from which to compute the percentage
     * @param percent  the percentage value (e.g., 10 for 10%, 19 for 19%)
     * @param included if true, calculates the percentage that is already included in the value;
     *                 if false, calculates a simple percentage of the value
     * @return the computed percentage as BigDecimal
     *
     * <pre>{@code
     * // Example 1: Calculate 19% of 100 (not included)
     * BigDecimal tax = BigDecimalUtils.computePercent(new BigDecimal("100"), 19, false);
     * // Returns: 19.00
     *
     * // Example 2: Extract the 19% tax from a price that already includes it
     * BigDecimal priceWithTax = new BigDecimal("119.00");
     * BigDecimal taxAmount = BigDecimalUtils.computePercent(priceWithTax, 19, true);
     * // Returns: 19.00 (the tax portion included in 119.00)
     * }</pre>
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
     * Financial function that returns the interest rate per period of an annuity.
     * You can use this method to calculate the periodic interest rate, then multiply
     * as required to derive the annual interest rate. The calculation is performed by iteration.
     *
     * <p>This method is useful for financial calculations such as loan interest rates
     * or investment returns based on regular payments.</p>
     *
     * @param numberPeriods     the total number of payment periods in the annuity
     * @param paymentByPeriod   the payment made each period (cannot change over the life of the annuity)
     * @param total             the present value (total amount of loan or investment)
     * @return the interest rate per period as a double (e.g., 0.01 for 1% per period)
     *
     * <pre>{@code
     * // Example: Calculate monthly interest rate for a loan
     * long months = 36; // 3-year loan
     * BigDecimal monthlyPayment = new BigDecimal("500.00");
     * BigDecimal loanAmount = new BigDecimal("15000.00");
     *
     * double monthlyRate = BigDecimalUtils.rate(months, monthlyPayment, loanAmount);
     * double annualRate = monthlyRate * 12 * 100; // Convert to annual percentage
     * // Returns: approximately 1.73% monthly rate, or 20.76% annual rate
     * }</pre>
     */
    public static double rate(long numberPeriods, BigDecimal paymentByPeriod, BigDecimal total) {
        return rate((double) numberPeriods, paymentByPeriod.doubleValue(), total.doubleValue());
    }

    /**
     * Financial function that returns the interest rate per period of an annuity using double precision.
     * This is the core implementation that calculates the rate through iterative approximation
     * using the bisection method.
     *
     * <p>The algorithm starts with an initial guess and refines it by checking if the calculated
     * payment is higher or lower than the actual payment, adjusting the rate boundaries accordingly
     * until convergence within an acceptable error margin (0.0000001).</p>
     *
     * @param numberPeriods     the total number of payment periods in the annuity
     * @param paymentByPeriod   the payment made each period (constant throughout the annuity)
     * @param totalValue        the present value (total amount of loan or investment)
     * @return the interest rate per period as a double (e.g., 0.01 for 1% per period)
     *
     * <pre>{@code
     * // Example: Calculate monthly interest rate for a car loan
     * double periods = 48.0; // 4-year loan
     * double payment = 350.0; // Monthly payment
     * double principal = 15000.0; // Loan amount
     *
     * double rate = BigDecimalUtils.rate(periods, payment, principal);
     * // Returns: approximately 0.0067 (0.67% monthly, or ~8% annual)
     * }</pre>
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

}
