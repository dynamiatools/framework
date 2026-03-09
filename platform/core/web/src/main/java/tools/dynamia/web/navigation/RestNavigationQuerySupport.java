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
package tools.dynamia.web.navigation;

import jakarta.servlet.http.HttpServletRequest;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Utility class that encapsulates all query-building support for the REST navigation layer.
 *
 * <p>Provides three independent, stateless helpers consumed by the read operation class:</p>
 * <ul>
 *   <li>{@link #parseConditions} — static JPQL conditions declared in a {@link ViewDescriptor}</li>
 *   <li>{@link #applyRequestFilters} — dynamic per-field filters from HTTP query parameters</li>
 *   <li>{@link #applyRequestSorting} — dynamic ORDER BY from {@code _sort} / {@code _order} parameters</li>
 * </ul>
 *
 * <p>All methods are {@code static} and {@code public} so they can be reused from outside
 * the package (e.g., custom operation subclasses or tests).</p>
 *
 * @author Mario A. Serrano Leones
 */
public final class RestNavigationQuerySupport {

    /** Reserved parameter names that must never be treated as field filters. */
    private static final Set<String> RESERVED_PARAMS = Set.of("page", "size");

    private RestNavigationQuerySupport() {
        // utility class — no instances
    }

    // -------------------------------------------------------------------------
    // Static conditions from ViewDescriptor
    // -------------------------------------------------------------------------

    /**
     * Applies any JPQL {@code conditions} declared in the {@link ViewDescriptor}'s parameters
     * to the given {@link QueryBuilder}. Each condition string is appended as an AND clause.
     *
     * @param query      the query builder to augment
     * @param descriptor the view descriptor that may contain a {@code conditions} parameter map
     */
    public static void parseConditions(QueryBuilder query, ViewDescriptor descriptor) {
        try {
            if (descriptor != null && descriptor.getParams().containsKey("conditions")) {
                @SuppressWarnings("unchecked")
                Map<String, String> conditions = (Map<String, String>) descriptor.getParams().get("conditions");
                conditions.forEach((k, v) -> query.and(v));
            }
        } catch (Exception e) {
            LoggingService.get(RestNavigationQuerySupport.class).error("Error parsing conditions", e);
        }
    }

    // -------------------------------------------------------------------------
    // Dynamic per-field filters from request parameters
    // -------------------------------------------------------------------------

    /**
     * Applies dynamic field filters derived from HTTP query parameters to the given {@link QueryBuilder}.
     *
     * <p>Any request parameter whose name does not start with {@code _} and is not a reserved
     * pagination keyword ({@code page}, {@code size}) is treated as a potential field filter.
     * The filter value is matched against the entity field registered in the {@link ViewDescriptor}
     * and the appropriate {@link QueryConditions} condition is selected based on the field's Java type:</p>
     *
     * <ul>
     *   <li>{@link String} — {@code LIKE} with auto-searchable wildcard wrapping</li>
     *   <li>{@link Number} / numeric primitives — exact equality ({@code =})</li>
     *   <li>{@link Boolean} / {@code boolean} — exact equality ({@code =})</li>
     *   <li>{@link Enum} subtypes — exact equality ({@code =}) using {@link Enum#valueOf}</li>
     *   <li>Any other type — skipped; not safe to cast without type information</li>
     * </ul>
     *
     * <p>Parameters for fields not present in the descriptor are silently ignored.</p>
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code GET /api/users?name=john&status=ACTIVE&age=30 }</pre>
     *
     * @param request    the current HTTP request carrying the filter parameters
     * @param query      the query builder to augment with filter conditions
     * @param descriptor the view descriptor used to resolve field metadata; if {@code null} this
     *                   method does nothing
     */
    public static void applyRequestFilters(HttpServletRequest request, QueryBuilder query, ViewDescriptor descriptor) {
        if (descriptor == null) {
            return;
        }

        request.getParameterMap().forEach((paramName, values) -> {
            if (paramName.startsWith("_") || RESERVED_PARAMS.contains(paramName) || values.length == 0) {
                return;
            }

            Field field = descriptor.getField(paramName);
            if (field == null) {
                return;
            }

            Class<?> fieldType = field.getFieldClass();
            if (fieldType == null && field.getPropertyInfo() != null) {
                fieldType = field.getPropertyInfo().getType();
            }
            if (fieldType == null) {
                return;
            }

            String rawValue = values[0];
            if (rawValue == null || rawValue.isBlank()) {
                return;
            }

            try {
                if (fieldType == String.class) {
                    query.and(paramName, QueryConditions.like(rawValue));

                } else if (Number.class.isAssignableFrom(fieldType)
                        || (fieldType.isPrimitive() && fieldType != boolean.class)) {
                    Object numericValue = parseNumber(rawValue, fieldType);
                    if (numericValue != null) {
                        query.and(paramName, QueryConditions.eq(numericValue));
                    }

                } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                    boolean boolValue = "true".equalsIgnoreCase(rawValue) || "1".equals(rawValue);
                    query.and(paramName, QueryConditions.eq(boolValue));

                } else if (fieldType.isEnum()) {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    Object enumValue = Enum.valueOf((Class<Enum>) fieldType, rawValue.toUpperCase());
                    query.and(paramName, QueryConditions.eq(enumValue));
                }
                // Date, entity references, collections → skipped (require richer handling)
            } catch (Exception e) {
                LoggingService.get(RestNavigationQuerySupport.class)
                        .warn("Ignoring filter param '" + paramName + "=" + rawValue + "': " + e.getMessage());
            }
        });
    }

    // -------------------------------------------------------------------------
    // Dynamic ORDER BY from request parameters
    // -------------------------------------------------------------------------

    /**
     * Applies dynamic ordering to the given {@link QueryBuilder} based on the HTTP request
     * parameters {@code _sort} and {@code _order}.
     *
     * <p>Supported parameters:</p>
     * <ul>
     *   <li>{@code _sort} — comma-separated list of field names (e.g., {@code name,age}).</li>
     *   <li>{@code _order} — comma-separated list of directions ({@code asc}/{@code desc}).
     *       When fewer directions than fields are given, the last direction is reused.
     *       Defaults to {@code asc}.</li>
     * </ul>
     *
     * <p>Each field is validated against the {@link ViewDescriptor} when one is provided;
     * unknown fields are silently skipped to prevent JPQL injection.</p>
     *
     * <p><b>Usage examples:</b></p>
     * <pre>{@code
     * GET /api/users?_sort=name              → ORDER BY e.name ASC
     * GET /api/users?_sort=name&_order=desc  → ORDER BY e.name DESC
     * GET /api/users?_sort=name,age&_order=asc,desc → ORDER BY e.name ASC, e.age DESC
     * }</pre>
     *
     * @param request    the current HTTP request
     * @param query      the query builder to augment with ORDER BY clauses
     * @param descriptor the view descriptor used to validate field names; if {@code null},
     *                   field names are accepted as-is
     */
    public static void applyRequestSorting(HttpServletRequest request, QueryBuilder query, ViewDescriptor descriptor) {
        String sortParam = request.getParameter("_sort");
        if (sortParam == null || sortParam.isBlank()) {
            return;
        }

        String orderParam = request.getParameter("_order");
        String[] fields = sortParam.split(",");
        String[] directions = (orderParam != null && !orderParam.isBlank())
                ? orderParam.split(",")
                : new String[0];

        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i].strip();
            if (fieldName.isEmpty()) {
                continue;
            }

            if (descriptor != null && descriptor.getField(fieldName) == null) {
                LoggingService.get(RestNavigationQuerySupport.class)
                        .warn("Ignoring unknown sort field '" + fieldName + "'");
                continue;
            }

            String rawDirection = (directions.length > 0)
                    ? directions[Math.min(i, directions.length - 1)].strip()
                    : "asc";
            String direction = "desc".equalsIgnoreCase(rawDirection) ? "DESC" : "ASC";

            query.orderBy(fieldName + " " + direction);
        }
    }

    // -------------------------------------------------------------------------
    // Pagination param reader
    // -------------------------------------------------------------------------

    /**
     * Reads an integer request parameter by name. Returns {@code 0} if the parameter
     * is absent or cannot be parsed as an integer.
     *
     * @param request the current HTTP request
     * @param name    the name of the request parameter
     * @return the parsed integer value, or {@code 0} if not present / invalid
     */
    public static int getParameterNumber(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return 0;
    }

    // -------------------------------------------------------------------------
    // Internal numeric parser
    // -------------------------------------------------------------------------

    /**
     * Parses a raw string value into the target numeric type.
     *
     * <p>Supports {@link Integer}, {@code int}, {@link Long}, {@code long},
     * {@link Double}, {@code double}, {@link Float}, {@code float},
     * {@link Short}, {@code short}, {@link Byte}, {@code byte},
     * and {@link BigDecimal}.</p>
     *
     * @param raw        the raw string to parse
     * @param targetType the target numeric {@link Class}
     * @return the parsed number, or {@code null} if the type is not supported
     */
    static Object parseNumber(String raw, Class<?> targetType) {
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(raw);
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(raw);
        if (targetType == Double.class || targetType == double.class) return Double.parseDouble(raw);
        if (targetType == Float.class || targetType == float.class) return Float.parseFloat(raw);
        if (targetType == Short.class || targetType == short.class) return Short.parseShort(raw);
        if (targetType == Byte.class || targetType == byte.class) return Byte.parseByte(raw);
        if (targetType == BigDecimal.class) return new BigDecimal(raw);
        return null;
    }
}


