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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import tools.dynamia.viewers.Field;
import tools.dynamia.zk.util.ZKUtil;

/**
 * Dashbaord Widget view
 *
 * @author Mario Serrano Leones
 */
public class DashboardWidgetWindow extends Div implements IdSpace {

    public static final String ON_EDIT = "onEdit";
    private DashboardWidget widget;
    private Field field;
    private boolean editable;
    private int span = 1;
    private Div content;
    private Div heading;
    private Div body;
    private DashboardContext dashboardContext;
    private Exception lastException;

    public DashboardWidgetWindow(DashboardWidget widget, Field field) {
        setZclass("dashboard-widget");


        this.widget = widget;
        this.field = field;
        this.content = new Div();

        this.content.setParent(this);


        this.heading = new Div();
        this.heading.setSclass("panel-heading");
        this.heading.setParent(content);
        this.heading.setStyle("display: none");

        this.body = new Div();
        this.body.setSclass("panel-body");

        this.body.setParent(content);


        if (widget.isTitleVisible()) {
            this.heading.setStyle("display: block");
            //content.setClosable(widget.isClosable());
            //content.setMaximizable(widget.isMaximizable());
            setEditable(widget.isEditable());
            this.content.setSclass("panel panel-colored panel-primary");
        }

    }

    public void showLoading() {
        body.getChildren().clear();
        body.appendChild(ZKUtil.createAjaxLoader(this.widget.getTitle()));
        if (getHeight() != null) {
            this.content.setVflex("1");
            this.body.setVflex("1");
        } else {
            this.content.setVflex(null);
            this.body.setVflex(null);
        }
    }

    public void initView() {

        if (lastException != null) {
            body.getChildren().clear();
            Label label = new Label("Error " + getField().getName() + ": " + lastException.getMessage());
            label.setStyle("color: red");
            body.appendChild(label);
            return;
        }

        if (dashboardContext == null) {
            return;
        }
        body.getChildren().clear();
        renderTitle();

        Object view = widget.getView();
        if (view instanceof Component) {
            ((Component) view).setParent(body);
        } else if (view instanceof String) {
            String url = (String) view;
            ZKUtil.createComponent(url, body, dashboardContext.getDataMap());
        }

        if (getHeight() != null) {
            this.content.setVflex("1");
            this.body.setVflex("1");
        } else {
            this.content.setVflex(null);
            this.body.setVflex(null);
        }
    }

    public void reload() {
        initWidget();
    }

    public DashboardContext getDashboardContext() {
        return dashboardContext;
    }

    public void setDashboardContext(DashboardContext dashboardContext) {
        this.dashboardContext = dashboardContext;
    }

    private void renderTitle() {
        if (widget.isTitleVisible()) {
            heading.getChildren().clear();
            Label caption = new Label(widget.getTitle());
            caption.setZclass("none");
            caption.setParent(heading);

            if (isEditable()) {
                Button btn = new Button();
                btn.setIconSclass("z-icon-edit");
                btn.setZclass("z-window-icon");
                btn.setParent(caption);
                btn.addEventListener(Events.ON_CLICK, evt -> Events.postEvent(new Event(ON_EDIT, this, widget)));
            }
        }
    }

    public int getSpan() {
        return span;
    }

    public void setSpan(int span) {
        if (span <= 0) {
            span = 1;
        } else if (span > 12) {
            span = 12;
        }
        this.span = span;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public DashboardWidget getWidget() {
        return widget;
    }

    public Field getField() {
        return field;
    }

    public Component getContent() {
        return content;
    }

    public void setTitleBackground(String bg) {
        if (widget.isTitleVisible()) {
            this.content.setSclass("panel panel-colored " + bg);
        }
    }

    public void initWidget() {
        if (dashboardContext != null) {
            getWidget().init(dashboardContext);
            lastException = null;
        }
    }

    public void exceptionCaught(Exception e) {
        this.lastException = e;
    }
}
