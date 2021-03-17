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
package tools.dynamia.actions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple class that represent an action component
 */
public class ActionComponent implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final Action action;
    private final ActionEventBuilder eventBuilder;
    private String align = "left";
    private final Map<String, Object> attributes = new HashMap<>();

    public ActionComponent(Action action, ActionEventBuilder eventBuilder) {
        super();
        this.action = action;
        this.eventBuilder = eventBuilder;
    }

    public void doAction() {
        action.actionPerformed(eventBuilder.buildActionEvent(this, getAttributes()));
    }

    public String getLabel() {
        return action.getName();
    }

    public String getIcon() {
        return action.getImage();
    }

    public String getTitle() {
        return action.getDescription();
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getAlign() {
        return align;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public Action getAction() {
        return action;
    }

    public boolean isSeparator() {
        return false;
    }
}
