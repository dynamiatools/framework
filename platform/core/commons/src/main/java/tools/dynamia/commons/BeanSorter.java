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
package tools.dynamia.commons;

import java.io.Serializable;
import java.util.List;


/**
 * <p>
 * BeanSorter is a generic utility class for sorting lists of Java beans (POJOs) based on a specified property (column).
 * It uses a {@link DynamicComparator} to perform property-based sorting and supports dynamic changes to the sort column and order (ascending/descending).
 * BeanSorter also provides property change notifications for UI or other listeners via {@link PropertyChangeSupport}.
 * </p>
 *
 * <p>
 * Typical use cases include table/grid sorting in UI frameworks, dynamic data ordering, and reusable sorting logic for collections of beans.
 * </p>
 *
 * <p>
 * Usage example:
 * <pre>
 *     BeanSorter<MyBean> sorter = new BeanSorter<>("name", true);
 *     sorter.sort(myBeanList);
 * </pre>
 * </p>
 *
 * @param <T> the type of bean to be sorted
 * @author Ing. Mario Serrano Leones
 * @since 2009
 */
public class BeanSorter<T> extends PropertyChangeSupport implements Serializable {

    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 961854285707596973L;

    /**
     * Comparator used for sorting beans by property.
     */
    private final DynamicComparator<T> comparator;

    /**
     * Stores the previous column name used for sorting.
     */
    private String oldColumn;

    /**
     * Stores the previous sort order (ascending/descending).
     */
    private boolean oldAscending;

    /**
     * Constructs a BeanSorter with no default sort column.
     * Default sort order is ascending.
     */
    public BeanSorter() {
        this(null);
    }

    /**
     * Constructs a BeanSorter with the specified default sort column.
     * Default sort order is ascending.
     *
     * @param defaultField the property name to sort by initially
     */
    public BeanSorter(String defaultField) {
        this(defaultField, true);
    }

    /**
     * Constructs a BeanSorter with the specified default sort column and order.
     *
     * @param defaultField the property name to sort by initially
     * @param asc          true for ascending order, false for descending
     */
    public BeanSorter(String defaultField, boolean asc) {
        comparator = new DynamicComparator<>(defaultField);
        comparator.setAscending(asc);
    }

    /**
     * Sets the property (column) name to sort by. Fires a property change event if changed.
     *
     * @param column the new property name to sort by
     */
    public void setColumnName(String column) {
        oldColumn = comparator.getField();
        comparator.setField(column);
        firePropertyChange("columnName", oldColumn, column);
    }

    /**
     * Returns the current property (column) name used for sorting.
     *
     * @return the property name
     */
    public String getColumnName() {
        return comparator.getField();
    }

    /**
     * Sets the sort order (ascending/descending). Fires a property change event if changed.
     *
     * @param ascending true for ascending order, false for descending
     */
    public void setAscending(boolean ascending) {
        oldAscending = comparator.isAscending();
        comparator.setAscending(ascending);
        firePropertyChange("ascending", oldAscending, ascending);
    }

    /**
     * Returns true if the current sort order is ascending.
     *
     * @return true if ascending, false if descending
     */
    public boolean isAscending() {
        return comparator.isAscending();
    }

    /**
     * Sorts the given list of beans by the current property and order.
     * Sorting is only performed if the property or order has changed since the last sort.
     *
     * @param data the list of beans to sort
     */
    public void sort(List<T> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        if (oldColumn == null) {
            oldColumn = "";
        }

        if (oldAscending != comparator.isAscending()
                || !oldColumn.equals(comparator.getField())) {
            data.sort(comparator);
            oldAscending = comparator.isAscending();
            oldColumn = comparator.getField();
        }
    }
}
