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
 * Extension of {@link Page} that executes an associated {@link Action} when opened.
 * <p>
 * This class is useful for pages that trigger an action automatically upon being accessed.
 * The action is resolved from the container and executed with this page as the event source.
 * </p>
 */
public class ActionPage extends Page {

    /**
     * The class type of the associated action to execute.
     */
    private final Class<? extends Action> actionClass;

    /**
     * Constructs an ActionPage using the given Action class.
     * The page name and id are derived from the action class or instance if available.
     *
     * @param actionClass the class of the action to associate and execute
     */
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

    /**
     * Constructs an ActionPage with custom id, name and action class.
     *
     * @param id the page id
     * @param name the page name
     * @param actionClass the class of the action to associate and execute
     */
    public ActionPage(String id, String name, Class<? extends Action> actionClass) {
        super(id, name, actionClass.getName());
        this.actionClass = actionClass;
    }

    /**
     * Constructs an ActionPage with custom id, name, closeable flag and action class.
     *
     * @param id the page id
     * @param name the page name
     * @param closeable whether the page can be closed
     * @param actionClass the class of the action to associate and execute
     */
    public ActionPage(String id, String name, boolean closeable, Class<? extends Action> actionClass) {
        super(id, name, actionClass.getName(), closeable);
        this.actionClass = actionClass;
    }

    /**
     * Executes the associated action for this page, if available.
     * The action is resolved from the container and triggered with this page as the event source.
     */
    public void execute() {
        Action action = Containers.get().findObject(actionClass);
        if (action != null) {
            action.actionPerformed(new ActionEvent(this, this));
        }
    }

    /**
     * Indicates that this page is temporal (not persistent).
     *
     * @return always true for ActionPage
     */
    @Override
    public boolean isTemporal() {
        return true;
    }
}
