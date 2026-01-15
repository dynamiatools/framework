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

import tools.dynamia.commons.BeanUtils;

/**
 * Helper class to quickly create an {@link Action} instance without extending classes or implementing methods.
 * <p>
 * FastAction provides a fluent API for configuring actions using chained methods, allowing you to set properties and event handlers concisely.
 * This is useful for scenarios where you need to define simple actions inline, such as adding actions to a viewer or UI component.
 * </p>
 * <p>
 * <b>Example usage:</b>
 * <pre>
 *     // Add a new FastAction to a Viewer using chained methods
 *     viewer.addAction(new FastAction("Save")
 *         .image("save")
 *         .type("primary")
 *         .onActionPerfomed(evt -> {
 *             // Custom save logic here
 *         })
 *     );
 * </pre>
 * </p>
 *
 * <p>
 * You can also use the various constructors and fluent setters to configure name, image, description, renderer, and event handler.
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public class FastAction extends AbstractAction {

    /**
     * Functional interface to handle action performed events.
     */
    private OnActionPerfomed onActionPerfomed;

    /**
     * Default constructor.
     */
    public FastAction() {
    }

    /**
     * Constructor with action name.
     *
     * @param name the name of the action
     */
    public FastAction(String name) {
        setName(name);
    }

    /**
     * Constructor with action name and event handler.
     *
     * @param name the name of the action
     * @param onActionPerfomed the event handler
     */
    public FastAction(String name, OnActionPerfomed onActionPerfomed) {
        this(name, null, onActionPerfomed);
    }

    /**
     * Constructor with name, image and event handler.
     *
     * @param name the name of the action
     * @param image the image for the action
     * @param onActionPerfomed the event handler
     */
    public FastAction(String name, String image, OnActionPerfomed onActionPerfomed) {
        this(name, image, null, null, onActionPerfomed);
    }

    /**
     * Constructor with name, image, description and event handler.
     *
     * @param name the name of the action
     * @param image the image for the action
     * @param description the description of the action
     * @param onActionPerfomed the event handler
     */
    public FastAction(String name, String image, String description, OnActionPerfomed onActionPerfomed) {
        this(name, image, description, null, onActionPerfomed);
    }

    /**
     * Constructor with name, image, description, renderer and event handler.
     *
     * @param name the name of the action
     * @param image the image for the action
     * @param description the description of the action
     * @param actionRenderer the renderer for the action
     * @param onActionPerfomed the event handler
     */
    public FastAction(String name, String image, String description, ActionRenderer<?> actionRenderer, OnActionPerfomed onActionPerfomed) {
        this(null, name, image, description, actionRenderer, onActionPerfomed);
    }

    /**
     * Constructor with name, image, renderer and event handler.
     *
     * @param name the name of the action
     * @param image the image for the action
     * @param actionRenderer the renderer for the action
     * @param onActionPerfomed the event handler
     */
    public FastAction(String name, String image, ActionRenderer<?> actionRenderer, OnActionPerfomed onActionPerfomed) {
        this(null, name, image, null, actionRenderer, onActionPerfomed);
    }

    /**
     * Full constructor with all properties.
     *
     * @param id the id of the action
     * @param name the name of the action
     * @param image the image for the action
     * @param description the description of the action
     * @param actionRenderer the renderer for the action
     * @param onActionPerfomed the event handler
     */
    public FastAction(String id, String name, String image, String description, ActionRenderer<?> actionRenderer,
                      OnActionPerfomed onActionPerfomed) {

        if (id == null) {
            id = name.replace(" ", "");
        }
        setId(id);
        setName(name);
        setImage(image);
        setDescription(description);
        setRenderer(actionRenderer);
        this.onActionPerfomed = onActionPerfomed;
        init();
    }

    /**
     * Initialization method. Can be overridden for custom initialization.
     */
    protected void init() {
        // Custom initialization logic can be added here
    }

    /**
     * Invokes the action performed event handler.
     *
     * @param evt the action event
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        if (onActionPerfomed != null) {
            onActionPerfomed.actionPerformed(evt);
        }
    }

    /**
     * Executes the action by firing an ActionEvent.
     */
    public void execute() {
        actionPerformed(new ActionEvent(null, this));
    }

    /**
     * Sets the action renderer class if it is not the default renderer.
     *
     * @param actionRendererClass the renderer class
     */
    public void setActionRendererClass(Class<? extends ActionRenderer<?>> actionRendererClass) {
        if (actionRendererClass != DefaultActionRenderer.class) {
            setRenderer(BeanUtils.newInstance(actionRendererClass));
        }
    }

    /**
     * Sets the background property and returns this instance (fluent API).
     *
     * @param background the background value
     * @return this FastAction instance
     */
    public FastAction background(String background) {
        setBackground(background);
        return this;
    }

    /**
     * Sets the color property and returns this instance (fluent API).
     *
     * @param color the color value
     * @return this FastAction instance
     */
    public FastAction color(String color) {
        setColor(color);
        return this;
    }

    /**
     * Sets the image property and returns this instance (fluent API).
     *
     * @param image the image value
     * @return this FastAction instance
     */
    public FastAction image(String image) {
        setImage(image);
        return this;
    }

    /**
     * Sets the description property and returns this instance (fluent API).
     *
     * @param description the description value
     * @return this FastAction instance
     */
    public FastAction description(String description) {
        setDescription(description);
        return this;
    }

    /**
     * Sets the name property and returns this instance (fluent API).
     *
     * @param name the name value
     * @return this FastAction instance
     */
    public FastAction name(String name) {
        setName(name);
        return this;
    }

    /**
     * Sets the id property and returns this instance (fluent API).
     *
     * @param id the id value
     * @return this FastAction instance
     */
    public FastAction id(String id) {
        setId(id);
        return this;
    }

    /**
     * Sets the renderer and returns this instance (fluent API).
     *
     * @param actionRenderer the renderer
     * @return this FastAction instance
     */
    public FastAction renderer(ActionRenderer<?> actionRenderer) {
        setRenderer(actionRenderer);
        return this;
    }

    /**
     * Sets the event handler and returns this instance (fluent API).
     *
     * @param onActionPerfomed the event handler
     * @return this FastAction instance
     */
    public FastAction onActionPerfomed(OnActionPerfomed onActionPerfomed) {
        this.onActionPerfomed = onActionPerfomed;
        return this;
    }

    /**
     * Sets the type property and returns this instance (fluent API).
     *
     * @param type the type value
     * @return this FastAction instance
     */
    public FastAction type(String type) {
        setType(type);
        return this;
    }

}
