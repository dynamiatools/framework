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

import tools.dynamia.actions.Control;
import tools.dynamia.commons.LocalDateRange;
import tools.dynamia.commons.LocalDateTimeRange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

/**
 * Turns the value a front end gives for a {@link Control} into a {@link QuerySpec.Filter}. ZK widgets hand over Java objects
 * (a range, a date, the chosen option) and a remote client sends the wire form; both end in the same filter, which
 * {@link FilterConditions} maps to a condition.
 */
public final class FilterValues {

    private FilterValues() {
    }

    /**
     * @param control the control the value comes from
     * @param value   the value: a range, a date, a text, an enum constant or an option key; null or empty means no filter
     * @return the filter, or empty when the value means "no filter" (an empty range, blank text, the "all" option)
     */
    public static Optional<QuerySpec.Filter> toFilter(Control control, Object value) {
        var field = control.field();
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof LocalDateRange range) {
            return range.isNull() ? Optional.empty() : Optional.of(range(field, range.getStartDate(), range.getEndDate()));
        }
        if (value instanceof LocalDateTimeRange range) {
            return range.isNull() ? Optional.empty() : Optional.of(range(field, range.getStartDateTime(), range.getEndDateTime()));
        }
        if (value instanceof LocalDate || value instanceof LocalDateTime) {
            return Optional.of(new QuerySpec.Filter(field, QuerySpec.Op.EQ.wire(), value.toString()));
        }
        if (value instanceof Enum<?> constant) {
            return Optional.of(new QuerySpec.Filter(field, QuerySpec.Op.EQ.wire(), constant.name()));
        }
        if (value instanceof Control.Option option) {
            return option.key() == null || option.key().isEmpty() ? Optional.empty()
                    : Optional.of(new QuerySpec.Filter(field, QuerySpec.Op.EQ.wire(), option.key()));
        }
        var text = value.toString().trim();
        if (text.isEmpty()) {
            return Optional.empty();
        }
        if (control.kind() == Control.Kind.DATE_RANGE || control.kind() == Control.Kind.DATETIME_RANGE) {
            var parts = text.split("/", -1);
            if (parts.length == 2) {
                return Optional.of(new QuerySpec.Filter(field, QuerySpec.Op.BETWEEN.wire(),
                        Arrays.asList(blankToNull(parts[0]), blankToNull(parts[1]))));
            }
        }
        var op = control.kind() == Control.Kind.TEXT ? QuerySpec.Op.LIKE : QuerySpec.Op.EQ;
        return Optional.of(new QuerySpec.Filter(field, op.wire(), text));
    }

    private static QuerySpec.Filter range(String field, Object from, Object to) {
        return new QuerySpec.Filter(field, QuerySpec.Op.BETWEEN.wire(),
                Arrays.asList(from == null ? null : from.toString(), to == null ? null : to.toString()));
    }

    private static String blankToNull(String text) {
        return text.isBlank() ? null : text.trim();
    }
}
