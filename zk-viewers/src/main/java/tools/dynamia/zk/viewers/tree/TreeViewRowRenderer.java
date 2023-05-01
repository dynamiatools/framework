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

import org.zkoss.bind.Binder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.PropertyChangeListenerContainer;
import tools.dynamia.commons.reflect.ReflectionException;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.ComponentCustomizerUtil;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ui.IconImage;
import tools.dynamia.zk.util.ZKBindingUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
public class TreeViewRowRenderer<E> implements TreeitemRenderer<TreeViewNode<E>> {

    private ViewDescriptor viewDescriptor;
    private TreeView treeView;

    public TreeViewRowRenderer() {
    }

    public TreeViewRowRenderer(ViewDescriptor descriptor) {
        setViewDescriptor(descriptor);
    }

    public TreeViewRowRenderer(ViewDescriptor descriptor, TreeView tableView) {
        setViewDescriptor(descriptor);
        this.treeView = tableView;
    }

    public final void setViewDescriptor(ViewDescriptor descriptor) {
        this.viewDescriptor = descriptor;
    }

    public final void setTreeView(TreeView tableView) {
        this.treeView = tableView;
    }

    @Override
    public void render(Treeitem item, TreeViewNode<E> node, int index) throws Exception {
        if (node != null) {
            item.setValue(node);
            E data = node.getData();
            Binder binder = ZKBindingUtil.createBinder();
            ZKBindingUtil.initBinder(binder, item, item);
            ZKBindingUtil.bindBean(item, "bean", data);

            item.setAttribute("DATA_BINDER", binder);
            Map<String, TreeFieldComponent> fieldsComponentsMap = new HashMap<>();
            item.setAttribute("TREE_FIELD_COMPONENTS", fieldsComponentsMap);

            if (node.getStyleClass() != null) {
                item.setSclass(node.getStyleClass());
            }

            Treerow row = new Treerow();
            row.setParent(item);
            if (treeView != null) {
                renderCommonsCell(row, index);
            }

            if (node.getIcon() == null) {
                if (node.isLeaf()) {
                    node.setIcon((String) viewDescriptor.getParams().get("leafNodeIcon"));
                } else {
                    node.setIcon((String) viewDescriptor.getParams().get("nodeIcon"));
                }
            }


            int colIndex = 0;
            List<Field> fields = Viewers.getFields(viewDescriptor);
            if (!node.isRoot()) {
                for (Field field : fields) {
                    Viewers.customizeField("tree", field);
                    renderFieldCell(row, node, binder, fieldsComponentsMap, field, colIndex);
                    colIndex++;
                }
            } else {
                Treecell cell = new Treecell(node.getLabel());

                cell.setSpan(fields.size());
                cell.setParent(row);
            }

            binder.loadComponent(item, false);

            if (data instanceof PropertyChangeListenerContainer) {
                ((PropertyChangeListenerContainer) data).addPropertyChangeListener(evt -> {
                    Field field = viewDescriptor.getField(evt.propertyName());
                    if (field != null) {
                        binder.loadComponent(item, false);
                    }
                });
            }
        } else {
            throw new NullPointerException("Null data in TreeNode " + item);
        }

    }


    @SuppressWarnings("rawtypes")
    private void renderFieldCell(Treerow row, TreeViewNode<E> node, Binder binder, Map<String, TreeFieldComponent> fieldsComponentsMap,
                                 Field field, int colIndex) {
        if (field.isVisible()) {
            E data = node.getData();
            Treecell cell = new Treecell();

            cell.setParent(row);
            Object cellValue = "";

            try {

                if (field.getFieldClass() != null && field.getFieldClass().equals(boolean.class)) {
                    cellValue = BeanUtils.invokeBooleanGetMethod(data, field.getName());
                } else {
                    cellValue = BeanUtils.invokeGetMethod(data, field.getName());
                }
            } catch (ReflectionException e) {
                // Suertee
            }

            if (cellValue == null) {
                cellValue = field.getParam(Viewers.PARAM_NULLVALUE);
                Label nullValue = new Label((String) cellValue);
                nullValue.setSclass(Viewers.PARAM_NULLVALUE);
                nullValue.setParent(cell);
            } else {
                Component comp = createFieldComponent(node, cellValue, field, cell, colIndex);
                BeanUtils.setupBean(comp, field.getParams());

                if (node.getStyle() != null && comp instanceof HtmlBasedComponent) {
                    ((HtmlBasedComponent) comp).setStyle(node.getStyle());
                }

                ComponentCustomizerUtil.customizeComponent(field, comp, field.getComponentCustomizer());
                fieldsComponentsMap.put(field.getName(), new TreeFieldComponent(field.getName(), comp));
                if (isBindiable(field, comp)) {
                    Object bmapObject = field.getParam(Viewers.PARAM_BINDINGS);
                    if (bmapObject != null && bmapObject instanceof Map bindingMap) {
                        ZKBindingUtil.bindComponent(binder, comp, bindingMap, Viewers.BEAN);
                    } else {
                        String converterExpression = (String) field.getParam(Viewers.PARAM_CONVERTER);
                        String attr = BindingComponentIndex.getInstance().getAttribute(comp.getClass());
                        String expression = Viewers.BEAN + "." + field.getName();
                        ZKBindingUtil.bindComponent(binder, comp, expression, converterExpression);
                    }
                }
            }
        }
    }

    protected Component createFieldComponent(TreeViewNode<E> node, Object cellValue, Field field, Treecell cell, int colIndex) {
        Component comp = (Component) BeanUtils.newInstance(field.getComponentClass());
        comp.setParent(cell);

        if (colIndex == 0 && node.getIcon() != null) {
            Hlayout h = new Hlayout();
            h.setStyle("display:inline");
            IconImage icon = new IconImage();
            icon.setSrc(node.getIcon());
            icon.setSize(IconSize.SMALL);
            h.appendChild(icon);
            comp.setParent(h);
            h.setParent(cell);
        }

        return comp;
    }

    protected boolean isBindiable(Field field, Component comp) {
        return true;
    }

    private void renderCommonsCell(Treerow row, int index) {
        if (treeView.isCheckmark()) {
            Treecell checkCell = new Treecell();
            checkCell.setSclass("treeCheckCell");
            checkCell.setParent(row);
        }

        if (treeView.isShowRowNumber()) {
            Treecell indexCell = new Treecell(String.valueOf(index));
            indexCell.setSclass("treeIndexCell");
            indexCell.setParent(row);
        }

        if (treeView.getContextMenu() != null) {
            ((Treeitem) row.getParent()).setContext(treeView.getContextMenu());
        }
    }
}
