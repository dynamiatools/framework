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
package tools.dynamia.navigation;

import tools.dynamia.actions.Action;
import java.io.Serializable;

/**
 * Represents an action related to a {@link Page} in the navigation system.
 * <p>
 * PageAction can be used to associate UI actions, delegates, icons, descriptions, and metadata with a page.
 * </p>
 */
public class PageAction extends NavigationElement<PageAction> implements Serializable {

    private static final long serialVersionUID = 4096406460140799114L;
    private Page page;
    private String actionClass;
    private double position;
    private boolean featured;
    private Action delegate;

    /**
     * Default constructor.
     */
    public PageAction() {
    }

    /**
     * Constructs a PageAction with the given id and name.
     *
     * @param id the action id
     * @param name the action name
     */
    public PageAction(String id, String name) {
        super(id, name);
    }

    /**
     * Constructs a PageAction associated with a specific page.
     *
     * @param page the {@link Page} to associate
     */
    public PageAction(Page page) {
        super();
        this.page = page;
    }

    /**
     * Constructs a PageAction with a page, id, and name.
     *
     * @param page the {@link Page}
     * @param id the action id
     * @param name the action name
     */
    public PageAction(Page page, String id, String name) {
        super(id, name);
        this.page = page;
    }

    /**
     * Constructs a PageAction from a delegate {@link Action} and associates it with a page.
     *
     * @param page the {@link Page}
     * @param delegate the {@link Action} delegate
     */
    public PageAction(Page page, Action delegate) {
        this(delegate.getId(), delegate.getName());
        setIcon(delegate.getImage());
        setDescription(delegate.getDescription());
        this.page = page;
        this.delegate = delegate;
        this.actionClass = delegate.getClass().getName();
        this.position = delegate.getPosition();
    }

    /**
     * Constructs a PageAction with page, id, name, and description.
     *
     * @param page the {@link Page}
     * @param id the action id
     * @param name the action name
     * @param description the action description
     */
    public PageAction(Page page, String id, String name, String description) {
        this(id, name);
        this.page = page;
        setDescription(description);
    }

    /**
     * Constructs a PageAction with page, id, name, description, and image.
     *
     * @param page the {@link Page}
     * @param id the action id
     * @param name the action name
     * @param description the action description
     * @param image the action image/icon
     */
    public PageAction(Page page, String id, String name, String description, String image) {
        this(page,id,name,description);
        setIcon(image);
    }

    /**
     * Returns the page associated with this action.
     *
     * @return the {@link Page}
     */
    public Page getPage() {
        return page;
    }

    /**
     * Sets the page associated with this action.
     *
     * @param page the {@link Page}
     */
    public void setPage(Page page) {
        this.page = page;
    }

    /**
     * Returns whether this action is featured.
     *
     * @return true if featured
     */
    public boolean isFeatured() {
        return featured;
    }

    /**
     * Sets whether this action is featured.
     *
     * @param featured true to set as featured
     */
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    /**
     * Sets the delegate {@link Action} for this PageAction.
     *
     * @param delegate the {@link Action}
     */
    public void setDelegate(Action delegate) {
        this.delegate = delegate;
    }

    /**
     * Sets the icon image for this action.
     *
     * @param image the icon image
     * @return this PageAction instance
     */
    public PageAction image(String image) {
        setIcon(image);
        return this;
    }

    /**
     * Sets the icon for this action.
     *
     * @param icon the icon name
     * @return this PageAction instance
     */
    public PageAction icon(String icon) {
        setIcon(icon);
        return this;
    }

    /**
     * Sets the name for this action.
     *
     * @param name the action name
     * @return this PageAction instance
     */
    public PageAction name(String name) {
        setName(name);
        return this;
    }

    /**
     * Sets the id for this action.
     *
     * @param id the action id
     * @return this PageAction instance
     */
    public PageAction id(String id) {
        setId(id);
        return this;
    }

    /**
     * Sets the description for this action.
     *
     * @param description the action description
     * @return this PageAction instance
     */
    public PageAction description(String description) {
        setDescription(description);
        return this;
    }

    /**
     * Sets the action class name for this action.
     *
     * @param actionClass the class name
     * @return this PageAction instance
     */
    public PageAction actionClass(String actionClass) {
        setActionClass(actionClass);
        return this;
    }

    /**
     * Marks this action as featured.
     *
     * @return this PageAction instance
     */
    public PageAction featured() {
        setFeatured(true);
        return this;
    }

    /**
     * Sets the position for this action.
     *
     * @param position the position value
     * @return this PageAction instance
     */
    public PageAction position(double position) {
        setPosition(position);
        return this;
    }

    /**
     * Returns the action class name.
     *
     * @return the class name string
     */
    public String getActionClass() {
        return actionClass;
    }

    /**
     * Sets the action class name.
     *
     * @param actionClass the class name string
     */
    public void setActionClass(String actionClass) {
        this.actionClass = actionClass;
    }

    /**
     * Returns the position of this action.
     *
     * @return the position value
     */
    public double getPosition() {
        return position;
    }

    /**
     * Sets the position of this action.
     *
     * @param position the position value
     */
    public void setPosition(double position) {
        this.position = position;
    }

    /**
     * Returns the delegate {@link Action} for this PageAction.
     *
     * @return the delegate Action
     */
    public Action getDelegate() {
        return delegate;
    }
}
