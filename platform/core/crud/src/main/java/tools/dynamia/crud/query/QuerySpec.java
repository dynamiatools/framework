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

import java.util.ArrayList;
import java.util.List;

/**
 * The standard message for asking a list of entities: filters, text search, sort and paging. It is plain data that travels
 * as JSON (`POST /api/{path}/find`), so a client, a saved view and a server filter all speak the same vocabulary. The
 * operators mirror the factories of {@code QueryConditions}, but no {@code QueryCondition} object ever crosses the network:
 * the server maps each {@link Filter} to one after checking the field against the view descriptor.
 *
 * <pre>{@code
 * {
 *   "version": 1,
 *   "filters": [
 *     {"field": "estado", "op": "in", "value": ["OPEN", "PAID"]},
 *     {"field": "creationDate", "op": "between", "value": ["2026-01-01", "2026-01-31"]}
 *   ],
 *   "search": "dune",
 *   "sort": [{"field": "creationDate", "dir": "desc"}],
 *   "page": 1,
 *   "size": 20
 * }
 * }</pre>
 *
 * @param version the message version; only {@code 1} exists
 * @param filters conditions combined with AND; at most one per field
 * @param search  text to look for in the visible text fields of the descriptor, or null
 * @param sort    order of the result
 * @param page    1-based page, or null for the first
 * @param size    page size, or null for the default
 */
public record QuerySpec(Integer version, List<Filter> filters, String search, List<Sort> sort, Integer page, Integer size) {

    /** Current version of the message. */
    public static final int VERSION = 1;

    /**
     * One condition on one field.
     *
     * @param field name of a field of the view descriptor
     * @param op    one of {@link Op#names()}: eq, like, between, gte, lte, in, isNull
     * @param value a scalar, or a list for {@code in} and {@code between} (a null side of a range is open); for
     *              {@code isNull} a boolean, true when missing
     */
    public record Filter(String field, String op, Object value) {
    }

    /**
     * One sort key.
     *
     * @param field name of a field of the view descriptor
     * @param dir   {@code asc} (default) or {@code desc}
     */
    public record Sort(String field, String dir) {
    }

    /** Operators of a {@link Filter}. */
    public enum Op {
        EQ("eq"), LIKE("like"), BETWEEN("between"), GTE("gte"), LTE("lte"), IN("in"), IS_NULL("isNull");

        private final String wire;

        Op(String wire) {
            this.wire = wire;
        }

        /** @return the name used in the message */
        public String wire() {
            return wire;
        }

        /** @return the names of every operator, as they appear in the message */
        public static List<String> names() {
            return java.util.Arrays.stream(values()).map(Op::wire).toList();
        }

        /**
         * @param name the name used in the message
         * @return the operator
         * @throws tools.dynamia.domain.ValidationError when there is no such operator
         */
        public static Op of(String name) {
            for (Op op : values()) {
                if (op.wire.equals(name)) {
                    return op;
                }
            }
            throw new tools.dynamia.domain.ValidationError("Unknown filter operator \"" + name + "\", expected one of " + names());
        }
    }

    /** Normalizes the lists so no accessor returns null. */
    public QuerySpec {
        filters = filters == null ? List.of() : List.copyOf(filters);
        sort = sort == null ? List.of() : List.copyOf(sort);
    }

    /** @return an empty query: no filters, no search, default order and paging */
    public static QuerySpec empty() {
        return new QuerySpec(VERSION, List.of(), null, List.of(), null, null);
    }

    /**
     * @param field name of a field
     * @return a copy without the filters on {@code field}
     */
    public QuerySpec without(String field) {
        return new QuerySpec(version, filters.stream().filter(f -> !java.util.Objects.equals(f.field(), field)).toList(),
                search, sort, page, size);
    }

    /**
     * @param filter the filter to add
     * @return a copy where {@code filter} replaces any other on its field
     */
    public QuerySpec with(Filter filter) {
        var list = new ArrayList<>(without(filter.field()).filters());
        list.add(filter);
        return new QuerySpec(version, list, search, sort, page, size);
    }

    /**
     * @param field name of a field
     * @return true when a filter on {@code field} exists
     */
    public boolean filters(String field) {
        return filters.stream().anyMatch(f -> java.util.Objects.equals(f.field(), field));
    }
}
