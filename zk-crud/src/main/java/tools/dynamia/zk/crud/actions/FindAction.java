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
package tools.dynamia.zk.crud.actions;

import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.ReadableOnly;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.reflect.AccessMode;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.query.ListDataSet;
import tools.dynamia.domain.query.QueryExecuter;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.zk.actions.FindActionRenderer;
import tools.dynamia.zk.crud.CrudController;
import tools.dynamia.zk.crud.CrudControllerAware;
import tools.dynamia.zk.crud.CrudView;

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
        renderer.setStartValue((String) crudController.getAttributes().get(LAST_QUERY_TEXT));
        return renderer;
    }

    @Override
    public void setCrudController(CrudController crudController) {
        this.crudController = crudController;
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {

        String text = evt.getData().toString();
        if (text != null && !text.isEmpty()) {
            search(text, evt);
        } else {
            evt.getController().doQuery();
        }

        if (!crudController.isQueryResultEmpty()) {
            crudController.getAttributes().put(LAST_QUERY_TEXT, text);
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void search(String txt, CrudActionEvent evt) {
        CrudController controller = (CrudController) evt.getController();
        if (controller.getDataPaginator() != null) {
            controller.getDataPaginator().reset();
        }

        String[] fields = initFields(evt);

        List result = null;
        if (BeanUtils.isAssignable(controller.getEntityClass(), QueryExecuter.class)) {
            QueryExecuter queryExecuter = (QueryExecuter) BeanUtils.newInstance(controller.getEntityClass());
            QueryParameters params = new QueryParameters();
            params.setHint(QueryParameters.HINT_TEXT_SEARCH, txt);
            result = queryExecuter.executeQuery(controller.getCrudService(), params);
        } else {
            result = controller.getCrudService().findByFields(controller.getEntityClass(), txt, controller.getParams(), fields);
        }
        controller.setQueryResult(new ListDataSet(result));

    }

    private boolean isBoolean(Field field) {
        return (field.getFieldClass() == Boolean.class || field.getFieldClass() == boolean.class);
//
    }

    private String[] initFields(CrudActionEvent evt) {
        if (getAttribute("searchFields") != null && getAttribute("searchFields") instanceof List) {
            List<String> searchFields = (List) getAttribute("searchFields");
            return searchFields.toArray(new String[0]);
        } else {
            return loadFieldsFromDescriptor(evt);
        }
    }

    private String[] loadFieldsFromDescriptor(CrudActionEvent evt) {
        CrudView view = (CrudView) evt.getCrudView();
        ViewDescriptor descriptor = view.getDataSetView().getViewDescriptor();

        List<String> fieldsNames = new ArrayList<>();
        for (Field field : descriptor.getFields()) {
            if (field.isVisible() && !isBoolean(field) && field.getPropertyInfo().getAccessMode() == AccessMode.READ_WRITE) {
                fieldsNames.add(field.getName());
            }
        }
        return fieldsNames.toArray(new String[0]);

    }
}
