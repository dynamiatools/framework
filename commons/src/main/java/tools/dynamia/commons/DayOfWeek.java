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


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Day of week enum, starting from SUNDAY(1) to SATURDAY(7).
 *
 * @author Mario A. Serrano Leones
 */
public enum DayOfWeek {

    /**
     * The sunday.
     */
    SUNDAY("SUN", 1),
    /**
     * The monday.
     */
    MONDAY("MON", 2),
    /**
     * The tuesday.
     */
    TUESDAY("TUE", 3),
    /**
     * The wednesday.
     */
    WEDNESDAY("WED", 4),
    /**
     * The thursday.
     */
    THURSDAY("THU", 5),
    /**
     * The friday.
     */
    FRIDAY("FRI", 6),
    /**
     * The saturday.
     */
    SATURDAY("SAT", 7);

    /**
     * The cron name.
     */
    private final String cronName;

    /**
     * The number.
     */
    private final int number;

    /**
     * Instantiates a new day of week.
     *
     * @param cronName the cron name
     * @param number   the number
     */
    DayOfWeek(String cronName, int number) {
        this.cronName = cronName;
        this.number = number;
    }

    /**
     * Gets the number.
     *
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Gets the cron name.
     *
     * @return the cron name
     */
    public String getCronName() {
        return cronName;
    }

    public String getDisplayName(Locale locale) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, number);
        SimpleDateFormat format = new SimpleDateFormat("EEEE", locale);
        return format.format(cal.getTime()).toUpperCase();
    }

    public String getDisplayName() {
        return getDisplayName(Messages.getDefaultLocale());
    }

    public static DayOfWeek today() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return DayOfWeek.of(day);

    }

    public static DayOfWeek of(int day) {
        return DayOfWeek.values()[day - 1];
    }


    public static List<DayOfWeek> valuesList() {
        return Stream.of(values()).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
