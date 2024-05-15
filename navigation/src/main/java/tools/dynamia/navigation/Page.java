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
 * Basic {@link NavigationElement} for {@link Module}. Can be nested using {@link PageGroup}
 *
 * @author Ing. Mario Serrano Leones
 */
public class Page extends NavigationElement<Page> implements Serializable, Cloneable {


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


    public Page() {
    }

    /**
     *
     */
    public Page(String id, String name, String path) {
        this(id, name, path, true);
    }

    public Page(String id, String name, String path, boolean closeable) {
        super(id, name);
        this.path = path;
        this.closable = closeable;
    }

    public String getPath() {
        return path;
    }

    public Page setPath(String contentFile) {
        this.path = contentFile;
        return this;
    }

    @Override
    public String getVirtualPath() {
        try {
            if (virtualPath == null) {
                if (getPageGroup() != null) {
                    if (getPageGroup().getParentModule() != null && getPageGroup() == getPageGroup().getParentModule().getDefaultPageGroup()) {
                        virtualPath = getPageGroup().getParentModule().getId() + "/" + getId();
                    } else {
                        virtualPath = getPageGroup().getVirtualPath() + "/" + getId();
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

    @Override
    public String getPrettyVirtualPath() {
        try {
            if (getPageGroup() != null) {
                if (getPageGroup().getParentModule() != null && getPageGroup() == getPageGroup().getParentModule().getDefaultPageGroup()) {
                    return getPageGroup().getParentModule().getPrettyVirtualPath() + "/" + StringUtils.simplifiedString(getName());
                } else {
                    return getPageGroup().getPrettyVirtualPath() + "/" + StringUtils.simplifiedString(getName());
                }
            } else {
                return getName();
            }
        } catch (Exception e) {
            throw new PageException("Error building pretty virtual path for page with [" + getId() + "]", e);
        }
    }

    public PageGroup getPageGroup() {
        return pageGroup;
    }

    public Page setPageGroup(PageGroup set) {
        this.pageGroup = set;
        return this;
    }

    public Page setShowAsPopup(boolean b) {
        this.showAsPopup = b;
        return this;
    }

    public boolean isShowAsPopup() {
        return showAsPopup;
    }

    public int getIndex() {
        return index;
    }

    public Page setIndex(int index) {
        this.index = index;
        return this;
    }

    public Page setClosable(boolean closable) {
        this.closable = closable;
        return this;
    }

    public boolean isClosable() {
        return closable;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Page clone() {
        Page page = (Page) super.clone();
        page.setIndex(index);
        page.setPath(path);
        page.setShowAsPopup(showAsPopup);
        page.setClosable(closable);
        return page;
    }

    /**
     * Add new page action
     * @param action
     * @return
     */
    public Page addAction(PageAction action) {
        if (!actions.contains(action)) {
            actions.add(action);
            action.setPage(this);
        }
        return this;
    }

    /**
     * Add new page actions
     * @param action
     * @param others
     * @return
     */
    public Page addActions(PageAction action, PageAction... others) {
        addAction(action);
        if(others!=null) {
            Stream.of(others).forEach(this::addAction);
        }
        return this;
    }

    public void removeAction(PageAction action) {
        if (actions.contains(action)) {
            actions.remove(action);
            action.setPage(null);
        }
    }

    public List<PageAction> getActions() {
        return actions;
    }

    public void onClose(Callback onCloseCallback) {
        this.onCloseCallback = onCloseCallback;
    }

    public void onOpen(Callback onOpenCallback) {
        this.onOpenCallback = onOpenCallback;
    }

    public void onUnload(Callback onUnloadCallback) {
        this.onUnloadCallback = onUnloadCallback;
    }

    Callback getOnCloseCallback() {
        return onCloseCallback;
    }

    Callback getOnOpenCallback() {
        return onOpenCallback;
    }

    Callback getOnUnloadCallback() {
        return onUnloadCallback;
    }

    @Override
    protected String getLocalizedText(Locale locale, String sufix, String defaultValue) {
        if (getPageGroup() != null && getPageGroup().getParentModule() != null) {
            return getPageGroup().getParentModule().findLocalizedTextByKey(locale, msgKey(sufix), defaultValue);
        }
        return defaultValue;
    }

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

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    /**
     * Set this page as a featured page. See setFeatured
     */
    public Page featured() {
        setFeatured(true);
        return this;
    }

    /**
     * Set as featured with priority
     */
    public Page featured(int priority) {
        setFeatured(true);
        setPriority(priority);
        return this;
    }


    /**
     * Set this page as main page. But could be override by other main page in other modules if has higher priority
     * @return
     */
    public Page main(){
        setMain(true);
        return this;
    }


    /**
     * Return the priority of this page. By default is 100.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Set the priority of this page. By default is 100
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isTemporal() {
        return temporal;
    }

    /**
     * If always allowed is true, temporal is setting to true
     */
    @Override
    public void setAlwaysAllowed(boolean alwaysAllowed) {
        super.setAlwaysAllowed(alwaysAllowed);
        if (alwaysAllowed) {
            setTemporal(true);
        }
    }

    public void setTemporal(boolean temporal) {
        this.temporal = temporal;
    }

    public boolean isHtml() {
        return path != null && (path.endsWith(".html") || path.contains(".html?"));
    }

    public boolean isMain() {
        return main;
    }


    public void setMain(boolean main) {
        this.main = main;
    }
}
