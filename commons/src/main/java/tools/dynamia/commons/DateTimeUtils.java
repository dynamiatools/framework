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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for common dates operations. Internally its use LocalDate, Instants and other Java 8 Date API
 *
 * @author Mario A. Serrano Leones
 */
public final class DateTimeUtils {

    /**
     * The Constant SHORT.
     */
    public static final int SHORT = DateFormat.SHORT;

    /**
     * The Constant MEDIUM.
     */
    public static final int MEDIUM = DateFormat.MEDIUM;

    /**
     * The Constant LONG.
     */
    public static final int LONG = DateFormat.LONG;

    /**
     * Return the last day of month
     *
     * @return last day
     */
    public static int getLastDayOfMonth(int month) {
        return getLastDayOfMonth(Month.of(month));
    }

    /**
     * Return the last day of month
     *
     * @return last day
     */
    public static int getLastDayOfMonth(Month month) {
        return month.length(LocalDate.now().isLeapYear());
    }

    /**
     * Instantiates a new date time utils.
     */
    private DateTimeUtils() {
    }

    /**
     * Checks if is future.
     *
     * @param date the date
     * @return true, if is future
     */
    public static boolean isFuture(Date date) {
        return date.after(new Date());
    }

    /**
     * Checks if is past.
     *
     * @param date the date
     * @return true, if is past
     */
    public static boolean isPast(Date date) {
        return date.before(new Date());
    }

    /**
     * Null safe check if passed date has the same year, month and day of current system date
     *
     * @return if is today
     */
    public static boolean isToday(Date date) {
        Date today = new Date();

        return getYear(today) == getYear(date) && getMonth(today) == getMonth(date) && getDay(today) == getDay(date);
    }

    /**
     * is the same that DateTimeUtils.getYear(new Date());
     *
     * @return the current year
     */
    public static int getCurrentYear() {
        return getYear(new Date());
    }

    /**
     * Gets the current month.
     *
     * @return the current month
     */
    public static int getCurrentMonth() {
        return getMonth(new Date());
    }

    /**
     * Gets the current month name.
     *
     * @return the current month name
     */
    public static String getCurrentMonthName() {
        DateFormat df = new SimpleDateFormat("MMMM");
        return df.format(new Date());
    }

    /**
     * Gets the current day.
     *
     * @return the current day
     */
    public static int getCurrentDay() {
        return getDay(new Date());
    }

    /**
     * Millis between.
     *
     * @param date1 the date1
     * @param date2 the date2
     * @return the long
     */
    public static long millisBetween(Date date1, Date date2) {
        LocalDate localDate1 = toLocalDate(date1);
        LocalDate localDate2 = toLocalDate(date2);

        return localDate1.until(localDate2, ChronoUnit.MILLIS);
    }


    /**
     * Months between.
     *
     * @param date1 the date1
     * @param date2 the date2
     * @return the long
     */
    public static long yearsBetween(Date date1, Date date2) {
        LocalDate localDate1 = toLocalDate(date1);
        LocalDate localDate2 = toLocalDate(date2);

        return localDate1.until(localDate2, ChronoUnit.YEARS);
    }

    /**
     * Months between.
     *
     * @param date1 the date1
     * @param date2 the date2
     * @return the long
     */
    public static long monthsBetween(Date date1, Date date2) {
        LocalDate localDate1 = toLocalDate(date1);
        LocalDate localDate2 = toLocalDate(date2);

        return localDate1.until(localDate2, ChronoUnit.MONTHS);
    }


    /**
     * Days between.
     *
     * @param date1 the date1
     * @param date2 the date2
     * @return the long
     */
    public static long daysBetween(Date date1, Date date2) {
        return toInstant(date1).until(toInstant(date2), ChronoUnit.DAYS);
    }

    /**
     * Hours between.
     *
     * @param date1 the date1
     * @param date2 the date2
     * @return the long
     */
    public static long hoursBetween(Date date1, Date date2) {
        return toInstant(date1).until(toInstant(date2), ChronoUnit.HOURS);
    }

    /**
     * Minutes between.
     *
     * @param date1 the date1
     * @param date2 the date2
     * @return the long
     */
    public static long minutesBetween(Date date1, Date date2) {
        return toInstant(date1).until(toInstant(date2), ChronoUnit.MINUTES);
    }

    /**
     * Seconds between.
     *
     * @param date1 the date1
     * @param date2 the date2
     * @return the long
     */
    public static long secondsBetween(Date date1, Date date2) {
        return toInstant(date1).until(toInstant(date2), ChronoUnit.SECONDS);
    }

    /**
     * Creates the date.
     *
     * @param timestamp the timestamp
     * @return the date
     */
    public static Date createDate(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar.getTime();
    }

    /**
     * Creates the date.
     *
     * @param dayOfMonth the day of month
     * @return the date
     */
    public static Date createDate(int dayOfMonth) {
        return toDate(LocalDate.now().withDayOfMonth(dayOfMonth));
    }

    /**
     * Creates the date.
     *
     * @param year  the year
     * @param month the month from 1 to 12
     * @param day   the day
     * @return the date
     */
    public static Date createDate(int year, int month, int day) {
        return toDate(LocalDate.of(year, month, day));
    }

    /**
     * Creates the date.
     *
     * @param year      the year
     * @param month     the month
     * @param day       the day
     * @param hourOfDay the hour of day
     * @param minute    the minute
     * @param second    the second
     * @return the date
     */
    public static Date createDate(int year, int month, int day, int hourOfDay, int minute, int second) {
        return toDate(LocalDateTime.of(year, month, day, hourOfDay, minute, second));
    }

    /**
     * Creates the date.
     *
     * @param year      the year
     * @param month     the month
     * @param day       the day
     * @param hourOfDay the hour of day
     * @param minute    the minute
     * @return the date
     */
    public static Date createDate(int year, int month, int day, int hourOfDay, int minute) {
        return toDate(LocalDateTime.of(year, month, day, hourOfDay, minute));
    }

    /**
     * Creates the date.
     *
     * @param dateText the date text
     * @return the date
     * @throws ParseException the parse exception
     */
    public static Date createDate(String dateText) throws ParseException {
        DateFormat df = DateFormat.getDateInstance();
        return df.parse(dateText);
    }

    /**
     * Creates the date.
     *
     * @param dateText the date text
     * @param pattern  the pattern
     * @return the date
     * @throws ParseException the parse exception
     */
    public static Date createDate(String dateText, String pattern) throws ParseException {
        DateFormat df = new SimpleDateFormat(pattern);
        return df.parse(dateText);
    }

    /**
     * Adds the days.
     * This method is immplemented using Java 7+ DateTime API
     *
     * @param date the date
     * @param days the days
     * @return the date
     */
    public static Date addDays(Date date, int days) {
        return toDate(toLocalDateTime(date).plusDays(days));

    }

    /**
     * Adds the months.
     * <p>
     * This method is immplemented using Java 7+ DateTime API
     *
     * @param date   the date
     * @param months the months
     * @return the date
     */
    public static Date addMonths(Date date, int months) {
        return toDate(toLocalDateTime(date).plusMonths(months));
    }

    /**
     * Adds the years.
     *
     * @param date  the date
     * @param years the years
     * @return the date
     */
    public static Date addYears(Date date, int years) {
        return toDate(toLocalDateTime(date).plusYears(years));
    }

    /**
     * Adds weeks.
     *
     * @param date the date
     * @return the date
     */
    public static Date addWeeks(Date date, int weeks) {
        return toDate(toLocalDateTime(date).plusWeeks(weeks));
    }

    /**
     * Adds the hours.
     *
     * @param date  the date
     * @param hours the hours
     * @return the date
     */
    public static Date addHours(Date date, int hours) {
        return toDate(toLocalDateTime(date).plus(hours, ChronoUnit.HOURS));
    }

    /**
     * Adds the minutes.
     *
     * @param date    the date
     * @param minutes the minutes
     * @return the date
     */
    public static Date addMinutes(Date date, int minutes) {
        return toDate(toLocalDateTime(date).plus(minutes, ChronoUnit.MINUTES));
    }

    /**
     * Format.
     *
     * @param date    the date
     * @param pattern the pattern
     * @return the string
     */
    public static String format(Date date, String pattern) {
        DateFormat df = new SimpleDateFormat(pattern, Messages.getDefaultLocale());
        return df.format(date);
    }

    /**
     * Format LocalDate, LocalTime, LocalDateTime and other {@link TemporalAccessor}
     *
     * @param temporalAccessor date, time, instant
     * @param pattern          date patter
     * @return
     */
    public static String format(TemporalAccessor temporalAccessor, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Messages.getDefaultLocale());
        return formatter.format(temporalAccessor);
    }

    /**
     * Format date.
     *
     * @param date  the date
     * @param style the style
     * @return the string
     */
    public static String formatDate(Date date, int style) {
        DateFormat df = DateFormat.getDateInstance(style);
        return df.format(date);
    }

    /**
     * Format date.
     *
     * @param date the date
     * @return the string
     */
    public static String formatDate(Date date) {
        return formatDate(date, DateFormat.MEDIUM);
    }

    /**
     * Format time.
     *
     * @param date  the date
     * @param style the style
     * @return the string
     */
    public static String formatTime(Date date, int style) {
        DateFormat df = DateFormat.getTimeInstance(style);
        return df.format(date);
    }

    /**
     * Format time.
     *
     * @param date the date
     * @return the string
     */
    public static String formatTime(Date date) {
        return formatTime(date, DateFormat.MEDIUM);
    }

    /**
     * return the month value in the date, where january is 1 and december is
     * 12.
     *
     * @param date the date
     * @return the month
     */
    public static int getMonth(Date date) {
        return toLocalDate(date).getMonth().getValue();
    }

    /**
     * Gets the day.
     *
     * @param date the date
     * @return the day
     */
    public static int getDay(Date date) {
        return toLocalDate(date).getDayOfMonth();
    }

    /**
     * Gets the year.
     *
     * @param date the date
     * @return the year
     */
    public static int getYear(Date date) {
        return toLocalDate(date).getYear();
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
     * Create a new date and substract one day
     *
     * @return date
     */
    public static Date yesterday() {
        return addDays(now(), -1);
    }

    /**
     * Create a tomorrow date and add one day
     */
    public static Date dayAfterTomorrow() {
        return addDays(tomorrow(), 1);
    }

    /**
     * Convert a Date to LocalDate
     */
    public static LocalDate toLocalDate(Date input) {
        if (input instanceof java.sql.Date) {
            return ((java.sql.Date) input).toLocalDate();
        } else {
            Instant instant = input.toInstant();
            ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
            return zdt.toLocalDate();
        }
    }

    public static LocalDateTime toLocalDateTime(Date input) {
        Instant instant = null;
        if (input instanceof java.sql.Date) {
            instant = Instant.ofEpochMilli(input.getTime());
        } else {
            instant = input.toInstant();
        }
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        return zdt.toLocalDateTime();
    }

    public static Instant toInstant(Date input) {
        if (input instanceof java.sql.Date) {
            LocalDate localDate = ((java.sql.Date) input).toLocalDate();
            Instant instant = null;
            try {
                instant = Instant.from(localDate);
            } catch (DateTimeException e) {
                instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
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
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date getEndOfDay(Date date) {
        return Date.from(toLocalDate(date).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date getStartOfDay(Date date) {
        return Date.from(toLocalDate(date).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
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


}
