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

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static tools.dynamia.commons.DateTimeUtils.addDays;
import static tools.dynamia.commons.DateTimeUtils.addMonths;
import static tools.dynamia.commons.DateTimeUtils.addYears;
import static tools.dynamia.commons.DateTimeUtils.createDate;
import static tools.dynamia.commons.DateTimeUtils.daysBetween;
import static tools.dynamia.commons.DateTimeUtils.hoursBetween;
import static tools.dynamia.commons.DateTimeUtils.monthsBetween;
import static tools.dynamia.commons.DateTimeUtils.now;
import static tools.dynamia.commons.DateTimeUtils.yearsBetween;

public class DateTimeUtilsTest {

    private static final long EXPECTED = 3;

    @Test
    public void shouldBe3Seconds() {
        Date start = createDate(2018, 1, 1, 8, 0, 0);
        Date end = createDate(2018, 1, 1, 8, 0, 3);
        long result = DateTimeUtils.secondsBetween(start, end);
        assertEquals(EXPECTED, result);
    }

    @Test
    public void shouldBe3Minutes() {
        Date start = createDate(2018, 1, 1, 8, 0, 0);
        Date end = createDate(2018, 1, 1, 8, 3, 0);
        long result = DateTimeUtils.minutesBetween(start, end);
        assertEquals(EXPECTED, result);
    }

    @Test
    public void shouldBe3Hours() {
        Date start = createDate(2018, 1, 1, 8, 0, 0);
        Date end = createDate(2018, 1, 1, 11, 0, 0);
        long result = hoursBetween(start, end);
        assertEquals(EXPECTED, result);
    }


    @Test
    public void shouldBe3Days() {
        Date start = createDate(2018, 1, 1, 8, 0, 0);
        Date end = createDate(2018, 1, 4, 8, 0, 0);
        long result = daysBetween(start, end);
        assertEquals(EXPECTED, result);
    }

    @Test
    public void shouldBe3Months() {
        Date start = createDate(2018, 1, 1, 8, 0, 0);
        Date end = createDate(2018, 4, 1, 8, 0, 0);
        long result = monthsBetween(start, end);
        assertEquals(EXPECTED, result);
    }

    @Test
    public void shouldBe3Years() {
        Date start = createDate(2018, 1, 1, 8, 0, 0);
        Date end = createDate(2021, 1, 1, 8, 0, 0);
        long result = yearsBetween(start, end);
        assertEquals(EXPECTED, result);
    }

    @Test
    public void shouldAdd3Days() {
        Date now = now();
        Date later = addDays(now, 3);
        long result = daysBetween(now, later);
        assertEquals(EXPECTED, result);
    }

    @Test
    public void shouldAdd3Months() {
        Date start = createDate(2018, 5, 1);
        Date later = addMonths(start, 3);
        long result = monthsBetween(start, later);
        assertEquals(EXPECTED, result);
    }

    @Test
    public void shouldAdd3Years() {
        Date start = createDate(2018, 5, 1);
        Date later = addYears(start, 3);
        long result = yearsBetween(start, later);
        assertEquals(EXPECTED, result);
    }
}
