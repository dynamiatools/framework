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
package tools.dynamia.zk.crud;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treeitem;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.domain.query.DataSet;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.domain.util.TreeCrudUtil;
import tools.dynamia.navigation.Page;
import tools.dynamia.zk.crud.ui.*;
import tools.dynamia.zk.viewers.tree.TreeModelDataSet;

import java.util.Collection;
import java.util.List;

/**
 * @param <E>
 * @author Mario A. Serrano Leones
 */
public class TreeCrudController<E> extends CrudController<E> implements ChildrenLoader<E>, EventListener {

    private String parentName = "parent";
    private String rootLabelField;
    private String rootLabel;
    private String rootIcon;

    private TreeCrudUtil<E> util;

    public TreeCrudController() {
        afterInit();
    }

    @Override
    protected void afterInit() {
        if (parentName == null) {
            parentName = "parent";
        }
        util = new TreeCrudUtil<>(crudService, getEntityClass(), parentName);
    }

    @Override
    public void query() {
        TreeModel model = createModel(loadRoots());
        setQueryResult(new TreeModelDataSet(model));
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
        afterInit();
    }

    protected TreeModel createModel(Collection<E> roots) {

        RootTreeNode root = new RootTreeNode("Root");

        RootTreeNode virtualRoot = createRootNode();
        root.addChild(virtualRoot);

        EntityTreeModel<E> model = new EntityTreeModel<>(root);

        if (roots != null) {
            for (E parent : roots) {
                EntityTreeNode<E> node = newNode(parent);
                node.setModel(model);
                virtualRoot.addChild(node);
            }
        }
        model.addOpenPath(new int[]{0});
        return model;
    }

    protected RootTreeNode createRootNode() {
        E rootEntity = null;
        String label = rootLabel;
        String icon = rootIcon;
        try {
            rootEntity = BeanUtils.newInstance(getEntityClass());
        } catch (Exception e) {
            // ignore
        }

        if (label == null && icon == null) {
            Page currentPage = Page.getCurrent();
            if (currentPage != null) {
                label = currentPage.getName();
                icon = currentPage.getIcon();
            }
        }

        if (label == null) {
            label = StringUtils.addSpaceBetweenWords(StringUtils.capitalize(getEntityClass().getName()));
        }

        if (rootLabelField != null && rootEntity != null) {
            BeanUtils.invokeSetMethod(rootEntity, rootLabelField, label);
        }
        return new RootTreeNode(rootEntity, icon, label);
    }

    @Override
    public void loadChildren(LazyEntityTreeNode<E> node) {
        E parent = node.getEntity();
        if (parent != null) {
            Collection<E> children = loadChildren(parent);
            if (children != null) {
                for (E child : children) {
                    node.addChild(newNode(child));
                }
            }
        }
    }

    @Override
    public void newEntity() {
        super.newEntity();
        if (getSelected() != null && DomainUtils.findEntityId(getSelected()) != null) {
            BeanUtils.invokeSetMethod(getEntity(), parentName, getSelected());
        }
    }

    protected EntityTreeNode<E> newNode(E value) {
        EntityTreeNode<E> node = new LazyEntityTreeNode<>(value, this);
        node.setOnOpenListener(this);
        if (isLeaf(node.getData())) {
            node = new EntityTreeNode<>(value);
        }
        return node;
    }

    @Override
    public void onEvent(Event evt) {
        if (evt.getTarget() instanceof Treeitem) {
            Treeitem item = (Treeitem) evt.getTarget();
            if (item.isOpen()) {
                if (item.getValue() instanceof LazyEntityTreeNode) {
                    LazyEntityTreeNode<E> node = item.getValue();
                    node.load();
                }

            }
        }
    }

    protected boolean isLeaf(E data) {
        return false;
    }

    @Override
    public void setQueryResult(DataSet queryResult) {
        if (queryResult.getData() instanceof Collection) {
            super.setQueryResult(new TreeModelDataSet(createModel((List<E>) queryResult.getData())));
        } else {
            super.setQueryResult(queryResult); // To change body of generated
            // methods, choose Tools |
            // Templates.
        }
    }

    protected Collection<E> loadRoots() {
        return util.getRoots();
    }

    protected Collection<E> loadChildren(E parent) {
        return util.getChildren(parent);
    }

    public String getRootLabelField() {
        return rootLabelField;
    }

    public void setRootLabelField(String rootLabelField) {
        this.rootLabelField = rootLabelField;
    }

    public String getRootLabel() {
        return rootLabel;
    }

    public void setRootLabel(String rootLabel) {
        this.rootLabel = rootLabel;
    }

    public String getRootIcon() {
        return rootIcon;
    }

    public void setRootIcon(String rootIcon) {
        this.rootIcon = rootIcon;
    }

}
