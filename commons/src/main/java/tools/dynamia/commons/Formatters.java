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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;


/**
 * Utility class providing basic formatters for numbers, dates, times, and currencies.
 * <p>
 * All methods are static and use the default locale from {@link Messages#getDefaultLocale()}.
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public class Formatters {

    /**
     * Formats a number as an integer string using the default locale.
     *
     * @param number the number to format
     * @return the formatted integer string
     */
    public static String formatInteger(Number number) {
        return NumberFormat.getIntegerInstance(Messages.getDefaultLocale()).format(number);
    }

    /**
     * Formats a number as a decimal string using the default locale.
     *
     * @param number the number to format
     * @return the formatted decimal string
     */
    public static String formatDecimal(Number number) {
        return DecimalFormat.getInstance(Messages.getDefaultLocale()).format(number);
    }

    /**
     * Formats a number as a currency string using the default locale.
     *
     * @param number the number to format
     * @return the formatted currency string
     */
    public static String formatCurrency(Number number) {
        return DecimalFormat.getCurrencyInstance(Messages.getDefaultLocale()).format(number);
    }

    /**
     * Formats a number as a currency string without decimals using the default locale.
     *
     * @param number the number to format
     * @return the formatted currency string without decimals
     */
    public static String formatCurrencySimple(Number number) {
        var locale = Messages.getDefaultLocale();
        var formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(number);
    }

    /**
     * Formats a number as a percent string using the default locale.
     *
     * @param number the number to format
     * @return the formatted percent string
     */
    public static String formatPercent(Number number) {
        return DecimalFormat.getPercentInstance(Messages.getDefaultLocale()).format(number);
    }

    /**
     * Formats a {@link Date} object as a date string using the default locale.
     *
     * @param date the date to format
     * @return the formatted date string
     */
    public static String formatDate(Date date) {
        return DateFormat.getDateInstance(DateFormat.DEFAULT, Messages.getDefaultLocale()).format(date);
    }

    /**
     * Formats a {@link LocalDate} object as an ISO date string.
     *
     * @param date the local date to format
     * @return the formatted ISO date string
     */
    public static String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Formats a {@link LocalTime} object as an ISO time string.
     *
     * @param localTime the local time to format
     * @return the formatted ISO time string
     */
    public static String formatTime(LocalTime localTime) {
        return localTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    /**
     * Formats a {@link LocalDateTime} object as an ISO date-time string.
     *
     * @param localDateTime the local date-time to format
     * @return the formatted ISO date-time string
     */
    public static String formatDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Formats a {@link LocalDate} object using a custom pattern.
     *
     * @param date    the local date to format
     * @param pattern the pattern to use for formatting
     * @return the formatted date string
     */
    public static String formatDate(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern)
                .withLocale(Messages.getDefaultLocale())
                .withZone(Messages.getDefaultTimeZone()));
    }

    /**
     * Formats a {@link LocalTime} object using a custom pattern.
     *
     * @param localTime the local time to format
     * @param pattern   the pattern to use for formatting
     * @return the formatted time string
     */
    public static String formatTime(LocalTime localTime, String pattern) {
        return localTime.format(DateTimeFormatter.ofPattern(pattern)
                .withLocale(Messages.getDefaultLocale())
                .withZone(Messages.getDefaultTimeZone()));
    }

    /**
     * Formats a {@link LocalDateTime} object using a custom pattern.
     *
     * @param localDateTime the local date-time to format
     * @param pattern       the pattern to use for formatting
     * @return the formatted date-time string
     */
    public static String formatDateTime(LocalDateTime localDateTime, String pattern) {
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern)
                .withLocale(Messages.getDefaultLocale())
                .withZone(Messages.getDefaultTimeZone()));
    }

    /**
     * Formats a {@link TemporalAccessor} object using a custom pattern.
     *
     * @param temporal the temporal accessor to format
     * @param pattern  the pattern to use for formatting
     * @return the formatted string
     */
    public static String formatTemporal(TemporalAccessor temporal, String pattern) {
        return DateTimeFormatter.ofPattern(pattern)
                .withLocale(Messages.getDefaultLocale())
                .withZone(Messages.getDefaultTimeZone())
                .format(temporal);
    }

    /**
     * Formats a {@link Date} object using a custom pattern and the default locale.
     *
     * @param date    the date to format
     * @param pattern the pattern to use for formatting
     * @return the formatted date string
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null || pattern == null) {
            return "";
        }

        var instant = DateTimeUtils.toInstant(date);
        if (instant != null) {
            return formatTemporal(instant.atZone(Messages.getDefaultTimeZone()), pattern);
        } else {
            return "";
        }

    }

    /**
     * Private constructor to prevent instantiation.
     */
    private Formatters() {
    }
}
