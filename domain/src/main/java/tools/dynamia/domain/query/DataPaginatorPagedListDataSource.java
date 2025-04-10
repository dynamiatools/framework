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

import tools.dynamia.commons.collect.PagedListDataSource;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;

import java.util.List;


/**
 * The Class DataPaginatorPagedListDataSource.
 *
 * @param <T> the generic type
 */
public class DataPaginatorPagedListDataSource<T> implements PagedListDataSource<T> {

    /**
     * The logger.
     */
    protected final LoggingService logger = new SLF4JLoggingService(getClass());

    /**
     * The data paginator.
     */
    private final DataPaginator dataPaginator;

    /**
     * The active page data.
     */
    private List<T> activePageData;

    /**
     * The last page.
     */
    private int lastPage = 1;

    private QueryMetadata queryMetadata;


    /**
     * Instantiates a new abstract data paginator paged list data source.
     *
     * @param dataPaginator the data paginator
     */
    public DataPaginatorPagedListDataSource(DataPaginator dataPaginator) {
        this.dataPaginator = dataPaginator;
    }

    public DataPaginatorPagedListDataSource(DataPaginator dataPaginator, QueryMetadata queryMetadata, List<T> activePageData) {
        this.dataPaginator = dataPaginator;
        this.activePageData = activePageData;
        this.queryMetadata = queryMetadata;
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.PagedListDataSource#getData(int)
     */
    @Override
    public T getData(int index) {
        lastPage = getActivePage();
        int relativeIndex = dataPaginator.scrollToIndex(index);

        loadPage();
        if (relativeIndex == activePageData.size()) {
            relativeIndex--;
        }
        if (activePageData.isEmpty()) {
            return null;
        }
        return activePageData.get(relativeIndex);
    }

    /**
     * Load page.
     */
    private void loadPage() {
        if (lastPage != getActivePage() || activePageData == null) {
            logger.debug("Loading DATA for PAGE " + getActivePage() + ", lastPage " + lastPage + ", activePage " + getActivePage());
            this.activePageData = loadActivePageData();
        }
    }

    /**
     * Load the data for current Active Page.
     *
     * @return the list
     */

    public List<T> loadActivePageData() {
        CrudService crudService = Containers.get().findObject(CrudService.class);
        //noinspection unchecked
        return (List<T>) crudService.find(getQueryMetadata());
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.PagedListDataSource#getTotalSize()
     */
    @Override
    public int getTotalSize() {
        return (int) dataPaginator.getTotalSize();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PageList DataSource:" + dataPaginator;
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.PagedListDataSource#clear()
     */
    @Override
    public void clear() {
        activePageData.clear();
        dataPaginator.setTotalSize(0);

    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.PagedListDataSource#getPageData()
     */
    @Override
    public List<T> getPageData() {
        if (activePageData == null) {
            loadPage();
        }
        return activePageData;
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.PagedListDataSource#getPageSize()
     */
    @Override
    public int getPageSize() {
        return dataPaginator.getPageSize();
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.PagedListDataSource#getPageCount()
     */
    @Override
    public int getPageCount() {
        return dataPaginator.getPagesNumber();
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.PagedListDataSource#getActivePage()
     */
    @Override
    public int getActivePage() {
        return dataPaginator.getPage();
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.PagedListDataSource#setActivePage(int)
     */
    @Override
    public void setActivePage(int activePage) {
        lastPage = getActivePage();
        dataPaginator.setPage(activePage);
        loadPage();
    }

    /**
     * Gets the data paginator.
     *
     * @return the data paginator
     */
    protected DataPaginator getDataPaginator() {
        return dataPaginator;
    }

    public QueryMetadata getQueryMetadata() {
        return queryMetadata;
    }


}
