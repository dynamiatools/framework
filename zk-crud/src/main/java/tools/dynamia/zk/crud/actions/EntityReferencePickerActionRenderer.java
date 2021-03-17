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

import org.zkoss.zk.ui.event.Events;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.zk.actions.ZKActionRenderer;
import tools.dynamia.zk.crud.ui.EntityPickerBox;
import tools.dynamia.zk.crud.ui.EntityReferencePickerBox;

import java.io.Serializable;

public class EntityReferencePickerActionRenderer extends ZKActionRenderer<EntityReferencePickerBox> {

    private final String entityAlias;
    private Serializable selectedId;


    public EntityReferencePickerActionRenderer(String entityAlias) {
        this.entityAlias = entityAlias;
    }

    public EntityReferencePickerActionRenderer(String entityAlias, Serializable selectedId) {
        this.entityAlias = entityAlias;
        this.selectedId = selectedId;
    }

    @Override
    public EntityReferencePickerBox render(Action action, ActionEventBuilder actionEventBuilder) {
        EntityReferencePickerBox entityPicker = new EntityReferencePickerBox();
        entityPicker.setEntityAlias(entityAlias);
        entityPicker.setSelectedId(selectedId);
        configureProperties(entityPicker, action);

        entityPicker.addEventListener(Events.ON_SELECT, e -> {
            ActionEvent event = actionEventBuilder.buildActionEvent(entityPicker, null);
            event.setData(entityPicker.getSelected());
            if(entityPicker.getSelected()!=null) {
                entityPicker.setValue(entityPicker.getSelected().getName());
            }
            action.actionPerformed(event);
        });

        return entityPicker;
    }


}
