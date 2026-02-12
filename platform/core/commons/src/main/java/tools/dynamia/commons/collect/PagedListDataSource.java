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
package tools.dynamia.commons.collect;

import java.util.List;

/**
 * <p>
 * PagedListDataSource is an interface that defines the contract for providing paginated access to a data set.
 * Implementations are responsible for managing the underlying data, pagination logic, and page navigation.
 * This interface is designed to support efficient handling of large data sets by exposing only the relevant page data.
 * </p>
 *
 * <p>
 * Typical use cases include UI components (tables, grids) or APIs that require paginated data access and navigation.
 * </p>
 *
 * @param <T> the type of elements in the data source
 * @author Dynamia Soluciones IT S.A.S
 * @since 2023
 */
public interface PagedListDataSource<T> {

    /**
     * Returns the element at the specified index in the entire data set (not just the current page).
     *
     * @param index the index of the element to retrieve (0-based)
     * @return the element at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    T getData(int index);

    /**
     * Returns the total number of elements in the data set (all pages).
     *
     * @return the total size of the data set
     */
    int getTotalSize();

    /**
     * Removes all elements from the data source, clearing the entire data set.
     */
    void clear();

    /**
     * Returns a list containing all elements in the current active page.
     *
     * @return a list of elements in the current page
     */
    List<T> getPageData();

    /**
     * Returns the size of each page (number of elements per page).
     *
     * @return the page size
     */
    int getPageSize();

    /**
     * Returns the total number of pages available in the data set.
     *
     * @return the total page count
     */
    int getPageCount();

    /**
     * Returns the index of the currently active page (0-based).
     *
     * @return the active page index
     */
    int getActivePage();

    /**
     * Sets the active page to the specified index.
     * Implementations should update the current page and ensure the index is within valid bounds.
     *
     * @param activePage the new active page index (0-based)
     * @throws IndexOutOfBoundsException if the page index is out of range
     */
    void setActivePage(int activePage);

}
