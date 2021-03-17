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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;


/**
 * Archivo: BeanSorter.java Fecha Creacion: 4/05/2009
 *
 * @author Ing. Mario Serrano Leones
 * @param <T> the generic type
 */
public class BeanSorter<T> implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 961854285707596973L;

    /**
     * The comparator.
     */
    private final DynamicComparator<T> comparator;

    /**
     * The old column.
     */
    private String oldColumn;

    /**
     * The old ascending.
     */
    private boolean oldAscending;

    /**
     * The property change support.
     */
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Instantiates a new bean sorter.
     */
    public BeanSorter() {
        this(null);
    }

    /**
     * Instantiates a new bean sorter.
     *
     * @param defaultField the default field
     */
    public BeanSorter(String defaultField) {
        this(defaultField, true);
    }

    /**
     * Instantiates a new bean sorter.
     *
     * @param defaultField the default field
     * @param asc the asc
     */
    public BeanSorter(String defaultField, boolean asc) {
        comparator = new DynamicComparator<>(defaultField);
        comparator.setAscending(asc);
    }

    /**
     * Sets the column name.
     *
     * @param column the new column name
     */
    public void setColumnName(String column) {
        oldColumn = comparator.getField();
        comparator.setField(column);
        propertyChangeSupport.firePropertyChange("columnName", oldColumn, column);

    }

    /**
     * Gets the column name.
     *
     * @return the column name
     */
    public String getColumnName() {
        return comparator.getField();
    }

    /**
     * Sets the ascending.
     *
     * @param ascending the new ascending
     */
    public void setAscending(boolean ascending) {
        oldAscending = comparator.isAscending();
        comparator.setAscending(ascending);
        propertyChangeSupport.firePropertyChange("ascending", oldAscending, ascending);
    }

    /**
     * Checks if is ascending.
     *
     * @return true, if is ascending
     */
    public boolean isAscending() {
        return comparator.isAscending();
    }

    /**
     * Sort.
     *
     * @param data the data
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

    /**
     * Adds the property change listener.
     *
     * @param listener the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes the property change listener.
     *
     * @param listener the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}
