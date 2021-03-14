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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represent a date range from start date to end date. Its provide usefull method to work with dates
 */
public class DateRange implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The end date.
     */
    private Date endDate;

    /**
     * The start date.
     */
    private Date startDate;

    /**
     * Instantiates a new date range.
     */
    public DateRange() {

    }

    /**
     * Instantiates a new date range.
     *
     * @param startDate the end date
     * @param endDate   the start date
     */
    public DateRange(Date startDate, Date endDate) {
        super();
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Gets the end date.
     *
     * @return the end date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Gets the start date.
     *
     * @return the start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the end date.
     *
     * @param endDate the new end date
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Sets the start date.
     *
     * @param startDate the new start date
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public long getDaysBetween() {
        return DateTimeUtils.daysBetween(startDate, endDate);
    }

    public long getHoursBetween() {
        return DateTimeUtils.hoursBetween(startDate, endDate);
    }

    public long getMinutesBetween() {
        return DateTimeUtils.minutesBetween(startDate, endDate);
    }

    public long getSecondsBetween() {
        return DateTimeUtils.secondsBetween(startDate, endDate);
    }

    public long getMonthsBetween() {
        return DateTimeUtils.monthsBetween(startDate, endDate);
    }

    public long getYearsBetween() {
        return DateTimeUtils.yearsBetween(startDate, endDate);
    }

    List<Date> getDaysBetweenList() {
        List<Date> dates = new ArrayList<>();
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

    @Override
    public String toString() {
        return String.valueOf(startDate) + "  /  " + String.valueOf(endDate);
    }
}
