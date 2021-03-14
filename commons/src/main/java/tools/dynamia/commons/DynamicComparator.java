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
package tools.dynamia.commons;

import java.util.Comparator;

/**
 * Archivo: DynamicComparator.java Fecha Creacion: 4/05/2009
 *
 * @author Ing. Mario Serrano Leones
 * @param <T> the generic type
 */
public class DynamicComparator<T> implements Comparator<T> {

    /**
     * The field.
     */
    private String field;

    /**
     * The ascending.
     */
    private boolean ascending;

    /**
     * Instantiates a new dynamic comparator.
     */
    public DynamicComparator() {
        this(null);
    }

    /**
     * Instantiates a new dynamic comparator.
     *
     * @param field the field
     */
    public DynamicComparator(String field) {
        setField(field);
    }

    public DynamicComparator(String field, boolean ascending) {
        super();
        this.field = field;
        this.ascending = ascending;
    }

    /**
     * Checks if is ascending.
     *
     * @return true, if is ascending
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Sets the ascending.
     *
     * @param ascending the new ascending
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * Gets the field.
     *
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * Sets the field.
     *
     * @param field the new field
     */
    public void setField(String field) {
        if (field != null && field.isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be empty ");
        }

        this.field = field;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
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
     * Gets the value.
     *
     * @param obj the obj
     * @return the value
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
