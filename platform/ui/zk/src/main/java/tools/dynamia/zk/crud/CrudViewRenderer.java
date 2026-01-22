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

import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.logger.AbstractLoggable;
import tools.dynamia.crud.CrudAction;
import tools.dynamia.crud.CrudDataSetViewBuilder;
import tools.dynamia.crud.CrudState;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewRenderer;
import tools.dynamia.viewers.ViewRendererException;
import tools.dynamia.viewers.util.Viewers;

import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CrudViewRenderer<T> extends AbstractLoggable implements ViewRenderer<T> {

    @Override
    public View<T> render(ViewDescriptor descriptor, T value) {
        return render(descriptor, value, null);
    }

    private void configure(CrudView crudView, CrudController crudController) {
        Class<? extends CrudController> preferredController = null;
        CrudDataSetViewBuilder cdsvb = CrudView.getDataSetViewBuilder(crudView.getDataSetViewType());
        if (cdsvb != null) {
            preferredController = cdsvb.getPreferredController();
            if (crudController == null) {
                if (crudView.getControllerClass() == null) {
                    crudView.setControllerClass(preferredController);
                } else if (!ObjectOperations.isAssignable(crudView.getControllerClass(), preferredController)) {
                    throw new ViewRendererException("CrudView: Controller class should be type or extended " + preferredController);
                }
            }
        }

        CrudController controller = null;

        if (crudController != null) {
            controller = crudController;
        } else if (crudView.getControllerClass() == null) {
            controller = new CrudController(crudView.getBeanClass());
        } else {
            controller = (CrudController) ObjectOperations.newInstance(crudView.getControllerClass());
            controller.setEntityClass(crudView.getBeanClass());
        }

        crudView.setController(controller);

    }

    public CrudView<T> render(ViewDescriptor descriptor, T value, CrudController crudController) {
        CrudView<T> crudView = newCrudView();
        crudView.setViewDescriptor(descriptor);
        crudView.setValue(value);
        crudView.setBeanClass(descriptor.getBeanClass());
        Map actions = (Map) descriptor.getParams().get(Viewers.PARAM_ACTIONS);
        crudView.setActionsParams(actions);
        if (descriptor.getParams().get("controllerClass") != null) {
            try {
                crudView.setControllerClass((String) descriptor.getParams().get("controllerClass"));
            } catch (ClassNotFoundException ex) {
                throw new ViewRendererException("Error configuring Controller class: " + ex.getMessage(), ex);
            }
        }
        ObjectOperations.setupBean(crudView, descriptor.getParams());
        configure(crudView, crudController);
        loadDescriptorActions(descriptor, crudView);
        crudView.setState(CrudState.READ);
        return crudView;
    }

    private void loadDescriptorActions(ViewDescriptor descriptor, CrudView<T> crudView) {
        try {
            Map actions = (Map) descriptor.getParams().get(Viewers.PARAM_ACTIONS);
            if (actions == null) {
                return;
            }
            for (Object actionKey : actions.keySet()) {
                Map actionDefinition = (Map) actions.get(actionKey);
                if (actionDefinition.containsKey(Viewers.ATTRIBUTE_CLASS)) {
                    String className = (String) actionDefinition.get(Viewers.ATTRIBUTE_CLASS);
                    CrudAction crudAction = ObjectOperations.newInstance(className);
                    crudView.addAction(crudAction);
                }
            }

        } catch (Exception e) {
            log("Error loading descriptor actions", e);
        }

    }

    protected CrudView<T> newCrudView() {
        return new CrudView<>();
    }

}
