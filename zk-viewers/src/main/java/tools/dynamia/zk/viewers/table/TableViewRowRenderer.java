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


package tools.dynamia.zk.viewers.table;

import org.zkoss.bind.Binder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.InputElement;
import tools.dynamia.actions.*;
import tools.dynamia.commons.BeanMap;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.MapBuilder;
import tools.dynamia.commons.reflect.ReflectionException;
import tools.dynamia.domain.PropertyChangeListenerContainer;
import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.IndexableComparator;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.ComponentCustomizerUtil;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.actions.BoostrapButtonActionRenderer;
import tools.dynamia.zk.ui.Import;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.util.ZKUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
public class TableViewRowRenderer implements ListitemRenderer<Object> {
    public static String ROW_BINDER_NAME = "rowBinder";

    private ViewDescriptor viewDescriptor;
    private TableView tableView;

    public TableViewRowRenderer() {
    }

    public TableViewRowRenderer(ViewDescriptor descriptor, TableView tableView) {
        setViewDescriptor(descriptor);
        this.tableView = tableView;
    }

    public final void setViewDescriptor(ViewDescriptor descriptor) {
        this.viewDescriptor = descriptor;
        descriptor.getFields().sort(new IndexableComparator());
    }

    public final void setTableView(TableView tableView) {
        this.tableView = tableView;
    }

    public TableView getTableView() {
        return tableView;
    }

    @Override
    public void render(Listitem item, Object data, int index) {
        if (data != null) {
            Binder binder = ZKBindingUtil.createBinder();
            ZKBindingUtil.initBinder(binder, item, item);
            item.setAttribute(ROW_BINDER_NAME, binder);
            item.setAttribute(Viewers.BEAN, data);
            item.setValue(data);

            renderRow(item, data, index, binder);
        }
    }

    protected void renderRow(Listitem item, Object data, int index, Binder binder) {
        Map<String, TableFieldComponent> fieldsComponentsMap = new HashMap<>();
        item.setAttribute(Viewers.ATTRIBUTE_TABLE_FIELD_COMPONENTS, fieldsComponentsMap);

        if (tableView != null) {
            renderCommonsCell(item, index);
        }

        for (Field field : viewDescriptor.getFields()) {
            Viewers.customizeField("table", field);
            renderFieldCell(binder, item, data, fieldsComponentsMap, field, index);
        }


        if (viewDescriptor.getParams().containsKey(Viewers.PARAM_ACTIONS)) {
            renderActions(item, data, binder);
        }

        binder.loadComponent(item, false);

        if (viewDescriptor.getParams().get(Viewers.PARAM_WRITABLE) == Boolean.TRUE) {
            if (data instanceof PropertyChangeListenerContainer) {
                ((PropertyChangeListenerContainer) data).addPropertyChangeListener(evt -> {
                    Field field = viewDescriptor.getField(evt.getPropertyName());
                    if (field != null) {
                        binder.loadComponent(item, false);
                        Events.postEvent(new Event(TableView.ON_VALUE_CHANGED, tableView, tableView.getValue()));
                    }
                });
            }
        }

        setupEnumColors(item, data);

    }

    private void renderActions(Listitem item, Object data, Binder binder) {
        try {
            BoostrapButtonActionRenderer defaultRenderer = new BoostrapButtonActionRenderer();
            defaultRenderer.setSmall(true);

            Map actions = (Map) viewDescriptor.getParams().get(Viewers.PARAM_ACTIONS);
            if (actions == null) {
                return;
            }
            Listcell cell = new Listcell();
            item.appendChild(cell);

            Hlayout hlayout = new Hlayout();
            cell.appendChild(hlayout);
            actions.forEach((Object k, Object v) -> {
                Map actionParams = null;
                if (v != null && v instanceof Map) {
                    actionParams = (Map) v;
                }
                TableViewRowAction action = Containers.get().findObjects(TableViewRowAction.class, a -> a.getId().equals(k)).stream().findFirst().orElse(null);
                if (action != null) {
                    ActionEvent evt = new ActionEvent(data, item);
                    evt.setSource(tableView.getSource() != null ? tableView.getSource() : item);
                    ActionRenderer actionRenderer = action.getRenderer() != null ? action.getRenderer() : defaultRenderer;
                    Component actionComp = (Component) actionRenderer.render(action, (s, p) -> evt);
                    action.onRendered(data, item, actionComp);

                    if (action.isEnabled()) {
                        hlayout.appendChild(actionComp);
                    }
                    if (actionParams != null && actionParams.containsKey(Viewers.PARAM_BINDINGS)) {
                        Map bindingMap = (Map) actionParams.get(Viewers.PARAM_BINDINGS);
                        if (bindingMap != null) {
                            bindingMap.put(ZKBindingUtil.KEY_EXP_PREFIX, Viewers.BEAN);
                        }
                        ZKBindingUtil.bindComponent(binder, actionComp, bindingMap);
                    }
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupEnumColors(Listitem item, Object data) {
        if (viewDescriptor.getParams().containsKey(Viewers.PARAM_ENUM_COLORS)) {
            try {
                Map cfg = (Map) viewDescriptor.getParams().get(Viewers.PARAM_ENUM_COLORS);
                String name = (String) cfg.get(Viewers.PARAM_NAME);
                Map colors = (Map) cfg.get(Viewers.PARAM_COLORS);

                Enum enumValue = (Enum) BeanUtils.invokeGetMethod(data, name);
                String color = (String) colors.get(enumValue.name());
                if (color != null) {
                    item.setStyle("background-color: " + color + " !important;");
                    item.getChildren().forEach(c -> {
                        if (c instanceof Listcell) {
                            ((Listcell) c).setStyle(item.getStyle());
                        }
                    });
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void renderFieldCell(Binder binder, Listitem item, Object data,
                                   Map<String, TableFieldComponent> fieldsComponentsMap, Field field, int index) {
        if (field.isVisible()) {
            Listcell cell = new Listcell();
            cell.setParent(item);
            BeanUtils.setupBean(cell, (Map) field.getParams().get("cell"));
            Object cellValue = "";

            try {

                if (field.getFieldClass() != null && field.getFieldClass().equals(boolean.class)) {
                    cellValue = BeanUtils.invokeBooleanGetMethod(data, field.getName());
                } else {
                    cellValue = BeanUtils.invokeGetMethod(data, field.getName());
                }
            } catch (ReflectionException e) {
                // Suertee
            }

            boolean renderWhenNull = field.getParams().get(Viewers.PARAM_RENDER_WHEN_NULL) == Boolean.TRUE;

            if (cellValue == null && !renderWhenNull) {
                cellValue = field.getParams().get(Viewers.PARAM_NULLVALUE);
                Label nullValue = new Label((String) cellValue);
                nullValue.setSclass(Viewers.PARAM_NULLVALUE);
                nullValue.setParent(cell);
            } else {
                Component comp = createFieldComponent(data, cellValue, field, cell);

                if (comp instanceof Import) {
                    Import importComp = (Import) comp;
                    importComp.setValue(data);
                    importComp.addArgs(field.getParams());
                    importComp.addArg("index", index);
                }
                BeanUtils.setupBean(comp, field.getParams());

                if (field.getParams().containsKey(Viewers.PARAMS_ATTRIBUTES)) {
                    Map attributes = (Map) field.getParams().get(Viewers.PARAMS_ATTRIBUTES);
                    if (attributes != null) {
                        attributes.forEach((k, v) -> comp.setAttribute(k.toString(), v));
                    }

                }

                comp.setParent(cell);

                ComponentCustomizerUtil.customizeComponent(field, comp, field.getComponentCustomizer());
                fieldsComponentsMap.put(field.getName(), new TableFieldComponent(field.getName(), comp));
                if (field.getParams().get(Viewers.PARAM_IGNORE_BINDINGS) != Boolean.TRUE) {
                    if (isBindiable(field, comp)) {
                        Object bmapObject = field.getParams().get(Viewers.PARAM_BINDINGS);
                        if (bmapObject != null && bmapObject instanceof Map) {
                            Map bindingMap = (Map) bmapObject;
                            bindingMap.put(ZKBindingUtil.KEY_EXP_PREFIX, Viewers.BEAN);
                            ZKBindingUtil.bindComponent(binder, comp, bindingMap);
                        } else {
                            String converterExpression = (String) field.getParams().get(Viewers.PARAM_CONVERTER);
                            String attr = (String) field.getParams().get(Viewers.PARAM_BINDING_ATTRIBUTE);
                            String expression = data instanceof BeanMap ? Viewers.BEAN + "['" + field.getName() + "']" : Viewers.BEAN + "." + field.getName();
                            ZKBindingUtil.bindComponent(binder, comp, attr, expression, converterExpression);
                        }
                    }
                }

                if (field.getAction() != null) {
                    String actionId = field.getAction();
                    Action action = ActionLoader.findActionById(Action.class, actionId);
                    if (action != null) {
                        if (comp instanceof HtmlBasedComponent) {
                            HtmlBasedComponent hcomp = (HtmlBasedComponent) comp;
                            if (hcomp.getTooltiptext() == null && action.getDescription() != null) {
                                hcomp.setTooltiptext(action.getDescription());
                            }
                        }
                        String event = Events.ON_CLICK;
                        if (comp instanceof InputElement) {
                            event = Events.ON_OK;
                        }
                        comp.addEventListener(event, e -> action.actionPerformed(new ActionEvent(data, comp, MapBuilder.put("field", field, "tableView", tableView))));
                    }
                }
            }
        }
    }

    protected Component createFieldComponent(Object data, Object cellValue, Field field, Listcell cell) {
        Component component = (Component) BeanUtils.newInstance(field.getComponentClass());
        if (component != null) {
            ZKUtil.changeReadOnly(component, tableView.isReadonly());
        }

        return component;
    }

    protected boolean isBindiable(Field field, Component comp) {
        return true;
    }

    protected void renderCommonsCell(Listitem item, int index) {
        if (tableView.isCheckmark()) {
            Listcell checkCell = new Listcell();
            checkCell.setSclass("tableCheckCell");
            checkCell.setParent(item);
        }

        if (tableView.isShowRowNumber()) {
            Listcell indexCell = new Listcell(String.valueOf(index + 1));
            indexCell.setSclass("tableIndexCell");
            indexCell.setParent(item);
        }

        if (tableView.getContextMenu() != null) {
            item.setContext(tableView.getContextMenu());
        }
    }

}
