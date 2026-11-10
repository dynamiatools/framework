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
package tools.dynamia.zk.crud;

import tools.dynamia.commons.BeanMap;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.collect.PagedList;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.zk.viewers.table.TableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Helpers for CRUD actions that work with the table of the list and with the rows the user marked with the
 * {@code selected} checkbox column. They work the same with entities and with read-only {@link BeanMap} rows (Open
 * Persistence In View disabled): rows are returned as real entities.
 *
 * <pre>{@code
 * List<Venta> ventas = CrudTables.checkedRows(evt, Venta.class);
 * }</pre>
 */
public final class CrudTables {

    /**
     * Name of the property the checkbox column reads and writes.
     */
    public static final String SELECTED = "selected";

    private CrudTables() {
    }

    /**
     * The table that shows the list of the CRUD of the event.
     *
     * @param evt the action event
     * @return the table, or empty if the event has no CRUD view or the list is not a table
     */
    public static Optional<TableView> tableOf(CrudActionEvent evt) {
        if (evt != null && evt.getCrudView() instanceof CrudView<?> crudView && crudView.getDataSetView() instanceof TableView table) {
            return Optional.of(table);
        }
        return Optional.empty();
    }

    /**
     * The rows of the table of the event that are marked as selected, as entities of the given type. Only the current
     * page is read when the list is paginated.
     *
     * @param evt  the action event
     * @param type the entity type
     * @param <T>  the entity type
     * @return the marked entities, empty if there is no table or nothing is marked
     */
    public static <T> List<T> checkedRows(CrudActionEvent evt, Class<T> type) {
        return tableOf(evt).map(table -> checkedRows(table.getValue(), type)).orElseGet(List::of);
    }

    /**
     * Marks or unmarks every row of the table of the event and refreshes the table.
     *
     * @param evt     the action event
     * @param checked the new state
     * @return the number of rows changed
     */
    public static int setAllChecked(CrudActionEvent evt, boolean checked) {
        return tableOf(evt).map(table -> {
            int count = setChecked(table.getValue(), checked);
            table.setValue(table.getValue());
            table.invalidate();
            return count;
        }).orElse(0);
    }

    /**
     * The rows of a list that are marked as selected, as entities of the given type. A {@link BeanMap} row is
     * loaded by id with {@link EntityMapperSupport#toEntity(Object)}.
     *
     * @param rows the list value (entities, BeanMaps or a {@link PagedList})
     * @param type the entity type
     * @param <T>  the entity type
     * @return the marked entities
     */
    public static <T> List<T> checkedRows(Object rows, Class<T> type) {
        List<T> checked = new ArrayList<>();
        for (Object row : PagedList.pageData(rows)) {
            if (BeanMap.isBeanOf(row, type) && isChecked(row)) {
                Object entity = EntityMapperSupport.toEntity(row);
                if (type.isInstance(entity)) {
                    checked.add(type.cast(entity));
                }
            }
        }
        return checked;
    }

    /**
     * Marks or unmarks every row of a list, entities and BeanMaps alike.
     *
     * @param rows    the list value (entities, BeanMaps or a {@link PagedList})
     * @param checked the new state
     * @return the number of rows changed
     */
    public static int setChecked(Object rows, boolean checked) {
        int count = 0;
        for (Object row : PagedList.pageData(rows)) {
            if (row instanceof BeanMap map) {
                map.setSelected(checked);
                count++;
            } else if (row != null && ObjectOperations.hasProperty(row, SELECTED)) {
                ObjectOperations.invokeSetMethod(row, SELECTED, checked);
                count++;
            }
        }
        return count;
    }

    private static boolean isChecked(Object row) {
        if (row instanceof BeanMap map) {
            return map.isSelected();
        }
        return ObjectOperations.hasProperty(row, SELECTED) && Boolean.TRUE.equals(ObjectOperations.invokeGetMethod(row, SELECTED));
    }
}
