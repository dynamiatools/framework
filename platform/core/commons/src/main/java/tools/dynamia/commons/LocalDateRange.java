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

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a date range from a start date to an end date.
 * Provides utility methods to work with date ranges, including calculations,
 * comparisons, and factory methods for common date ranges.
 *
 * <p>This class is immutable-safe as it encapsulates LocalDate instances,
 * which are themselves immutable. It offers various factory methods to create
 * common date ranges such as today, yesterday, current month, year-to-date, etc.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Create a range for the current month
 * LocalDateRange range = LocalDateRange.currentMonth();
 *
 * // Check if a date falls within the range
 * if (range.isBetween(LocalDate.now())) {
 *     System.out.println("Date is within range");
 * }
 *
 * // Calculate days between start and end
 * long days = range.getDaysBetween();
 * }</pre>
 *
 * @see LocalDate
 * @see DateTimeUtils
 */
public class LocalDateRange implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The end date.
     */
    private LocalDate endDate;

    /**
     * The start date.
     */
    private LocalDate startDate;

    /**
     * Instantiates a new date range.
     */
    public LocalDateRange() {

    }

    /**
     * Instantiates a new date range.
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     */
    public LocalDateRange(LocalDate startDate, LocalDate endDate) {
        super();
        this.startDate = startDate;
        this.endDate = endDate;
    }


    /**
     * Instantiates a new date range from LocalDateTime instances.
     * The time portion is discarded and only the date is used.
     *
     * @param startDateTime the start date-time, or null
     * @param endDateTime   the end date-time, or null
     */
    public LocalDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        super();
        this.startDate = startDateTime != null ? startDateTime.toLocalDate() : null;
        this.endDate = endDateTime != null ? endDateTime.toLocalDate() : null;
    }

    /**
     * Creates a LocalDateRange for the entire last month.
     * The range spans from the first day to the last day of the previous month.
     *
     * <p>Example:</p>
     * <pre>{@code
     * // If today is 2026-01-18
     * LocalDateRange range = LocalDateRange.lastMonth();
     * // Returns: range from 2025-12-01 to 2025-12-31
     * }</pre>
     *
     * @return a LocalDateRange covering the entire last month
     */
    public static LocalDateRange lastMonth() {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfLastMonth = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());
        return new LocalDateRange(firstDayOfLastMonth, lastDayOfLastMonth);
    }

    /**
     * Creates a LocalDateRange for the entire next month.
     * The range spans from the first day to the last day of the next month.
     *
     * <p>Example:</p>
     * <pre>{@code
     * // If today is 2026-01-18
     * LocalDateRange range = LocalDateRange.nextMonth();
     * // Returns: range from 2026-02-01 to 2026-02-28
     * }</pre>
     *
     * @return a LocalDateRange covering the entire next month
     */
    public static LocalDateRange nextMonth() {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfNextMonth = now.plusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfNextMonth = firstDayOfNextMonth.withDayOfMonth(firstDayOfNextMonth.lengthOfMonth());
        return new LocalDateRange(firstDayOfNextMonth, lastDayOfNextMonth);
    }

    /**
     * Gets the end date.
     *
     * @return the end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }


    /**
     * Gets the start date.
     *
     * @return the start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }


    /**
     * Sets the end date.
     *
     * @param endDate the new end date
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Sets the start date.
     *
     * @param startDate the new start date
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Calculates the number of days between the start date and end date.
     *
     * @return the number of days between start and end dates
     * @see DateTimeUtils#daysBetween(LocalDate, LocalDate)
     */
    public long getDaysBetween() {
        return DateTimeUtils.daysBetween(startDate, endDate);
    }


    /**
     * Calculates the number of months between the start date and end date.
     *
     * @return the number of months between start and end dates
     * @see DateTimeUtils#monthsBetween(LocalDate, LocalDate)
     */
    public long getMonthsBetween() {
        return DateTimeUtils.monthsBetween(startDate, endDate);
    }

    /**
     * Calculates the number of years between the start date and end date.
     *
     * @return the number of years between start and end dates
     * @see DateTimeUtils#yearsBetween(LocalDate, LocalDate)
     */
    public long getYearsBetween() {
        return DateTimeUtils.yearsBetween(startDate, endDate);
    }

    /**
     * Returns a list of all dates between the start date and end date (exclusive).
     * Each day in the range is represented as a separate LocalDate in the list.
     *
     * <p>Example:</p>
     * <pre>{@code
     * LocalDateRange range = LocalDateRange.of(
     *     LocalDate.of(2026, 1, 1),
     *     LocalDate.of(2026, 1, 5)
     * );
     * List<LocalDate> days = range.getDaysBetweenList();
     * // Returns: [2026-01-01, 2026-01-02, 2026-01-03, 2026-01-04]
     * }</pre>
     *
     * @return a list of LocalDate objects representing each day in the range
     */
    List<LocalDate> getDaysBetweenList() {
        List<LocalDate> dates = new ArrayList<>();
        long days = getDaysBetween();
        for (int i = 0; i < days; i++) {
            dates.add(DateTimeUtils.addDays(startDate, i));
        }
        return dates;
    }

    /**
     * Checks if is null. Return true if endDate OR startDate are null
     *
     * @return true, if is null
     */
    public boolean isNull() {
        return endDate == null || startDate == null;
    }

    /**
     * Creates a LocalDateRange from a number of days ago until today.
     *
     * @param daysAgo the number of days ago to start the range
     * @return a LocalDateRange from daysAgo until today
     */
    public static LocalDateRange fromDaysAgo(int daysAgo) {
        LocalDate targetDate = LocalDate.now().minusDays(daysAgo);
        return new LocalDateRange(targetDate, LocalDate.now());
    }

    /**
     * Creates a LocalDateRange from today until a number of days from now.
     *
     * @param daysFromNow the number of days from now to end the range
     * @return a LocalDateRange from today until daysFromNow
     */
    public static LocalDateRange untilDaysFromNow(int daysFromNow) {
        LocalDate targetDate = LocalDate.now().plusDays(daysFromNow);
        return new LocalDateRange(LocalDate.now(), targetDate);
    }

    /**
     * Creates a LocalDateRange for today.
     *
     * @return a LocalDateRange for today
     */
    public static LocalDateRange today() {
        LocalDate today = LocalDate.now();
        return new LocalDateRange(today, today);
    }

    /**
     * Creates a LocalDateRange for yesterday.
     *
     * @return a LocalDateRange for yesterday
     */
    public static LocalDateRange yesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return new LocalDateRange(yesterday, yesterday);
    }

    /**
     * Creates a LocalDateRange for tomorrow.
     *
     * @return a LocalDateRange for tomorrow
     */
    public static LocalDateRange tomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return new LocalDateRange(tomorrow, tomorrow);
    }

    /**
     * Factory method to create a LocalDateRange from two LocalDate instances.
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return a new LocalDateRange instance
     */
    public static LocalDateRange of(LocalDate startDate, LocalDate endDate) {
        return new LocalDateRange(startDate, endDate);
    }

    /**
     * Factory method to create a LocalDateRange from two LocalDateTime instances.
     * The time portion is discarded and only the date is used.
     *
     * @param startDateTime the start date-time of the range
     * @param endDateTime   the end date-time of the range
     * @return a new LocalDateRange instance
     */
    public static LocalDateRange of(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return new LocalDateRange(startDateTime, endDateTime);
    }

    /**
     * Creates a LocalDateRange from the first day of the current month until today.
     *
     * <p>Example:</p>
     * <pre>{@code
     * // If today is 2026-01-18
     * LocalDateRange range = LocalDateRange.monthToDate();
     * // Returns: range from 2026-01-01 to 2026-01-18
     * }</pre>
     *
     * @return a LocalDateRange from the first day of the month to today
     */
    public static LocalDateRange monthToDate() {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        return new LocalDateRange(firstDayOfMonth, now);
    }

    /**
     * Creates a LocalDateRange from the first day of the current year until today.
     *
     * <p>Example:</p>
     * <pre>{@code
     * // If today is 2026-01-18
     * LocalDateRange range = LocalDateRange.yearToDate();
     * // Returns: range from 2026-01-01 to 2026-01-18
     * }</pre>
     *
     * @return a LocalDateRange from the first day of the year to today
     */
    public static LocalDateRange yearToDate() {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfYear = now.withDayOfYear(1);
        return new LocalDateRange(firstDayOfYear, now);
    }

    /**
     * Creates a LocalDateRange for the entire current month.
     * The range spans from the first day to the last day of the current month.
     *
     * <p>Example:</p>
     * <pre>{@code
     * // If today is 2026-01-18
     * LocalDateRange range = LocalDateRange.currentMonth();
     * // Returns: range from 2026-01-01 to 2026-01-31
     * }</pre>
     *
     * @return a LocalDateRange covering the entire current month
     */
    public static LocalDateRange currentMonth() {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        return new LocalDateRange(firstDayOfMonth, lastDayOfMonth);
    }

    /**
     * Creates a LocalDateRange for the entire current year.
     * The range spans from the first day to the last day of the current year.
     *
     * <p>Example:</p>
     * <pre>{@code
     * // If today is 2026-01-18
     * LocalDateRange range = LocalDateRange.currentYear();
     * // Returns: range from 2026-01-01 to 2026-12-31
     * }</pre>
     *
     * @return a LocalDateRange covering the entire current year
     */
    public static LocalDateRange currentYear() {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfYear = now.withDayOfYear(1);
        LocalDate lastDayOfYear = now.withDayOfYear(now.lengthOfYear());
        return new LocalDateRange(firstDayOfYear, lastDayOfYear);
    }


    /**
     * Checks if the given date is between the startDate and endDate (inclusive).
     *
     * @param date for comparison
     * @return true if the date is between startDate and endDate
     */
    public boolean isBetween(LocalDate date) {
        if (date == null || isNull()) {
            return false;
        }
        return (date.isEqual(startDate) || date.isAfter(startDate)) &&
                (date.isEqual(endDate) || date.isBefore(endDate));
    }

    /**
     * Checks if the endDate is after the given date.
     *
     * @param date for comparison
     * @return true if endDate is after the given date
     */
    public boolean isAfter(LocalDate date) {
        if (date == null || isNull()) {
            return false;
        }
        return endDate.isAfter(date);
    }


    /**
     * Checks if the startDate is before the given date.
     *
     * @param date for comparison
     * @return true if startDate is before the given date
     */
    public boolean isBefore(LocalDate date) {
        if (date == null || isNull()) {
            return false;
        }
        return startDate.isBefore(date);
    }

    @Override
    public String toString() {
        return startDate + "  /  " + endDate;
    }
}
