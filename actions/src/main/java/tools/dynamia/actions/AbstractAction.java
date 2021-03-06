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

import tools.dynamia.commons.ClassMessages;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Ready to use action implementation. Extend this class and implement actionPerfomed(evt) method. Also annotated the
 * new class using {@link InstallAction} to autodiscover the action.
 *
 * @author Mario A. Serrano Leones
 */
public abstract class AbstractAction implements Action {
    private final LoggingService logger = new SLF4JLoggingService(getClass());

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


    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getImage() {
        return image;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public ActionGroup getGroup() {
        return group;
    }

    public void setGroup(ActionGroup group) {
        if (group != null) {
            this.group = group;
        }
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    @Override
    public ActionRenderer getRenderer() {
        return actionRenderer;
    }

    public void setRenderer(ActionRenderer actionRenderer) {
        this.actionRenderer = actionRenderer;
    }

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
     * Shortcut to setAttribute("background","#back")
     *
     * @param background
     */
    public void setBackground(String background) {
        setAttribute("background", background);
    }

    /**
     * Shortcut to getAttribute("background")
     *
     * @return
     */
    public String getBackground() {
        return (String) getAttribute("background");
    }

    /**
     * Shortcut to setAttribute("color","#back")
     *
     * @param color
     */
    public void setColor(String color) {
        setAttribute("color", color);
    }

    /**
     * Shortcut to getAttribute("color")
     *
     * @return
     */
    public String getColor() {
        return (String) getAttribute("color");
    }


    @Override
    public String getLocalizedName(Locale locale) {
        String key = getId() + ".name";
        String localizedName = Messages.get(getClass(), key, locale);
        if (key.equals(localizedName)) {
            return getName();
        } else {
            return localizedName;
        }
    }

    @Override
    public String getLocalizedDescription(Locale locale) {
        String key = getId() + ".description";
        String localizedDescription = Messages.get(getClass(), key, locale);
        if (key.equals(localizedDescription)) {
            return getDescription();
        } else {
            return localizedDescription;
        }
    }

    @Override
    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    protected void log(String message) {
        logger.info(message);
    }

    protected void log(String messsage, Throwable exception) {
        logger.error(messsage, exception);
    }

    protected void logWarn(String message) {
        logger.warn(message);
    }

    /**
     * Get message based in current locale. It use {@link ClassMessages} to find messages Bundles. Messages.properties files should
     * be in the same package ot this action. One Message.properties by locale, ex. Message_es.properties, Messages_kr.properties
     *
     * @param key
     * @return
     */
    protected String msg(String key) {
        return messages.get(key);
    }

    /**
     * Get message based in current locale. It use {@link ClassMessages} to find messages Bundles. Messages.properties files should
     * be in the same package ot this action. One Message.properties by locale, ex. Message_es.properties, Messages_kr.properties
     *
     * @param key
     * @param params
     * @return
     */
    protected String msg(String key, Object... params) {
        return messages.get(key, params);
    }

}
