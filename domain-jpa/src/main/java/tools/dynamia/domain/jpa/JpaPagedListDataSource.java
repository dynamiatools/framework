/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
package tools.dynamia.domain.jpa;

import tools.dynamia.domain.query.DataPaginatorPagedListDataSource;
import tools.dynamia.domain.query.QueryMetadata;

import java.util.List;


/**
 * The Class JPAPagedListDataSource.
 *
 * @param <T> the generic type
 */
public class JpaPagedListDataSource<T> extends DataPaginatorPagedListDataSource<T> {


    /**
     * Instantiates a new JPA paged list data source.
     *
     * @param queryMetadata   the query metadata
     * @param currentPageData the current page data
     */
    public JpaPagedListDataSource(QueryMetadata queryMetadata, List<T> currentPageData) {
        super(queryMetadata.getParameters().getPaginator(), queryMetadata, currentPageData);

    }



}
