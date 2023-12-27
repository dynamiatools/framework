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

import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.event.TreeDataEvent;
import tools.dynamia.zk.viewers.tree.TreeViewNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mario A. Serrano Leones
 */
public class EntityTreeNode<E> extends TreeViewNode<E> implements Serializable {

    private String tooltiptext;
    private String contextMenuID;
    private String viewDescriptorID;

    private EventListener onRightClickListener;
    private EventListener onOpenListener;
    private EntityTreeModel<E> model;


    public EntityTreeNode(String label, String icon) {
        this(null, label, icon);
    }

    public EntityTreeNode(E entity) {
        this(entity, entity != null ? entity.toString() : null);
    }

    public EntityTreeNode(E entity, String label) {
        this(entity, label, null);

    }

    public EntityTreeNode(E entity, String label, String icon) {
        super(entity);
        setLabel(label);
        setIcon(icon);
    }

    @Override
    public void addChild(TreeViewNode<E> child) {
        super.addChild(child);
        if (child instanceof EntityTreeNode etn) {
            //noinspection unchecked
            etn.setModel(model);

            if (model != null) {
                int[] path = getModel().getPath(this);

                int index = indexOf(child);

                getModel().fireEvent(TreeDataEvent.INTERVAL_ADDED, path, index, index);
            }
        }
    }

    @Override
    public void removeChild(TreeViewNode<E> child) {

        if (child instanceof EntityTreeNode) {

            int[] childPath = getModel().getPath((EntityTreeNode<E>) child);
            model.removeSelectionPath(childPath);
            model.removeOpenPath(childPath);
            int index = indexOf(child);
            getChildren().remove(index);
            child.setParent(null);
            getModel().fireEvent(TreeDataEvent.INTERVAL_REMOVED, getModel().getPath(this), index, index, childPath);
        } else {
            super.removeChild(child);
        }
    }

    @Override
    public void remove() {

        super.remove();
    }

    public EntityTreeNode<E> addChild(E entity) {
        EntityTreeNode<E> node = new EntityTreeNode<>(entity);
        this.addChild(node);
        return node;
    }


    /**
     * Add children using a entity list
     *
     * @param entities
     * @return children nodes
     */
    public List<EntityTreeNode<E>> addChildren(List<? extends E> entities) {
        return addChildren(entities, null);
    }

    /**
     * Add children using an entity list with default icon
     * @param entities
     * @param defaultIcon
     * @return chidlren nodes
     */
    public List<EntityTreeNode<E>> addChildren(List<? extends E> entities, String defaultIcon) {
        List<EntityTreeNode<E>> newChildren = new ArrayList<>();
        entities.forEach(e -> {
            var childNode = addChild(e);
            childNode.setIcon(defaultIcon);
            newChildren.add(childNode);
        });

        return newChildren;
    }

    public String getViewDescriptorID() {
        return viewDescriptorID;
    }

    public void setViewDescriptorID(String viewDescriptorID) {
        this.viewDescriptorID = viewDescriptorID;
    }

    public E getEntity() {
        return getData();
    }

    public void setEntity(E entity) {
        setData(entity);
    }

    public String getTooltiptext() {
        return tooltiptext;
    }

    public void setTooltiptext(String tooltiptext) {
        this.tooltiptext = tooltiptext;
    }

    public String getContextMenuID() {
        return contextMenuID;
    }

    public void setContextMenuID(String contextMenuID) {
        this.contextMenuID = contextMenuID;
    }

    public EventListener getOnRightClickListener() {
        return onRightClickListener;
    }

    public void setOnRightClickListener(EventListener onRightClickListener) {
        this.onRightClickListener = onRightClickListener;
    }

    public EventListener getOnOpenListener() {
        return onOpenListener;
    }

    public void setOnOpenListener(EventListener onOpenListener) {
        this.onOpenListener = onOpenListener;
    }

    public EntityTreeModel<E> getModel() {
        return model;
    }

    public void setModel(EntityTreeModel<E> model) {
        this.model = model;
    }

    @Override
    public EntityTreeNode<E> getParent() {
        return (EntityTreeNode<E>) super.getParent();

    }

    @Override
    public String toString() {
        if (getEntity() != null) {
            return getEntity().toString();
        } else if (getLabel() != null) {
            return getLabel();
        } else {
            return super.toString();
        }
    }

    public Class getEntityType() {
        if (getData() != null) {
            return getData().getClass();
        } else {
            return null;
        }
    }

    public void reload() {
        int index = getParent().indexOf(this);
        int[] path = getModel().getPath(this);


        getModel().fireEvent(TreeDataEvent.CONTENTS_CHANGED, path, index, index, path);
    }
}
