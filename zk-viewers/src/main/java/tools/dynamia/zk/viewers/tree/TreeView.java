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

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import tools.dynamia.domain.query.DataSet;
import tools.dynamia.viewers.DataSetView;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings("unchecked")
public class TreeView<T> extends Tree implements DataSetView<TreeModel<TreeViewNode<T>>> {

    static {
        BindingComponentIndex.getInstance().put("value", TreeView.class);
        ComponentAliasIndex.getInstance().add(TreeView.class);
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * Event to listen double click events in ListItmes
     */
    public static final String ON_ITEM_DOUBLE_CLICK = "onItemDoubleClick";
    public static final String ON_ITEM_CLICK = "onItemDoubleClick";
    public static final String ON_ITEMS_RENDERED = "onItemsRendered";
    public static final String ON_VALUE_CHANGED = "onValueChanged";

    private ViewDescriptor viewDescriptor;

    private boolean showRowNumber;

    private View parentView;
    private String orderBy;
    private Menupopup contextMenu;
    private TreeViewRowRenderer itemRenderer;
    private Object source;
    private Consumer onSourceChange;

    public TreeView() {
    }

    public TreeView(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
    }

    @Override
    public TreeModel<TreeViewNode<T>> getValue() {
        return getModel();
    }

    @Override
    public void setValue(DataSet<TreeModel<TreeViewNode<T>>> dataSet) {
        setValue(dataSet.getData());
    }

    @Override
    public void setValue(TreeModel<TreeViewNode<T>> value) {
        setModel(value);
        Events.postEvent(ON_VALUE_CHANGED, this, null);
    }

    @Override
    public T getSelected() {
        if (getSelectedItem() != null) {
            T selectedValue = getSelectedItem().getValue();
            if (selectedValue instanceof TreeViewNode) {
                selectedValue = (T) ((TreeViewNode) selectedValue).getData();
            }
            return selectedValue;
        } else {
            return null;
        }
    }

    @Override
    public void setSelected(Object selected) {

    }

    public TreeFieldComponent getTreeFieldComponent(String fieldName, Treeitem item) {
        try {

            Map<String, TreeFieldComponent> tfcMap = (Map<String, TreeFieldComponent>) item
                    .getAttribute("TREE_FIELD_COMPONENTS");
            return tfcMap.get(fieldName);
        } catch (Exception e) {
            return null;
        }
    }

    public Treeitem getTreeItemByValue(T value) {
        Treeitem target = null;
        for (Object obj : getItems()) {
            Treeitem item = (Treeitem) obj;
            if (item.getValue().equals(value)) {
                target = item;
                break;
            }
        }
        return target;
    }

    @Override
    public ViewDescriptor getViewDescriptor() {
        return viewDescriptor;
    }

    @Override
    public void setViewDescriptor(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
    }

    @Override
    public void setItemRenderer(TreeitemRenderer renderer) {
        super.setItemRenderer(renderer);
        if (renderer instanceof TreeViewRowRenderer) {
            TreeViewRowRenderer tvrr = (TreeViewRowRenderer) renderer;
            tvrr.setViewDescriptor(viewDescriptor);
            tvrr.setTreeView(this);
        }
    }

    public void setShowRowNumber(boolean showRowNumber) {
        this.showRowNumber = showRowNumber;
    }

    public boolean isShowRowNumber() {
        return showRowNumber;
    }

    public boolean isTreeitemSelected() {
        return getSelectedCount() > 0;
    }

    @Override
    public View getParentView() {
        return parentView;
    }

    @Override
    public void setParentView(View parentView) {
        this.parentView = parentView;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Menupopup getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(Menupopup contextMenu) {
        this.contextMenu = contextMenu;
    }

    @Override
    public TreeViewRowRenderer getItemRenderer() {
        return itemRenderer;
    }

    public void setItemRenderer(TreeViewRowRenderer itemRenderer) {
        this.itemRenderer = itemRenderer;
        super.setItemRenderer(itemRenderer);
    }

    @Override
    public boolean isEmpty() {
        return getModel() == null || getModel().getRoot() == null;
    }

    @Override
    public void setSource(Object source) {
        this.source = source;
        if (onSourceChange != null) {
            onSourceChange.accept(source);
        }
    }

    @Override
    public Object getSource() {
        return source;
    }

    public void onSourceChanged(Consumer onSourceChange) {
        this.onSourceChange = onSourceChange;
    }
}
