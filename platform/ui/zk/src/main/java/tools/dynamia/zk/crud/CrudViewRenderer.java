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
 * Renderer that creates and configures {@link CrudView} instances from descriptors.
 * <p>
 * It wires controller class selection, descriptor actions, bean setup and initial state.
 *
 * @param <T> entity type managed by the rendered CRUD view
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CrudViewRenderer<T> extends AbstractLoggable implements ViewRenderer<T> {

    /**
     * Renders a CRUD view using descriptor metadata and optional initial value.
     *
     * @param descriptor descriptor to render
     * @param value initial value
     * @return rendered view
     */
    @Override
    public View<T> render(ViewDescriptor descriptor, T value) {
        return render(descriptor, value, null);
    }

    /**
     * Applies controller selection rules and binds the resolved controller to the view.
     */
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

    /**
     * Renders a CRUD view using an externally provided controller when available.
     *
     * @param descriptor descriptor to render
     * @param value initial value
     * @param crudController optional external controller
     * @return configured CRUD view
     */
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

    /**
     * Loads additional CRUD actions declared inside descriptor parameters.
     */
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

    /**
     * Factory method for creating CRUD view instances.
     *
     * @return new CRUD view
     */
    protected CrudView<T> newCrudView() {
        return new CrudView<>();
    }

}
