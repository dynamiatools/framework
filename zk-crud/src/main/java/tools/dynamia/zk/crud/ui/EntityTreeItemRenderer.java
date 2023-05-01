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
package tools.dynamia.zk.crud.ui;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import tools.dynamia.ui.icons.Icon;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.ui.icons.IconsTheme;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class EntityTreeItemRenderer implements TreeitemRenderer {

    @Override
    public void render(Treeitem item, Object data, int index) {

        item.setValue(data);
        EntityTreeNode node = (EntityTreeNode) data;
        Treerow row = new Treerow();
        Treecell cell = new Treecell();
        cell.setAttribute("entity", node.getEntity());
        cell.setAttribute("node", node);
        cell.setContext(node.getContextMenuID());

        Hlayout hl = new Hlayout();
        Icon icon = IconsTheme.get().getIcon(node.getIcon(), "file");
        hl.appendChild(new Image(icon.getRealPath(IconSize.SMALL)));
        hl.appendChild(new Label(node.getLabel()));
        hl.setStyle("display:inline-block; _display: inline");

        cell.appendChild(hl);

        if (node.getOnRightClickListener() != null) {
            //noinspection unchecked
            cell.addEventListener(Events.ON_RIGHT_CLICK, node.getOnRightClickListener());
        }
        if (node.getOnOpenListener() != null) {
            //noinspection unchecked
            item.addEventListener(Events.ON_OPEN, node.getOnOpenListener());
        }
        cell.setParent(row);
        addColumns(item, row, node);
        row.setParent(item);
    }

    /**
     * Overwrite this method is you wanna add new columns to tree
     *
     */
    protected void addColumns(Treeitem item, Treerow row, EntityTreeNode node) {
        // for extension
    }
}
