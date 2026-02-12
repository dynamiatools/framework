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

import org.zkoss.zk.ui.event.Events;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.actions.Actions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.zk.actions.ZKActionRenderer;
import tools.dynamia.zk.crud.ui.EntityPickerBox;

public class EntityPickerActionRenderer extends ZKActionRenderer<EntityPickerBox> {

    private final Class entityClass;
    private final QueryParameters defaultParams = new QueryParameters();
    private String popupWidth;

    public EntityPickerActionRenderer(Class entityClass) {
        super();
        this.entityClass = entityClass;
    }

    @Override
    public EntityPickerBox render(Action action, ActionEventBuilder actionEventBuilder) {
        EntityPickerBox entityPicker = new EntityPickerBox(entityClass);
        configureProperties(entityPicker, action);

        entityPicker.addEventListener(Events.ON_SELECT, e -> Actions.run(action, actionEventBuilder, entityPicker, entityPicker.getSelected()));

        return entityPicker;
    }

    public void addDefaultParam(String name, Object value) {
        defaultParams.add(name, value);
    }

    public QueryParameters getDefaultParams() {
        return defaultParams;
    }

    public String getPopupWidth() {
        return popupWidth;
    }

    public void setPopupWidth(String popupWidth) {
        this.popupWidth = popupWidth;
    }

    public Class getEntityClass() {
        return entityClass;
    }
}
