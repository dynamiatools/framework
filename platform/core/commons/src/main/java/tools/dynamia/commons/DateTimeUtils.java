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

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility class providing a wide range of static methods for date and time manipulation.
 * <p>
 * Leverages Java 8 Date and Time API (LocalDate, LocalDateTime, Instant, ZoneId, etc.) and legacy {@link java.util.Date}.
 * Includes methods for conversion, arithmetic, formatting, parsing, and comparison of dates and times.
 * <p>
 * All methods are stateless and thread-safe. This class cannot be instantiated.
 *
 * @author Mario A. Serrano Leones
 */
public final class DateTimeUtils {

    /**
     * Constant for short date format.
     */
    public static final int SHORT = DateFormat.SHORT;

    /**
     * Constant for medium date format.
     */
    public static final int MEDIUM = DateFormat.MEDIUM;

    /**
     * Constant for long date format.
     */
    public static final int LONG = DateFormat.LONG;

    /**
     * Returns the last day of the specified month as an integer value.
     *
     * @param month the month as an integer (1-12)
     * @return the last day of the month (28-31)
     */
    public static int getLastDayOfMonth(int month) {
        return getLastDayOfMonth(Month.of(month));
    }

    /**
     * Returns the last day of the specified {@link Month}.
     *
     * @param month the {@link Month} instance
     * @return the last day of the month (28-31)
     */
    public static int getLastDayOfMonth(Month month) {
        return month.length(LocalDate.now().isLeapYear());
    }

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private DateTimeUtils() {
    }

    /**
     * Determines if the given {@link Date} is in the future compared to the current system date.
     *
     * @param date the date to check
     * @return true if the date is in the future, false otherwise
     */
    public static boolean isFuture(Date date) {
        return date != null && date.after(new Date());
    }

    /**
     * Determines if the given {@link TemporalAccessor} (e.g., LocalDate, LocalDateTime) is in the future compared to the current system date.
     *
     * @param date the temporal object to check
     * @return true if the temporal is in the future, false otherwise
     */
    public static boolean isFuture(TemporalAccessor date) {
        if (date == null) {
            return false;
        }
        ChronoLocalDate chronoDate = ChronoLocalDate.from(date);
        return chronoDate.isAfter(LocalDate.now());
    }

    /**
     * Determines if the given {@link Date} is in the past compared to the current system date.
     *
     * @param date the date to check
     * @return true if the date is in the past, false otherwise
     */
    public static boolean isPast(Date date) {
        return date != null && date.before(new Date());
    }

    /**
     * Determines if the given {@link TemporalAccessor} (e.g., LocalDate, LocalDateTime) is in the past compared to the current system date.
     *
     * @param temporal the temporal object to check
     * @return true if the temporal is in the past, false otherwise
     */
    public static boolean isPast(TemporalAccessor temporal) {
        if (temporal == null) {
            return false;
        }
        ChronoLocalDate date = ChronoLocalDate.from(temporal);
        return date.isBefore(LocalDate.now());
    }

    /**
     * Checks if the given {@link Date} is today, comparing year, month, and day.
     *
     * @param date the date to check
     * @return true if the date is today, false otherwise
     */
    public static boolean isToday(Date date) {
        if (date == null) {
            return false;
        }
        Date today = new Date();
        return getYear(today) == getYear(date) && getMonth(today) == getMonth(date) && getDay(today) == getDay(date);
    }

    /**
     * Checks if the given {@link TemporalAccessor} (e.g., LocalDate, LocalDateTime) is today, comparing year, month, and day.
     *
     * @param temporal the temporal object to check
     * @return true if the temporal is today, false otherwise
     */
    public static boolean isToday(TemporalAccessor temporal) {
        if (temporal == null) {
            return false;
        }
        LocalDate date = LocalDate.from(temporal);
        LocalDate today = LocalDate.now();
        return date.isEqual(today);
    }

    /**
     * Returns the current year as an integer.
     *
     * @return the current year
     */
    public static int getCurrentYear() {
        return getYear(new Date());
    }

    /**
     * Returns the current month as an integer (1-12).
     *
     * @return the current month
     */
    public static int getCurrentMonth() {
        return getMonth(new Date());
    }

    /**
     * Returns the name of the current month in the default locale.
     *
     * @return the current month name
     */
    public static String getCurrentMonthName() {
        DateFormat df = new SimpleDateFormat("MMMM");
        return df.format(new Date());
    }

    /**
     * Returns the current day of the month as an integer.
     *
     * @return the current day of the month
     */
    public static int getCurrentDay() {
        return getDay(new Date());
    }

    /**
     * Calculates the number of milliseconds between two {@link Date} instances.
     *
     * @param date1 the start date
     * @param date2 the end date
     * @return the number of milliseconds between date1 and date2
     */
    public static long millisBetween(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        LocalDate localDate1 = toLocalDate(date1);
        LocalDate localDate2 = toLocalDate(date2);
        return localDate1.until(localDate2, ChronoUnit.MILLIS);
    }

    /**
     * Calculates the number of years between two {@link Date} instances.
     *
     * @param date1 the start date
     * @param date2 the end date
     * @return the number of years between date1 and date2
     */
    public static long yearsBetween(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        LocalDate localDate1 = toLocalDate(date1);
        LocalDate localDate2 = toLocalDate(date2);
        return localDate1.until(localDate2, ChronoUnit.YEARS);
    }

    /**
     * Calculates the number of months between two {@link Date} instances.
     *
     * @param date1 the start date
     * @param date2 the end date
     * @return the number of months between date1 and date2
     */
    public static long monthsBetween(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        LocalDate localDate1 = toLocalDate(date1);
        LocalDate localDate2 = toLocalDate(date2);
        return localDate1.until(localDate2, ChronoUnit.MONTHS);
    }

    public static long monthsBetween(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        return date1.until(date2, ChronoUnit.MONTHS);
    }

    /**
     * Calculates the number of days between two {@link Date} instances.
     *
     * @param date1 the start date
     * @param date2 the end date
     * @return the number of days between date1 and date2
     */
    public static long daysBetween(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        return toInstant(date1).until(toInstant(date2), ChronoUnit.DAYS);
    }

    /**
     * Calculates the number of days between two {@link LocalDate} instances.
     *
     * @param date1 the start date
     * @param date2 the end date
     * @return the number of days between date1 and date2
     */
    public static long daysBetween(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        return date1.until(date2, ChronoUnit.DAYS);
    }

    /**
     * Calculates the number of days between two {@link LocalDateTime} instances.
     *
     * @param date1 the start date
     * @param date2 the end date
     * @return the number of days between date1 and date2
     */
    public static long daysBetween(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        return date1.until(date2, ChronoUnit.DAYS);
    }

    /**
     * Calculates the number of hours between two {@link Date} instances.
     *
     * @param date1 the start date
     * @param date2 the end date
     * @return the number of hours between date1 and date2
     */
    public static long hoursBetween(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        return toInstant(date1).until(toInstant(date2), ChronoUnit.HOURS);
    }

    public static long hoursBetween(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        return date1.until(date2, ChronoUnit.HOURS);
    }

    /**
     * Calculates the number of minutes between two {@link Date} instances.
     *
     * @param date1 the start date
     * @param date2 the end date
     * @return the number of minutes between date1 and date2
     */
    public static long minutesBetween(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        return toInstant(date1).until(toInstant(date2), ChronoUnit.MINUTES);
    }
    public static long minutesBetween(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        return date1.until(date2, ChronoUnit.MINUTES);
    }

    /**
     * Calculates the number of seconds between two {@link Date} instances.
     *
     * @param date1 the start date
     * @param date2 the end date
     * @return the number of seconds between date1 and date2
     */
    public static long secondsBetween(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        return toInstant(date1).until(toInstant(date2), ChronoUnit.SECONDS);
    }

    public static  long secondsBetween(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        return date1.until(date2, ChronoUnit.SECONDS);
    }

    /**
     * Creates a {@link Date} from a given timestamp in milliseconds.
     *
     * @param timestamp the timestamp in milliseconds since epoch
     * @return a {@link Date} representing the given timestamp
     */
    public static Date createDate(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar.getTime();
    }

    /**
     * Creates a {@link Date} for the current month and specified day.
     *
     * @param dayOfMonth the day of the month (1-31)
     * @return a {@link Date} for the specified day in the current month
     */
    public static Date createDate(int dayOfMonth) {
        return toDate(LocalDate.now().withDayOfMonth(dayOfMonth));
    }

    /**
     * Creates a {@link LocalDate} for the current month and specified day.
     *
     * @param dayOfMonth the day of the month (1-31)
     * @return a {@link LocalDate} for the specified day in the current month
     */
    public static LocalDate createLocalDate(int dayOfMonth) {
        return LocalDate.now().withDayOfMonth(dayOfMonth);
    }

    /**
     * Creates a {@link Date} for the specified year, month, and day.
     *
     * @param year  the year
     * @param month the month (1-12)
     * @param day   the day of the month (1-31)
     * @return a {@link Date} for the specified year, month, and day
     */
    public static Date createDate(int year, int month, int day) {
        return toDate(LocalDate.of(year, month, day));
    }

    /**
     * Creates a {@link LocalDate} for the specified year, month, and day.
     *
     * @param year  the year
     * @param month the month (1-12)
     * @param day   the day of the month (1-31)
     * @return a {@link LocalDate} for the specified year, month, and day
     */
    public static LocalDate createLocalDate(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    /**
     * Creates a {@link Date} for the specified year, month, day, hour, minute, and second.
     *
     * @param year      the year
     * @param month     the month (1-12)
     * @param day       the day of the month (1-31)
     * @param hourOfDay the hour of day (0-23)
     * @param minute    the minute (0-59)
     * @param second    the second (0-59)
     * @return a {@link Date} for the specified date and time
     */
    public static Date createDate(int year, int month, int day, int hourOfDay, int minute, int second) {
        return toDate(LocalDateTime.of(year, month, day, hourOfDay, minute, second));
    }

    /**
     * Creates a {@link Date} for the specified year, month, day, hour, and minute.
     *
     * @param year      the year
     * @param month     the month (1-12)
     * @param day       the day of the month (1-31)
     * @param hourOfDay the hour of day (0-23)
     * @param minute    the minute (0-59)
     * @return a {@link Date} for the specified date and time
     */
    public static Date createDate(int year, int month, int day, int hourOfDay, int minute) {
        return toDate(LocalDateTime.of(year, month, day, hourOfDay, minute));
    }

    /**
     * Parses a {@link Date} from a string using the default date format for the current locale.
     *
     * @param dateText the date string to parse
     * @return the parsed {@link Date}
     * @throws ParseException if the string cannot be parsed
     */
    public static Date createDate(String dateText) throws ParseException {
        DateFormat df = DateFormat.getDateInstance();
        return df.parse(dateText);
    }

    /**
     * Parses a {@link Date} from a string using the specified pattern.
     *
     * @param dateText the date string to parse
     * @param pattern  the date pattern (e.g. "yyyy-MM-dd")
     * @return the parsed {@link Date}
     * @throws ParseException if the string cannot be parsed
     */
    public static Date createDate(String dateText, String pattern) throws ParseException {
        DateFormat df = new SimpleDateFormat(pattern);
        return df.parse(dateText);
    }

    /**
     * Returns a new {@link Date} by adding the specified number of days to the given date.
     *
     * @param date the original date
     * @param days the number of days to add (can be negative)
     * @return a new {@link Date} with the days added
     */
    public static Date addDays(Date date, int days) {
        return toDate(toLocalDateTime(date).plusDays(days));
    }


    /**
     * Returns a new {@link LocalDate} by adding the specified number of days to the given date.
     *
     * @param date original date
     * @param days number of days to add (can be negative)
     * @return a new {@link LocalDate} with the days added
     */
    public static LocalDate addDays(LocalDate date, int days) {
        return date.plusDays(days);
    }

    /**
     * Returns a new {@link Date} by adding the specified number of months to the given date.
     *
     * @param date   the original date
     * @param months the number of months to add (can be negative)
     * @return a new {@link Date} with the months added
     */
    public static Date addMonths(Date date, int months) {
        return toDate(toLocalDateTime(date).plusMonths(months));
    }

    /**
     * Returns a new {@link LocalDate} by adding the specified number of months to the given date.
     *
     * @param date   original date
     * @param months number of months to add (can be negative)
     * @return a new {@link LocalDate} with the months added
     */
    public static LocalDate addMonths(LocalDate date, int months) {
        return date.plusMonths(months);
    }

    /**
     * Returns a new {@link Date} by adding the specified number of years to the given date.
     *
     * @param date  the original date
     * @param years the number of years to add (can be negative)
     * @return a new {@link Date} with the years added
     */
    public static Date addYears(Date date, int years) {
        return toDate(toLocalDateTime(date).plusYears(years));
    }

    /**
     * Returns a new {@link LocalDate} by adding the specified number of years to the given date.
     *
     * @param date  original date
     * @param years number of years to add (can be negative)
     * @return a new {@link LocalDate} with the years added
     */
    public static LocalDate addYears(LocalDate date, int years) {
        return date.plusYears(years);
    }

    /**
     * Returns a new {@link Date} by adding the specified number of weeks to the given date.
     *
     * @param date  the original date
     * @param weeks the number of weeks to add (can be negative)
     * @return a new {@link Date} with the weeks added
     */
    public static Date addWeeks(Date date, int weeks) {
        return toDate(toLocalDateTime(date).plusWeeks(weeks));
    }

    /**
     * Returns a new {@link LocalDate} by adding the specified number of weeks to the given date.
     *
     * @param date  original date
     * @param weeks number of weeks to add (can be negative)
     * @return a new {@link LocalDate} with the weeks added
     */
    public static LocalDate addWeeks(LocalDate date, int weeks) {
        return date.plusWeeks(weeks);
    }

    /**
     * Returns a new {@link Date} by adding the specified number of hours to the given date.
     *
     * @param date  the original date
     * @param hours the number of hours to add (can be negative)
     * @return a new {@link Date} with the hours added
     */
    public static Date addHours(Date date, int hours) {
        if (date == null) {
            return null;
        }
        return toDate(toLocalDateTime(date).plusHours(hours));
    }

    /**
     * Returns a new {@link LocalDateTime} by adding the specified number of hours to the given date.
     *
     * @param date  original date
     * @param hours number of hours to add (can be negative)
     * @return a new {@link LocalDateTime} with the hours added
     */
    public static LocalDateTime addHours(LocalDateTime date, int hours) {
        if (date == null) {
            return null;
        }
        return date.plusHours(hours);
    }

    /**
     * Returns a new {@link Date} by adding the specified number of minutes to the given date.
     *
     * @param date    the original date
     * @param minutes the number of minutes to add (can be negative)
     * @return a new {@link Date} with the minutes added
     */
    public static Date addMinutes(Date date, int minutes) {
        if (date == null) {
            return null;
        }
        return toDate(toLocalDateTime(date).plusMinutes(minutes));
    }

    /**
     * Returns a new {@link LocalDateTime} by adding the specified number of minutes to the given date.
     *
     * @param date    original date
     * @param minutes number of minutes to add (can be negative)
     * @return a new {@link LocalDateTime} with the minutes added
     */
    public static LocalDateTime addMinutes(LocalDateTime date, int minutes) {
        if (date == null) {
            return null;
        }
        return date.plusMinutes(minutes);
    }

    /**
     * Formats a {@link Date} using the specified pattern and default locale.
     *
     * @param date    the date to format
     * @param pattern the date pattern (e.g. "yyyy-MM-dd")
     * @return the formatted date string
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        DateFormat df = new SimpleDateFormat(pattern, Messages.getDefaultLocale());
        return df.format(date);
    }

    /**
     * Formats a {@link TemporalAccessor} (such as LocalDate, LocalTime, LocalDateTime) using the specified pattern and default locale.
     *
     * @param temporalAccessor the temporal object to format
     * @param pattern          the date pattern (e.g. "yyyy-MM-dd HH:mm:ss")
     * @return the formatted date/time string
     */
    public static String format(TemporalAccessor temporalAccessor, String pattern) {
        if (temporalAccessor == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Messages.getDefaultLocale());
        return formatter.format(temporalAccessor);
    }

    /**
     * Formats a {@link Date} as a date string using the specified style.
     *
     * @param date  the date to format
     * @param style the formatting style (SHORT, MEDIUM, LONG)
     * @return the formatted date string
     */
    public static String formatDate(Date date, int style) {
        if (date == null) {
            return null;
        }
        DateFormat df = DateFormat.getDateInstance(style);
        return df.format(date);
    }

    /**
     * Formats a {@link Date} as a date string using the medium style.
     *
     * @param date the date to format
     * @return the formatted date string
     */
    public static String formatDate(Date date) {
        return formatDate(date, DateFormat.MEDIUM);
    }

    /**
     * Formats a {@link LocalDate} as a date string in "dd/MM/yyyy" format.
     *
     * @param localDate the LocalDate to format
     * @return the formatted date string
     */
    public static String formatDate(LocalDate localDate) {
        return format(localDate, "dd/MM/yyyy");
    }

    /**
     * Formats a {@link LocalDateTime} as a date string in "dd/MM/yyyy HH:mm:ss" format.
     *
     * @param localDateTime the LocalDateTime to format
     * @return the formatted date string
     */
    public static String formatDate(LocalDateTime localDateTime) {
        return format(localDateTime, "dd/MM/yyyy HH:mm:ss");
    }

    /**
     * Formats a {@link LocalTime} as a time string in "HH:mm:ss" format.
     *
     * @param localTime the LocalTime to format
     * @return the formatted time string
     */
    public static String formatTime(LocalTime localTime) {
        return format(localTime, "HH:mm:ss");
    }

    /**
     * Formats a {@link LocalDateTime} as a time string in "HH:mm:ss" format.
     *
     * @param localDateTime the LocalDateTime to format
     * @return the formatted time string
     */
    public static String formatTime(LocalDateTime localDateTime) {
        return format(localDateTime, "HH:mm:ss");
    }

    /**
     * Formats a {@link Date} as a time string using the specified style.
     *
     * @param date  the date to format
     * @param style the formatting style (SHORT, MEDIUM, LONG)
     * @return the formatted time string
     */
    public static String formatTime(Date date, int style) {
        DateFormat df = DateFormat.getTimeInstance(style);
        return df.format(date);
    }

    /**
     * Formats a {@link Date} as a time string using the medium style.
     *
     * @param date the date to format
     * @return the formatted time string
     */
    public static String formatTime(Date date) {
        if (date == null) {
            return null;
        }
        return formatTime(date, DateFormat.MEDIUM);
    }

    /**
     * Returns the month value (1-12) from the given {@link Date}.
     *
     * @param date the date to extract the month from
     * @return the month value (1 for January, 12 for December)
     */
    public static int getMonth(Date date) {
        if (date == null) {
            return 0;
        }
        return toLocalDate(date).getMonth().getValue();
    }

    /**
     * Returns the month value (1-12) from the given {@link LocalDate}.
     *
     * @param date the date to extract the month from
     * @return the month value (1 for January, 12 for December)
     */
    public static int getMonth(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return date.getMonth().getValue();
    }

    /**
     * Returns the day of the month from the given {@link Date}.
     *
     * @param date the date to extract the day from
     * @return the day of the month
     */
    public static int getDay(Date date) {
        if (date == null) {
            return 0;
        }
        return toLocalDate(date).getDayOfMonth();
    }

    public static int getDay(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return date.getDayOfMonth();
    }

    ;

    /**
     * Returns the year from the given {@link Date}.
     *
     * @param date the date to extract the year from
     * @return the year
     */
    public static int getYear(Date date) {
        if (date == null) {
            return 0;
        }
        return toLocalDate(date).getYear();
    }

    /**
     * Returns the year from the given {@link LocalDate}.
     *
     * @param date the date to extract the year from
     * @return the year
     */
    public static int getYear(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return date.getYear();
    }

    /**
     * Create a new instance of Date and add one day
     *
     * @return date
     */
    public static Date tomorrow() {
        return addDays(now(), 1);
    }

    /**
     * Just create a new instance of date
     *
     * @return date
     */
    public static Date now() {
        return new Date();
    }

    /**
     * Returns a {@link Date} representing yesterday (one day before the current date).
     *
     * @return the date of yesterday
     */
    public static Date yesterday() {
        return addDays(now(), -1);
    }

    /**
     * Returns a {@link Date} representing the day after tomorrow (two days after the current date).
     *
     * @return the date of the day after tomorrow
     */
    public static Date dayAfterTomorrow() {
        return addDays(tomorrow(), 1);
    }

    /**
     * Converts a {@link Date} to a {@link LocalDate} using the system default time zone.
     * Supports both {@link java.util.Date} and {@link java.sql.Date}.
     *
     * @param input the date to convert
     * @return the corresponding LocalDate
     */
    public static LocalDate toLocalDate(Date input) {
        if (input == null) {
            return null;
        }
        if (input instanceof java.sql.Date) {
            return ((java.sql.Date) input).toLocalDate();
        } else {
            Instant instant = input.toInstant();
            ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
            return zdt.toLocalDate();
        }
    }

    /**
     * Converts a {@link LocalDateTime} to a {@link Date} using the system default time zone.
     *
     * @param input the LocalDateTime to convert
     * @return the corresponding Date
     */
    public static LocalDate toLocalDate(LocalDateTime input) {
        if (input == null) {
            return null;
        }
        return input.toLocalDate();
    }

    /**
     * Converts a {@link LocalDate} to a {@link LocalDate}.
     *
     * @param input the LocalDate to convert
     * @return the corresponding LocalDate
     */
    public static LocalDate toLocalDate(LocalDate input) {
        return input;
    }

    /**
     * Converts a {@link Date} to a {@link LocalDateTime} using the system default time zone.
     * Supports both {@link java.util.Date} and {@link java.sql.Date}.
     *
     * @param input the date to convert
     * @return the corresponding LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(LocalDate input) {
        if (input == null) {
            return null;
        }
        return input.atStartOfDay();
    }

    /**
     * Converts a {@link LocalDateTime} to a {@link LocalDateTime}.
     *
     * @param input the LocalDateTime to convert
     * @return the corresponding LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(LocalDateTime input) {
        return input;
    }

    /**
     * Converts a {@link LocalTime} to a {@link LocalDate} using epoch date.
     *
     * @param input the LocalTime to convert
     * @return the corresponding LocalDate
     */
    public static LocalDate toLocalDate(LocalTime input) {
        if (input == null) {
            return null;
        }
        return input.atDate(LocalDate.EPOCH).toLocalDate();
    }

    /**
     * Converts a {@link Date} to a {@link LocalDate} using a specific {@link ZoneId}.
     *
     * @param input  the date to convert
     * @param zoneId the time zone to use for conversion
     * @return the corresponding LocalDate
     */
    public static LocalDate toLocalDate(Date input, ZoneId zoneId) {
        if (input == null || zoneId == null) {
            return null;
        }
        return toInstant(input).atZone(zoneId).toLocalDate();
    }

    /**
     * Converts a {@link Date} to a {@link LocalDateTime} using the system default time zone.
     * Supports both {@link java.util.Date} and {@link java.sql.Date}.
     *
     * @param input the date to convert
     * @return the corresponding LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date input) {
        if (input == null) {
            return null;
        }
        Instant instant = null;
        if (input instanceof java.sql.Date) {
            instant = Instant.ofEpochMilli(input.getTime());
        } else if (input instanceof Time) {
            instant = ((Time) input).toLocalTime().atDate(LocalDate.of(1970, 1, 1)).atZone(ZoneId.systemDefault()).toInstant();
        } else {
            instant = input.toInstant();
        }
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        return zdt.toLocalDateTime();
    }

    /**
     * Convert a Date to LocalDateTime with specific ZoneId
     *
     * @param input  original date
     * @param zoneId zone id
     * @return local date time
     */
    public static LocalDateTime toLocalDateTime(Date input, ZoneId zoneId) {
        if (input == null || zoneId == null) {
            return null;
        }
        return toInstant(input).atZone(zoneId).toLocalDateTime();
    }

    /**
     * Combine a Date and a Time into a LocalDateTime using specific ZoneId
     *
     * @param date   original date
     * @param time   original time
     * @param zoneId zone id
     * @return local date time
     */
    public static LocalDateTime toLocalDateTime(Date date, Date time, ZoneId zoneId) {
        if (date == null || time == null || zoneId == null) {
            return null;
        }
        LocalDate localDate = toLocalDate(date, zoneId);
        LocalTime localTime = toLocalTime(time, zoneId);
        return LocalDateTime.of(localDate, localTime);
    }

    /**
     * Convert a Date to LocalTime
     */
    public static LocalTime toLocalTime(Date input) {
        if (input == null) {
            return null;
        }
        Instant instant = null;
        if (input instanceof java.sql.Date) {
            instant = Instant.ofEpochMilli(input.getTime());
        } else if (input instanceof Time time) {
            return time.toLocalTime();
        } else {
            instant = input.toInstant();
        }
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        return zdt.toLocalTime();
    }

    /**
     * Convert a LocalTime to LocalTime.
     *
     * @param input the LocalTime to convert
     * @return the corresponding LocalTime
     */
    public static LocalTime toLocalTime(LocalTime input) {
        return input;
    }

    /**
     * Convert a Date to LocalTime with specific ZoneId
     *
     * @param input  original date
     * @param zoneId zone id
     * @return local time
     */
    public static LocalTime toLocalTime(Date input, ZoneId zoneId) {
        if (input == null || zoneId == null) {
            return null;
        }
        return toInstant(input).atZone(zoneId).toLocalTime();
    }


    /**
     * Convert a Date to Instant
     */
    public static Instant toInstant(Date input) {
        if (input == null) {
            return null;
        }
        return toInstant(input, ZoneId.systemDefault());
    }

    /**
     * Convert a Date to Instant with specific ZoneId
     *
     * @param input  original date
     * @param zoneId zone id
     * @return instant
     */
    public static Instant toInstant(Date input, ZoneId zoneId) {
        if (input == null || zoneId == null) {
            return null;
        }
        if (input instanceof java.sql.Date) {
            LocalDate localDate = ((java.sql.Date) input).toLocalDate();
            Instant instant = null;
            try {
                instant = Instant.from(localDate);
            } catch (DateTimeException e) {
                instant = localDate.atStartOfDay(zoneId).toInstant();
            }
            return instant;
        } else if (input instanceof java.sql.Time) {
            LocalTime localTime = ((java.sql.Time) input).toLocalTime();
            Instant instant = null;
            try {
                instant = Instant.from(localTime);
            } catch (DateTimeException e) {
                instant = localTime.atDate(LocalDate.of(1970, 1, 1)).atZone(zoneId).toInstant();
            }
            return instant;

        } else {
            return input.toInstant();
        }
    }

    /**
     * Convert a LocalDate to Date
     */
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convert a LocalDateTime to Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convert a Instant to Date
     */
    public static Date getEndOfDay(Date date) {
        if (date == null) {
            return null;
        }
        return Date.from(toLocalDate(date).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convert a Instant to Date
     */
    public static Date getStartOfDay(Date date) {
        if (date == null) {
            return null;
        }
        return Date.from(toLocalDate(date).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime getStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    public static LocalDateTime getEndOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(23, 59, 59);
    }

    /**
     * Return a date range from first day of the current month to last day of the current month. Like 1 to 31 of December
     *
     * @return DateRange
     */
    public static DateRange getCurrentMonthRange() {
        Date startDate = createDate(getCurrentYear(), getCurrentMonth(), 1);
        Date endDate = createDate(getCurrentYear(), getCurrentMonth(), getLastDayOfMonth(getCurrentMonth()));
        return new DateRange(startDate, endDate);
    }

    /**
     * Parse string to {@link Date} using pattern
     *
     * @return date
     */
    public static Date parse(String source, String pattern) throws ParseException {
        DateFormat df = new SimpleDateFormat(pattern);
        return df.parse(source);
    }

    /**
     * Parse string to {@link LocalDate} using pattern
     *
     * @return local date
     */
    public static LocalDate parseLocalDate(String source, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(source, formatter);
    }


    /**
     * The same as now()
     */
    public static Date today() {
        return now();
    }

    /**
     * Create a new Date with specific time
     */
    public static Date today(int hourOfDay, int minutes, int seconds) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, seconds);
        return calendar.getTime();
    }

    /**
     * Format start date and end date from {@link DateRange}
     */
    public static String formatDate(DateRange dateRange) {
        return formatDate(dateRange.getStartDate()) + " - " + formatDate(dateRange.getEndDate());
    }

    /**
     * Format start date and end date from {@link DateRange} using pattern
     */
    public static String format(DateRange dateRange, String pattern) {
        return format(dateRange.getStartDate(), pattern) + " - " + format(dateRange.getEndDate(), pattern);
    }

    /**
     * Null safe check if date is between start and end date
     *
     * @param date
     * @param start
     * @param end
     * @return
     */
    public static boolean isBetween(Date date, Date start, Date end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        return (date.after(start) || date.equals(start)) && (date.before(end) || date.equals(end));
    }

    /**
     * Null safe check if date is between start and end date
     *
     * @param date
     * @param start
     * @param end
     * @return
     */
    public static boolean isBetween(ChronoLocalDate date, ChronoLocalDate start, ChronoLocalDate end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        return (date.isAfter(start) || date.equals(start)) && (date.isBefore(end) || date.equals(end));
    }

    /**
     * Null safe check if datetime is between start and end date
     *
     * @param date
     * @param start
     * @param end
     * @return
     */
    public static boolean isBetween(LocalDateTime date, LocalDateTime start, LocalDateTime end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        return (date.isAfter(start) || date.equals(start)) && (date.isBefore(end) || date.equals(end));
    }

    /**
     * Null safe check if instant is between start and end instant
     *
     * @param date
     * @param start
     * @param end
     * @return
     */
    public static boolean isBetween(Instant date, Instant start, Instant end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        return (date.isAfter(start) || date.equals(start)) && (date.isBefore(end) || date.equals(end));
    }

    /**
     * Null safe check if date1 is after or equals to date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isAfterOrEquals(ChronoLocalDate date1, ChronoLocalDate date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.isAfter(date2) || date1.equals(date2);
    }

    /**
     * Null safe check if date1 is before or equals to date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isBeforeOrEquals(ChronoLocalDate date1, ChronoLocalDate date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.isBefore(date2) || date1.equals(date2);
    }

    /**
     * Null safe check if date1 is after or equals to date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isAfterOrEquals(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.after(date2) || date1.equals(date2);
    }

    /**
     * Null safe check if date1 is before or equals to date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isBeforeOrEquals(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.before(date2) || date1.equals(date2);
    }


    /**
     * Null safe check if date1 is after or equals to date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isAfterOrEquals(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.isAfter(date2) || date1.equals(date2);
    }

    /**
     * Null safe check if date1 is before or equals to date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isBeforeOrEquals(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.isBefore(date2) || date1.equals(date2);
    }

    /**
     * Null safe check if date1 is after or equals to date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isAfterOrEquals(Instant date1, Instant date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.isAfter(date2) || date1.equals(date2);
    }

    /**
     * Null safe check if date1 is before or equals to date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isBeforeOrEquals(Instant date1, Instant date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.isBefore(date2) || date1.equals(date2);
    }


    /**
     * Null safe check if date1 is before date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isBefore(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return date1.before(date2);
    }

    /**
     * Null safe check if date1 is before date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isBefore(ChronoLocalDate date1, ChronoLocalDate date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return date1.isBefore(date2);
    }

    /**
     * Null safe check if date1 is before date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isBefore(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return date1.isBefore(date2);
    }

    /**
     * Null safe check if date1 is before date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isBefore(Instant date1, Instant date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return date1.isBefore(date2);
    }


    /**
     * Null safe check if date1 is after date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isAfter(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return date1.after(date2);
    }

    /**
     * Null safe check if date1 is after date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isAfter(ChronoLocalDate date1, ChronoLocalDate date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return date1.isAfter(date2);
    }

    /**
     * Null safe check if date1 is after date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isAfter(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return date1.isAfter(date2);
    }

    /**
     * Null safe check if date1 is after date 2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isAfter(Instant date1, Instant date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return date1.isAfter(date2);
    }


    public static long yearsBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.YEARS.between(startDate, endDate);
    }
}
