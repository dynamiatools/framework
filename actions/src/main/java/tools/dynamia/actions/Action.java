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

import tools.dynamia.commons.LocalizedMessagesProvider;
import tools.dynamia.commons.Messages;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Central interface representing a user or application action in the Dynamia Tools framework.
 * <p>
 * Any class that implements or extends this interface is considered an actionable entity, capable of being executed
 * by the user or system. Actions are the core building blocks for user interaction, automation, and integration.
 * </p>
 * <p>
 * <b>Key features:</b>
 * <ul>
 *   <li>Defines all essential properties for an action: id, name, description, image, group, position, attributes, etc.</li>
 *   <li>Supports localization for name and description via {@link LocalizedMessagesProvider}.</li>
 *   <li>Allows custom rendering through {@link ActionRenderer}.</li>
 *   <li>Supports hierarchical actions via parent/child relationships.</li>
 *   <li>Can be executed server-side using {@link #actionPerformed(ActionEvent)} or externally via {@link #execute(ActionExecutionRequest)} (e.g., REST API).</li>
 *   <li>Provides visibility and enablement controls for UI and security.</li>
 * </ul>
 * <p>
 * <b>Usage:</b>
 * <pre>
 *     // Example: Custom action implementation
 *     public class SaveAction extends AbstractAction {
 *         public SaveAction() {
 *             setId("save");
 *             setName("Save");
 *             setDescription("Save the current document");
 *             setImage("save");
 *         }
 *         @Override
 *         public void actionPerformed(ActionEvent evt) {
 *             // Save logic here
 *         }
 *     }
 * </pre>
 * <p>
 * Actions can be triggered from UI components, toolbars, menus, or programmatically, and can respond to external requests
 * such as REST API calls using {@link #execute(ActionExecutionRequest)}.
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public interface Action extends Comparable<Action>, Serializable {

    /**
     * Returns the unique identifier for this action.
     *
     * @return the id
     */
    String getId();

    /**
     * Returns the display name of the action, typically used as a label in UI components.
     *
     * @return the name
     */
    String getName();

    /**
     * Returns a text description of the action, commonly used as tooltip text or help.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Returns the image icon name or path for this action.
     *
     * @return the image
     */
    String getImage();

    /**
     * Returns the group to which this action belongs (for grouping in UI or logic).
     *
     * @return the group
     */
    ActionGroup getGroup();

    /**
     * Returns the position value for ordering actions in lists, toolbars, etc.
     *
     * @return the position
     */
    double getPosition();

    /**
     * Returns a custom attribute value by key.
     *
     * @param key the attribute key
     * @return the attribute value
     */
    Object getAttribute(String key);

    /**
     * Sets a custom attribute value by key.
     *
     * @param key   the attribute key
     * @param value the attribute value
     */
    void setAttribute(String key, Object value);

    /**
     * Returns all custom attributes as a map.
     *
     * @return the attributes map
     */
    Map<String, Object> getAttributes();

    /**
     * Returns the renderer for this action, used to create UI components.
     *
     * @return the renderer
     */
    ActionRenderer getRenderer();

    /**
     * Indicates whether this action is enabled (can be executed or interacted with).
     *
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Returns the localized name of this action for the default locale.
     *
     * @return the localized name
     */
    default String getLocalizedName() {
        return getLocalizedName(Messages.getDefaultLocale());
    }

    /**
     * Returns the localized name of this action for a specific locale.
     *
     * @param locale the locale
     * @return the localized name
     */
    String getLocalizedName(Locale locale);

    /**
     * Returns the localized description of this action for the default locale.
     *
     * @return the localized description
     */
    default String getLocalizedDescription() {
        return getLocalizedDescription(Messages.getDefaultLocale());
    }

    /**
     * Returns the localized description of this action for a specific locale.
     *
     * @param locale the locale
     * @return the localized description
     */
    String getLocalizedDescription(Locale locale);

    /**
     * Returns the key code associated with this action (for keyboard shortcuts).
     *
     * @return the key code (default: 0)
     */
    default int getKeyCode() {
        return 0;
    }

    /**
     * Returns the provider for localized messages for this action.
     *
     * @return the localized messages provider
     */
    LocalizedMessagesProvider getLocalizedMessagesProvider();

    /**
     * Returns the classifier string for message localization or grouping.
     *
     * @return the message classifier
     */
    String getMessageClassifier();

    /**
     * Indicates whether this action is visible in the UI or available for execution.
     *
     * @return true if visible, false otherwise
     */
    boolean isVisible();

    /**
     * Sets the parent action for hierarchical relationships.
     *
     * @param action the parent action
     */
    void setParent(Action action);

    /**
     * Returns the parent action in a hierarchy, or null if none.
     *
     * @return the parent action
     */
    Action getParent();

    /**
     * Called when the action is performed server-side (e.g., from UI or logic).
     * <p>
     * Implement this method to define the behavior when the action is triggered by the user or system.
     * </p>
     *
     * @param evt the action event
     */
    void actionPerformed(ActionEvent evt);

    /**
     * Executes this action using an {@link ActionExecutionRequest} instead of an {@link ActionEvent}.
     * <p>
     * Implement this method to process the action result for external systems (e.g., REST API, automation).
     * The default implementation returns a response with success=false.
     * </p>
     *
     * @param request the execution request
     * @return the execution response
     */
    default ActionExecutionResponse execute(ActionExecutionRequest request) {
        return new ActionExecutionResponse(false);
    }

    /**
     * Converts this action into an {@link ActionReference} for lightweight representation.
     *
     * @return the action reference
     */
    default ActionReference toReference() {
        var ref = new ActionReference(getId());
        ref.setDescription(getDescription());
        ref.setLabel(getName());
        ref.setIcon(getImage());
        ref.setType(getClass().getName());
        ref.setVisible(isVisible());

        if (getAttributes() != null) {
            ref.setAttributes(new HashMap<>(getAttributes()));
        }
        return ref;
    }

    /**
     * Configures this action based on the provided {@link ActionReference}.
     * <p>
     * Implement this method to customize action properties from a reference.
     * The default implementation does nothing.
     * </p>
     *
     * @param reference the action reference
     */
    default void config(ActionReference reference) {
        // No default implementation
    }
}
