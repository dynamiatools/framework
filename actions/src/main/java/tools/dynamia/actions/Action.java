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
import java.util.Locale;
import java.util.Map;


/**
 * Represent a single application (or user) action. You can extend {@link AbstractAction} and in the
 * class contructor setup the properties and  implement actionPerformed(evt) method.
 *
 * @author Mario A. Serrano Leones
 */
public interface Action extends Comparable<Action>, Serializable {

    /**
     * Unique action id.
     *
     * @return the id
     */
    String getId();

    /**
     * Return the name of the action, it maybe used as the label of components.
     *
     * @return the name
     */
    String getName();

    /**
     * Text description of the action, commonly used as tooltip text.
     *
     * @return the description
     */
    String getDescription();

    /**
     * The image icon name.
     *
     * @return the image
     */
    String getImage();

    /**
     * Gets the group.
     *
     * @return the group
     */
    ActionGroup getGroup();

    /**
     * Gets the position.
     *
     * @return the position
     */
    double getPosition();

    /**
     * Gets the attribute.
     *
     * @param key the key
     * @return the attribute
     */
    Object getAttribute(String key);

    /**
     * Sets the attribute.
     *
     * @param key   the key
     * @param value the value
     */
    void setAttribute(String key, Object value);

    /**
     * Gets the attributes.
     *
     * @return the attributes
     */
    Map<String, Object> getAttributes();

    /**
     * Gets the renderer.
     *
     * @return the renderer
     */
    ActionRenderer getRenderer();

    /**
     * Checks if is enabled.
     *
     * @return true, if is enabled
     */
    boolean isEnabled();

    /**
     * Get the localized name of this action
     * @return
     */
    default String getLocalizedName() {
        return getLocalizedName(Messages.getDefaultLocale());
    }

    /**
     * Get the localized name of this action using a custom locale
     * @param locale
     * @return
     */
    String getLocalizedName(Locale locale);

    /**
     * Get the localized description of this action
     * @return the localized description
     */
    default String getLocalizedDescription() {
        return getLocalizedDescription(Messages.getDefaultLocale());
    }

    /**
     * Get the localized description of this action using a custom locale
     *
     * @param locale the locale to use
     * @return the localized description
     */
    String getLocalizedDescription(Locale locale);

    /**
     * Gets the keyboard key code associated with this action.
     *
     * @return the key code, or 0 if no key is associated
     */
    default int getKeyCode() {
        return 0;
    }

    /**
     * Gets the localized messages provider for this action.
     *
     * @return the localized messages provider
     */
    LocalizedMessagesProvider getLocalizedMessagesProvider();

    /**
     * Gets the message classifier for this action.
     *
     * @return the message classifier
     */
    String getMessageClassifier();

    /**
     * Checks if this action is visible.
     *
     * @return true if visible, false otherwise
     */
    boolean isVisible();

    /**
     * Sets the parent action of this action.
     *
     * @param action the parent action
     */
    void setParent(Action action);

    /**
     * Gets the parent action of this action.
     *
     * @return the parent action
     */
    Action getParent();

    /**
     * When action is performed.
     *
     * @param evt the evt
     */
    void actionPerformed(ActionEvent evt);


    /**
     * Execute this action using an {@link ActionExecutionRequest} instead of an {@link ActionEvent}. Implement this when
     * you need process the action result without worry about other stuff
     * @param request received
     * @return response
     */
    default ActionExecutionResponse execute(ActionExecutionRequest request) {
        return new ActionExecutionResponse(false);
    }

}
