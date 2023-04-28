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

import tools.dynamia.crud.CrudDataSetViewBuilder;
import tools.dynamia.crud.GenericCrudView;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.viewers.DataSetView;
import tools.dynamia.viewers.ViewFactory;
import tools.dynamia.viewers.ViewRendererException;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.crud.ui.EntityTreeViewRowRenderer;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.form.FormView;
import tools.dynamia.zk.viewers.table.TableView;
import tools.dynamia.zk.viewers.tree.TreeView;
import tools.dynamia.zk.viewers.tree.TreeViewType;

/**
 *
 * @author Mario A. Serrano Leones
 */
@Provider
public class CrudTreeViewBuilder implements CrudDataSetViewBuilder {

    @Override
    public String getViewTypeName() {
        return new TreeViewType().getName();
    }

    @Override
    public Class getPreferredController() {
        return TreeCrudController.class;
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public DataSetView build(GenericCrudView view) {
        CrudView crudView;
        if (view instanceof CrudView) {
            crudView = (CrudView) view;
        } else {
            throw new ViewRendererException("Cannot build " + getViewTypeName() + " DataSetView because " + view.getClass()
                    + " is not supported");
        }
        final String device = HttpUtils.detectDevice();
        final ViewFactory viewFactory = Containers.get().findObject(ViewFactory.class);
        final TreeView treeView = (TreeView) viewFactory.getView(getViewTypeName(), device, null, crudView.getBeanClass());
        treeView.setSizedByContent(true);
        treeView.setContextMenu(crudView.getContextMenu());
        treeView.setItemRenderer(new EntityTreeViewRowRenderer(treeView.getViewDescriptor(), treeView));
        crudView.getController().setPaginator(treeView.getPaginal());
        treeView.addEventListener(TableView.ON_ITEM_DOUBLE_CLICK, event -> {
            if (treeView.getSelectedCount() > 0) {
                Object value = event.getData();
                FormView formView = (FormView) viewFactory.getView("form", value);
                formView.setReadonly(true);
                ZKUtil.showDialog(value.toString(), formView);
            }
        });

        TreeCrudController controller = (TreeCrudController) crudView.getController();
        String parentName = (String) crudView.getViewDescriptor().getParams().get(Viewers.PARAM_PARENT_NAME);
        if (parentName != null && !parentName.isEmpty()) {
            controller.setParentName(parentName);
        }
        controller.setRootLabelField((String) crudView.getViewDescriptor().getParams().get(Viewers.PARAM_ROOT_LABEL_FIELD));
        controller.setRootLabel((String) crudView.getViewDescriptor().getParams().get(Viewers.PARAM_ROOT_LABEL));
        controller.setRootIcon((String) crudView.getViewDescriptor().getParams().get(Viewers.PARAM_ROOT_ICON));

        return treeView;
    }

}
