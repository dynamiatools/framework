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

import java.io.Serializable;


/**
 * The Class DataPaginator.
 *
 * @author Mario Serrano Leones
 */
public final class DataPaginator implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 9017651240609317436L;

    /**
     * The total size.
     */
    private long totalSize;

    /**
     * The page size.
     */
    private int pageSize = 30;

    /**
     * The first result.
     */
    private int firstResult;

    /**
     * The page.
     */
    private int page = 1;

    /**
     * The pages number.
     */
    private int pagesNumber;

    /**
     * Instantiates a new data paginator.
     *
     * @param pageSize the page size
     */
    public DataPaginator(int pageSize) {
        setPageSize(pageSize);
    }

    /**
     * Instantiates a new data paginator.
     *
     * @param totalSize the total size
     * @param pageSize the page size
     * @param page the page
     */
    public DataPaginator(long totalSize, int pageSize, int page) {
        setPageSize(pageSize);
        setTotalSize(totalSize);
        setPage(page);
    }

    /**
     * Instantiates a new data paginator.
     */
    public DataPaginator() {
    }

    /**
     * Gets the page size.
     *
     * @return the page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets the page size.
     *
     * @param pageSize the new page size
     */
    public void setPageSize(int pageSize) {
        if (pageSize > 0) {
            this.pageSize = pageSize;
        }
    }

    /**
     * Gets the page.
     *
     * @return the page
     */
    public int getPage() {
        return page;
    }

    /**
     * Sets the page.
     *
     * @param page the new page
     */
    public void setPage(int page) {
        if (page > 0 && page <= getPagesNumber()) {
            this.page = page;
            recalculate();
        }
    }

    /**
     * Gets the total size.
     *
     * @return the total size
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * Sets the total size.
     *
     * @param totalSize the new total size
     */
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;

        pagesNumber = (int) (((double) totalSize) / ((double) pageSize));
        if (pagesNumber * pageSize < totalSize) {
            pagesNumber++;
        }
    }

    /**
     * Gets the pages number.
     *
     * @return the pages number
     */
    public int getPagesNumber() {
        return pagesNumber;
    }

    /**
     * Gets the first result.
     *
     * @return the first result
     */
    public int getFirstResult() {
        return firstResult;
    }

    /**
     * Next page.
     */
    public void nextPage() {
        setPage(getPage() + 1);
    }

    /**
     * Previous page.
     */
    public void previousPage() {
        setPage(getPage() - 1);
    }

    /**
     * First page.
     */
    public void firstPage() {
        setPage(1);
    }

    /**
     * Last page.
     */
    public void lastPage() {
        setPage(getPagesNumber());
    }

    /**
     * Reset.
     */
    public void reset() {
        firstResult = 0;
        page = 1;
        totalSize = 0;
        pagesNumber = 0;
    }

    /**
     * Change the current page to page that have the element with the
     * absoluteIndex.
     *
     * @param absoluteIndex the absolute index
     * @return relativeIndex
     */
    public int scrollToIndex(int absoluteIndex) {
        if (absoluteIndex < 0) {
            throw new IndexOutOfBoundsException(absoluteIndex + " is negative ");
        }

        int ps = getPageSize();

        int newPage = absoluteIndex / ps + 1; // new page
        int relativeIndex = absoluteIndex - (ps * newPage - ps); // relative index

        if (newPage > getPagesNumber()) {
            throw new IndexOutOfBoundsException(absoluteIndex + " index not found in current selection");
        }

        setPage(newPage);
        return relativeIndex;
    }

    /**
     * Recalculate.
     */
    private void recalculate() {
        if (page == 1) {
            firstResult = 0;
        } else {
            firstResult = (page - 1) * pageSize;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DataPaginator{" + "totalResult=" + totalSize + ", pageSize=" + pageSize + ", firstResult=" + firstResult + ", page=" + page + ", pagesNumber=" + pagesNumber + '}';
    }
}
