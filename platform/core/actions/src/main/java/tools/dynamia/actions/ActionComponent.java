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
package tools.dynamia.actions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a UI component or element associated with an {@link Action}.
 * <p>
 * This class encapsulates the logic and metadata for rendering and executing an action in the user interface.
 * It provides access to the underlying action, event builder, alignment, and custom attributes.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     ActionComponent component = new ActionComponent(action, eventBuilder);
 *     component.setAlign("right");
 *     component.getAttributes().put("style", "color: red;");
 *     component.doAction();
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public class ActionComponent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The underlying {@link Action} associated with this component.
     */
    private final Action action;

    /**
     * The {@link ActionEventBuilder} used to build events for this action.
     */
    private final ActionEventBuilder eventBuilder;

    /**
     * The alignment of the component in the UI (e.g., "left", "right").
     */
    private String align = "left";

    /**
     * Custom attributes for the component, allowing additional metadata or configuration.
     */
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * Constructs an ActionComponent with the specified action and event builder.
     *
     * @param action the action associated with this component
     * @param eventBuilder the event builder for creating action events
     */
    public ActionComponent(Action action, ActionEventBuilder eventBuilder) {
        super();
        this.action = action;
        this.eventBuilder = eventBuilder;
    }

    /**
     * Executes the associated action using the event builder and component attributes.
     */
    public void doAction() {
        Actions.run(action, eventBuilder, this, getAttributes());
    }

    /**
     * Returns the label (name) of the action for display purposes.
     *
     * @return the action label
     */
    public String getLabel() {
        return action.getName();
    }

    /**
     * Returns the icon of the action for display purposes.
     *
     * @return the action icon
     */
    public String getIcon() {
        return action.getImage();
    }

    /**
     * Returns the title (description) of the action for display purposes.
     *
     * @return the action title
     */
    public String getTitle() {
        return action.getDescription();
    }

    /**
     * Returns the custom attributes for this component.
     *
     * @return a map of attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Returns the alignment of the component in the UI.
     *
     * @return the alignment string
     */
    public String getAlign() {
        return align;
    }

    /**
     * Sets the alignment of the component in the UI.
     *
     * @param align the alignment string to set (e.g., "left", "right")
     */
    public void setAlign(String align) {
        this.align = align;
    }

    /**
     * Returns the underlying {@link Action} associated with this component.
     *
     * @return the action instance
     */
    public Action getAction() {
        return action;
    }

    /**
     * Indicates whether this component is a separator (default: false).
     *
     * @return {@code false} (not a separator)
     */
    public boolean isSeparator() {
        return false;
    }
}
