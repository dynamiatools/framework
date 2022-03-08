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

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SortEvent;
import org.zkoss.zul.*;
import tools.dynamia.commons.BeanSorter;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.MapBuilder;
import tools.dynamia.commons.Messages;
import tools.dynamia.domain.fx.CrudServiceMultiFunctionProcessor;
import tools.dynamia.domain.fx.Functions;
import tools.dynamia.domain.fx.MultiFunctionProcessor;
import tools.dynamia.ui.LocalizedMessagesProvider;
import tools.dynamia.viewers.*;
import tools.dynamia.viewers.util.ViewRendererUtil;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.viewers.ZKViewersUtil;

import java.util.*;

/**
 * @author Mario A. Serrano Leones
 */
public class TableViewRenderer<T> implements ViewRenderer<List<T>> {

    private LocalizedMessagesProvider messagesProvider;

    @Override
    public View<List<T>> render(ViewDescriptor descriptor, List<T> value) {
        if (value != null && !(value instanceof Collection)) {
            throw new ViewRendererException(value + " value is not a collection");
        }

        TableView<T> table = new TableView<>(descriptor);

        if (descriptor.getParams().get(Viewers.PARAM_ITEM_RENDERER) != null) {
            table.setItemRenderer((ListitemRenderer) BeanUtils.newInstance(descriptor.getParams().get(Viewers.PARAM_ITEM_RENDERER).toString()));
        } else {
            table.setItemRenderer(new TableViewRowRenderer(descriptor, table));
        }

        table.setVflex("1");
        table.setHflex("1");
        table.setSclass("table-view");

        if (descriptor.getParams().get(Viewers.PARAM_PAGINATION) != Boolean.FALSE) {
            table.setMold("paging");
        }


        ViewRendererUtil.beforeRender(descriptor, table);
        Viewers.setupView(table, descriptor.getParams());
        renderGroups(table, descriptor);
        renderHeaders(table, descriptor);
        renderFooters(table, descriptor);
        ViewRendererUtil.afterRender(descriptor, table);
        table.setValue(value);
        return table;
    }

    private void renderGroups(TableView<T> table, ViewDescriptor descriptor) {
        if (descriptor.getFieldGroups() != null && !descriptor.getFieldGroups().isEmpty()) {

            Auxhead auxhead = new Auxhead();
            auxhead.setParent(table);

            descriptor.getFieldGroups().forEach(grp -> {
                String grplabel = grp.getLocalizedLabel(Messages.getDefaultLocale());
                grplabel = filterFieldGroupLabel(grp, grplabel);
                Auxheader auxheader = new Auxheader(grplabel);
                BeanUtils.setupBean(auxheader, grp.getParams());
                if (auxheader.getColspan() == 1 && grp.getFields().size() > 1) {
                    auxheader.setColspan(grp.getFields().size());
                }
                auxheader.setParent(auxhead);
            });
        }
    }

    private void renderHeaders(TableView<T> table, ViewDescriptor descriptor) {
        Listhead head = new Listhead();

        // head.setSizable(true);
        head.setParent(table);

        if (table.isCheckmark()) {
            TableViewHeader checkHeader = new TableViewHeader(table, " ", null, "32px");
            checkHeader.setSclass("tableCheckHeader");
            checkHeader.setParent(head);
        }

        if (table.isShowRowNumber()) {
            TableViewHeader indexHeader = new TableViewHeader(table, "#", null, "40px");
            indexHeader.setSclass("tableIndexHeader");
            indexHeader.setParent(head);
        }

        descriptor.getFields().sort(new IndexableComparator());

        for (Field field : descriptor.getFields()) {
            if (field.isVisible()) {

                String label = field.getLocalizedLabel(Messages.getDefaultLocale());
                label = filterFieldLabel(field, label);

                String description = field.getLocalizedDescription(Messages.getDefaultLocale());
                description = filterFieldDescription(field,description);

                TableViewHeader header = new TableViewHeader(table, label);
                header.setTooltiptext(description);
                header.setParent(head);
                header.setField(field);
                ZKViewersUtil.setupFieldIcon(field, header);
                if (field.isShowIconOnly()) {
                    header.setAlign("center");
                }
                header.setAttribute("field-name", field.getName());
                header.setAttribute("field-class", field.getFieldClass());

                try {
                    Map headerParams = (Map) field.getParams().get("header");
                    if (headerParams != null) {
                        BeanUtils.setupBean(header, headerParams);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (descriptor.getParams().get(Viewers.PARAMS_SORTABLE) == Boolean.TRUE) {
                    header.setSortAscending(new FieldComparator(field.getName(), true));
                    header.setSortDescending(new FieldComparator(field.getName(), false));
                    header.addEventListener(Events.ON_SORT, event -> {
                        SortEvent sortEvent = (SortEvent) event;

                        var data = table.getValue();
                        if (data != null) {
                            BeanSorter sorter = new BeanSorter();
                            sorter.setAscending(sortEvent.isAscending());
                            sorter.setColumnName(field.getName());
                            sorter.sort(data);
                            table.setValue(data);
                        }

                    });
                }
            }
        }

        if (descriptor.getParams().containsKey(Viewers.PARAM_FROZEN_COLUMNS)) {
            Frozen frozen = new Frozen();
            frozen.setColumns(Integer.parseInt(descriptor.getParams().get(Viewers.PARAM_FROZEN_COLUMNS).toString()));
            frozen.setParent(table);
        }

        if (descriptor.getParams().get(Viewers.PARAM_ACTIONS) != null) {
            renderActionsHeader(table, head, descriptor);
        }


    }

    private void renderActionsHeader(TableView<T> table, Listhead head, ViewDescriptor descriptor) {
        Listheader header = new Listheader();
        header.setAlign("center");
        header.setParent(head);
        Map actionsHeader = (Map) descriptor.getParams().get("actionsHeader");
        if (actionsHeader != null) {
            BeanUtils.setupBean(header, actionsHeader);
        }

    }

    private void renderFooters(TableView<T> table, ViewDescriptor descriptor) {
        Listfoot foot = new Listfoot();

        boolean footRequired = false;

        if (table.isCheckmark()) {
            TableViewFooter checkFooter = new TableViewFooter(table);
            checkFooter.setSclass("tableCheckFooter");
            checkFooter.setParent(foot);
        }

        if (table.isShowRowNumber()) {
            TableViewFooter indexFooter = new TableViewFooter(table);
            indexFooter.setSclass("tableIndexFooter");
            indexFooter.setParent(foot);
        }

        descriptor.getFields().sort(new IndexableComparator());
        List<TableViewFooter> footersWithFunctions = new ArrayList<>();

        for (Field field : descriptor.getFields()) {
            if (field.isVisible()) {
                TableViewFooter footer = new TableViewFooter(table, field);
                footer.setTooltiptext(field.getDescription());
                footer.setParent(foot);
                footer.setAttribute("field-name", field.getName());
                footer.setAttribute("field-class", field.getFieldClass());

                try {
                    Map footerParams = (Map) field.getParams().get(Viewers.PARAM_FOOTER);
                    if (footerParams != null) {
                        footRequired = true;
                        BeanUtils.setupBean(footer, footerParams);
                        if (footer.getFunctionConverter() == null && field.getParams().containsKey(Viewers.PARAM_CONVERTER)) {
                            footer.setFunctionConverter((String) field.getParams().get(Viewers.PARAM_CONVERTER));
                        }

                        if (footer.getFunction() != null && !footer.getFunction().isBlank()) {
                            footersWithFunctions.add(footer);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (footRequired) {
            foot.setParent(table);
        }

        setupFootersFunctions(table, footersWithFunctions);
    }

    private void setupFootersFunctions(TableView<T> table, List<TableViewFooter> footersWithFunctions) {
        if (footersWithFunctions != null && !footersWithFunctions.isEmpty()) {

            MultiFunctionProcessor p = null;
            if (table.getViewDescriptor().getParams().containsKey(Viewers.PARAM_MULTI_FUNCTION_PROCESSOR)) {
                String processorName = table.getViewDescriptor().getParams().get(Viewers.PARAM_MULTI_FUNCTION_PROCESSOR).toString();
                if (processorName.equals("crud") || processorName.equals("auto")) {
                    p = new CrudServiceMultiFunctionProcessor();
                } else {
                    p = BeanUtils.newInstance(processorName);
                }
            }
            final MultiFunctionProcessor processor = p;

            table.addEventListener(TableView.ON_VALUE_CHANGED, event -> {
                Object value = event.getData();
                footersWithFunctions.forEach(TableViewFooter::clear);

                if (value != null) {
                    if (processor != null) {
                        var result = processor.compute(value, new HashMap<>(), footersWithFunctions);
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
            });
        }
    }

    protected String filterFieldLabel(Field field, String label) {
        if (messagesProvider == null) {
            return label;
        } else {
            return messagesProvider.getMessage(field.getName(), Viewers.buildMessageClasffier(field.getViewDescriptor()), Messages.getDefaultLocale(), label);
        }
    }

    protected String filterFieldGroupLabel(FieldGroup fieldGroup, String label) {
        if (messagesProvider == null) {
            return label;
        } else {
            return messagesProvider.getMessage("Group " + fieldGroup.getName(), Viewers.buildMessageClasffier(fieldGroup.getViewDescriptor()), Messages.getDefaultLocale(), label);
        }
    }

    protected String filterFieldDescription(Field field, String description) {
        if (messagesProvider == null) {
            return description;
        } else {
            return messagesProvider.getMessage(field.getName() + " Description", Viewers.buildMessageClasffier(field.getViewDescriptor()), Messages.getDefaultLocale(), description);
        }
    }

    public LocalizedMessagesProvider getMessagesProvider() {
        return messagesProvider;
    }

    public void setMessagesProvider(LocalizedMessagesProvider messagesProvider) {
        this.messagesProvider = messagesProvider;
    }
}
