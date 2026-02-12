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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.West;
import org.zkoss.zul.Window;
import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.ReadableOnly;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.actions.ToolbarbuttonActionRenderer;
import tools.dynamia.zk.crud.CrudView;
import tools.dynamia.zk.crud.ui.EntityFiltersPanel;
import tools.dynamia.zk.util.ZKUtil;

@InstallAction
public class FiltersAction extends AbstractCrudAction implements ReadableOnly {

    private EntityFiltersPanel filtersPanel;
    private boolean open;

    public FiltersAction() {
        setName(Messages.get(getClass(), "filters"));
        setImage("filter");
        setGroup(ActionGroup.get("CRUD_FIND"));
        setPosition(2);
    }

    @Override
    public CrudState[] getApplicableStates() {
        return new CrudState[]{CrudState.READ};
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void actionPerformed(final CrudActionEvent evt) {

        final CrudView crudView = (CrudView) evt.getCrudView();

        initFilterPanel(crudView, evt);
        Component filterContainerPanel = getFilterContainerPanel(crudView);
        open(filterContainerPanel);

        if (evt.getSource() instanceof Toolbarbutton button) {
            if (button.isChecked()) {
                close(filterContainerPanel, evt);
            } else if (open) {
                button.setChecked(true);
            }
        }

        if (filterContainerPanel instanceof Window) {
            filterContainerPanel.addEventListener(Events.ON_CLOSE, e -> close(filterContainerPanel, evt));

        }
    }

    private Component getFilterContainerPanel(final CrudView crudView) {
        Component container = null;
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

            filtersPanel.setParent(container);
        }
        return container;
    }

    private void initFilterPanel(final CrudView crudView, final CrudActionEvent evt) {
        if (filtersPanel == null) {
            try {
                filtersPanel = (EntityFiltersPanel) Viewers.getView(crudView.getBeanClass(), "entityfilters", null);
            } catch (Exception e) {
                filtersPanel = new EntityFiltersPanel(crudView.getBeanClass());
            }

            if (getAttribute("viewDescriptor") != null) {
                ViewDescriptor viewDescriptor = Viewers.findViewDescriptor(getAttribute("viewDescriptor").toString());
                filtersPanel.setViewDescriptor(viewDescriptor);
            }
            filtersPanel.addEventListener(EntityFiltersPanel.ON_SEARCH, event -> {
                QueryParameters params = (QueryParameters) event.getData();
                evt.getController().clear();
                evt.getController().setParams(params);
                evt.getController().doQuery();

                if (filtersPanel.getParent() instanceof Window window) {
                    window.detach();
                }
            });

        }
    }

    @Override
    public ActionRenderer getRenderer() {
        ToolbarbuttonActionRenderer renderer = new ToolbarbuttonActionRenderer();
        renderer.setToggleMode(true);
        return renderer;
    }

    private Window createWindow() {
        Window window = new Window(Messages.get(getClass(), "filters"), "normal", true);
        window.setPage(ZKUtil.getFirstPage());
        Caption caption = new Caption(getName());
        ZKUtil.configureComponentIcon(getImage(), caption, IconSize.NORMAL);
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
        west.setTitle(Messages.get(getClass(), "filters"));

        west.setCollapsible(true);
        west.setSize("18%");
        west.setSplittable(true);

        return west;
    }

    private void close(Component panel, CrudActionEvent evt) {
        if (panel != null) {
            panel.detach();
            panel = null;

        }
        open = false;

        evt.getController().getParams().clear();
        evt.getController().doQuery();
        filtersPanel = null;

    }

    private void open(Component panel) {
        if (panel != null) {
            if (panel instanceof West) {
                ((West) panel).setOpen(true);
            } else if (panel instanceof Window) {
                panel.setVisible(true);
            }
        }

        open = true;
    }
}
