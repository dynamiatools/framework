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

import java.text.NumberFormat;


/**
 * The Class Formatters.
 *
 * @author Mario A. Serrano Leones
 */
public class Formatters {

    /**
     * The integer.
     */
    private static final NumberFormat integer = NumberFormat.getIntegerInstance();

    /**
     * The decimal.
     */
    private static final NumberFormat decimal = NumberFormat.getNumberInstance();

    /**
     * The currency.
     */
    private static final NumberFormat currency = NumberFormat.getCurrencyInstance();

    /**
     * Format integer.
     *
     * @param number the number
     * @return the string
     */
    public static String formatInteger(Number number) {
        return integer.format(number);
    }

    /**
     * Format.
     *
     * @param number the number
     * @return the string
     */
    public static String format(Number number) {
        return decimal.format(number);
    }

    /**
     * Format currency.
     *
     * @param number the number
     * @return the string
     */
    public static String formatCurrency(Number number) {
        return currency.format(number);
    }

    private Formatters() {
    }
}
