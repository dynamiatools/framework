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

import org.zkoss.bind.Binder;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.Div;
import org.zkoss.zul.North;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.crud.CrudAction;
import tools.dynamia.domain.query.DataSet;
import tools.dynamia.viewers.DataSetView;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorNotFoundException;
import tools.dynamia.viewers.impl.DefaultViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.actions.ActionToolbar;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.viewers.table.TableView;

import java.util.List;
import java.util.Map;

public class SimpleCrudView<T> extends Div implements DataSetView<List<T>>, ActionEventBuilder {

    private Class beanClass;

    private ViewDescriptor viewDescriptor;
    private View parentView;
    private Borderlayout layout;
    private ActionToolbar actionToolbar;
    private TableView<T> tableView;
    private CrudViewModel crudViewModel;


    public SimpleCrudView() {
        initLayout();
    }

    private void initLayout() {
        this.layout = new Borderlayout();
        layout.appendChild(new North());
        layout.appendChild(new Center());
        layout.setVflex("1");

        appendChild(layout);
        actionToolbar = new ActionToolbar(this);
        layout.getNorth().appendChild(actionToolbar);
    }

    /**
     * Init table view and data
     */
    public void initTable() {
        if (viewDescriptor == null) {
            throw new ViewDescriptorNotFoundException("Cannot init table for SimpleCrudView because view descriptor is null");
        }
        if (beanClass == null) {
            throw new ViewDescriptorNotFoundException("Cannot init table for SimpleCrudView because beanClass is null");
        }

        DefaultViewDescriptor descriptor = new DefaultViewDescriptor(beanClass, "table");
        descriptor.merge(viewDescriptor);
        tableView = (TableView<T>) Viewers.getView(viewDescriptor);
        if (tableView != null) {
            tableView.setVflex("1");
            layout.getCenter().getChildren().clear();
            layout.getCenter().appendChild(tableView);
        }
    }

    public void initViewModel() {
        if (crudViewModel != null) {
            Binder binder = ZKBindingUtil.createBinder();
            ZKBindingUtil.initBinder(binder, this, crudViewModel);

        }
    }


    public void addAction(CrudAction crudAction) {
        this.actionToolbar.addAction(crudAction);
    }

    public void clearActions() {
        this.actionToolbar.clear();
    }


    @Override
    public void setValue(List<T> value) {
        if (tableView != null) {
            tableView.setValue(value);
        }
    }


    @Override
    public List<T> getValue() {
        if (tableView != null) {
            return tableView.getValue();
        }
        return null;
    }

    @Override
    public ViewDescriptor getViewDescriptor() {
        return viewDescriptor;
    }

    @Override
    public void setViewDescriptor(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
        if (viewDescriptor != null) {
            this.beanClass = viewDescriptor.getBeanClass();
        }
    }


    @Override
    public View getParentView() {
        return parentView;
    }

    @Override
    public void setParentView(View parentView) {
        this.parentView = parentView;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public void setBeanClass(String className) throws ClassNotFoundException {
        setBeanClass(Class.forName(className));
    }

    @Override
    public void setValue(DataSet<List<T>> dataSet) {
        if (tableView != null) {
            tableView.setValue(dataSet);
        }
    }

    @Override
    public Object getSelected() {
        if (tableView != null) {
            return tableView.getSelected();
        }
        return null;
    }

    @Override
    public void setSelected(Object selected) {
        if (tableView != null) {
            tableView.setSelected(selected);
        }
    }

    @Override
    public boolean isEmpty() {
        if (tableView != null) {
            return tableView.isEmpty();
        }
        return true;
    }

    @Override
    public ActionEvent buildActionEvent(Object source, Map<String, Object> params) {
        return new ActionEvent(getSelected(), this, params);
    }
}
