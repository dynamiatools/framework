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

import org.zkoss.zul.AbstractTreeModel;

/**
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings("unchecked")
public class EntityTreeModel<E> extends AbstractTreeModel<EntityTreeNode<E>> {

    /**
     *
     */
    private static final long serialVersionUID = 817662050637827509L;

    @SuppressWarnings("unchecked")
    public EntityTreeModel(EntityTreeNode<E> root) {
        //noinspection unchecked
        super(root);
        //noinspection unchecked
        root.setModel(this);
        //noinspection unchecked
        if (getChildCount(root) > 0) {
            for (Object object : root.getChildren()) {
                @SuppressWarnings("unchecked") EntityTreeNode<E> node = (EntityTreeNode<E>) object;
                node.setModel(this);
            }
        }
    }

    @Override
    public boolean isLeaf(EntityTreeNode<E> node) {
        return node.isLeaf();
    }

    @Override
    public EntityTreeNode<E> getChild(EntityTreeNode<E> parent, int index) {
        return (EntityTreeNode<E>) parent.getChildren().get(index);
    }

    @Override
    public int getChildCount(EntityTreeNode<E> parent) {
        return parent.getChildren().size();

    }

}
