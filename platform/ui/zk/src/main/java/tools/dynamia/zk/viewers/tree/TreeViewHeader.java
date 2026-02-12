
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

import org.zkoss.zul.Treecol;
import tools.dynamia.viewers.Field;

/**
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings("rawtypes")
public class TreeViewHeader extends Treecol {

    /**
     *
     */
    private static final long serialVersionUID = -8401376819420773521L;

    private TreeView treeView;
    private Field field;

    public TreeViewHeader() {
    }

    public TreeViewHeader(TreeView tableView) {
        this.treeView = tableView;
    }

    public TreeViewHeader(TreeView tableView, String label) {
        super(label);
        this.treeView = tableView;
    }

    public TreeViewHeader(TreeView tableView, String label, String src) {
        super(label, src);
        this.treeView = tableView;
    }

    public TreeViewHeader(TreeView tableView, String label, String src, String width) {
        super(label, src, width);
        this.treeView = tableView;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public TreeView getTableView() {
        return treeView;
    }

}
