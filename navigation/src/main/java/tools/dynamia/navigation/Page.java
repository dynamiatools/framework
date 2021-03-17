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
package tools.dynamia.navigation;

import tools.dynamia.commons.Callback;
import tools.dynamia.commons.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
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
    private boolean featured;
    private int priority = 100;
    private boolean temporal;


    public Page() {
    }

    /**
     * @param id
     * @param name
     * @param path
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
            if(virtualPath==null) {
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

    public Page addAction(PageAction action) {
        if (!actions.contains(action)) {
            actions.add(action);
            action.setPage(this);
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

    Callback getOnCloseCallback() {
        return onCloseCallback;
    }

    Callback getOnOpenCallback() {
        return onOpenCallback;
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
     *
     * @return
     */
    public Page featured() {
        setFeatured(true);
        return this;
    }

    /**
     * Set as featured with priority
     *
     * @param priority
     * @return
     */
    public Page featured(int priority) {
        setFeatured(true);
        setPriority(priority);
        return this;
    }


    /**
     * Return the priority of this page. By default is 100.
     * @return
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Set the priority of this page. By default is 100
     * @param priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isTemporal() {
        return temporal;
    }

    public void setTemporal(boolean temporal) {
        this.temporal = temporal;
    }
}
