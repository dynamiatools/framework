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


package tools.dynamia.zk.viewers.table;

import org.zkoss.bind.Binder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listfoot;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Menupopup;
import tools.dynamia.commons.MapBuilder;
import tools.dynamia.commons.PropertyChangeListenerContainer;
import tools.dynamia.commons.collect.PagedList;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.domain.fx.Functions;
import tools.dynamia.domain.fx.MultiFunctionProcessor;
import tools.dynamia.domain.query.DataSet;
import tools.dynamia.viewers.TableViewComponent;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.ui.CanBeReadonly;
import tools.dynamia.zk.util.ZKBindingUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings("unchecked")
public class TableView<T> extends Listbox implements TableViewComponent<T>, CanBeReadonly {

    static {
        BindingComponentIndex.getInstance().put("value", TableView.class);
        ComponentAliasIndex.getInstance().add(TableView.class);
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * Event to listen double click events in ListItmes
     */
    public static final String ON_ITEM_DOUBLE_CLICK = "onItemDoubleClick";
    public static final String ON_ITEM_CLICK = "onItemDoubleClick";
    public static final String ON_ITEMS_RENDERED = "onItemsRendered";
    public static final String ON_VALUE_CHANGED = "onValueChanged";
    public static final String ON_MODEL_CHANGED = "onModelChanged";
    private List<T> value;
    private ViewDescriptor viewDescriptor;

    private boolean showRowNumber = true;
    private List<T> defaultValue;
    private View parentView;
    private String orderBy;
    private int maxResults;
    private Menupopup contextMenu;
    private Object source;
    private Consumer onSourceChange;
    private boolean projection;
    private PagedList<T> pageList;
    private boolean readonly;
    private List<TableViewFooter> footersWithFunctions;
    private MultiFunctionProcessor multiFunctionProcesor;


    public TableView() {

    }

    public TableView(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
    }

    public int getAbsoluteIndex(Listitem item) {
        int ai = item.getIndex() + 1;
        if (pageList != null) {
            int pai = getPaginal().getPageSize() * getPaginal().getActivePage();
            ai += pai;
        }

        return ai;
    }

    @Override
    public List<T> getValue() {
        return value;
    }

    @Override
    public void setValue(DataSet<List<T>> dataSet) {
        setValue(dataSet.getData());
    }

    @Override
    public void setValue(List<T> value) {
        if (value instanceof PagedList) {
            this.pageList = (PagedList<T>) value;
            //  value = this.pageList.getDataSource().getPageData();
        } else {
            this.pageList = null;
        }


        boolean changed = false;
        if (defaultValue != null && !defaultValue.isEmpty()) {
            this.value = new ArrayList<>(defaultValue);
            if (value != null) {
                this.value.addAll(value);
                changed = true;
            }
        } else {
            try {
                if ((this.value == null && value != null) || (this.value != null && value == null) ||
                        (this.value != null && value != null && this.value.size() != value.size()) || this.value != value) {
                    changed = true;
                }
            } catch (NoSuchElementException e) {
                changed = true;
            }
            this.value = value;
        }


        if (this.value == null) {
            setModel((ListModel) null);
        } else {
            setModel(new TableViewModel<>(this.value, true, isMultiple()));
        }

        if (changed) {
            Events.postEvent(ON_VALUE_CHANGED, this, this.value);
        }
    }

    @Override
    public T getSelected() {
        if (getSelectedItem() != null) {
            return getSelectedItem().getValue();
        } else {
            return null;
        }
    }

    @Override
    public void setSelected(Object selected) {
        if (selected != null && getModel() instanceof AbstractListModel model) {
            model.addToSelection(selected);
        }
    }


    public void setDefaultValue(List<T> defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public TableFieldComponent getTableFieldComponent(String fieldName, int rowIndex) {
        try {
            Listitem item = getItemAtIndex(rowIndex);
            Map<String, TableFieldComponent> tfcMap = (Map<String, TableFieldComponent>) item
                    .getAttribute("TABLE_FIELD_COMPONENTS");
            return tfcMap.get(fieldName);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public TableFieldComponent getTableFieldComponent(String fieldName, Object item) {
        try {
            if (item instanceof Component component) {
                Map<String, TableFieldComponent> tfcMap = (Map<String, TableFieldComponent>) component
                        .getAttribute("TABLE_FIELD_COMPONENTS");
                return tfcMap.get(fieldName);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public void clear() {
        setValue((List) null);
    }

    public Listitem getListItemByValue(T value) {
        Listitem target = null;
        for (Object obj : getItems()) {
            Listitem item = (Listitem) obj;
            if (item.getValue().equals(value)) {
                target = item;
                break;
            }
        }
        return target;
    }

    @Override
    public ViewDescriptor getViewDescriptor() {
        return viewDescriptor;
    }

    @Override
    public void setViewDescriptor(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
    }

    @Override
    public void setItemRenderer(ListitemRenderer renderer) {
        super.setItemRenderer(renderer);
        if (renderer instanceof TableViewRowRenderer tvrr) {
            tvrr.setViewDescriptor(viewDescriptor);
            tvrr.setTableView(this);
        }
    }

    @Override
    public void setShowRowNumber(boolean showRowNumber) {
        this.showRowNumber = showRowNumber;
    }

    @Override
    public boolean isShowRowNumber() {
        return showRowNumber;
    }

    @Override
    public boolean isListitemSelected() {
        return getSelectedItem() != null;
    }

    @Override
    public View getParentView() {
        return parentView;
    }

    @Override
    public void setParentView(View parentView) {
        this.parentView = parentView;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Menupopup getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(Menupopup contextMenu) {
        this.contextMenu = contextMenu;
    }

    @Override
    public TableViewFooter getFooter(String fieldName) {
        Listfoot foot = getListfoot();
        if (foot != null) {
            List<TableViewFooter> footers = foot.getChildren();
            for (TableViewFooter listfooter : footers) {
                Object fn = listfooter.getAttribute("field-name");
                if (fn != null && fn.toString().equals(fieldName)) {
                    return listfooter;
                }
            }
        }
        return null;
    }

    @Override
    public TableViewHeader getHeader(String fieldName) {
        Listhead head = getListhead();
        if (head != null) {
            List<TableViewHeader> headers = head.getChildren();
            for (TableViewHeader header : headers) {
                Object fn = header.getAttribute("field-name");
                if (fn != null && fn.toString().equals(fieldName)) {
                    return header;
                }
            }
        }
        return null;
    }

    public void updateUI() {
        Events.postEvent(ON_MODEL_CHANGED, this, this.value);

        Events.postEvent(ON_VALUE_CHANGED, this, this.value);

    }

    @Override
    public boolean isEmpty() {
        return getModel() == null || getModel().getSize() == 0;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public void setSource(Object source) {
        this.source = source;
        if (onSourceChange != null) {
            onSourceChange.accept(source);
        }

        if (source instanceof PropertyChangeListenerContainer) {
            ((PropertyChangeListenerContainer) source).addPropertyChangeListener(l -> computeFooters());
        }
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public boolean isProjection() {
        return projection;
    }

    public void setProjection(boolean projection) {
        this.projection = projection;
    }

    @Override
    public boolean isReadonly() {
        return readonly;
    }

    @Override
    public void setReadonly(boolean readonly) {
        boolean old = this.readonly;
        this.readonly = readonly;

        if (old != this.readonly) {
            renderAll();
        }

    }

    /**
     * Reload component binding for selected row
     */
    public void updateSelectedItem() {
        try {
            var selectRow = getSelectedItem();
            var binder = (Binder) selectRow.getAttribute(TableViewRowRenderer.ROW_BINDER_NAME);
            ZKBindingUtil.bindBean(selectRow, Viewers.BEAN, selectRow.getValue());
            binder.loadComponent(selectRow, false);
        } catch (Exception e) {
            //cannot doit
        }
    }

    public void onSourceChanged(Consumer onSourceChange) {
        this.onSourceChange = onSourceChange;
    }

    void setFootersWithFunctions(List<TableViewFooter> footersWithFunctions) {
        this.footersWithFunctions = footersWithFunctions;
    }

    List<TableViewFooter> getFootersWithFunctions() {
        return footersWithFunctions;
    }

    /**
     * Update all footer with functions
     */
    public void computeFooters() {
        try {
            if (footersWithFunctions != null) {
                footersWithFunctions.forEach(TableViewFooter::clear);

                if (value != null) {
                    if (multiFunctionProcesor != null) {
                        var result = multiFunctionProcesor.compute(value, new HashMap<>(), footersWithFunctions);
                        result.forEach((f, v) -> footersWithFunctions.stream()
                                .filter(ft -> ft.equals(f)).findFirst()
                                .ifPresent(tableViewFooter -> tableViewFooter.setValue(v)));
                    } else {
                        footersWithFunctions.forEach(footer -> {
                            if (value instanceof Collection) {
                                if (!((Collection) value).isEmpty()) {
                                    Map args = MapBuilder.put("property", footer.getField().getName());
                                    Object result = Functions.compute(footer.getFunction(), value, args);
                                    footer.setValue(result);
                                }
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            LoggingService.get(getClass()).error("Error computing footers", e);
        }
    }

    public void setMultiFunctionProcesor(MultiFunctionProcessor multiFunctionProcesor) {
        this.multiFunctionProcesor = multiFunctionProcesor;
    }

    public MultiFunctionProcessor getMultiFunctionProcesor() {
        return multiFunctionProcesor;
    }
}
