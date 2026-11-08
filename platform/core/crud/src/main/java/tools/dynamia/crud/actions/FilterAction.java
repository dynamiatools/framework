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
import tools.dynamia.actions.Control;
import tools.dynamia.actions.HeadlessActionRenderer;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.query.FilterConditions;
import tools.dynamia.crud.query.FilterValues;

/**
 * An action that is a filter of the list: it leaves a {@link Control} in the toolbar and, when its value changes, sets the
 * condition on the list query and runs it. It declares the control and the field; there is nothing to implement.
 *
 * <pre>{@code
 * @InstallAction
 * @RunsOn(ActionRuntime.FRONTEND)
 * public class FilterByStatusAction extends FilterAction {
 *     public FilterByStatusAction() {
 *         super(Invoice.class, Control.choice("status", Status.class).allLabel("All"));
 *         setName("Status");
 *     }
 * }
 * }</pre>
 *
 * Declare {@code @RunsOn(ActionRuntime.FRONTEND)} on the concrete class, as for any action: the annotation is not inherited, and
 * without it the action is not published. A remote client then gets the {@link Control} in the metadata instead of an endpoint.
 * <p>
 * In ZK the control is drawn with the widget of its kind and this action runs on every change. A remote client draws the
 * same control from the metadata and sends the value as a filter of {@code POST /find}, so it never runs this class: the
 * filtering is the same through {@link FilterConditions}.
 */
public abstract class FilterAction extends AbstractCrudAction {

    private final Control control;
    private final Class<?> entityClass;

    /**
     * @param entityClass the entity whose list is filtered
     * @param control     the control to show; its field is a property of {@code entityClass}
     */
    protected FilterAction(Class<?> entityClass, Control control) {
        this.entityClass = entityClass;
        this.control = control;
        setApplicableClass(entityClass);
        setGroup(ActionGroup.get("CRUD"));
        setRenderer(HeadlessActionRenderer.of(control));
    }

    /** @return the control this action leaves in the toolbar */
    public Control getControl() {
        return control;
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        var controller = evt.getController();
        var field = control.field();
        var filter = FilterValues.toFilter(control, evt.getData());
        if (filter.isPresent()) {
            var property = ObjectOperations.getPropertyInfo(entityClass, field);
            if (property == null) {
                throw new IllegalStateException(entityClass.getSimpleName() + " has no property " + field);
            }
            controller.setParemeter(field, FilterConditions.toCondition(property.getType(), filter.get()));
        } else if (controller.getParams() != null) {
            controller.getParams().remove(field);
        }
        controller.doQuery();
    }
}
