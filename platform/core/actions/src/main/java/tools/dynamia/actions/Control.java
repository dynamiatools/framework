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
package tools.dynamia.actions;

import java.util.Arrays;
import java.util.List;

/**
 * Describes, as plain data, the control an action leaves in a toolbar when it is not launched from a button: a date range, a
 * search box, a choice. Every front end draws it with its own widgets and applies the value to the list query field it names,
 * so the action behind it is the same in ZK and in a remote client. It never holds a component, a callback or a class name.
 *
 * <pre>{@code
 * Control.dateRange("creationDate").label("Created")
 * Control.choice("estado", EstadoCredito.class).allLabel("Todos")
 * }</pre>
 *
 * @param kind     what to draw
 * @param field    the list query field the value filters, a field of the view descriptor
 * @param label    text shown with the control, or null
 * @param options  the options of a {@link Kind#CHOICE}; empty otherwise
 * @param allLabel text of the "no filter" option of a choice, or null when there is none
 * @param preset   a symbolic initial value the client resolves in the user's time zone ({@code TODAY}, {@code THIS_MONTH},
 *                 {@code LAST_MONTH}), or null
 */
public record Control(Kind kind, String field, String label, List<Option> options, String allLabel, String preset) {

    /** The controls there are. */
    public enum Kind {
        /** One date. */
        DATE,
        /** A range of dates. */
        DATE_RANGE,
        /** A range of dates with time. */
        DATETIME_RANGE,
        /** Free text. */
        TEXT,
        /** One of a list of options. */
        CHOICE
    }

    /**
     * One option of a choice.
     *
     * @param key   what the filter sends
     * @param label what the user reads
     */
    public record Option(String key, String label) {
    }

    /** Normalizes {@code options} so it is never null. */
    public Control {
        options = options == null ? List.of() : List.copyOf(options);
    }

    /** @return a date control on {@code field} */
    public static Control date(String field) {
        return new Control(Kind.DATE, field, null, null, null, null);
    }

    /** @return a date range control on {@code field} */
    public static Control dateRange(String field) {
        return new Control(Kind.DATE_RANGE, field, null, null, null, null);
    }

    /** @return a date and time range control on {@code field} */
    public static Control dateTimeRange(String field) {
        return new Control(Kind.DATETIME_RANGE, field, null, null, null, null);
    }

    /** @return a text control on {@code field} */
    public static Control text(String field) {
        return new Control(Kind.TEXT, field, null, null, null, null);
    }

    /**
     * @param field  the field the choice filters
     * @param values the constants of an enum, offered by name
     * @return a choice control
     */
    public static Control choice(String field, Class<? extends Enum<?>> values) {
        return choice(field, Arrays.stream(values.getEnumConstants()).map(e -> new Option(e.name(), e.toString())).toList());
    }

    /**
     * @param field   the field the choice filters
     * @param options the options
     * @return a choice control
     */
    public static Control choice(String field, List<Option> options) {
        return new Control(Kind.CHOICE, field, null, options, null, null);
    }

    /** @return a copy with {@code label} */
    public Control label(String label) {
        return new Control(kind, field, label, options, allLabel, preset);
    }

    /** @return a copy whose choice also offers a "no filter" option with {@code allLabel} */
    public Control allLabel(String allLabel) {
        return new Control(kind, field, label, options, allLabel, preset);
    }

    /** @return a copy that starts with {@code preset} ({@code TODAY}, {@code THIS_MONTH}, {@code LAST_MONTH}) */
    public Control preset(String preset) {
        return new Control(kind, field, label, options, allLabel, preset);
    }
}
