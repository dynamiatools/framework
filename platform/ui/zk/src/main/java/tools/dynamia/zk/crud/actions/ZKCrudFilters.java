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
package tools.dynamia.zk.crud.actions;

import tools.dynamia.integration.sterotypes.Provider;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.West;
import org.zkoss.zul.Window;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionRenderProvider;
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudFiltersProvider;
import tools.dynamia.crud.actions.FiltersAction;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.actions.ToolbarbuttonActionRenderer;
import tools.dynamia.zk.crud.CrudView;
import tools.dynamia.zk.crud.ui.EntityFiltersPanel;
import tools.dynamia.zk.util.ZKUtil;

/**
 * The filters panel of {@link FiltersAction} in ZK: an {@link EntityFiltersPanel} in the west area of the CRUD layout (or
 * in a window on small screens), plus the toggle button that opens it. The state of the panel between clicks lives in an
 * attribute of the action, which is created per CRUD view.
 *
 * @author Mario A. Serrano Leones
 */
@Provider
public class ZKCrudFilters implements CrudFiltersProvider, ActionRenderProvider {

    private static final String STATE = "zk.filters.state";

    /** What the panel needs to remember between clicks. */
    private static class State {
        private EntityFiltersPanel filtersPanel;
        private boolean open;
    }

    @Override
    public String getName() {
        return FiltersAction.RENDERER;
    }

    @Override
    public ActionRenderer<?> getActionRenderer() {
        ToolbarbuttonActionRenderer renderer = new ToolbarbuttonActionRenderer();
        renderer.setToggleMode(true);
        return renderer;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void toggle(Action action, CrudActionEvent evt) {
        State state = state(action);
        final CrudView crudView = (CrudView) evt.getCrudView();
        initFilterPanel(state, action, crudView, evt);

        org.zkoss.zk.ui.Component filterContainerPanel = getFilterContainerPanel(state, crudView);
        open(state, filterContainerPanel);

        if (evt.getSource() instanceof Toolbarbutton button) {
            if (button.isChecked()) {
                close(state, filterContainerPanel, evt);
            } else if (state.open) {
                button.setChecked(true);
            }
        }

        if (filterContainerPanel instanceof Window) {
            filterContainerPanel.addEventListener(Events.ON_CLOSE, e -> close(state, filterContainerPanel, evt));
        }
    }

    private State state(Action action) {
        if (!(action.getAttribute(STATE) instanceof State)) {
            action.setAttribute(STATE, new State());
        }
        return (State) action.getAttribute(STATE);
    }

    @SuppressWarnings("rawtypes")
    private org.zkoss.zk.ui.Component getFilterContainerPanel(State state, final CrudView crudView) {
        org.zkoss.zk.ui.Component container;
        if (crudView.getLayout() instanceof Borderlayout && !HttpUtils.isSmartphone()) {
            West west = ((Borderlayout) crudView.getLayout()).getWest();
            if (west == null) {
                west = createWest();
                west.setParent(crudView.getLayout());
            }
            container = west;
        } else {
            container = createWindow();
        }

        if (container != null) {
            container.getChildren().clear();
            state.filtersPanel.setParent(container);
        }
        return container;
    }

    @SuppressWarnings("rawtypes")
    private void initFilterPanel(State state, Action action, final CrudView crudView, final CrudActionEvent evt) {
        if (state.filtersPanel == null) {
            try {
                state.filtersPanel = (EntityFiltersPanel) Viewers.getView(crudView.getBeanClass(), "entityfilters", null);
            } catch (Exception e) {
                state.filtersPanel = new EntityFiltersPanel(crudView.getBeanClass());
            }

            if (action.getAttribute("viewDescriptor") != null) {
                ViewDescriptor viewDescriptor = Viewers.findViewDescriptor(action.getAttribute("viewDescriptor").toString());
                state.filtersPanel.setViewDescriptor(viewDescriptor);
            }

            state.filtersPanel.addEventListener(EntityFiltersPanel.ON_SEARCH, event -> {
                QueryParameters params = (QueryParameters) event.getData();
                evt.getController().clear();
                evt.getController().setParams(params);
                evt.getController().doQuery();
                if (state.filtersPanel.getParent() instanceof Window window) {
                    window.detach();
                }
            });
        }
    }

    private Window createWindow() {
        Window window = new Window(Messages.get(FiltersAction.class, "filters"), "normal", true);
        window.setPage(ZKUtil.getFirstPage());
        Caption caption = new Caption(Messages.get(FiltersAction.class, "filters"));
        ZKUtil.configureComponentIcon("filter", caption, IconSize.NORMAL);
        caption.setParent(window);
        if ("smartphone".equals(HttpUtils.detectDevice())) {
            window.setHeight("95%");
            window.setWidth("95%");
        } else {
            window.setHeight("400px");
            window.setWidth("400px");
        }
        window.doModal();
        return window;
    }

    private West createWest() {
        West west = new West();
        west.setTitle(Messages.get(FiltersAction.class, "filters"));
        west.setCollapsible(true);
        west.setSize("18%");
        west.setSplittable(true);
        return west;
    }

    private void close(State state, org.zkoss.zk.ui.Component panel, CrudActionEvent evt) {
        if (panel != null) {
            panel.detach();
        }
        state.open = false;
        evt.getController().getParams().clear();
        evt.getController().doQuery();
        state.filtersPanel = null;
    }

    private void open(State state, org.zkoss.zk.ui.Component panel) {
        if (panel != null) {
            if (panel instanceof West) {
                ((West) panel).setOpen(true);
            } else if (panel instanceof Window) {
                panel.setVisible(true);
            }
        }
        state.open = true;
    }
}
