
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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;

import tools.dynamia.io.FileInfo;
import tools.dynamia.zk.crud.ui.ChildrenLoader;
import tools.dynamia.zk.crud.ui.EntityTreeModel;
import tools.dynamia.zk.crud.ui.EntityTreeNode;
import tools.dynamia.zk.crud.ui.LazyEntityTreeNode;

/**
 *
 * @author Mario Serrano Leones
 */
public class DirectoryExplorer extends Window implements ChildrenLoader<FileInfo>, EventListener<Event> {

    private String value;
    private EntityTreeModel<FileInfo> treeModel;
    private Tree tree;
    private EntityTreeNode<FileInfo> rootNode;
    private boolean showHiddenFolders;

    public DirectoryExplorer() {
        init();
    }

    public void reset() {
        initModel();
    }

    private void init() {
        tree = new Tree();
        tree.setHflex("1");
        tree.setVflex("1");
        tree.addEventListener(Events.ON_CLICK, this);
        tree.setItemRenderer(new DirectoryTreeItemRenderer());
        appendChild(tree);

        setVflex("1");
        setHflex("1");

        initModel();
    }

    private void initModel() {
        FileInfo file = new FileInfo(new File("/"));

        rootNode = new EntityTreeNode<>(file);
        treeModel = new EntityTreeModel<>(rootNode);
        for (EntityTreeNode<FileInfo> entityTreeNode : getSubdirectories(file)) {
            rootNode.addChild(entityTreeNode);
        }
        tree.setModel(treeModel);

    }

    private Collection<EntityTreeNode<FileInfo>> getSubdirectories(FileInfo file) {
        File[] subs = file.getFile().listFiles(pathname -> {
            if (pathname.isDirectory()) {
                if (!isShowHiddenFolders()) {
                    return !pathname.isHidden() && !pathname.getName().startsWith(".");
                }
                return true;
            }
            return false;
        });

        List<EntityTreeNode<FileInfo>> subdirectories = new ArrayList<EntityTreeNode<FileInfo>>();
        if (subs != null) {
            for (File sub : subs) {
                subdirectories.add(new DirectoryTreeNode(new FileInfo(sub), this));
            }
        }

        subdirectories.sort(Comparator.comparing(o -> o.getData().getName()));

        return subdirectories;
    }

    @Override
    public void loadChildren(LazyEntityTreeNode<FileInfo> node) {
        for (EntityTreeNode<FileInfo> treeNode : getSubdirectories(node.getData())) {
            node.addChild(treeNode);
        }
    }

    @Override
    public void onEvent(Event event) {
        Treeitem item = tree.getSelectedItem();
        if (item != null) {
            DirectoryTreeNode node = item.getValue();
            setValue(node.getData().getFile().getAbsolutePath());
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        Events.postEvent(new Event(Events.ON_CHANGE, this, value));
    }

    public boolean isShowHiddenFolders() {
        return showHiddenFolders;
    }

    public void setShowHiddenFolders(boolean showHiddenFolders) {
        this.showHiddenFolders = showHiddenFolders;
    }

}
