
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

package tools.dynamia.zk.viewers.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Mario A. Serrano Leones
 */
public class TreeViewNode<E> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1741561796996850134L;
    private TreeViewNode<E> parent;
    private E data;
    private final List<TreeViewNode<E>> children = new ArrayList<>();
    protected boolean root;

    private String icon;
    private String label;
    private boolean open;
    private String style;
    private String styleClass;

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public TreeViewNode(E data) {
        this.data = data;
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    public TreeViewNode<E> getParent() {
        return parent;
    }

    public void setParent(TreeViewNode<E> parent) {
        this.parent = parent;
    }

    public List<TreeViewNode<E>> getChildren() {
        return children;
    }

    public void addChild(TreeViewNode<E> child) {
        if (child == this) {
            throw new IllegalArgumentException("Hey you cannot add child of the same node");
        }

        getChildren().add(child);
        child.parent = this;

    }

    public void removeChild(TreeViewNode<E> child) {
        getChildren().remove(child);
    }

    public void removeChildren() {
        List<TreeViewNode<E>> childrenCopy = new ArrayList<>(children);
        for (TreeViewNode<E> treeViewNode : childrenCopy) {
            removeChild(treeViewNode);
        }
    }

    public void remove() {
        getParent().removeChild(this);

    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isLeaf() {
        return getChildren().isEmpty();
    }

    public int indexOf(TreeViewNode<E> child) {
        return getChildren().indexOf(child);
    }

    public int getChildCount() {
        return getChildren().size();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "data=" + data + '}';
    }

    public boolean isRoot() {
        return root;
    }

    public void open() {
        setOpen(true);

    }

    public void close() {
        setOpen(false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeViewNode<?> that = (TreeViewNode<?>) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
