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

package tools.dynamia.modules.entityfile.ui.actions;

import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudControllerAPI;
import tools.dynamia.crud.CrudViewComponent;
import tools.dynamia.modules.entityfile.domain.EntityFile;

import java.util.Map;

public class EntityFileActionEvent extends CrudActionEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Object targetEntity;

    public EntityFileActionEvent(Object targetEntity, Object data, Object source, CrudViewComponent view, CrudControllerAPI controller) {
        super(data, source, view, controller);
        this.targetEntity = targetEntity;
    }

    public EntityFileActionEvent(Object targetEntity, Object data, Object source, Map<String, Object> params, CrudViewComponent view,
                                 CrudControllerAPI controller) {
        super(data, source, params, view, controller);
        this.targetEntity = targetEntity;
    }

    public EntityFile getEntityFile() {
        return (EntityFile) getData();
    }

    public Object getTargetEntity() {
        return targetEntity;
    }

}
