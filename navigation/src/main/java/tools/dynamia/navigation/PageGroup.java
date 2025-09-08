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

import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Represents a group of {@link Page} objects, allowing hierarchical organization of pages within modules.
 * <p>
 * PageGroup can contain pages and other PageGroups, supporting nested navigation structures and dynamic grouping.
 * </p>
 *
 * @author Ing. Mario Serrano Leones
 */
public class PageGroup extends NavigationElement<PageGroup> implements Serializable, Cloneable {

    private static final long serialVersionUID = -7191971531078734873L;
    private final List<Page> pages = new ArrayList<>();
    private final List<PageGroup> pageGroups = new ArrayList<>();
    private Module parentModule;
    private PageGroup parentGroup;
    private String listeners;
    private static final LoggingService logger = new SLF4JLoggingService(PageGroup.class);
    private boolean dynamic;
    private String prettyVirtualPath;
    private String virtualPath;

    /**
     * Default constructor.
     */
    public PageGroup() {
    }

    /**
     * Constructs a PageGroup with the given id and name.
     *
     * @param id the group id
     * @param name the group name
     */
    public PageGroup(String id, String name) {
        super(id, name);
    }

    /**
     * Constructs a PageGroup with id, name, and description.
     *
     * @param id the group id
     * @param name the group name
     * @param description the group description
     */
    public PageGroup(String id, String name, String description) {
        super(id, name, description);
    }

    /**
     * Adds a page to this group. Throws exception if a page with the same ID already exists.
     *
     * @param page the {@link Page} to add
     * @return this PageGroup instance
     */
    public PageGroup addPage(Page page) {
        if (getPageById(page.getId()) != null) {
            throw new PageAlreadyExistsException("There is a page with the same ID " + page.getId() + " added in page group: " + getVirtualPath());
        } else {
            page.setPageGroup(this);
            page.setIndex(pages.size());
            pages.add(page);
        }
        return this;
    }

    /**
     * Adds multiple pages to this group.
     *
     * @param pages array of {@link Page}
     * @return this PageGroup instance
     */
    public PageGroup addPage(Page... pages) {
        if (pages != null) {
            for (Page page : pages) {
                addPage(page);
            }
        }
        return this;
    }

    /**
     * Adds an inner PageGroup to this group. Throws exception if a group with the same ID already exists.
     *
     * @param group the {@link PageGroup} to add
     * @return this PageGroup instance
     */
    public PageGroup addPageGroup(PageGroup group) {
        if (getPageGroupById(group.getId()) != null) {
            throw new PageAlreadyExistsException("There is a page group with the same ID " + group.getId() + "added in :" + getVirtualPath());
        } else {
            group.setParentModule(null);
            group.setParentGroup(this);
            pageGroups.add(group);
        }
        return this;
    }

    /**
     * Adds multiple inner PageGroups to this group.
     *
     * @param groups array of {@link PageGroup}
     * @return this PageGroup instance
     */
    public PageGroup addPageGroup(PageGroup... groups) {
        if (groups != null) {
            for (PageGroup group : groups) {
                addPageGroup(group);
            }
        }
        return this;
    }

    /**
     * Returns the list of pages in this group.
     *
     * @return list of {@link Page}
     */
    public List<Page> getPages() {
        return pages;
    }

    /**
     * Returns the list of inner PageGroups in this group.
     *
     * @return list of {@link PageGroup}
     */
    public List<PageGroup> getPageGroups() {
        return pageGroups;
    }

    /**
     * Finds a page by its ID.
     *
     * @param id the page ID
     * @return the {@link Page} or null if not found
     */
    public Page getPageById(String id) {
        for (Page p : pages) {
            if (p.getId() != null && p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Finds an inner PageGroup by its ID.
     *
     * @param id the group ID
     * @return the {@link PageGroup} or null if not found
     */
    public PageGroup getPageGroupById(String id) {
        for (PageGroup p : pageGroups) {
            if (p.getId() != null && p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Returns the parent module of this group.
     *
     * @return the {@link Module}
     */
    public Module getParentModule() {
        return parentModule;
    }

    /**
     * Returns the parent group of this group.
     *
     * @return the parent {@link PageGroup}
     */
    public PageGroup getParentGroup() {
        return parentGroup;
    }

    /**
     * Sets the parent group for this group.
     *
     * @param parentGroup the parent {@link PageGroup}
     */
    public void setParentGroup(PageGroup parentGroup) {
        this.parentGroup = parentGroup;
    }

    /**
     * Sets the parent module for this group.
     *
     * @param parentModule the parent {@link Module}
     */
    public void setParentModule(Module parentModule) {
        this.parentModule = parentModule;
    }

    /**
     * Returns the listeners string for this group.
     *
     * @return the listeners string
     */
    public String getListeners() {
        return listeners;
    }

    /**
     * Sets the listeners string for this group.
     *
     * @param listeners the listeners string
     */
    public void setListeners(String listeners) {
        this.listeners = listeners;
    }

    /**
     * Returns the virtual path of this group.
     *
     * @return the virtual path string
     */
    @Override
    public String getVirtualPath() {
        if (virtualPath == null) {
            virtualPath = buildVirtualPath(this);
        }
        return virtualPath;
    }

    /**
     * Builds the virtual path for a given group.
     *
     * @param group the {@link PageGroup}
     * @return the virtual path string
     */
    protected String buildVirtualPath(PageGroup group) {
        if (group.getParentGroup() != null) {
            return group.getParentGroup().getVirtualPath() + PATH_SEPARATOR + group.getId();
        } else {
            return (parentModule != null ? parentModule.getVirtualPath() + PATH_SEPARATOR : "") + group.getId();
        }
    }

    /**
     * Returns a human-readable virtual path for this group.
     *
     * @return the pretty virtual path string
     */
    @Override
    public String getPrettyVirtualPath() {
        if (prettyVirtualPath == null) {
            prettyVirtualPath = buildPrettyVirtualPath(this);
        }
        return prettyVirtualPath;
    }

    /**
     * Builds a human-readable virtual path for a given group.
     *
     * @param group the {@link PageGroup}
     * @return the pretty virtual path string
     */
    protected String buildPrettyVirtualPath(PageGroup group) {
        if (group.getParentGroup() != null) {
            return group.getParentGroup().getPrettyVirtualPath() + PATH_SEPARATOR + StringUtils.simplifiedString(group.getName());
        } else {
            return (parentModule != null ? parentModule.getPrettyVirtualPath() + PATH_SEPARATOR : "") + StringUtils.simplifiedString(group.getName());
        }
    }

    /**
     * Creates a clone of this PageGroup, copying listeners.
     *
     * @return the cloned {@link PageGroup}
     */
    @Override
    public PageGroup clone() {
        PageGroup pg = (PageGroup) super.clone();
        pg.setListeners(listeners);
        return pg;
    }

    /**
     * Returns the name of this group.
     *
     * @return the group name
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Returns the first page in this group, or null if none exist.
     *
     * @return the first {@link Page} or null
     */
    public Page getFirstPage() {
        return getPages().stream().findFirst().orElse(null);
    }

    /**
     * Returns a localized text for this group, using its module hierarchy.
     *
     * @param locale the locale
     * @param sufix the message key suffix
     * @param defaultValue the default value if not found
     * @return the localized text
     */
    @Override
    protected String getLocalizedText(Locale locale, String sufix, String defaultValue) {
        if (getParentModule() != null) {
            return getParentModule().findLocalizedTextByKey(locale, msgKey(sufix), defaultValue);
        }
        return defaultValue;
    }

    /**
     * Returns the full name of this group, including its parent hierarchy.
     *
     * @return the full name string
     */
    public String getFullName() {
        String fullname = "";
        if (getParentGroup() != null && getParentGroup().getName() != null) {
            fullname = getParentGroup().getFullName();
        } else if (getParentModule() != null) {
            fullname = getParentModule().getName();
        }
        if (getName() != null) {
            if (!fullname.isBlank()) {
                fullname = fullname + " / " + getName();
            } else {
                fullname = getName();
            }

        }

        return fullname;
    }

    /**
     * Finds and returns all featured pages in this group.
     *
     * @return list of featured {@link Page}
     */
    public List<Page> findFeaturedPages() {
        return getPages().stream().filter(Page::isFeatured).collect(Collectors.toList());
    }

    /**
     * Returns whether this group is dynamic.
     *
     * @return true if dynamic
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * Sets whether this group is dynamic.
     *
     * @param dynamic true to set as dynamic
     */
    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }
}
