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

import org.zkoss.zul.Div;
import tools.dynamia.actions.ActionLoader;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.viewers.*;
import tools.dynamia.viewers.util.Viewers;

import java.util.ArrayList;
import java.util.List;

/**
 * Render {@link ViewDescriptor} of type 'dashboard' @{@link DashboardViewType}
 *
 * @author Mario Serrano Leones
 */
public class DashboardViewRenderer implements ViewRenderer<List<DashboardWidgetWindow>> {

    private static final int DEFAULT_COLUMNS = 4;
    private LoggingService logger = new SLF4JLoggingService(DashboardViewRenderer.class);

    @Override
    public View<List<DashboardWidgetWindow>> render(ViewDescriptor descriptor, List<DashboardWidgetWindow> value) {
        System.out.println("Rendering dashboard " + descriptor);
        value = new ArrayList<>();
        ViewLayout layout = descriptor.getLayout();

        int columns = DEFAULT_COLUMNS;

        if (layout.getParams().containsKey(Viewers.LAYOUT_PARAM_COLUMNS)) {
            columns = (int) layout.getParams().get(Viewers.LAYOUT_PARAM_COLUMNS);
        }

        Dashboard dashboard = new Dashboard();
        dashboard.setViewDescriptor(descriptor);
        renderFields(dashboard, descriptor, value, columns);
        loadActions(dashboard);
        dashboard.setValue(value);
        BeanUtils.setupBean(dashboard, descriptor.getParams());
        dashboard.initWidgets();
        loadAccountId(dashboard);

        return dashboard;
    }

    private void loadAccountId(Dashboard dashboard) {
        if (dashboard.getAccountId() == null) {
            var accountService = Containers.get().findObject(AccountServiceAPI.class);
            if (accountService != null) {
                try {
                    dashboard.setAccountId(accountService.getCurrentAccountId());
                } catch (Exception e) {
                    logger.warn("Cannot load account id from dashbarod " + dashboard + ": " + e.getMessage());
                }
            }
        }
    }

    private void loadActions(Dashboard dashboard) {
        ActionLoader<DashboardAction> loader = new ActionLoader<>(DashboardAction.class);
        loader.load().forEach(dashboard::addAction);

    }

    private int toBootstrapColumns(int columns) {
        return 12 / columns;
    }

    private void renderFields(Dashboard dashboard, ViewDescriptor descriptor, List<DashboardWidgetWindow> value,
                              int columns) {
        int spaceLeft = 12;
        Div row = newRow(dashboard);
        for (Field field : Viewers.getFields(descriptor)) {

            DashboardWidget widget = getWidget(field);
            DashboardWidgetWindow window = new DashboardWidgetWindow(widget, field);
            BeanUtils.setupBean(window, field.getParams());
            window.showLoading();
            value.add(window);
            if (field.getParams().containsKey(Viewers.PARAM_SPAN)) {
                window.setSpan((int) field.getParams().get(Viewers.PARAM_SPAN));
            }
            int realSpan = getRealSpan(window.getSpan(), columns);

            String colxs = "";
            if (field.getParams().containsKey(Viewers.PARAM_SPAN + "-xs")) {
                int spanxs = (int) field.getParams().get(Viewers.PARAM_SPAN + "-xs");
                colxs = " dt-col-" + toBootstrapColumns(spanxs);
            }

            int tabletColSpan = 6;
            try {
                if (field.getParams().containsKey(Viewers.PARAM_SPAN + "-sm")) {
                    tabletColSpan = Integer.parseInt(field.getParams().get(Viewers.PARAM_SPAN + "-sm").toString());
                    tabletColSpan = getRealSpan(tabletColSpan, columns);
                }
            } catch (Exception e) {
            }

            window.setSclass("dt-col-md-" + realSpan + " dt-col-sm-" + tabletColSpan + colxs);
            spaceLeft = spaceLeft - realSpan;
            window.setParent(row);
            if (spaceLeft <= 0) {
                spaceLeft = 12;
                row = newRow(dashboard);
            }


        }
    }

    public Div newRow(Dashboard dashboard) {
        Div row = new Div();
        row.setZclass("dt-row");
        row.setParent(dashboard);
        return row;
    }

    protected int getRealSpan(int span, int columns) {
        return toBootstrapColumns(columns) * span;

    }

    private DashboardWidget getWidget(Field field) {
        String widgetId = (String) field.getParams().get("widget");
        if (widgetId == null) {
            throw new ViewRendererException("Field " + field.getName() + " dont have widget param");
        }

        DashboardWidget widget = DashboardUtils.getWidgetById(widgetId);
        if (widget == null) {
            throw new ViewRendererException("No widget found with id " + widgetId);
        }
        return widget;
    }

}
