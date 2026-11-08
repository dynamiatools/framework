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
package tools.dynamia.crud.query;

import org.junit.jupiter.api.Test;
import tools.dynamia.actions.Control;
import tools.dynamia.commons.LocalDateRange;
import tools.dynamia.commons.LocalDateTimeRange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** How the value a front end gives for a control becomes a filter. */
class FilterValuesTest {

    enum State { OPEN, PAID }

    @Test
    void aDateRangeIsABetweenWithIsoDates() {
        var filter = FilterValues.toFilter(Control.dateRange("issued"),
                new LocalDateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))).orElseThrow();

        assertEquals("issued", filter.field());
        assertEquals("between", filter.op());
        assertEquals(List.of("2026-01-01", "2026-01-31"), filter.value());
    }

    @Test
    void aDateTimeRangeKeepsTheTime() {
        var range = new LocalDateTimeRange(LocalDateTime.of(2026, 1, 1, 8, 30), LocalDateTime.of(2026, 1, 1, 17, 0));

        var filter = FilterValues.toFilter(Control.dateTimeRange("created"), range).orElseThrow();

        assertEquals(List.of("2026-01-01T08:30", "2026-01-01T17:00"), filter.value());
    }

    @Test
    void anEmptyValueMeansNoFilter() {
        assertTrue(FilterValues.toFilter(Control.dateRange("issued"), new LocalDateRange()).isEmpty());
        assertTrue(FilterValues.toFilter(Control.text("number"), "   ").isEmpty());
        assertTrue(FilterValues.toFilter(Control.choice("state", State.class), null).isEmpty());
        assertTrue(FilterValues.toFilter(Control.choice("state", State.class), new Control.Option("", "All")).isEmpty());
    }

    @Test
    void aChoiceSendsTheKeyOfTheEnumOrTheOption() {
        var control = Control.choice("state", State.class);

        assertEquals("PAID", FilterValues.toFilter(control, State.PAID).orElseThrow().value());
        assertEquals("OPEN", FilterValues.toFilter(control, new Control.Option("OPEN", "Open")).orElseThrow().value());
    }

    @Test
    void textSearchesWithLikeAndADateIsExact() {
        assertEquals("like", FilterValues.toFilter(Control.text("number"), "A-1").orElseThrow().op());
        assertEquals("eq", FilterValues.toFilter(Control.date("issued"), LocalDate.of(2026, 3, 5)).orElseThrow().op());
    }

    @Test
    void theWireFormOfARangeIsReadToo() {
        var filter = FilterValues.toFilter(Control.dateRange("issued"), "2026-01-01/").orElseThrow();

        assertEquals(java.util.Arrays.asList("2026-01-01", null), filter.value());
    }

    @Test
    void aChoiceBuiltFromAnEnumListsItsConstantsAndKeepsItsOptions() {
        var control = Control.choice("state", State.class).allLabel("All").label("State");

        assertEquals(2, control.options().size());
        assertEquals("OPEN", control.options().get(0).key());
        assertEquals("All", control.allLabel());
        assertEquals("State", control.label());
        assertEquals(Control.Kind.CHOICE, control.kind());
    }
}
