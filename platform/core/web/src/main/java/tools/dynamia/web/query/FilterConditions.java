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
package tools.dynamia.web.query;

import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.GreaterEqualsThan;
import tools.dynamia.domain.query.LessEqualsThan;
import tools.dynamia.domain.query.QueryCondition;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.viewers.Field;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Turns a {@link QuerySpec.Filter} into a {@link QueryCondition} for a field: the one mapping shared by the REST endpoint
 * and any front end that builds a filter, so they cannot drift. Values are coerced by the type of the field; a date given
 * without time covers the whole day when the field has time. Anything invalid is a {@link ValidationError} (422) naming the
 * field, never ignored.
 */
public final class FilterConditions {

    private enum Edge { START, END, EXACT }

    private FilterConditions() {
    }

    /**
     * @param field  the descriptor field the filter is on
     * @param filter the filter
     * @return the condition
     * @throws ValidationError when the operator does not fit the type of the field or a value is invalid
     */
    public static QueryCondition toCondition(Field field, QuerySpec.Filter filter) {
        var op = QuerySpec.Op.of(filter.op());
        var type = typeOf(field);
        var name = filter.field();
        var value = filter.value();
        try {
            switch (op) {
                case IS_NULL:
                    return value == null || truthy(value) ? QueryConditions.isNull() : QueryConditions.isNotNull();
                case EQ: {
                    var scalar = scalar(name, value);
                    if (hasTime(type) && isDateOnly(scalar)) {
                        return QueryConditions.between(convert(name, type, scalar, Edge.START), convert(name, type, scalar, Edge.END));
                    }
                    return QueryConditions.eq(convert(name, type, scalar, Edge.EXACT));
                }
                case LIKE:
                    if (type != String.class) {
                        throw refuse(name, "like works on text fields only");
                    }
                    return QueryConditions.like(scalar(name, value).toString());
                case IN: {
                    var list = list(name, value, QuerySpecLimits.MAX_IN_VALUES);
                    var converted = new ArrayList<Object>();
                    for (Object item : list) {
                        converted.add(convert(name, type, scalar(name, item), Edge.EXACT));
                    }
                    if (converted.isEmpty()) {
                        throw refuse(name, "in needs at least one value");
                    }
                    return QueryConditions.in(converted);
                }
                case GTE:
                    return atLeast(name, type, convert(name, type, scalar(name, value), Edge.START));
                case LTE:
                    return atMost(name, type, convert(name, type, scalar(name, value), Edge.END));
                case BETWEEN: {
                    var list = list(name, value, 2);
                    if (list.size() != 2) {
                        throw refuse(name, "between needs [from, to]; use null for an open side");
                    }
                    var lo = list.get(0) == null ? null : convert(name, type, scalar(name, list.get(0)), Edge.START);
                    var hi = list.get(1) == null ? null : convert(name, type, scalar(name, list.get(1)), Edge.END);
                    if (lo == null && hi == null) {
                        throw refuse(name, "between needs at least one side");
                    }
                    if (lo == null) {
                        return atMost(name, type, hi);
                    }
                    if (hi == null) {
                        return atLeast(name, type, lo);
                    }
                    if (lo instanceof Comparable c && c.compareTo(hi) > 0) {
                        throw refuse(name, "the end of the range is before its start");
                    }
                    ordered(name, type);
                    return QueryConditions.between(lo, hi);
                }
                default:
                    throw refuse(name, "unsupported operator");
            }
        } catch (ValidationError e) {
            throw e;
        } catch (RuntimeException e) {
            throw refuse(name, "invalid value: " + e.getMessage());
        }
    }

    private static QueryCondition atLeast(String name, Class<?> type, Object value) {
        ordered(name, type);
        if (value instanceof Number n) return new GreaterEqualsThan(n);
        if (value instanceof LocalDate d) return new GreaterEqualsThan(d);
        if (value instanceof LocalDateTime d) return new GreaterEqualsThan(d);
        if (value instanceof Date d) return new GreaterEqualsThan(d);
        throw refuse(name, "gte does not fit this field");
    }

    private static QueryCondition atMost(String name, Class<?> type, Object value) {
        ordered(name, type);
        if (value instanceof Number n) return new LessEqualsThan(n);
        if (value instanceof LocalDate d) return new LessEqualsThan(d);
        if (value instanceof LocalDateTime d) return new LessEqualsThan(d);
        if (value instanceof Date d) return new LessEqualsThan(d);
        throw refuse(name, "lte does not fit this field");
    }

    private static void ordered(String name, Class<?> type) {
        if (type == String.class || type == Boolean.class || type.isEnum()) {
            throw refuse(name, "a range does not fit a field of this type");
        }
    }

    private static Class<?> typeOf(Field field) {
        Class<?> type = field.getFieldClass();
        if (type == null && field.getPropertyInfo() != null) {
            type = field.getPropertyInfo().getType();
        }
        if (type == null) {
            throw new ValidationError("Field " + field.getName() + " has no known type");
        }
        if (type.isPrimitive()) {
            return switch (type.getName()) {
                case "boolean" -> Boolean.class;
                case "int" -> Integer.class;
                case "long" -> Long.class;
                case "double" -> Double.class;
                case "float" -> Float.class;
                case "short" -> Short.class;
                default -> type;
            };
        }
        return type;
    }

    private static boolean hasTime(Class<?> type) {
        return type == LocalDateTime.class || type == Date.class;
    }

    private static boolean isDateOnly(Object value) {
        return value instanceof String s && s.length() == 10;
    }

    private static boolean truthy(Object value) {
        return value instanceof Boolean b ? b : "true".equalsIgnoreCase(String.valueOf(value));
    }

    private static Object scalar(String name, Object value) {
        if (value == null || value instanceof List<?> || value instanceof java.util.Map<?, ?>) {
            throw refuse(name, "a single value is expected");
        }
        return value;
    }

    private static List<?> list(String name, Object value, int max) {
        if (!(value instanceof List<?> list)) {
            throw refuse(name, "a list of values is expected");
        }
        if (list.size() > max) {
            throw refuse(name, "at most " + max + " values are accepted");
        }
        return list;
    }

    private static Object convert(String name, Class<?> type, Object raw, Edge edge) {
        var text = raw.toString().trim();
        try {
            if (type == String.class) return raw.toString();
            if (type == Boolean.class) return raw instanceof Boolean b ? b : Boolean.valueOf(requireBoolean(name, text));
            if (type == Integer.class) return raw instanceof Number n ? n.intValue() : Integer.valueOf(text);
            if (type == Long.class) return raw instanceof Number n ? n.longValue() : Long.valueOf(text);
            if (type == Short.class) return raw instanceof Number n ? n.shortValue() : Short.valueOf(text);
            if (type == Double.class) return raw instanceof Number n ? n.doubleValue() : Double.valueOf(text);
            if (type == Float.class) return raw instanceof Number n ? n.floatValue() : Float.valueOf(text);
            if (type == BigDecimal.class) return new BigDecimal(text);
            if (type == BigInteger.class) return new BigInteger(text);
            if (type.isEnum()) return enumValue(name, type, text);
            if (type == LocalDate.class) return LocalDate.parse(text);
            if (type == LocalDateTime.class) return dateTime(text, edge);
            if (type == Date.class) return Date.from(dateTime(text, edge).atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException | NumberFormatException e) {
            throw refuse(name, "\"" + text + "\" is not a valid " + type.getSimpleName());
        }
        throw refuse(name, "fields of type " + type.getSimpleName() + " cannot be filtered");
    }

    private static String requireBoolean(String name, String text) {
        if (!"true".equalsIgnoreCase(text) && !"false".equalsIgnoreCase(text)) {
            throw refuse(name, "\"" + text + "\" is not a boolean");
        }
        return text;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object enumValue(String name, Class<?> type, String text) {
        for (Object constant : type.getEnumConstants()) {
            if (((Enum) constant).name().equalsIgnoreCase(text)) {
                return constant;
            }
        }
        throw refuse(name, "\"" + text + "\" is not one of " + java.util.Arrays.toString(type.getEnumConstants()));
    }

    private static LocalDateTime dateTime(String text, Edge edge) {
        if (text.length() == 10) {
            var date = LocalDate.parse(text);
            return edge == Edge.END ? date.atTime(LocalTime.MAX) : date.atStartOfDay();
        }
        return LocalDateTime.parse(text);
    }

    private static ValidationError refuse(String field, String message) {
        return new ValidationError("Filter on " + field + ": " + message, null, field, null);
    }
}
