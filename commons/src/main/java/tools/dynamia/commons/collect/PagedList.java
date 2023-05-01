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
 * The Class PagedList.
 *
 * @param <T> the generic type
 */
public class PagedList<T> extends AbstractList<T> {

    /**
     * The data source.
     */
    private final PagedListDataSource<T> dataSource;

    /**
     * Instantiates a new paged list.
     *
     * @param dataSource the data source
     */
    public PagedList(PagedListDataSource<T> dataSource) {
        super();
        this.dataSource = dataSource;
    }

    /* (non-Javadoc)
	 * @see java.util.AbstractList#get(int)
     */
    @Override
    public T get(int index) {
        return dataSource.getData(index);
    }

    /* (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return dataSource.getTotalSize();
    }

    /* (non-Javadoc)
	 * @see java.util.AbstractList#clear()
     */
    @Override
    public void clear() {
        dataSource.clear();
    }

    /* (non-Javadoc)
	 * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        return dataSource.toString();
    }


    /**
     * Create and return an array with CURRENT page data. NOT ALL DATA like a normal List
     * @return array
     */
    @Override
    public Object[] toArray() {
        return dataSource.getPageData().toArray();
    }

    /**
     * Create and return an array with CURRENT page data. NOT ALL DATA like a normal List
     * @return array
     */
    @Override
    public <U> U[] toArray(U[] a) {
        return dataSource.getPageData().toArray(a);
    }

    /**
     * Gets the data source.
     *
     * @return the data source
     */
    public PagedListDataSource<T> getDataSource() {
        return dataSource;
    }

    /* (non-Javadoc)
	 * @see java.util.AbstractList#add(int, java.lang.Object)
     */
    @Override
    public void add(int index, T element) {
        dataSource.getPageData().add(index, element);
    }



}
