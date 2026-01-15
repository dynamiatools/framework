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

import tools.dynamia.commons.ClassMessages;
import tools.dynamia.commons.Lambdas;
import tools.dynamia.commons.LocalizedMessagesProvider;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.logger.AbstractLoggable;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static tools.dynamia.commons.Lambdas.ifNotNull;

/**
 * Abstract base class for implementing user or system actions in the Dynamia Tools framework.
 * <p>
 * This class is the central piece of the actions module. Any class that extends or implements this class
 * represents a user or system action, which can be executed server-side via {@link #actionPerformed(ActionEvent)}
 * or respond to external requests (such as REST API calls) via {@link #execute(ActionExecutionRequest)}.
 * </p>
 * <p>
 * <b>Main features:</b>
 * <ul>
 *   <li>Provides all core properties for an action: id, name, description, image, group, position, attributes, etc.</li>
 *   <li>Supports localization for name and description using {@link LocalizedMessagesProvider} and message bundles.</li>
 *   <li>Allows custom rendering via {@link ActionRenderer}.</li>
 *   <li>Supports hierarchical actions (parent/child).</li>
 *   <li>Visibility and enablement controls for UI and security.</li>
 *   <li>Fluent API for setting common UI attributes (background, color, type, etc.).</li>
 * </ul>
 * <p>
 * <b>Usage example:</b>
 * <pre>
 *     @InstallAction
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
 * <p>
 * <b>Localization:</b> To support multiple languages, place Messages.properties files in the same package as your action class.
 * For example: Messages_es.properties, Messages_fr.properties, etc.
 * </p>
 * <p>
 * <b>Fluent API:</b> Use helper methods like setBackground, setColor, setType, setShowLabel, setSclass, setAlwaysVisible for UI customization.
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public abstract class AbstractAction extends AbstractLoggable implements Action {
    public static final String CLASSIFIER = "* UI Actions";

    private String name;
    private String description;
    private String image;
    private ActionGroup group = ActionGroup.NONE;
    private double position;
    private Map<String, Object> attributes = new HashMap<>();
    private String id = getClass().getSimpleName();
    private boolean enabled = true;
    private ActionRenderer actionRenderer = null;
    private int keyCode;
    private final ClassMessages messages = ClassMessages.get(getClass());
    private LocalizedMessagesProvider localizedMessagesProvider;
    private String messageClassifier = CLASSIFIER;
    private boolean visible = true;

    private Action parent;


    /**
     * Returns whether this action is enabled and can be executed or interacted with.
     *
     * @return true if enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether this action is enabled and can be executed or interacted with.
     *
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the unique identifier for this action.
     *
     * @return the id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this action.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the display name for this action.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the description for this action.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the image icon for this action.
     *
     * @param image the image name or path
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Returns the description of this action.
     *
     * @return the description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Returns the image icon name or path for this action.
     *
     * @return the image
     */
    @Override
    public String getImage() {
        return image;
    }

    /**
     * Returns the display name of this action.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets a custom attribute value by key.
     *
     * @param key   the attribute key
     * @param value the attribute value
     */
    @Override
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * Returns the group to which this action belongs.
     *
     * @return the group
     */
    @Override
    public ActionGroup getGroup() {
        return group;
    }

    /**
     * Sets the group for this action.
     *
     * @param group the group to set
     */
    public void setGroup(ActionGroup group) {
        if (group != null) {
            this.group = group;
        }
    }

    /**
     * Returns a custom attribute value by key.
     *
     * @param key the attribute key
     * @return the attribute value
     */
    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Returns a custom attribute value as a string by key.
     *
     * @param key the attribute key
     * @return the attribute value as string
     */
    public String getStringAttribute(String key) {
        Object value = getAttribute(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Returns a custom attribute value as a string by key, with a default if not found.
     *
     * @param key          the attribute key
     * @param defaultValue the default value if attribute not found
     * @return the attribute value as string or default
     */
    public String getStringAttribute(String key, String defaultValue) {
        Object value = getAttribute(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Returns all custom attributes as a map.
     *
     * @return the attributes map
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Sets all custom attributes for this action.
     *
     * @param attributes the attributes map to set
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * Returns the position value for ordering actions in lists, toolbars, etc.
     *
     * @return the position
     */
    @Override
    public double getPosition() {
        return position;
    }

    /**
     * Sets the position value for ordering actions.
     *
     * @param position the position value
     */
    public void setPosition(double position) {
        this.position = position;
    }

    /**
     * Returns the renderer for this action, used to create UI components.
     *
     * @return the renderer
     */
    @Override
    public ActionRenderer getRenderer() {
        return actionRenderer;
    }

    /**
     * Sets the renderer for this action.
     *
     * @param actionRenderer the renderer to set
     */
    public void setRenderer(ActionRenderer actionRenderer) {
        this.actionRenderer = actionRenderer;
    }

    /**
     * Compares this action to another by position for sorting purposes.
     *
     * @param o the other action
     * @return comparison result
     */
    @Override
    public int compareTo(Action o) {
        if (getPosition() > o.getPosition()) {
            return 1;
        } else if (getPosition() < o.getPosition()) {
            return -1;
        } else {
            return this.equals(o) ? 0 : 1;
        }
    }

    /**
     * Sets the background color for this action (UI helper).
     *
     * @param background the background value
     */
    public void setBackground(String background) {
        setAttribute("background", background);
    }

    /**
     * Returns the background color for this action (UI helper).
     *
     * @return the background value
     */
    public String getBackground() {
        return (String) getAttribute("background");
    }

    /**
     * Sets the color for this action (UI helper).
     *
     * @param color the color value
     */
    public void setColor(String color) {
        setAttribute("color", color);
    }

    /**
     * Returns the color for this action (UI helper).
     *
     * @return the color value
     */
    public String getColor() {
        return (String) getAttribute("color");
    }

    /**
     * Returns the localized name of this action for the given locale.
     * <p>
     * Uses the LocalizedMessagesProvider if set, otherwise falls back to Messages bundle.
     * </p>
     *
     * @param locale the locale
     * @return the localized name
     */
    @Override
    public String getLocalizedName(Locale locale) {
        String key = getId() + ".name";

        if (localizedMessagesProvider != null) {
            return localizedMessagesProvider.getMessage(key, getMessageClassifier(), locale, getName());
        }

        String localizedName = Messages.get(getClass(), key, locale);
        if (key.equals(localizedName)) {
            return getName();
        } else {
            return localizedName;
        }
    }

    /**
     * Returns the localized description of this action for the given locale.
     * <p>
     * Uses the LocalizedMessagesProvider if set, otherwise falls back to Messages bundle.
     * </p>
     *
     * @param locale the locale
     * @return the localized description
     */
    @Override
    public String getLocalizedDescription(Locale locale) {
        String key = getId() + ".description";
        if (localizedMessagesProvider != null) {
            return localizedMessagesProvider.getMessage(key, CLASSIFIER, locale, getDescription());
        }

        String localizedDescription = Messages.get(getClass(), key, locale);
        if (key.equals(localizedDescription)) {
            return getDescription();
        } else {
            return localizedDescription;
        }
    }

    /**
     * Returns the key code associated with this action (for keyboard shortcuts).
     *
     * @return the key code
     */
    @Override
    public int getKeyCode() {
        return keyCode;
    }

    /**
     * Sets the key code for this action (for keyboard shortcuts).
     *
     * @param keyCode the key code to set
     */
    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    /**
     * Returns a localized message for the given key using the current locale.
     * <p>
     * Messages.properties files should be in the same package as this action. One file per locale, e.g. Messages_es.properties.
     * </p>
     *
     * @param key the message key
     * @return the localized message
     */
    protected String msg(String key) {
        return messages.get(key);
    }

    /**
     * Returns a localized message for the given key and parameters using the current locale.
     * <p>
     * Messages.properties files should be in the same package as this action. One file per locale, e.g. Messages_es.properties.
     * </p>
     *
     * @param key    the message key
     * @param params parameters for the message
     * @return the localized message
     */
    protected String msg(String key, Object... params) {
        return messages.get(key, params);
    }

    /**
     * Returns the provider for localized messages for this action.
     *
     * @return the localized messages provider
     */
    @Override
    public LocalizedMessagesProvider getLocalizedMessagesProvider() {
        return localizedMessagesProvider;
    }

    /**
     * Sets the provider for localized messages for this action.
     *
     * @param localizedMessagesProvider the provider to set
     */
    public void setLocalizedMessagesProvider(LocalizedMessagesProvider localizedMessagesProvider) {
        this.localizedMessagesProvider = localizedMessagesProvider;
    }

    /**
     * Returns the classifier string for message localization or grouping.
     *
     * @return the message classifier
     */
    public String getMessageClassifier() {
        return messageClassifier;
    }

    /**
     * Sets the classifier string for message localization or grouping.
     *
     * @param messageClassifier the classifier to set
     */
    public void setMessageClassifier(String messageClassifier) {
        this.messageClassifier = messageClassifier;
    }

    /**
     * Returns whether this action is visible in the UI or available for execution.
     *
     * @return true if visible, false otherwise
     */
    @Override
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets whether this action is visible in the UI or available for execution.
     *
     * @param visible true to make visible, false to hide
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Returns the parent action in a hierarchy, or null if none.
     *
     * @return the parent action
     */
    @Override
    public Action getParent() {
        return parent;
    }

    /**
     * Sets the parent action for hierarchical relationships.
     *
     * @param parent the parent action to set
     */
    @Override
    public void setParent(Action parent) {
        this.parent = parent;
    }

    /**
     * Sets the type attribute for this action (UI helper).
     *
     * @param type the type value
     */
    public void setType(String type) {
        setAttribute("type", type);
    }

    /**
     * Returns the type attribute for this action (UI helper).
     *
     * @return the type value
     */
    public String getType() {
        return (String) getAttribute("type");
    }

    /**
     * Sets whether the action label should be shown in the UI (UI helper).
     *
     * @param showLabel true to show label, false to hide
     */
    public void setShowLabel(boolean showLabel) {
        setAttribute("showLabel", showLabel);
    }

    /**
     * Returns whether the action label should be shown in the UI (UI helper).
     *
     * @return true if label should be shown, false otherwise
     */
    public boolean isShowLabel() {
        return Boolean.TRUE.equals(getAttribute("showLabel"));
    }

    /**
     * Sets the CSS class for this action (UI helper).
     *
     * @param sclass the CSS class to set
     */
    public void setSclass(String sclass) {
        setAttribute("sclass", sclass);
    }

    /**
     * Returns the CSS class for this action (UI helper).
     *
     * @return the CSS class
     */
    public String getSclass() {
        return (String) getAttribute("sclass");
    }

    /**
     * Sets whether this action should always be visible in the UI (UI helper).
     *
     * @param visible true to always show, false otherwise
     */
    public void setAlwaysVisible(boolean visible) {
        setAttribute("alwaysVisible", visible);
    }

    /**
     * Returns whether this action should always be visible in the UI (UI helper).
     *
     * @return true if always visible, false otherwise
     */
    public boolean isAlwaysVisible() {
        return Boolean.TRUE.equals(getAttribute("alwaysVisible"));
    }


    /**
     * Configures this action based on the provided action reference.
     *
     * @param reference the action reference
     */
    @Override
    public void config(ActionReference reference) {
        if (reference != null) {
            ifNotNull(reference.getLabel(), this::setName);
            ifNotNull(reference.getIcon(), this::setImage);
            ifNotNull(reference.getDescription(), this::setDescription);
            ifNotNull(reference.getWidth(), w -> setAttribute("width", w));
            setVisible(reference.isVisible());
        }
    }
}
