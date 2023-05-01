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

package tools.dynamia.navigation;

import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.integration.Containers;

/**
 * {@link tools.dynamia.navigation.Page} extension that execute associated action when open
 */
public class ActionPage extends Page {

    private final Class<? extends Action> actionClass;

    public ActionPage(Class<? extends Action> actionClass) {
        this(actionClass.getName(), null, actionClass);

        String name = StringUtils.addSpaceBetweenWords(actionClass.getSimpleName());
        String id = actionClass.getSimpleName();

        Action action = Containers.get().findObject(actionClass);
        if (action != null) {
            name = action.getName();
            id = action.getId();
        }

        setName(name);
        setId(id);
    }

    public ActionPage(String id, String name, Class<? extends Action> actionClass) {
        super(id, name, actionClass.getName());
        this.actionClass = actionClass;
    }

    public ActionPage(String id, String name, boolean closeable, Class<? extends Action> actionClass) {
        super(id, name, actionClass.getName(), closeable);
        this.actionClass = actionClass;

    }


    public void execute() {
        Action action = Containers.get().findObject(actionClass);
        if (action != null) {
            action.actionPerformed(new ActionEvent(this, this));
        }
    }

    @Override
    public boolean isTemporal() {
        return true;
    }
}
