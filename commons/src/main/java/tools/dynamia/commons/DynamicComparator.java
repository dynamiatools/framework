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

import java.util.Comparator;

/**
 * <p>
 * DynamicComparator is a generic {@link Comparator} implementation that enables dynamic sorting of Java beans (POJOs)
 * based on a specified property (field) name. It uses reflection to access the property value and supports both ascending and descending order.
 * </p>
 *
 * <p>
 * This comparator is useful for sorting lists of beans in UI tables, grids, or any scenario where the sort field and order may change at runtime.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 *     DynamicComparator<MyBean> comparator = new DynamicComparator<>("name", true);
 *     Collections.sort(myBeanList, comparator);
 * </pre>
 * </p>
 *
 * <p>
 * Note: The property to compare must be accessible via a getter method and implement {@link Comparable}.
 * </p>
 *
 * @param <T> the type of objects to compare
 * @author Ing. Mario Serrano Leones
 * @since 2009
 */
public class DynamicComparator<T> implements Comparator<T> {

    /**
     * The property (field) name to compare.
     */
    private String field;

    /**
     * Indicates if sorting is ascending (true) or descending (false).
     */
    private boolean ascending;

    /**
     * Constructs a DynamicComparator with no default field. Field must be set before use.
     * Default order is ascending.
     */
    public DynamicComparator() {
        this(null);
    }

    /**
     * Constructs a DynamicComparator for the specified field. Default order is ascending.
     *
     * @param field the property name to compare
     */
    public DynamicComparator(String field) {
        setField(field);
    }

    /**
     * Constructs a DynamicComparator for the specified field and order.
     *
     * @param field the property name to compare
     * @param ascending true for ascending order, false for descending
     */
    public DynamicComparator(String field, boolean ascending) {
        super();
        this.field = field;
        this.ascending = ascending;
    }

    /**
     * Returns true if sorting is ascending, false if descending.
     *
     * @return true if ascending, false if descending
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Sets the sort order.
     *
     * @param ascending true for ascending, false for descending
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * Returns the property (field) name used for comparison.
     *
     * @return the property name
     */
    public String getField() {
        return field;
    }

    /**
     * Sets the property (field) name to use for comparison.
     *
     * @param field the property name
     * @throws IllegalArgumentException if field is empty
     */
    public void setField(String field) {
        if (field != null && field.isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be empty ");
        }
        this.field = field;
    }

    /**
     * Compares two objects by the configured property value, using reflection.
     * Handles null values and applies the configured sort order.
     *
     * @param o1 the first object to compare
     * @param o2 the second object to compare
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second
     * @throws RuntimeException if reflection fails or property is not Comparable
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public int compare(T o1, T o2) {
        int result = 0;
        Comparable value1 = getValue(o1);
        Comparable value2 = getValue(o2);
        if (value1 != null && value2 == null) {
            result = 1;
        } else if (value1 == null && value2 != null) {
            result = -1;
        } else if (value1 == null && value2 == null) {
            result = 0;
        } else {
            result = value1.compareTo(value2);
        }
        if (!isAscending()) {
            if (result < 0) {
                result = 1;
            } else if (result > 0) {
                result = -1;
            }
        }
        return result;
    }

    /**
     * Retrieves the value of the configured property from the given object using reflection.
     * Only values implementing {@link Comparable} are considered for comparison.
     *
     * @param obj the object from which to retrieve the property value
     * @return the property value as Comparable, or null if not found or not Comparable
     * @throws RuntimeException if reflection fails
     */
    @SuppressWarnings("rawtypes")
    private Comparable getValue(T obj) {
        Comparable value = null;
        if (obj != null && field != null) {
            try {
                Object result = BeanUtils.invokeGetMethod(obj, field);
                if (result instanceof Comparable) {
                    value = (Comparable) result;
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return value;
    }
}
