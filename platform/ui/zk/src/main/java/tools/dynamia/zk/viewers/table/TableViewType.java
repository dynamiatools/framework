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




package tools.dynamia.zk.viewers.table;

import tools.dynamia.integration.sterotypes.Component;
import tools.dynamia.viewers.ViewRenderer;
import tools.dynamia.viewers.ViewType;

/**
 * View type registration for table-based views in the ZK viewers module.
 * <p>
 * Provides the canonical type name and creates the default {@link TableViewRenderer}.
 */
@Component
public class TableViewType implements ViewType {

    /**
     * Canonical identifier used to request table views.
     */
    public static final String NAME = "table";

    /**
     * Returns the registered view type identifier.
     *
     * @return {@link #NAME}
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Creates the renderer responsible for table view instances.
     *
     * @return table view renderer
     */
    @Override
    public ViewRenderer getViewRenderer() {
        return new TableViewRenderer();
    }
}
