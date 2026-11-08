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

/** Bounds of a {@link QuerySpec}, so a client cannot ask for an unbounded query. */
public final class QuerySpecLimits {

    /** Most filters one query may carry. */
    public static final int MAX_FILTERS = 20;
    /** Most values of an {@code in} filter. */
    public static final int MAX_IN_VALUES = 100;
    /** Most sort keys. */
    public static final int MAX_SORT = 5;
    /** Longest search text. */
    public static final int MAX_SEARCH_LENGTH = 200;
    /** Page size when the query has none. */
    public static final int DEFAULT_SIZE = 50;
    /** Largest page size. */
    public static final int MAX_SIZE = 500;

    /**
     * Fields a client can never filter: other code scopes queries with them (the SaaS listener adds the current account
     * unless the parameters already hold one), so a client value would switch the scoping off.
     */
    public static final java.util.Set<String> RESERVED_FIELDS = java.util.Set.of("accountId");

    private QuerySpecLimits() {
    }
}
