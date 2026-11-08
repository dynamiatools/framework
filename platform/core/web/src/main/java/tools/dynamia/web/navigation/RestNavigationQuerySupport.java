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
import tools.dynamia.commons.reflect.AccessMode;
import tools.dynamia.domain.query.CustomQueryCondition;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;

import java.util.ArrayList;
import java.util.Map;

/**
 * Utility class that encapsulates all query-building support for the REST navigation layer.
 *
 * <p>Provides independent, stateless helpers consumed by the read operation class:</p>
 * <ul>
 *   <li>{@link #parseConditions} — static JPQL conditions declared in a {@link ViewDescriptor}</li>
 *   <li>{@link #applyRequestSearch} — free text search from the {@code _q} parameter (field filters go through
 *       {@code POST /find} with a {@code QuerySpec})</li>
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
    // Free text search from the _q request parameter
    // -------------------------------------------------------------------------

    /** Request parameter with the text to search in every visible text field of the descriptor. */
    public static final String SEARCH_PARAM = "_q";

    private static final String SEARCH_QUERY_PARAM = "searchText";

    /**
     * Applies the free text search of the {@code _q} request parameter, the REST counterpart of the CRUD {@code FindAction}:
     * the entities where <b>any</b> visible, writable {@link String} field of the descriptor contains the text, ignoring case.
     * The group is combined with the other conditions using AND. Nothing is added when the parameter is missing or blank, or
     * when the descriptor has no such field.
     *
     * <pre>{@code GET /api/store/books?_q=dune&year=1965 }</pre>
     *
     * @param request    the current HTTP request
     * @param query      the query builder to augment
     * @param descriptor the view descriptor whose visible fields are searched; if {@code null} this method does nothing
     * @param alias      the alias of the entity in the query
     */
    public static void applyRequestSearch(HttpServletRequest request, QueryBuilder query, ViewDescriptor descriptor, String alias) {
        applySearch(request.getParameter(SEARCH_PARAM), query, descriptor, alias);
    }

    /**
     * Same as {@link #applyRequestSearch} with the text given, so a {@code QuerySpec} can use it too.
     *
     * @param text       the text to search, ignored when null or blank
     * @param query      the query builder to augment
     * @param descriptor the view descriptor whose visible fields are searched; if {@code null} this method does nothing
     * @param alias      the alias of the entity in the query
     */
    public static void applySearch(String text, QueryBuilder query, ViewDescriptor descriptor, String alias) {
        if (descriptor == null || text == null || text.isBlank()) {
            return;
        }

        var conditions = new ArrayList<String>();
        for (Field field : descriptor.getFields()) {
            if (field.isVisible() && field.getFieldClass() == String.class && field.getPropertyInfo() != null
                    && field.getPropertyInfo().getAccessMode() == AccessMode.READ_WRITE) {
                conditions.add("lower(" + alias + "." + field.getName() + ") like :" + SEARCH_QUERY_PARAM);
            }
        }
        if (conditions.isEmpty()) {
            return;
        }

        // A plain query parameter named "searchText" would also become an "e.searchText like ..." condition, which fails (or
        // silently filters) depending on whether the entity happens to have such an attribute. A custom condition renders the
        // OR group as is and binds the value itself, so the entity model plays no part.
        String pattern = "%" + text.trim().toLowerCase() + "%";
        query.getQueryParameters().add(SEARCH_PARAM, new CustomQueryCondition("(" + String.join(" or ", conditions) + ")",
                q -> q.setParameter(SEARCH_QUERY_PARAM, pattern)));
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
}
