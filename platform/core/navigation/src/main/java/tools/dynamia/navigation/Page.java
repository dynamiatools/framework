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

import tools.dynamia.commons.Callback;
import tools.dynamia.commons.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Represents a navigable page in a module. Can be nested using {@link PageGroup}.
 * <p>
 * Pages are the main navigation elements and can have actions, callbacks, and metadata such as priority, featured status, and temporal state.
 * </p>
 *
 * @author Ing. Mario Serrano Leones
 */
public class Page extends NavigationElement<Page> implements Serializable, Cloneable {

    /**
     * Returns the current active page from the navigation manager.
     *
     * @return the current {@link Page}
     */
    public static Page getCurrent() {
        return NavigationManager.getCurrent().getCurrentPage();
    }

    private static final long serialVersionUID = -2827398935971117969L;
    private int index;
    private String path;
    private boolean showAsPopup;
    private PageGroup pageGroup;
    private boolean closable = true;
    private final List<PageAction> actions = new ArrayList<>();
    private Callback onCloseCallback;
    private Callback onOpenCallback;
    private Callback onUnloadCallback;
    private boolean featured;
    private int priority = 100;
    private boolean temporal;
    private boolean main;
    private String prettyVirtualPath;
    private String virtualPath;

    /**
     * Default constructor.
     */
    public Page() {
    }

    /**
     * Constructs a page with the given id, name, and path. The page is closable by default.
     *
     * @param id the page id
     * @param name the page name
     * @param path the content path
     */
    public Page(String id, String name, String path) {
        this(id, name, path, true);
    }

    /**
     * Constructs a page with the given id, name, path, and closable flag.
     *
     * @param id the page id
     * @param name the page name
     * @param path the content path
     * @param closeable whether the page can be closed
     */
    public Page(String id, String name, String path, boolean closeable) {
        super(id, name);
        this.path = path;
        this.closable = closeable;
    }

    /**
     * Returns the content path of this page.
     *
     * @return the path string
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the content path for this page.
     *
     * @param contentFile the new path
     * @return this page instance
     */
    public Page setPath(String contentFile) {
        this.path = contentFile;
        return this;
    }

    /**
     * Returns the virtual path of this page, including its group/module hierarchy.
     *
     * @return the virtual path string
     */
    @Override
    public String getVirtualPath() {
        try {
            if (virtualPath == null) {
                if (getPageGroup() != null) {
                    if (getPageGroup().getParentModule() != null && getPageGroup() == getPageGroup().getParentModule().getDefaultPageGroup()) {
                        virtualPath = getPageGroup().getParentModule().getVirtualPath() + PATH_SEPARATOR + getId();
                    } else {
                        virtualPath = getPageGroup().getVirtualPath() + PATH_SEPARATOR + getId();
                    }
                } else {
                    virtualPath = getId();
                }
            }
            return virtualPath;
        } catch (Exception e) {
            throw new PageException("Error building virtual path for page with [" + getId() + "]", e);
        }
    }

    /**
     * Returns a simplified, human-readable virtual path for this page.
     *
     * @return the pretty virtual path string
     */
    @Override
    public String getPrettyVirtualPath() {
        try {
            String simplifiedName = StringUtils.simplifiedString(getName());
            if (prettyVirtualPath == null) {
                if (getPageGroup() != null) {
                    if (getPageGroup().getParentModule() != null && getPageGroup() == getPageGroup().getParentModule().getDefaultPageGroup()) {
                        prettyVirtualPath = getPageGroup().getParentModule().getPrettyVirtualPath() + PATH_SEPARATOR + simplifiedName;
                    } else {
                        prettyVirtualPath = getPageGroup().getPrettyVirtualPath() + PATH_SEPARATOR + simplifiedName;
                    }
                } else {
                    prettyVirtualPath = simplifiedName;
                }
            }
            return prettyVirtualPath;
        } catch (Exception e) {
            throw new PageException("Error building pretty virtual path for page with [" + getId() + "]", e);
        }
    }

    /**
     * Returns the group to which this page belongs.
     *
     * @return the {@link PageGroup}
     */
    public PageGroup getPageGroup() {
        return pageGroup;
    }

    /**
     * Sets the group for this page.
     *
     * @param set the {@link PageGroup}
     * @return this page instance
     */
    public Page setPageGroup(PageGroup set) {
        this.pageGroup = set;
        return this;
    }

    /**
     * Sets whether this page should be shown as a popup.
     *
     * @param b true to show as popup
     * @return this page instance
     */
    public Page setShowAsPopup(boolean b) {
        this.showAsPopup = b;
        return this;
    }

    /**
     * Returns whether this page is shown as a popup.
     *
     * @return true if shown as popup
     */
    public boolean isShowAsPopup() {
        return showAsPopup;
    }

    /**
     * Returns the index of this page within its group.
     *
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index of this page within its group.
     *
     * @param index the index value
     * @return this page instance
     */
    public Page setIndex(int index) {
        this.index = index;
        return this;
    }

    /**
     * Sets whether this page is closable.
     *
     * @param closable true if closable
     * @return this page instance
     */
    public Page setClosable(boolean closable) {
        this.closable = closable;
        return this;
    }

    /**
     * Returns whether this page is closable.
     *
     * @return true if closable
     */
    public boolean isClosable() {
        return closable;
    }

    /**
     * Returns the name of this page.
     *
     * @return the page name
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Creates a deep clone of this page, including its actions and callbacks.
     *
     * @return the cloned {@link Page}
     */
    @Override
    public Page clone() {
        Page page = (Page) super.clone();
        page.index = index;
        page.path = path;
        page.showAsPopup = showAsPopup;
        page.closable = closable;
        page.featured = featured;
        page.priority = priority;
        page.temporal = temporal;
        page.main = main;
        page.pageGroup = pageGroup;
        page.actions.addAll(actions);
        page.onCloseCallback = onCloseCallback;
        page.onOpenCallback = onOpenCallback;
        page.onUnloadCallback = onUnloadCallback;
        page.virtualPath = getVirtualPath();
        page.prettyVirtualPath = getPrettyVirtualPath();
        return page;
    }

    /**
     * Adds a new action to this page.
     *
     * @param action the {@link PageAction} to add
     * @return this page instance
     */
    public Page addAction(PageAction action) {
        if (!actions.contains(action)) {
            actions.add(action);
            action.setPage(this);
        }
        return this;
    }

    /**
     * Adds multiple actions to this page.
     *
     * @param action the first {@link PageAction}
     * @param others additional {@link PageAction}s
     * @return this page instance
     */
    public Page addActions(PageAction action, PageAction... others) {
        addAction(action);
        if (others != null) {
            Stream.of(others).forEach(this::addAction);
        }
        return this;
    }

    /**
     * Removes an action from this page.
     *
     * @param action the {@link PageAction} to remove
     */
    public void removeAction(PageAction action) {
        if (actions.contains(action)) {
            actions.remove(action);
            action.setPage(null);
        }
    }

    /**
     * Returns the list of actions associated with this page.
     *
     * @return the list of {@link PageAction}
     */
    public List<PageAction> getActions() {
        return actions;
    }

    /**
     * Sets a callback to be executed when the page is closed.
     *
     * @param onCloseCallback the callback
     */
    public void onClose(Callback onCloseCallback) {
        this.onCloseCallback = onCloseCallback;
    }

    /**
     * Sets a callback to be executed when the page is opened.
     *
     * @param onOpenCallback the callback
     */
    public void onOpen(Callback onOpenCallback) {
        this.onOpenCallback = onOpenCallback;
    }

    /**
     * Sets a callback to be executed when the page is unloaded.
     *
     * @param onUnloadCallback the callback
     */
    public void onUnload(Callback onUnloadCallback) {
        this.onUnloadCallback = onUnloadCallback;
    }

    /**
     * Returns the callback for page close event.
     *
     * @return the callback
     */
    Callback getOnCloseCallback() {
        return onCloseCallback;
    }

    /**
     * Returns the callback for page open event.
     *
     * @return the callback
     */
    Callback getOnOpenCallback() {
        return onOpenCallback;
    }

    /**
     * Returns the callback for page unload event.
     *
     * @return the callback
     */
    Callback getOnUnloadCallback() {
        return onUnloadCallback;
    }

    /**
     * Returns a localized text for this page, using its group/module hierarchy.
     *
     * @param locale the locale
     * @param sufix the message key suffix
     * @param defaultValue the default value if not found
     * @return the localized text
     */
    @Override
    protected String getLocalizedText(Locale locale, String sufix, String defaultValue) {
        if (getPageGroup() != null && getPageGroup().getParentModule() != null) {
            return getPageGroup().getParentModule().findLocalizedTextByKey(locale, msgKey(sufix), defaultValue);
        }
        return defaultValue;
    }

    /**
     * Returns the full name of this page, including its group hierarchy.
     *
     * @return the full name string
     */
    public String getFullName() {
        String fullName = "";
        if (pageGroup != null) {
            fullName = pageGroup.getFullName();
            if (!fullName.isBlank()) {
                fullName += " / ";
            }
        }
        fullName = fullName + getName();
        return fullName;
    }

    /**
     * Returns whether this page is featured.
     *
     * @return true if featured
     */
    public boolean isFeatured() {
        return featured;
    }

    /**
     * Sets whether this page is featured.
     *
     * @param featured true to set as featured
     */
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    /**
     * Marks this page as featured.
     *
     * @return this page instance
     */
    public Page featured() {
        setFeatured(true);
        return this;
    }

    /**
     * Marks this page as featured and sets its priority.
     *
     * @param priority the priority value
     * @return this page instance
     */
    public Page featured(int priority) {
        setFeatured(true);
        setPriority(priority);
        return this;
    }

    /**
     * Marks this page as the main page.
     *
     * @return this page instance
     */
    public Page main() {
        setMain(true);
        return this;
    }

    /**
     * Returns the priority of this page. Default is 100.
     *
     * @return the priority value
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the priority of this page. Default is 100.
     *
     * @param priority the priority value
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Returns whether this page is temporal (not persistent).
     *
     * @return true if temporal
     */
    public boolean isTemporal() {
        return temporal;
    }

    /**
     * Sets always allowed flag. If true, sets temporal to true.
     *
     * @param alwaysAllowed true to always allow
     */
    @Override
    public void setAlwaysAllowed(boolean alwaysAllowed) {
        super.setAlwaysAllowed(alwaysAllowed);
        if (alwaysAllowed) {
            setTemporal(true);
        }
    }

    /**
     * Sets whether this page is temporal.
     *
     * @param temporal true if temporal
     */
    public void setTemporal(boolean temporal) {
        this.temporal = temporal;
    }

    /**
     * Returns whether the page content is HTML.
     *
     * @return true if path ends with .html or contains .html?
     */
    public boolean isHtml() {
        return path != null && (path.endsWith(".html") || path.contains(".html?"));
    }

    /**
     * Returns whether this page is marked as main.
     *
     * @return true if main
     */
    public boolean isMain() {
        return main;
    }

    /**
     * Sets whether this page is main.
     *
     * @param main true to set as main
     */
    public void setMain(boolean main) {
        this.main = main;
    }
}
