
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

package tools.dynamia.modules.entityfile.ui.components;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;

import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.ui.icons.IconsTheme;

/**
 *
 * @author Mario Serrano Leones
 */
public class DirectoryTreeItemRenderer implements TreeitemRenderer<DirectoryTreeNode>, EventListener<Event> {

    @Override
    public void render(Treeitem item, DirectoryTreeNode data, int index) {

        item.setValue(data);
        item.setLabel(data.getData().getName());
        item.addEventListener(Events.ON_OPEN, this);
        item.addEventListener(Events.ON_CLOSE, this);

        setupIcon(item);
    }

    @Override
    public void onEvent(Event event) {
        Treeitem item = (Treeitem) event.getTarget();
        DirectoryTreeNode node = item.getValue();
        if (item.isOpen()) {
            node.load();
        } else {
            node.getChildren().clear();
        }

        setupIcon(item);
    }

    private void setupIcon(Treeitem item) {

        DirectoryTreeNode node = item.getValue();

        if (item.isOpen()) {
            item.setImage(IconsTheme.get().getIcon("folder-open").getRealPath(IconSize.SMALL));
        } else {
            item.setImage(IconsTheme.get().getIcon("folder").getRealPath(IconSize.SMALL));
        }

        if (!node.getData().getFile().canRead() || !node.getData().getFile().canWrite()) {
            if (item.isOpen()) {
                item.setImage(IconsTheme.get().getIcon("folder-red-open").getRealPath(IconSize.SMALL));
            } else {
                item.setImage(IconsTheme.get().getIcon("folder-red").getRealPath(IconSize.SMALL));
            }
        }
    }

}
