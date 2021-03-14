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
package tools.dynamia.actions;

import tools.dynamia.commons.BeanUtils;

/**
 * Helper class to create and action instance without need to extend classes or implement methods
 */
public class FastAction extends AbstractAction {


    private OnActionPerfomed onActionPerfomed;

    public FastAction(String name, OnActionPerfomed onActionPerfomed) {
        this(name, null, onActionPerfomed);
    }

    public FastAction(String name, String image, OnActionPerfomed onActionPerfomed) {
        this(name, image, null, null, onActionPerfomed);
    }

    public FastAction(String name, String image, String description, OnActionPerfomed onActionPerfomed) {
        this(name, image, description, null, onActionPerfomed);
    }

    public FastAction(String name, String image, String description, ActionRenderer<?> actionRenderer, OnActionPerfomed onActionPerfomed) {
        this(null, name, image, description, actionRenderer, onActionPerfomed);
    }

    public FastAction(String name, String image, ActionRenderer<?> actionRenderer, OnActionPerfomed onActionPerfomed) {
        this(null, name, image, null, actionRenderer, onActionPerfomed);
    }

    public FastAction(String id, String name, String image, String description, ActionRenderer<?> actionRenderer,
                      OnActionPerfomed onActionPerfomed) {

        if (id == null) {
            id = name.replace(" ", "");
        }
        setId(id);
        setName(name);
        setImage(image);
        setDescription(description);
        setRenderer(actionRenderer);
        this.onActionPerfomed = onActionPerfomed;
        init();
    }

    protected void init() {

    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (onActionPerfomed != null) {
            onActionPerfomed.actionPerformed(evt);
        }
    }

    public void execute() {
        actionPerformed(new ActionEvent(null, this));
    }

    public void setActionRendererClass(Class<? extends ActionRenderer<?>> actionRendererClass) {
        if (actionRendererClass != DefaultActionRenderer.class) {
            setRenderer(BeanUtils.newInstance(actionRendererClass));
        }
    }


}
