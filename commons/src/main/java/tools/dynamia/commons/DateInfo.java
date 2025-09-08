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

import java.io.Serializable;
import java.util.Date;

/**
 * Simple container for date values: year, month, and day.
 * <p>
 * Used to represent and transfer date components independently of full date objects.
 * Useful for date calculations, UI components, and validation logic.
 * By default, values are zero and negative values are not allowed.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * DateInfo info = new DateInfo(2025, 9, 7);
 * int year = info.getYear();
 * }
 * </pre>
 *
 * @author Mario A. Serrano Leones
 */
public class DateInfo implements Serializable {

    private int year;
    private int month;
    private int day;

    public DateInfo() {
    }

    /**
     *
     */
    public DateInfo(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public int getYear() {
        return year;
    }

    public final void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public final void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public final void setDay(int day) {
        this.day = day;
    }

    /**
     * Create a date object from values
     *
     */
    public Date toDate() {
        return DateTimeUtils.createDate(year, month, day);
    }

    /**
     * Convert current values to a DateRange. Example, if this DateInfo is a
     * year date range start from January 1st to December 31 of setted year
     *
     */
    public DateRange toRange() {
        Date start = null;
        Date end = null;

        if (isFullDate()) {
            start = DateTimeUtils.createDate(year, month, day, 0, 0);
            end = DateTimeUtils.createDate(year, month, day + 1, 0, 0);
        } else if (isMonth()) {
            start = DateTimeUtils.createDate(year, month, 1);
            end = DateTimeUtils.createDate(year, month, DateTimeUtils.getLastDayOfMonth(month));
        } else {
            start = DateTimeUtils.createDate(year, 1, 1);
            end = DateTimeUtils.createDate(year, 12, 31);
        }

        return new DateRange(start, end);
    }

    /**
     * Return true is year value is greater than zero but not month and day
     *
     */
    public boolean isYear() {
        return year > 0 && month == 0 && day == 0;
    }

    /**
     * Return true is year and month values are greater than zero but not day
     *
     */
    public boolean isMonth() {
        return year > 0 && month > 0 && day == 0;
    }

    /**
     * Return true if year, month and day values are greater than zero
     *
     */
    public boolean isFullDate() {
        return year > 0 && month > 0 && day > 0;
    }

}
