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
package tools.dynamia.zk.crud.ui;

import org.zkoss.zul.Div;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.ViewCustomizer;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.impl.DefaultViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.ui.DynamicListItemRenderer;
import tools.dynamia.zk.viewers.ZKWrapperView;
import tools.dynamia.zk.viewers.table.TableView;

import java.util.List;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class EntityPickerPanel<E> extends Div {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final CrudService crudService = Containers.get().findObject(CrudService.class);
    private E selected;
    private TableView<E> tableView;
    private Class<E> entityClass;
    private String[] fields;
    private final DynamicListItemRenderer renderer = new DynamicListItemRenderer();
    private String entityName;
    private String param;
    private final QueryParameters defaultParameters = new QueryParameters();

    public EntityPickerPanel(Class entityClass) {
        setEntityClass(entityClass);
        setStyle("padding:0;margin:0");
    }

    private void render() {
        createListbox();
    }

    // -----------CREATE UI ---------------------//
    private void createListbox() {
        ViewDescriptor descriptor = Viewers.findViewDescriptor(entityClass, "entitypicker");
        DefaultViewDescriptor tableDescriptor = new DefaultViewDescriptor(entityClass, "table", false);
        tableDescriptor.merge(descriptor);
        tableDescriptor.getParams().put("showRowNumber", false);
        tableView = (TableView<E>) Viewers.getView(tableDescriptor);
        tableView.setHflex("1");
        tableView.setVflex("1");
        tableView.setMold("paging");

        tableView.setShowRowNumber(false);
        tableView.setParent(this);
        if (tableView.getOrderBy() != null) {
            defaultParameters.orderBy(tableView.getOrderBy(), true);
        }
        if (descriptor.getViewCustomizerClass() != null) {
            try {
                ViewCustomizer customizer = BeanUtils.newInstance(descriptor.getViewCustomizerClass());
                customizer.customize(new ZKWrapperView(this));
            } catch (Exception e) {
            }
        }

        if (fields != null && fields.length > 0) {
            setFields(fields);
        } else {
            fields = Viewers.getFieldsNames(descriptor);
        }
    }

    private void createHeaders() {
        if (tableView != null && fields.length > 0) {
            if (tableView.getListhead() != null) {
                tableView.getListhead().detach();
            }

            Listhead listhead = new Listhead();
            listhead.setParent(tableView);

            new Listheader("Id", null, "50px").setParent(listhead);
            for (String header : fields) {
                header = header.replace(".", " ");
                new Listheader(StringUtils.capitalize(StringUtils.addSpaceBetweenWords(header))).setParent(listhead);
            }
        }
    }

    // ------------------ Logic -----------------------------/
    public int search(String param) {
        this.param = param;
        return search();
    }

    private int search() {
        DataPaginator dataPaginator = new DataPaginator();
        dataPaginator.setPageSize(tableView.getPageSize());
        defaultParameters.paginate(dataPaginator);

        List<E> result = crudService.findByFields(entityClass, param, defaultParameters, fields);

        tableView.setValue(result);
        return result.size();
    }

    public void select() {
        if (tableView.isListitemSelected()) {
            selected = tableView.getSelectedItem().getValue();
        } else {
            selected = null;
        }
    }

    // -------------------GETTERS AND SETTERS--------------------/
    public E getSelected() {
        return selected;
    }

    private void setEntityClass(Class<E> clazz) {
        this.entityClass = clazz;
        if (clazz != null) {
            this.entityName = StringUtils.addSpaceBetweenWords(clazz.getSimpleName());
            render();
        }
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public void setFields(String... fields) {
        this.fields = fields;
        renderer.setFields(fields);
        if (tableView != null) {
            tableView.setItemRenderer(renderer);
        }
        createHeaders();
    }

    public void setControlsVisible(boolean flag) {
        for (Object object : getChildren()) {
            org.zkoss.zk.ui.Component comp = (org.zkoss.zk.ui.Component) object;
            if (comp != tableView) {
                comp.setVisible(flag);
            }
        }
    }

    public TableView<E> getTableView() {
        return tableView;
    }

    void clear() {
        tableView.clear();
    }

    public String getEntityName() {
        return entityName;
    }

    public void addDefaultParameter(String name, Object value) {
        defaultParameters.add(name, value);
    }

    public void removeDefaultParameter(String name) {
        defaultParameters.remove(name);
    }

    public QueryParameters getDefaultParameters() {
        return defaultParameters;
    }
}
