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

package tools.dynamia.zk.viewers;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.*;
import tools.dynamia.actions.*;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.crud.ChangedStateEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.actions.*;
import tools.dynamia.zk.crud.CrudView;
import tools.dynamia.zk.crud.CrudViewRenderer;
import tools.dynamia.zk.crud.actions.FindAction;
import tools.dynamia.zk.util.ZKUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BootstrapCrudView<T> extends CrudView<T> {

    /**
     *
     */
    private static final long serialVersionUID = 1773528227238113127L;

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


        String menuId = "actionMenu" + StringUtils.randomString().substring(0, 4);
        actionsMenu = new Menupopup();
        actionsMenu.setId(menuId);
        actionsMenu.setParent(this);

        actionsButton = new Button();
        actionsButton.setZclass("btn btn-sm btn-primary actions actiontb-a");
        actionsButton.setPopup(menuId + ", after_start");
        ZKUtil.configureComponentIcon("fa-ellipsis-v", actionsButton, IconSize.SMALL);

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
            defaultActionRenderer.setZclass("btn btn-sm btn-default");
            defaultActionRenderer.setShowLabels(false);

        }
        return defaultActionRenderer;
    }

    @Override
    protected void loadActions(CrudState state) {
        actionsMenu.getChildren().clear();

        if (HttpUtils.isSmartphone()) {
            toolbarContainer.getChildren().clear();
            if (state == CrudState.READ) {
                toolbarContainer.appendChild(actionsButton);
            } else {
                borderlayout.getNorth().setVisible(false);
            }
        }

        super.loadActions(state);
    }

    protected Component renderAction(Action action) {
        ActionRenderer actionRenderer = action.getRenderer();
        if (actionRenderer == null) {
            actionRenderer = getDefaultActionRenderer();
        }
        Component component = (Component) Actions.render(actionRenderer, action, this);

        String actionId = action.getId();
        if (action.getAttribute("internalId") != null) {
            actionId = action.getAttribute("internalId").toString();
        }
        if (component instanceof HtmlBasedComponent hcom) {
            String background = (String) action.getAttribute("background");
            String color = (String) action.getAttribute("color");
            hcom.setSclass("actiontb-a " + actionId);

            if (background != null && background.startsWith(".")) {
                hcom.setSclass(hcom.getSclass() + " " + background.substring(1));
            }
            if (color != null && color.startsWith(".")) {
                hcom.setSclass(hcom.getSclass() + " " + color.substring(1));
            }
            var tooltip = action.getLocalizedName();
            hcom.setTooltiptext(tooltip);
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
            renderSmartphoneReadActions(actionGroup);
        } else {
            actionGroup.getActions().forEach(a -> showAction(actionGroup, a));
        }
    }

    protected void renderSmartphoneReadActions(ActionGroup actionGroup) {
        MenuitemActionRenderer renderer = new MenuitemActionRenderer();
        for (Action action : actionGroup.getActions()) {
            ActionRenderer actionRenderer = action.getRenderer();

            if (action.getClass().isAnnotationPresent(PrimaryAction.class)) {
                showAction(actionGroup, action);
            } else if (actionRenderer == null || actionRenderer instanceof ToolbarbuttonActionRenderer
                    || actionRenderer instanceof ButtonActionRenderer) {
                actionsMenu.appendChild(Actions.render(renderer, action, this));
            } else if (actionRenderer instanceof MenuitemActionRenderer mir) {
                actionsMenu.appendChild(Actions.render(mir, action, this));
            } else if (actionRenderer instanceof MenuActionRenderer mar) {
                Menubar menubar = Actions.render(mar, action, this);
                actionsMenu.appendChild(menubar.getFirstChild());
            } else {
                showAction(actionGroup, action);
            }
        }
    }

    @Override
    protected void showAction(ActionGroup actionGroup, Action action) {
        if (isFormActive()) {
            formView.addAction(action);
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

                if (actionComp instanceof Button) {
                    ((Button) actionComp).setLabel(null);
                    ((Button) actionComp).setSclass(((Button) actionComp).getSclass() + " actions");
                }
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


    private void controlChangedState(ChangedStateEvent evt) {
        CrudState crudState = evt.getNewState();

        switch (crudState) {
            case READ -> borderlayout.getNorth().setVisible(true);
            default -> {
                if (leftActions != null && rightActions != null) {
                    if (leftActions.getChildren().isEmpty() && rightActions.getChildren().isEmpty()) {
                        borderlayout.getNorth().setVisible(false);
                    }
                }
            }
        }
    }

    @Override
    public void clearActions() {
        super.clearActions();
        actionsMenu.getChildren().clear();
        if (formView != null) {
            formView.clearActions();
        }
        if (leftActions != null && rightActions != null) {
            leftActions.getChildren().clear();
            rightActions.getChildren().clear();
        }
        if (actionGroupContainers != null) {
            this.actionGroupContainers.clear();
        }

    }


    @SuppressWarnings("rawtypes")
    @Override
    protected CrudViewRenderer getCrudViewRenderer() {
        return new BootstrapCrudViewRenderer();
    }

}
