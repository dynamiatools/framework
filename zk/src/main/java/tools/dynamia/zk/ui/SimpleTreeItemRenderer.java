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
package tools.dynamia.zk.ui;

import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import tools.dynamia.commons.BeanUtils;

public class SimpleTreeItemRenderer implements TreeitemRenderer {

    @Override
    public void render(Treeitem item, Object node, int index) {

        final TreeNode dtn = (TreeNode) node;
        final Object data = dtn.getData();
        item.setValue(data);

        Treerow treeRow = new Treerow();
        item.appendChild(treeRow);

        treeRow.appendChild(new Treecell(BeanUtils.getInstanceName(data)));

    }

}
