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
package tools.dynamia.crud;

import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionLoader;
import tools.dynamia.commons.ApplicableClass;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.Messages;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.ObjectMatcher;
import tools.dynamia.navigation.PageAction;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorFactory;
import tools.dynamia.viewers.util.Viewers;

import java.util.List;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CrudPage extends AbstractCrudPage<Object> {

    private static final long serialVersionUID = -4645019919823261595L;
    private boolean actionLoaded;
    private String crudServiceName;


    public CrudPage(Class entityClass) {
        super(entityClass);
    }

    public CrudPage(String id, String name, Class entityClass) {
        super(id, name, entityClass);

    }

    public CrudPage(String id, String name, Class entityClass, String crudServiceName) {
        super(id, name, entityClass);
        this.crudServiceName = crudServiceName;

    }

    public void setCrudServiceName(String crudServiceName) {
        this.crudServiceName = crudServiceName;
    }

    public String getCrudServiceName() {
        return crudServiceName;
    }

    @Override
    public Object renderPage() {


        loadObjectClass();
        Object value = ObjectOperations.newInstance(getEntityClass());

        ViewDescriptor descriptor = Viewers.getViewDescriptor(getEntityClass(), "crud");
        if (crudServiceName != null && !crudServiceName.isEmpty()) {
            descriptor.addParam(Viewers.PARAM_CRUDSERVICE_NAME, crudServiceName);
        }
        return Viewers.getView(descriptor);
    }

    @Override
    public List<PageAction> getActions() {
        if (!actionLoaded) {
            loadPageActions();
        }

        return super.getActions();
    }

    private void loadPageActions() {
        ViewDescriptor crudDescriptor = Containers.get().findObject(ViewDescriptorFactory.class)
                .getDescriptor(getEntityClass(), "crud");

        ActionLoader loader = new ActionLoader(CrudAction.class);
        if (crudDescriptor.getParams().get(Viewers.PARAM_ACTIONS) instanceof Map) {
            loader.setActionAttributes((Map<String, Object>) crudDescriptor.getParams().get(Viewers.PARAM_ACTIONS));
        }

        List<Action> crudActions = loader.getActionsReferences((ObjectMatcher<CrudAction>) action -> ApplicableClass.isApplicable(getEntityClass(), action.getApplicableClasses()));

        addAction(new PageAction(this, null, Messages.get(CrudPage.class, "allActions"), "", "circle"));

        for (Action action : crudActions) {
            if (action.isEnabled()) {
                PageAction pageAction = new PageAction(this, action);
                addAction(pageAction);
            }
        }
        actionLoaded = true;

    }
}
