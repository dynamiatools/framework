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

import java.util.AbstractList;

/**
 * <p>
 * PagedList is a specialized implementation of {@link AbstractList} that provides access to a paginated data source.
 * It delegates all data operations to a {@link PagedListDataSource}, allowing efficient handling of large datasets
 * by loading only the current page into memory. This class is useful for UI components or APIs that require
 * paginated access to data, such as tables or grids.
 * </p>
 *
 * <p>
 * Note: Methods like {@code toArray()} and {@code add(int, T)} operate only on the current page, not the entire dataset.
 * </p>
 *
 * @param <T> the type of elements in this list
 * @author Dynamia Soluciones IT S.A.S
 * @since 2023
 */
public class PagedList<T> extends AbstractList<T> {

    /**
     * The data source providing paginated access to the underlying dataset.
     */
    private final PagedListDataSource<T> dataSource;

    /**
     * Constructs a new {@code PagedList} backed by the specified {@link PagedListDataSource}.
     *
     * @param dataSource the data source to use for pagination and data retrieval
     * @throws NullPointerException if {@code dataSource} is {@code null}
     */
    public PagedList(PagedListDataSource<T> dataSource) {
        super();
        this.dataSource = dataSource;
    }

    /**
     * Returns the element at the specified position in the overall dataset.
     * The element is retrieved from the data source, which may load it from the current page or fetch it as needed.
     *
     * @param index index of the element to return
     * @return the element at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    public T get(int index) {
        return dataSource.getData(index);
    }

    /**
     * Returns the total number of elements in the dataset, not just the current page.
     *
     * @return the total size of the dataset
     */
    @Override
    public int size() {
        return dataSource.getTotalSize();
    }

    /**
     * Removes all elements from the data source, clearing the entire dataset.
     * This operation affects all pages, not just the current one.
     */
    @Override
    public void clear() {
        dataSource.clear();
    }

    /**
     * Returns a string representation of the data source, typically including paging information.
     *
     * @return a string representation of this paged list
     */
    @Override
    public String toString() {
        return dataSource.toString();
    }

    /**
     * Returns an array containing all elements in the current page.
     * Unlike standard {@code List#toArray()}, this does NOT include all elements in the dataset.
     *
     * @return an array containing the elements of the current page
     */
    @Override
    public Object[] toArray() {
        return dataSource.getPageData().toArray();
    }

    /**
     * Returns an array containing all elements in the current page; the runtime type of the returned array is that of the specified array.
     * If the array is too small, a new array of the same runtime type is allocated for this purpose.
     *
     * @param a the array into which the elements of the current page are to be stored, if it is big enough; otherwise, a new array of the same runtime type is allocated
     * @param <U> the runtime type of the array to contain the elements
     * @return an array containing the elements of the current page
     */
    @Override
    public <U> U[] toArray(U[] a) {
        return dataSource.getPageData().toArray(a);
    }

    /**
     * Returns the underlying {@link PagedListDataSource} used by this list.
     *
     * @return the data source backing this paged list
     */
    public PagedListDataSource<T> getDataSource() {
        return dataSource;
    }

    /**
     * Inserts the specified element at the specified position in the current page.
     * Shifts the element currently at that position (if any) and any subsequent elements to the right.
     * This operation only affects the current page, not the entire dataset.
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    public void add(int index, T element) {
        dataSource.getPageData().add(index, element);
    }

}

