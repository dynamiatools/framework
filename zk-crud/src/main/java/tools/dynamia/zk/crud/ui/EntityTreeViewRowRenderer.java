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
package tools.dynamia.zk.crud.ui;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Treeitem;
import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.zk.viewers.tree.TreeView;
import tools.dynamia.zk.viewers.tree.TreeViewNode;
import tools.dynamia.zk.viewers.tree.TreeViewRowRenderer;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class EntityTreeViewRowRenderer<E extends AbstractEntity> extends TreeViewRowRenderer<E> {

    public EntityTreeViewRowRenderer() {
    }

    public EntityTreeViewRowRenderer(ViewDescriptor descriptor) {
        super(descriptor);
    }

    public EntityTreeViewRowRenderer(ViewDescriptor descriptor, TreeView tableView) {
        super(descriptor, tableView);
    }

    @Override
    public void render(Treeitem item, TreeViewNode<E> data, int index) throws Exception {
        super.render(item, data, index); //To change body of generated methods, choose Tools | Templates.

        if (data instanceof EntityTreeNode) {
            EntityTreeNode node = (EntityTreeNode) data;
            if (node.getOnRightClickListener() != null) {
                item.addEventListener(Events.ON_RIGHT_CLICK, node.getOnRightClickListener());
            }
            if (node.getOnOpenListener() != null) {
                item.addEventListener(Events.ON_OPEN, node.getOnOpenListener());
            }
        }
    }

}
