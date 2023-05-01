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

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SortEvent;
import org.zkoss.zul.FieldComparator;
import tools.dynamia.commons.BeanSorter;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.reflect.AccessMode;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudDataSetViewBuilder;
import tools.dynamia.crud.GenericCrudView;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.viewers.DataSetView;
import tools.dynamia.viewers.ViewFactory;
import tools.dynamia.viewers.ViewRendererException;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.crud.actions.ViewDataAction;
import tools.dynamia.zk.viewers.table.TableView;
import tools.dynamia.zk.viewers.table.TableViewHeader;
import tools.dynamia.zk.viewers.table.TableViewType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 *
 * @author Mario A. Serrano Leones
 */
@Provider
public class CrudTableViewBuilder implements CrudDataSetViewBuilder {

    @Override
    public String getViewTypeName() {
        return new TableViewType().getName();
    }

    @Override
    public Class getPreferredController() {
        return CrudController.class;
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public DataSetView build(GenericCrudView view) {
        CrudView crudView;
        if (view instanceof CrudView) {
            crudView = (CrudView) view;
        } else {
            throw new ViewRendererException("Cannot build " + getViewTypeName() + " DataSetView because " + view.getClass()
                    + " is not supported");
        }

        final ViewFactory viewFactory = Containers.get().findObject(ViewFactory.class);
        final String device = HttpUtils.detectDevice();
        final TableView tableView = (TableView) viewFactory.getView("table", device, new ArrayList(), crudView.getBeanClass());

        if (!tableView.getViewDescriptor().getParams().containsKey("sizedByContent")) {
            tableView.setSizedByContent(true);
        }

        tableView.setContextMenu(crudView.getContextMenu());
        crudView.getController().setPaginator(tableView.getPaginal());
        tableView.addEventListener(TableView.ON_ITEM_DOUBLE_CLICK, event -> {
            if (tableView.getSelectedCount() > 0) {
                Object value = event.getData();
                ViewDataAction viewDataAction = Containers.get().findObject(ViewDataAction.class);
                viewDataAction.actionPerformed(new CrudActionEvent(value, value, view, view.getController()));
            }
        });
        initSorters(crudView, tableView);
        return tableView;
    }

    private void initSorters(CrudView crudView, TableView tableView) {
        boolean inMemory = tableView.getViewDescriptor().getParams().get(Viewers.PARAM_IN_MEMORY_SORTING) == Boolean.TRUE;
        tableView.getListhead().getChildren().stream()
                .filter(c -> c instanceof TableViewHeader)
                .map(c -> (TableViewHeader) c)
                .filter(h -> h.getField() != null
                        && h.getField().getParams().get(Viewers.PARAMS_SORTABLE) != Boolean.FALSE)
                .forEach(h -> initSorter(crudView, h, inMemory));

    }

    private void initSorter(CrudView crudView, TableViewHeader header, boolean inMemory) {

        final String finalName = header.getField().getName();
        header.setSortAscending(new FieldComparator(finalName, true));
        header.setSortDescending(new FieldComparator(finalName, false));
        header.addEventListener(Events.ON_SORT, event -> {
            SortEvent sortEvent = (SortEvent) event;

            Class beanClass = header.getTableView().getViewDescriptor().getBeanClass();
            String orderBy = finalName;

            Map<String, Object> fieldParams = header.getField().getParams();
            if (fieldParams.get(Viewers.PARAM_BIND) instanceof String) {
                orderBy = (String) fieldParams.get(Viewers.PARAM_BIND);
            } else if (fieldParams.get(Viewers.PARAM_BINDINGS) instanceof Map bindings) {
                StringJoiner joiner = new StringJoiner(",");

                for (Object value : bindings.values()) {
                    if (value instanceof String) {
                        PropertyInfo info = BeanUtils.getPropertyInfo(beanClass, (String) value);
                        if (info != null && info.getAccessMode() == AccessMode.READ_WRITE) {
                            joiner.add(value + " " + (sortEvent.isAscending() ? "ASC" : "DESC"));
                        }
                    }
                }
                if (joiner.length() > 0) {
                    orderBy = joiner.toString();
                }
            }

            if (fieldParams.get(Viewers.PARAM_ORDER_BY) != null) {
                orderBy = (String) fieldParams.get(Viewers.PARAM_ORDER_BY);
            }

            crudView.getController().getParams().orderBy(orderBy, sortEvent.isAscending());
            if (inMemory) {
                Object data = crudView.getController().getQueryResult().getData();
                if (data instanceof List) {
                    BeanSorter sorter = crudView.getController().getSorter();
                    sorter.sort((List) data);
                    crudView.getController().setQueryResult((List) data);
                }
            } else {
                crudView.getController().doQuery();
            }
        });
    }

}
