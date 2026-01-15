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

package tools.dynamia.modules.dashboard;

import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.zk.actions.ActionToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Dashboard main view. Need to implement a {@link ViewDescriptor} in YML or XML to describe dashboard widget. See {@link DashboardViewRenderer}
 *
 * @author Mario Serrano Leones
 */
public class Dashboard extends Div implements View<List<DashboardWidgetWindow>>, IdSpace {


    public static final String COMMAND = "dashboard-loaded";
    private LoggingService logger = new SLF4JLoggingService(Dashboard.class);
    private ViewDescriptor viewDescriptor;
    private View parentView;
    private List<DashboardWidgetWindow> value = new ArrayList<>();
    private ActionToolbar actionToolbar;
    private boolean loaded;
    private boolean rendered;
    private boolean asyncLoad = true;
    private Long accountId;


    public Dashboard() {
        setSclass("dashboard");
        actionToolbar = new ActionToolbar((source, params) -> new ActionEvent(Dashboard.this, Dashboard.this));
        appendChild(actionToolbar);
    }

    public void initWidgets() {
        this.loaded = false;
        this.rendered = false;

        if (isAsyncLoad()) {
            if (EventQueues.exists(viewDescriptor.getId())) {
                UIMessages.showMessage("Cargando Dashboard.. espere");
                return; //busy
            }

            var queue = EventQueues.lookup(viewDescriptor.getId());
            queue.subscribe(op -> loadWidgets(), callback -> {
                renderWidgets();
                EventQueues.remove(viewDescriptor.getId());
            });

            queue.publish(new Event("start"));
        } else {
            addEventListener(Events.ON_FULFILL, evt -> {


                for (DashboardWidgetWindow window : value) {
                    try {
                        new DashboardContext(this, window, window.getField());
                        window.initWidget();
                        window.initView();
                    } catch (Exception e) {
                        logger.error("Error loading dashboard widget -  " + window.getWidget(), e);
                        window.exceptionCaught(e);
                    }
                }
                loaded = true;
                rendered = true;
            });
            Events.postEvent(new Event(Events.ON_FULFILL, this));
        }

    }

    private void loadWidgets() {
        logger.info("Loading dashboard widgets ");
        for (DashboardWidgetWindow window : value) {
            try {
                new DashboardContext(this, window, window.getField());
                window.initWidget();
            } catch (Exception e) {
                logger.error("Error loading dashboard widget -  " + window.getWidget(), e);
                window.exceptionCaught(e);
            }
        }
        this.loaded = true;
        logger.info("Dashboard " + getViewDescriptor().getId() + " Loaded");
    }


    public void renderWidgets() {
        try {
            for (DashboardWidgetWindow window : value) {
                try {
                    new DashboardContext(this, window, window.getField());
                    window.initView();
                } catch (Exception e) {
                    window.exceptionCaught(e);
                    window.initView();
                    logger.error("Error rendering dashboard widget -  " + window.getWidget(), e);
                }
            }
            this.rendered = true;
            logger.info("Dashboard Rendered");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateWidgets(Map<String, Object> params) {

        for (DashboardWidgetWindow window : value) {
            window.getWidget().update(params);
        }
    }


    @Override
    public ViewDescriptor getViewDescriptor() {
        return viewDescriptor;
    }

    @Override
    public void setViewDescriptor(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
    }

    @Override
    public View getParentView() {
        return parentView;
    }

    @Override
    public void setParentView(View parentView) {
        this.parentView = parentView;
    }

    @Override
    public List<DashboardWidgetWindow> getValue() {
        return value;
    }

    @Override
    public void setValue(List<DashboardWidgetWindow> value) {
        this.value = value;
    }

    public DashboardWidgetWindow getWidgetWindow(String name) {
        Optional<DashboardWidgetWindow> windows = value.stream().filter(w -> w.getField().getName().equals(name))
                .findFirst();
        return windows.orElse(null);
    }

    public ActionToolbar getActionToolbar() {
        return actionToolbar;
    }

    public void addAction(Action action) {
        actionToolbar.addAction(action);
    }

    public void setActionsVisible(boolean visible) {
        actionToolbar.setVisible(visible);
    }

    public boolean isActionsVisible() {
        return actionToolbar.isVisible();
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isRendered() {
        return rendered;
    }

    public boolean isAsyncLoad() {
        return asyncLoad;
    }

    public void setAsyncLoad(boolean asyncLoad) {
        this.asyncLoad = asyncLoad;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
