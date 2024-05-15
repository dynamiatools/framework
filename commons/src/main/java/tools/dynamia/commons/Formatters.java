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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;


/**
 * Basic formatters
 *
 * @author Mario A. Serrano Leones
 */
public class Formatters {


    /**
     * Format integer.
     *
     * @param number the number
     * @return the string
     */
    public static String formatInteger(Number number) {
        return NumberFormat.getIntegerInstance(Messages.getDefaultLocale()).format(number);
    }

    /**
     * Format.
     *
     * @param number the number
     * @return the string
     */
    public static String formatDecimal(Number number) {
        return DecimalFormat.getInstance(Messages.getDefaultLocale()).format(number);
    }

    /**
     * Format currency.
     *
     * @param number the number
     * @return the string
     */
    public static String formatCurrency(Number number) {
        return DecimalFormat.getCurrencyInstance(Messages.getDefaultLocale()).format(number);
    }

    /**
     * Format currency without decimals.
     *
     * @param number the number
     * @return the string
     */
    public static String formatCurrencySimple(Number number) {
        var locale = Messages.getDefaultLocale();
        var formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(number);
    }

    /**
     * Format a percent
     *
     * @param number
     * @return
     */
    public static String formatPercent(Number number) {
        return DecimalFormat.getPercentInstance(Messages.getDefaultLocale()).format(number);
    }

    /**
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        return DateFormat.getDateInstance(DateFormat.DEFAULT, Messages.getDefaultLocale()).format(date);
    }

    /**
     * @param date
     * @return
     */
    public static String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_DATE);
    }

    /**
     * @param date
     * @return
     */
    public static String formatTime(LocalTime date) {
        return date.format(DateTimeFormatter.ISO_DATE);
    }

    private Formatters() {
    }
}
