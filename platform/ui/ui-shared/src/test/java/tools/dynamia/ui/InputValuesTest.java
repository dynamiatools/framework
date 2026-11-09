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
package tools.dynamia.ui;

import org.junit.jupiter.api.Test;
import tools.dynamia.commons.LocalDateRange;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputValuesTest {

    @Test
    void theInputTypeFollowsTheClassTheActionAsksFor() {
        assertEquals("daterange", InputValues.typeOf(LocalDateRange.class));
        assertEquals("date", InputValues.typeOf(LocalDate.class));
        assertEquals("number", InputValues.typeOf(Integer.class));
        assertEquals("number", InputValues.typeOf(double.class));
        assertEquals("text", InputValues.typeOf(String.class));
    }

    @Test
    void aDateRangeIsAnsweredAsStartSlashEnd() {
        var range = InputValues.convert("2026-01-01/2026-01-31", LocalDateRange.class);

        assertEquals(LocalDate.of(2026, 1, 1), range.getStartDate());
        assertEquals(LocalDate.of(2026, 1, 31), range.getEndDate());
    }

    @Test
    void eitherSideOfARangeMayBeOpen() {
        var from = InputValues.convert("2026-01-01/", LocalDateRange.class);
        var until = InputValues.convert("/2026-01-31", LocalDateRange.class);

        assertEquals(LocalDate.of(2026, 1, 1), from.getStartDate());
        assertNull(from.getEndDate());
        assertNull(until.getStartDate());
        assertEquals(LocalDate.of(2026, 1, 31), until.getEndDate());
    }

    @Test
    void anInvalidRangeIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> InputValues.convert("2026-02-01/2026-01-01", LocalDateRange.class));
        assertThrows(IllegalArgumentException.class, () -> InputValues.convert("2026-02-01", LocalDateRange.class));
        assertThrows(RuntimeException.class, () -> InputValues.convert("nonsense/2026-01-01", LocalDateRange.class));
    }

    @Test
    void aDefaultRangeTravelsInTheSameFormat() {
        var range = new LocalDateRange(LocalDate.of(2026, 3, 1), null);

        assertEquals("2026-03-01/", InputValues.toWire(range));
        assertEquals(5, InputValues.toWire(5));
    }
}
