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

import tools.dynamia.commons.LocalDateRange;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * What a single-value prompt ({@link UIMessages#showInput}) asks for and how its answer becomes a Java value. Shared by
 * the front ends that receive the answer as text (the headless runtime and the test platform), so they agree.
 * <p>
 * A date range travels as {@code "start/end"} with ISO dates; either side may be empty for an open range.
 */
public final class InputValues {

    /** Input type of a free text value. */
    public static final String TEXT = "text";
    /** Input type of a number. */
    public static final String NUMBER = "number";
    /** Input type of a date. */
    public static final String DATE = "date";
    /** Input type of a {@link LocalDateRange}. */
    public static final String DATE_RANGE = "daterange";

    private InputValues() {
    }

    /**
     * @param valueClass the class the action asks for
     * @return the input type a client must render for it: {@code text}, {@code number}, {@code date} or {@code daterange}
     */
    public static String typeOf(Class<?> valueClass) {
        if (valueClass == LocalDateRange.class) {
            return DATE_RANGE;
        }
        if (valueClass == LocalDate.class) {
            return DATE;
        }
        if (Number.class.isAssignableFrom(valueClass) || valueClass == int.class || valueClass == long.class
                || valueClass == double.class) {
            return NUMBER;
        }
        return TEXT;
    }

    /**
     * The text a client shows for a default value, in the same format it answers with.
     *
     * @param value the default value, may be null
     * @return the text form, or null when there is no default
     */
    public static Object toWire(Object value) {
        if (value instanceof LocalDateRange range) {
            return (range.getStartDate() == null ? "" : range.getStartDate()) + "/"
                    + (range.getEndDate() == null ? "" : range.getEndDate());
        }
        return value;
    }

    /**
     * Converts the answer of a client to {@code type}.
     *
     * @param answer the answer: already of {@code type}, or its text form
     * @param type   the class the action asked for
     * @return the value
     * @throws IllegalArgumentException when the answer is not valid for {@code type}
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object answer, Class<T> type) {
        if (type.isInstance(answer)) {
            return (T) answer;
        }
        var text = String.valueOf(answer).trim();
        Object value;
        if (type == String.class) value = text;
        else if (type == Integer.class || type == int.class) value = Integer.valueOf(text);
        else if (type == Long.class || type == long.class) value = Long.valueOf(text);
        else if (type == Double.class || type == double.class) value = Double.valueOf(text);
        else if (type == BigDecimal.class) value = new BigDecimal(text);
        else if (type == Boolean.class || type == boolean.class) value = Boolean.valueOf(text);
        else if (type == LocalDate.class) value = LocalDate.parse(text);
        else if (type == LocalDateRange.class) value = parseRange(text);
        else throw new IllegalArgumentException("Cannot convert the answer to " + type.getName());
        return (T) value;
    }

    private static LocalDateRange parseRange(String text) {
        var parts = text.split("/", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException("A date range is \"start/end\", got: " + text);
        }
        var start = parts[0].isBlank() ? null : LocalDate.parse(parts[0].trim());
        var end = parts[1].isBlank() ? null : LocalDate.parse(parts[1].trim());
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("The end of the range is before its start: " + text);
        }
        return new LocalDateRange(start, end);
    }
}
