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

import tools.dynamia.actions.ActionGroup;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;

import java.util.function.Consumer;

/**
 * Basic crud action
 */
public class FastCrudAction extends AbstractCrudAction {

    private final Consumer<CrudActionEvent> onAction;

    public void FastCrudAction(String name) {
        setName(name);
    }

    public FastCrudAction(String name, String image, Consumer<CrudActionEvent> onAction) {
        setName(name);
        setImage(image);
        setMenuSupported(true);
        setAttribute("showLabel", true);
        setPosition(-100);
        setGroup(ActionGroup.get(".FAST_ACTIONS"));
        this.onAction = onAction;
    }


    @Override
    public void actionPerformed(CrudActionEvent evt) {
        if (onAction != null) {
            onAction.accept(evt);
        }
    }
}
