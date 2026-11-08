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

import tools.dynamia.crud.query.QuerySpec;
import tools.dynamia.crud.query.FilterConditions;
import tools.dynamia.crud.query.QuerySpecLimits;

import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.web.navigation.RestNavigationQuerySupport;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * The one place where a {@link QuerySpec} becomes a query, so no operation can skip a rule: limits, then the
 * {@link QuerySpecFilter}s, then the view descriptor as the whitelist of fields, then the conditions.
 *
 * <pre>{@code
 * var checked = QuerySpecProcessor.filter(context, spec);
 * QuerySpecProcessor.apply(checked, query, descriptor, "e");
 * }</pre>
 */
public final class QuerySpecProcessor {

    private QuerySpecProcessor() {
    }

    /**
     * Checks the limits and runs the {@link QuerySpecFilter} beans, by order.
     *
     * @param context what is queried
     * @param spec    the query as the client sent it
     * @return the query to run
     * @throws ValidationError when the query is over a limit
     */
    public static QuerySpec filter(QueryContext context, QuerySpec spec) {
        List<QuerySpecFilter> filters = Containers.get() == null ? List.of()
                : Containers.get().findObjects(QuerySpecFilter.class).stream()
                .sorted(Comparator.comparingInt(QuerySpecFilter::order)).toList();
        return filter(context, spec, filters);
    }

    /**
     * Same as {@link #filter(QueryContext, QuerySpec)} with the filters given.
     *
     * @param context what is queried
     * @param spec    the query as the client sent it
     * @param filters the chain, already ordered
     * @return the query to run
     */
    public static QuerySpec filter(QueryContext context, QuerySpec spec, List<QuerySpecFilter> filters) {
        if (spec == null) {
            spec = QuerySpec.empty();
        }
        if (spec.version() != null && spec.version() != QuerySpec.VERSION) {
            throw new ValidationError("Unsupported query version " + spec.version() + ", expected " + QuerySpec.VERSION);
        }
        checkLimits(spec);
        for (QuerySpecFilter filter : filters) {
            var result = filter.filter(context, spec);
            if (result == null) {
                throw new IllegalStateException(filter.getClass().getName() + " returned a null query");
            }
            spec = result;
        }
        checkLimits(spec);
        return spec;
    }

    /**
     * Adds the filters, search and sort of {@code spec} to {@code query}. Every field must be one of the descriptor.
     *
     * @param spec       the query already passed through {@link #filter}
     * @param query      the query to augment
     * @param descriptor the whitelist of fields
     * @param alias      alias of the entity in the query
     * @throws ValidationError when a field is not in the descriptor, is repeated, or a value is invalid
     */
    public static void apply(QuerySpec spec, QueryBuilder query, ViewDescriptor descriptor, String alias) {
        var seen = new HashSet<String>();
        for (var filter : spec.filters()) {
            Field field = field(descriptor, filter.field());
            if (!seen.add(filter.field())) {
                throw new ValidationError("Filter on " + filter.field() + ": only one filter per field is accepted", null, filter.field(), null);
            }
            query.and(field.getName(), FilterConditions.toCondition(field, filter));
        }
        if (spec.search() != null && !spec.search().isBlank()) {
            RestNavigationQuerySupport.applySearch(spec.search(), query, descriptor, alias);
        }
        for (var sort : spec.sort()) {
            Field field = field(descriptor, sort.field());
            query.orderBy(field.getName() + ("desc".equalsIgnoreCase(sort.dir()) ? " DESC" : " ASC"));
        }
    }

    /**
     * @param spec the query
     * @return the page size to use, bounded by {@link QuerySpecLimits}
     */
    public static int pageSize(QuerySpec spec) {
        return spec.size() == null ? QuerySpecLimits.DEFAULT_SIZE : spec.size();
    }

    private static Field field(ViewDescriptor descriptor, String name) {
        Field field = name == null || descriptor == null ? null : descriptor.getField(name);
        if (field == null) {
            throw new ValidationError("Unknown field " + name, null, name, null);
        }
        return field;
    }

    private static void checkLimits(QuerySpec spec) {
        if (spec.filters().size() > QuerySpecLimits.MAX_FILTERS) {
            throw new ValidationError("At most " + QuerySpecLimits.MAX_FILTERS + " filters are accepted");
        }
        if (spec.sort().size() > QuerySpecLimits.MAX_SORT) {
            throw new ValidationError("At most " + QuerySpecLimits.MAX_SORT + " sort keys are accepted");
        }
        if (spec.search() != null && spec.search().length() > QuerySpecLimits.MAX_SEARCH_LENGTH) {
            throw new ValidationError("The search text is longer than " + QuerySpecLimits.MAX_SEARCH_LENGTH);
        }
        if (spec.size() != null && (spec.size() < 1 || spec.size() > QuerySpecLimits.MAX_SIZE)) {
            throw new ValidationError("size must be between 1 and " + QuerySpecLimits.MAX_SIZE);
        }
        if (spec.page() != null && spec.page() < 1) {
            throw new ValidationError("page must be 1 or more");
        }
    }
}
