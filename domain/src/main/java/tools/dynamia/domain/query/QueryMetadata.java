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
package tools.dynamia.domain.query;

import tools.dynamia.domain.util.QueryBuilder;

import java.io.Serializable;


/**
 * The Class QueryMetadata.
 */
public class QueryMetadata implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The query text.
     */
    private final String queryText;

    /**
     * The query builder.
     */
    private final QueryBuilder queryBuilder;

    /**
     * The query parameters.
     */
    private final QueryParameters queryParameters;

    /**
     * Instantiates a new query metadata.
     *
     * @param queryText the query text
     * @param queryBuilder the query builder
     * @param queryParameters the query parameters
     */
    public QueryMetadata(String queryText, QueryBuilder queryBuilder, QueryParameters queryParameters) {
        this.queryText = queryText;
        this.queryBuilder = queryBuilder;
        this.queryParameters = queryParameters;
    }

    /**
     * Gets the query builder.
     *
     * @return the query builder
     */
    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {
        return queryText;
    }

    /**
     * Gets the parameters.
     *
     * @return the parameters
     */
    public QueryParameters getParameters() {
        return queryParameters;
    }
}
