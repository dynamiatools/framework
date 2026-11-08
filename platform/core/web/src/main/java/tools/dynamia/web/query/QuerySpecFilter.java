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

/**
 * Hook that sees every {@link QuerySpec} before it becomes a query, to change it or refuse it. Register one as a bean: the
 * processor finds them all and runs them by {@link #order()}, like an {@code ActionFilter}.
 *
 * <pre>{@code
 * @Component
 * class NoAccountFilter implements QuerySpecFilter {
 *     public QuerySpec filter(QueryContext context, QuerySpec spec) {
 *         return spec.without("accountId");                 // drop what the client may not choose
 *     }
 * }
 * }</pre>
 *
 * A filter refuses a query by throwing {@code ValidationError} (422) or {@code NavigationNotAllowedException} (403), and it
 * may add a condition the client cannot override with {@link QuerySpec#with}. A filter only narrows what the view descriptor
 * allows: after the chain every field is checked against the descriptor again.
 */
public interface QuerySpecFilter {

    /** @return position in the chain, lower first; default 0 */
    default int order() {
        return 0;
    }

    /**
     * @param context what is being queried
     * @param spec    the query so far
     * @return the query to continue with; never null
     */
    QuerySpec filter(QueryContext context, QuerySpec spec);
}
