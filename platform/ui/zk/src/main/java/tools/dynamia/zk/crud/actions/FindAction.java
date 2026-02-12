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
package tools.dynamia.zk.crud.actions;

import tools.dynamia.actions.*;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.reflect.AccessMode;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.query.ListDataSet;
import tools.dynamia.domain.query.QueryExecuter;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.actions.FindActionRenderer;
import tools.dynamia.zk.crud.CrudController;
import tools.dynamia.zk.crud.CrudControllerAware;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mario A. Serrano Leones
 */
@InstallAction
public class FindAction extends AbstractCrudAction implements CrudControllerAware, ReadableOnly {

    private static final String LAST_QUERY_TEXT = "lastQueryText";

    private CrudController crudController;

    public FindAction() {
        setName(Messages.get(FindAction.class, "find"));
        setImage("find");
        setGroup(ActionGroup.get("CRUD_SEARCH", "right"));
        setPosition(1);

    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.READ);
    }

    @Override
    public ActionRenderer getRenderer() {
        FindActionRenderer renderer = new FindActionRenderer();
        if (crudController != null) {
            renderer.setStartValue((String) crudController.getAttributes().get(LAST_QUERY_TEXT));
        }
        return renderer;
    }

    @Override
    public void setCrudController(CrudController crudController) {
        this.crudController = crudController;
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        CrudController controller = (CrudController) evt.getController();
        String text = evt.getData().toString();
        if (text != null && !text.isEmpty()) {

            if (controller.getDataPaginator() != null) {
                controller.getDataPaginator().reset();
            }

            String[] fields = loadFields(evt.getCrudView().getDataSetView().getViewDescriptor());
            var result = search(text, controller.getParams(), controller.getEntityClass(), controller.getCrudService(), fields);
            controller.setQueryResult(new ListDataSet(result));
        } else {
            controller.doQuery();
        }

        if (!crudController.isQueryResultEmpty()) {
            //noinspection unchecked
            crudController.getAttributes().put(LAST_QUERY_TEXT, text);
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List search(String txt, QueryParameters defaultParams, Class entityClass, CrudService crudService, String[] fields) {


        List result = null;
        if (ObjectOperations.isAssignable(entityClass, QueryExecuter.class)) {
            QueryExecuter queryExecuter = (QueryExecuter) ObjectOperations.newInstance(entityClass);
            QueryParameters params = new QueryParameters();
            params.setHint(QueryParameters.HINT_TEXT_SEARCH, txt);
            result = queryExecuter.executeQuery(crudService, params);
        } else {
            result = crudService.findByFields(entityClass, txt, defaultParams, fields);
        }
        return result;
    }

    private boolean isBoolean(Field field) {
        return (field.getFieldClass() == Boolean.class || field.getFieldClass() == boolean.class);
//
    }

    private String[] loadFields(ViewDescriptor viewDescriptor) {
        if (getAttribute("searchFields") != null && getAttribute("searchFields") instanceof List) {
            @SuppressWarnings("unchecked") List<String> searchFields = (List) getAttribute("searchFields");
            return searchFields.toArray(new String[0]);
        } else {
            return loadFieldsFromDescriptor(viewDescriptor);
        }
    }

    private String[] loadFieldsFromDescriptor(ViewDescriptor descriptor) {
        List<String> fieldsNames = new ArrayList<>();
        for (Field field : descriptor.getFields()) {
            if (field.isVisible() && !isBoolean(field) && field.getPropertyInfo() != null &&
                    field.getPropertyInfo().getAccessMode() == AccessMode.READ_WRITE) {
                fieldsNames.add(field.getName());
            }
        }
        return fieldsNames.toArray(new String[0]);

    }

    @Override
    public ActionExecutionResponse execute(ActionExecutionRequest request) {
        List result = null;
        if (request.getData() instanceof String text && request.getDataType() != null) {
            Class entityClass = ObjectOperations.findClass(request.getDataType());
            ViewDescriptor descriptor = Viewers.getViewDescriptor(entityClass, "table");
            result = search(text, new QueryParameters(), entityClass, crudService(), loadFields(descriptor));
        }
        return new ActionExecutionResponse(result);
    }
}
