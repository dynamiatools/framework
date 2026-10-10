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
package tools.dynamia.crud.actions;

import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.DelegateActionRender;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.ReadableOnly;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.reflect.AccessMode;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudControllerAPI;
import tools.dynamia.crud.CrudControllerAware;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.query.ListDataSet;
import tools.dynamia.domain.query.QueryExecuter;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Searches the entities of a CRUD by the text the user typed, over the visible fields of its view (or the
 * {@code searchFields} attribute of the action). The search box itself is drawn by the front end: this action asks for the
 * renderer named {@value #RENDERER} (an {@code ActionRenderProvider} bean), and the data of the action event is the text.
 *
 * @author Mario A. Serrano Leones
 */
@InstallAction
@RunsOn(ActionRuntime.FRONTEND)
public class FindAction extends AbstractCrudAction implements CrudControllerAware, ReadableOnly {

    /** Name of the {@code ActionRenderProvider} that draws the search box. */
    public static final String RENDERER = "find";

    private static final String LAST_QUERY_TEXT = "lastQueryText";

    private CrudControllerAPI<?> crudController;

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
        return new DelegateActionRender(RENDERER);
    }

    /**
     * @return the text of the last search that found something, to refill the search box when the CRUD is shown again,
     * or {@code null}
     */
    public String getLastQueryText() {
        return crudController == null ? null : (String) crudController.getAttributes().get(LAST_QUERY_TEXT);
    }

    @Override
    public void setCrudController(CrudControllerAPI<?> crudController) {
        this.crudController = crudController;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void actionPerformed(CrudActionEvent evt) {
        CrudControllerAPI controller = evt.getController();
        String text = evt.getData() == null ? null : evt.getData().toString();

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

        if (!controller.isQueryResultEmpty()) {
            controller.getAttributes().put(LAST_QUERY_TEXT, text);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List search(String txt, QueryParameters defaultParams, Class entityClass, CrudService crudService, String[] fields) {
        List result;
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
        return field.getFieldClass() == Boolean.class || field.getFieldClass() == boolean.class;
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
}
