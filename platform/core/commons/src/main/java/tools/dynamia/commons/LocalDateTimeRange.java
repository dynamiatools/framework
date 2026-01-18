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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent a date range from start date to end date. Its provide usefull method to work with dates
 */
public class LocalDateTimeRange implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The end date.
     */
    private LocalDateTime endDateTime;

    /**
     * The start date.
     */
    private LocalDateTime startDateTime;

    /**
     * Instantiates a new date range.
     */
    public LocalDateTimeRange() {

    }

    /**
     * Instantiates a new date range.
     *
     * @param startDate   the end date
     * @param endDateTime the start date
     */
    public LocalDateTimeRange(LocalDateTime startDate, LocalDateTime endDateTime) {
        super();
        this.startDateTime = startDate;
        this.endDateTime = endDateTime;
    }

    /**
     * Today date range from start of day to end of day
     *
     * @return the local date range
     */
    public static LocalDateTimeRange today() {
        return new LocalDateTimeRange(LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));
    }

    /**
     * Yesterday date range from start of day to end of day
     *
     * @return the local date range
     */
    public static LocalDateTimeRange yesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return new LocalDateTimeRange(yesterday.atStartOfDay(), yesterday.atTime(LocalTime.MAX));
    }

    /**
     * Tomorrow date range from start of day to end of day
     *
     * @return the local date range
     */
    public static LocalDateTimeRange tomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return new LocalDateTimeRange(tomorrow.atStartOfDay(), tomorrow.atTime(LocalTime.MAX));
    }

    /**
     * Gets the end date.
     *
     * @return the end date
     */
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }


    /**
     * Gets the start date.
     *
     * @return the start date
     */
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }


    /**
     * Sets the end date.
     *
     * @param endDateTime the new end date
     */
    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    /**
     * Sets the start date.
     *
     * @param startDateTime the new start date
     */
    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public long getDaysBetween() {

        return DateTimeUtils.daysBetween(startDateTime.toLocalDate(), endDateTime.toLocalDate());
    }


    public long getMonthsBetween() {
        return DateTimeUtils.monthsBetween(startDateTime.toLocalDate(), endDateTime.toLocalDate());
    }

    public long getYearsBetween() {
        return DateTimeUtils.yearsBetween(startDateTime.toLocalDate(), endDateTime.toLocalDate());
    }

    public long getHoursBetween() {
        return DateTimeUtils.hoursBetween(startDateTime, endDateTime);
    }

    public long getMinutesBetween() {
        return DateTimeUtils.minutesBetween(startDateTime, endDateTime);
    }

    List<LocalDate> getDaysBetweenList() {
        List<LocalDate> dates = new ArrayList<>();
        long days = getDaysBetween();
        for (int i = 0; i < days; i++) {
            dates.add(DateTimeUtils.addDays(startDateTime.toLocalDate(), i));
        }
        return dates;
    }

    /**
     * Checks if is null. Return true if endDate OR startDate are null
     *
     * @return true, if is null
     */
    public boolean isNull() {
        return endDateTime == null || startDateTime == null;
    }

    @Override
    public String toString() {
        return startDateTime + "  /  " + endDateTime;
    }
}
