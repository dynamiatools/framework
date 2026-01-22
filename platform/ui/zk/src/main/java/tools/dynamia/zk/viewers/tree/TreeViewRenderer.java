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

import org.zkoss.zul.Frozen;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecol;
import org.zkoss.zul.Treecols;
import org.zkoss.zul.TreeitemRenderer;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.LocalizedMessagesProvider;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.FieldGroup;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewRenderer;
import tools.dynamia.viewers.util.ViewRendererUtil;
import tools.dynamia.viewers.util.Viewers;

import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
public class TreeViewRenderer<T> implements ViewRenderer<TreeModel<TreeViewNode<T>>> {

    private LocalizedMessagesProvider messagesProvider;

    @Override
    public View<TreeModel<TreeViewNode<T>>> render(ViewDescriptor descriptor, TreeModel<TreeViewNode<T>> value) {

        TreeView<T> tree = new TreeView<>(descriptor);
        if (descriptor.getParams().get("itemRenderer") != null) {
            tree.setItemRenderer((TreeitemRenderer) ObjectOperations.newInstance(descriptor.getParams().get("itemRenderer").toString()));
        } else {
            tree.setItemRenderer(new TreeViewRowRenderer(descriptor, tree));
        }
        if (tree.getItemRenderer() != null) {
            TreeViewRowRenderer renderer = tree.getItemRenderer();
            renderer.setTreeView(tree);
            renderer.setViewDescriptor(descriptor);
        }
        tree.setValue(value);
        tree.setVflex(true);
        tree.setSclass("tree-view");
        tree.setMold("paging");
        tree.getPagingChild().setMold("os");

        ObjectOperations.setupBean(tree, descriptor.getParams());
        renderHeaders(tree, descriptor);
        return tree;
    }

    private void renderHeaders(TreeView tree, ViewDescriptor descriptor) {
        Treecols head = new Treecols();
        head.setParent(tree);

        if (tree.isCheckmark()) {
            Treecol checkHeader = new Treecol("", null, "32px");
            checkHeader.setSclass("treeCheckHeader");
            checkHeader.setParent(head);
        }

        if (tree.isShowRowNumber()) {
            Treecol indexHeader = new Treecol("", null, "40px");
            indexHeader.setSclass("treeIndexHeader");
            indexHeader.setParent(head);
        }


        for (Field field : ViewRendererUtil.filterRenderableFields(tree, descriptor)) {
            TreeViewHeader header = new TreeViewHeader(tree, field.getLocalizedLabel(Messages.getDefaultLocale()));
            header.setTooltiptext(field.getLocalizedDescription(Messages.getDefaultLocale()));
            header.setParent(head);
            header.setField(field);
            header.setAttribute("field-name", field.getName());
            header.setAttribute("field-class", field.getFieldClass());

            try {
                Map headerParams = (Map) field.getParams().get("header");
                if (headerParams != null) {
                    //noinspection unchecked
                    ObjectOperations.setupBean(header, headerParams);
                }
            } catch (Exception e) {
                LoggingService.get(TreeViewRenderer.class).error("Error setting header for field " + field.getName(), e);
            }
        }

        if (descriptor.getParams().containsKey("frozenColumns")) {
            Frozen frozen = new Frozen();
            frozen.setColumns(Integer.parseInt(descriptor.getParams().get("frozenColumns").toString()));
            frozen.setParent(tree);
        }
    }

    public static boolean hasIcons(ViewDescriptor descriptor) {
        return descriptor.getParams().containsKey("nodeIcon") || descriptor.getParams().containsKey("leafNodeIcon");
    }

    protected String filterFieldGroupLabel(FieldGroup fieldGroup, String label) {
        if (messagesProvider == null) {
            return label;
        } else {
            return messagesProvider.getMessage("Group " + fieldGroup.getName(), Viewers.buildMessageClasffier(fieldGroup.getViewDescriptor()), Messages.getDefaultLocale(), label);
        }
    }

    protected String filterFieldDescription(Field field, String description) {
        if (messagesProvider == null) {
            return description;
        } else {
            return messagesProvider.getMessage(field.getName() + " Description", Viewers.buildMessageClasffier(field.getViewDescriptor()), Messages.getDefaultLocale(), description);
        }
    }

    public LocalizedMessagesProvider getMessagesProvider() {
        return messagesProvider;
    }

    public void setMessagesProvider(LocalizedMessagesProvider messagesProvider) {
        this.messagesProvider = messagesProvider;
    }
}
