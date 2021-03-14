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
package tools.dynamia.zk.actions;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import tools.dynamia.actions.*;
import tools.dynamia.zk.ComponentAliasIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
public class ActionPanel extends Div {

    /**
     *
     */
    private static final long serialVersionUID = -7223083923123174793L;

    static {
        ComponentAliasIndex.getInstance().add(ActionPanel.class);
    }

    private List<Action> actions = new ArrayList<>();
    private ActionEventBuilder eventBuilder;
    private ActionRenderer<?> actionRenderer = new BoostrapButtonActionRenderer();

    public ActionPanel() {
        init();
        this.eventBuilder = ActionEvent::new;
    }

    public ActionPanel(ActionEventBuilder eventBuilder) {
        this.eventBuilder = eventBuilder;
        init();
    }

    private void init() {
        setSclass("actionpanel");
    }

    public void addAction(Action action) {
        actions.add(action);
        renderAction(action);
    }

    private void renderAction(Action action) {
        if (eventBuilder == null) {
            throw new ActionRendererException("Cannot render action without ActionEventBuilder");
        }

        ActionRenderer<?> currentActionRenderer = action.getRenderer();
        if (currentActionRenderer == null) {
            currentActionRenderer = getActionRenderer();
        }

        Component component = (Component) currentActionRenderer.render(action, eventBuilder);
        component.setParent(this);

        if (component instanceof HtmlBasedComponent) {
            HtmlBasedComponent hcom = (HtmlBasedComponent) component;
            hcom.setSclass(hcom.getSclass() + " actionpanel-a");
        }
    }

    public void setEventBuilder(ActionEventBuilder eventBuilder) {

        this.eventBuilder = eventBuilder;
        if (actions != null && eventBuilder != null) {

            getChildren().clear();
            actions.forEach(this::renderAction);
            Events.postEvent(new Event(Events.ON_CHANGE, this));
        }
    }

    public ActionEventBuilder getEventBuilder() {
        return eventBuilder;
    }


    public ActionRenderer<?> getActionRenderer() {
        return actionRenderer;
    }

    public void setActionRenderer(ActionRenderer<?> actionRenderer) {
        this.actionRenderer = actionRenderer;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
        if (actions != null && eventBuilder != null) {

            getChildren().clear();
            actions.forEach(this::renderAction);
            Events.postEvent(new Event(Events.ON_CHANGE, this));
        }
    }
}
