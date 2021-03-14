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
package tools.dynamia.integration.scheduling;

import tools.dynamia.commons.DayOfWeek;


/**
 * Util class for formating Cron Expressions.
 *
 * @author Mario A. Serrano Leones
 */
public class CronMaker {

    /**
     * Generate a CRON expression is a string comprising 5 or 6 fields separated
     * by white space.
     *
     * @param seconds mandatory = yes. allowed values = {@code  0-59    * / , -}
     * @param minutes mandatory = yes. allowed values = {@code  0-59    * / , -}
     * @param hours mandatory = yes. allowed values = {@code 0-23   * / , -}
     * @param dayOfMonth mandatory = yes. allowed values =
     * {@code 1-31  * / , - ? L W}
     * @param month mandatory = yes. allowed values =
     * {@code 1-12 or JAN-DEC    * / , -}
     * @param dayOfWeek mandatory = yes. allowed values =
     * {@code 0-6 or SUN-SAT * / , - ? L #}
     * @param year mandatory = no. allowed values = {@code 1970–2099    * / , -}
     * @return a CRON Formatted String.
     */
    public static String generate(final String seconds, final String minutes, final String hours, final String dayOfMonth, final String month, final String dayOfWeek, final String year) {
        return String.format("%1$s %2$s %3$s %4$s %5$s %6$s %7%s", seconds, minutes, hours, dayOfMonth, month, dayOfWeek, year);
    }

    /**
     * Minutes.
     *
     * @param minutes the minutes
     * @return the string
     */
    public static String minutes(int minutes) {
        return generate("0", "0/" + minutes, "*", "1/1", "*", "?", "*");
    }

    /**
     * Hourly.
     *
     * @param hours the hours
     * @return the string
     */
    public static String hourly(int hours) {
        return generate("0", "0", "0/" + hours, "1/1", "*", "?", "*");
    }

    /**
     * Create a cron expresion for every day at specified hour and minutes
     * Example: dailyAt("20","30") means everyday at "20:30".
     *
     * @param hours the hours
     * @param minutes the minutes
     * @return the string
     */
    public static String daily(int hours, int minutes) {

        return generate("0", String.valueOf(minutes), String.valueOf(hours), "1/1", "*", "?", "*");
    }

    /**
     * Create a cron expresion for every day at specified hour and minutes
     * between Monday and Friday
     *
     * Example: dailyWeekDayAt("20","30") means every Monday to Friday at
     * "20:30".
     *
     * @param hours the hours
     * @param minutes the minutes
     * @return the string
     */
    public static String dailyWeekDay(int hours, int minutes) {
        return generate("0", String.valueOf(minutes), String.valueOf(hours), "?", "*", "MON-FRI", "*");
    }

    /**
     * Weekly.
     *
     * @param hours the hours
     * @param minutes the minutes
     * @param days the days
     * @return the string
     */
    public static String weekly(int hours, int minutes, DayOfWeek... days) {
        StringBuilder sb = new StringBuilder();

        if (days != null && days.length > 0) {
            for (DayOfWeek day : days) {
                sb.append(day.getCronName()).append(",");
            }
        } else {
            throw new IllegalArgumentException("At least one DayOfWeek is required");
        }

        String daysText = sb.toString();
        daysText = daysText.substring(0, daysText.length() - 1);

        return generate("0", String.valueOf(minutes), String.valueOf(hours), "?", "*", daysText, "*");

    }

    private CronMaker() {
    }
}
