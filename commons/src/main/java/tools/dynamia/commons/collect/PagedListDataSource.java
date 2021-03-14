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
package tools.dynamia.commons.collect;

import java.util.List;


/**
 * The Interface PagedListDataSource.
 *
 * @param <T> the generic type
 */
public interface PagedListDataSource<T> {

    /**
     * Gets the data.
     *
     * @param index the index
     * @return the data
     */
    T getData(int index);

    /**
     * Gets the total size.
     *
     * @return the total size
     */
    int getTotalSize();

    /**
     * Clear.
     */
    void clear();

    /**
     * Gets the page data.
     *
     * @return the page data
     */
    List<T> getPageData();

    /**
     * Gets the page size.
     *
     * @return the page size
     */
    int getPageSize();

    /**
     * Gets the page count.
     *
     * @return the page count
     */
    int getPageCount();

    /**
     * Gets the active page.
     *
     * @return the active page
     */
    int getActivePage();

    /**
     * Sets the active page.
     *
     * @param activePage the new active page
     */
    void setActivePage(int activePage);

}
