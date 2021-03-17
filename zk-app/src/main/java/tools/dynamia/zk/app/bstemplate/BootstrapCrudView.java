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

package tools.dynamia.zk.app.bstemplate;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.*;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.crud.ChangedStateEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.crud.actions.CancelAction;
import tools.dynamia.ui.icons.Icon;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.ui.icons.IconType;
import tools.dynamia.ui.icons.IconsTheme;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.actions.ActionToolbar;
import tools.dynamia.zk.actions.ButtonActionRenderer;
import tools.dynamia.zk.actions.MenuitemActionRenderer;
import tools.dynamia.zk.actions.ToolbarbuttonActionRenderer;
import tools.dynamia.zk.crud.CrudView;
import tools.dynamia.zk.crud.CrudViewRenderer;
import tools.dynamia.zk.crud.actions.FindAction;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.ZKWrapperView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BootstrapCrudView<T> extends CrudView<T> {

    /**
     *
     */
    private static final long serialVersionUID = 1773528227238113127L;
    private Div formLeftActions;
    private Div formRightActions;
    private Div formActions;
    private Component leftActions;
    private Component rightActions;
    private Menupopup actionsMenu;
    private Button actionsButton;

    private Borderlayout borderlayout;
    private Map<ActionGroup, Div> actionGroupContainers;
    private ButtonActionRenderer defaultActionRenderer;

    @Override
    protected void buildGeneralView() {
        super.buildGeneralView();

        borderlayout = (Borderlayout) layout;

        Div header = new Div();
        header.setZclass("crudview-header " + ActionToolbar.CONTAINER_SCLASS);
        header.setSclass("clearfix");
        header.setParent(borderlayout.getNorth());
        toolbarContainer = header;

        formActions = new Div();
        formActions.setZclass("crudview-footer row");

        formLeftActions = new Div();
        formLeftActions.setZclass("col-xs-12 col-md-12");
        formLeftActions.setParent(formActions);

        formRightActions = new Div();
        formRightActions.setZclass("col-xs-12 col-md-12");
        formRightActions.setStyle("text-align: right");
        formRightActions.setParent(formActions);

        String menuId = "actionMenu" + StringUtils.randomString().substring(0, 4);
        actionsMenu = new Menupopup();
        actionsMenu.setId(menuId);
        actionsMenu.setParent(this);
        actionsButton = new Button();
        actionsButton.setZclass("btn btn-primary actions");
        actionsButton.setPopup(menuId + ", after_start");
        ZKUtil.configureComponentIcon("process", actionsButton, IconSize.NORMAL);

        addCrudStateChangedListener(this::controlChangedState);
    }

    @Override
    protected void buildToolbars() {
        if (!HttpUtils.isSmartphone()) {
            Div toolbar = new Div();
            toolbar.setZclass("btn-toolbar");

            leftActions = toolbar;

            toolbar = new Div();
            toolbar.setZclass("btn-toolbar");
            toolbar.setSclass("actiontb-right");
            rightActions = toolbar;
        }
    }

    @Override
    protected void buildToolbarContainer() {
        if (!HttpUtils.isSmartphone()) {
            toolbarContainer.appendChild(leftActions);
            toolbarContainer.appendChild(rightActions);
        }

    }

    @SuppressWarnings("rawtypes")
    @Override
    protected ActionRenderer getDefaultActionRenderer() {
        if (defaultActionRenderer == null) {
            defaultActionRenderer = new ButtonActionRenderer();
            defaultActionRenderer.setZclass("btn btn-default");
            defaultActionRenderer.setShowLabels(false);

        }
        return defaultActionRenderer;
    }

    @Override
    protected void loadActions(CrudState state) {
        actionsMenu.getChildren().clear();

        if (HttpUtils.isSmartphone() && state == CrudState.READ) {

            toolbarContainer.getChildren().clear();

            toolbarContainer.appendChild(actionsButton);
        }

        super.loadActions(state);
    }

    protected Component renderAction(Action action) {
        ActionRenderer actionRenderer = action.getRenderer();
        if (actionRenderer == null) {
            actionRenderer = getDefaultActionRenderer();
        }
        Component component = (Component) actionRenderer.render(action, this);

        String actionId = action.getId();
        if (action.getAttribute("internalId") != null) {
            actionId = action.getAttribute("internalId").toString();
        }
        if (component instanceof HtmlBasedComponent) {
            HtmlBasedComponent hcom = (HtmlBasedComponent) component;
            hcom.setSclass("actiontb-a " + actionId + " " + hcom.getSclass());

            if (HttpUtils.isSmartphone()) {
                if (!(component instanceof Button)) {
                    hcom.setSclass(hcom.getSclass() + " flexit");
                }
            }
        }

        return component;

    }

    @Override
    protected void showActionGroup(ActionGroup actionGroup) {
        if (HttpUtils.isSmartphone() && getState() == CrudState.READ) {
            MenuitemActionRenderer renderer = new MenuitemActionRenderer();
            for (Action action : actionGroup.getActions()) {
                if (action.getRenderer() == null || action.getRenderer() instanceof ToolbarbuttonActionRenderer) {
                    Menuitem menuitem = renderer.render(action, this);
                    actionsMenu.appendChild(menuitem);
                } else {
                    showAction(actionGroup, action);
                }
            }
        } else {

            actionGroup.getActions().forEach(a -> showAction(actionGroup, a));
        }
    }

    @Override
    protected void showAction(ActionGroup actionGroup, Action action) {
        if ((getState() == CrudState.CREATE || getState() == CrudState.UPDATE)
                && (action.getRenderer() == null || action.getRenderer() instanceof ToolbarbuttonActionRenderer)) {
            ButtonActionRenderer renderer = new ButtonActionRenderer();
            Button button = renderer.render(action, this);
            button.setAttribute(ACTION, action);
            applyButtonStyle(button, action);
            addButton(actionGroup, button);
        } else {
            fixFindAction(action);

            Component actionComp = renderAction(action);
            if (!HttpUtils.isSmartphone()) {
                Component group = getActionGroupContainer(actionGroup);
                group.appendChild(actionComp);
                if (group.getParent() == null) {
                    group.setParent("left".equals(actionGroup.getAlign()) ? leftActions : rightActions);
                }

            } else {
                toolbarContainer.appendChild(actionComp);
            }
        }
    }

    private Component getActionGroupContainer(ActionGroup actionGroup) {
        if (actionGroupContainers == null) {
            this.actionGroupContainers = new HashMap<>();
        }

        Div group = this.actionGroupContainers.get(actionGroup);
        if (group == null) {
            group = new Div();
            group.setZclass("btn-group");
            this.actionGroupContainers.put(actionGroup, group);
        }

        return group;
    }

    private void fixFindAction(Action action) {
        if (HttpUtils.isSmartphone()) {
            if (action instanceof FindAction) {
                action.setAttribute("zclass", "form-control");

            }
        }
    }

    private void displayMenuActions(List<Component> actionComponents) {

        toolbarContainer.appendChild(actionsButton);
        MenuitemActionRenderer menuRenderer = new MenuitemActionRenderer();
        for (Component component : actionComponents) {
            Action action = (Action) component.getAttribute(ACTION);
            if (action != null) {
                Menuitem menuitem = menuRenderer.render(action, this);
                actionsMenu.appendChild(menuitem);
            }
        }

    }

    private void addButton(ActionGroup group, Component btn) {

        Div btnGroup = new Div();
        if (HttpUtils.isSmartphone()) {
            btnGroup.setZclass("btn-group col-md-3 col-sm-3 col-xs-12");
        } else {
            btnGroup.setZclass("btn-group");
        }
        btnGroup.appendChild(btn);

        if ("right".equals(group.getAlign())) {
            btnGroup.setSclass("pull-right");
            formRightActions.appendChild(btnGroup);
        } else {
            formLeftActions.appendChild(btnGroup);
        }

    }

    private void applyButtonStyle(Button button, Action action) {
        if (action instanceof CancelAction) {
            button.setZclass("btn btn-danger");
        } else if (action.getPosition() <= 1) {
            button.setZclass("btn btn-primary");
        } else {
            button.setZclass("btn btn-default");
        }

        Icon icon = IconsTheme.get().getIcon(action.getImage());
        if (icon.getType() == IconType.IMAGE) {
            button.setImage(null);
        }
    }

    private void controlChangedState(ChangedStateEvent evt) {
        CrudState crudState = evt.getNewState();

        if (crudState == CrudState.READ) {
            borderlayout.getNorth().setVisible(true);
        } else {
            if (leftActions != null && leftActions.getChildren().isEmpty() && rightActions != null && rightActions.getChildren().isEmpty()) {
                borderlayout.getNorth().setVisible(false);
            }
        }
    }

    @Override
    public void clearActions() {
        super.clearActions();
        actionsMenu.getChildren().clear();
        formLeftActions.getChildren().clear();
        formRightActions.getChildren().clear();
        if (leftActions != null && rightActions != null) {
            leftActions.getChildren().clear();
            rightActions.getChildren().clear();
        }
        if (actionGroupContainers != null) {
            this.actionGroupContainers.clear();
        }

    }

    @Override
    protected void addFormViewToContainer(String formViewTitle) {
        ZKWrapperView<Object> wrapperView = new ZKWrapperView<>(formView);
        formActions.setParent(wrapperView);
        formViewContainer.addView(formViewTitle, wrapperView);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected CrudViewRenderer getCrudViewRenderer() {
        return new BoostrapCrudViewRenderer<>();
    }

}
