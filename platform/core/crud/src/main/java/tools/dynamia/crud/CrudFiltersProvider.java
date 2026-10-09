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
package tools.dynamia.crud;

import tools.dynamia.actions.Action;

/**
 * SPI behind {@code FiltersAction}: opens or closes the panel where the user builds the filters of a CRUD query. Each front
 * end that has such a panel registers its own as a bean (ZK does); a front end without one implements the behaviour on the
 * client and the action does nothing on the server.
 */
public interface CrudFiltersProvider {

    /**
     * Toggles the filters panel of the CRUD the action was run from.
     *
     * @param action the filters action, which holds the state of the panel between clicks
     * @param evt    the event, with the CRUD view and its controller
     */
    void toggle(Action action, CrudActionEvent evt);
}
