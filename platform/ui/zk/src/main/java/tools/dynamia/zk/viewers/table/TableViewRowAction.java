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

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listitem;
import tools.dynamia.viewers.ViewAction;

/**
 * Base action type for table row actions with a rendering lifecycle hook.
 * <p>
 * Subclasses can override {@link #onRendered(Object, Listitem, Component)} to
 * customize the generated action component per row.
 */
public abstract class TableViewRowAction extends ViewAction {

    /**
     * Callback invoked after the row action component has been rendered.
     *
     * @param data current row data
     * @param listitem row container
     * @param component rendered action component
     */
    protected void onRendered(Object data, Listitem listitem, Component component) {

    }
}
