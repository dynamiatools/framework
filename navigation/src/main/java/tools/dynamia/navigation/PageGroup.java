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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
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

    public PageGroup() {
    }

    public PageGroup(String id, String name) {
        super(id, name);
    }

    public PageGroup(String id, String name, String description) {
        super(id, name, description);
    }

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
     * Add multiple pages
     *
     * @param pages list
     * @return this
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
     * Add inner page group
     *
     * @param group
     * @return this
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
     * Add multiple inner groups
     *
     * @param groups
     * @return
     */
    public PageGroup addPageGroup(PageGroup... groups) {
        if (groups != null) {
            for (PageGroup group : groups) {
                addPageGroup(group);
            }
        }
        return this;
    }

    public List<Page> getPages() {
        return pages;
    }

    public List<PageGroup> getPageGroups() {
        return pageGroups;
    }

    public Page getPageById(String id) {
        for (Page p : pages) {
            if (p.getId() != null && p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public PageGroup getPageGroupById(String id) {
        for (PageGroup p : pageGroups) {
            if (p.getId() != null && p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public Module getParentModule() {
        return parentModule;
    }

    public PageGroup getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(PageGroup parentGroup) {
        this.parentGroup = parentGroup;
    }

    public void setParentModule(Module parentModule) {
        this.parentModule = parentModule;
    }

    public String getListeners() {
        return listeners;
    }

    public void setListeners(String listeners) {
        this.listeners = listeners;
    }

    @Override
    public String getVirtualPath() {
        if (virtualPath == null) {
            virtualPath = buildVirtualPath(this);
        }
        return virtualPath;
    }

    protected String buildVirtualPath(PageGroup group) {
        if (group.getParentGroup() != null) {
            return group.getParentGroup().getVirtualPath() + PATH_SEPARATOR + group.getId();
        } else {
            return (parentModule != null ? parentModule.getVirtualPath() + PATH_SEPARATOR : "") + group.getId();
        }
    }

    @Override
    public String getPrettyVirtualPath() {
        if (prettyVirtualPath == null) {
            prettyVirtualPath = buildPrettyVirtualPath(this);
        }
        return prettyVirtualPath;
    }

    protected String buildPrettyVirtualPath(PageGroup group) {
        if (group.getParentGroup() != null) {
            return group.getParentGroup().getPrettyVirtualPath() + PATH_SEPARATOR + StringUtils.simplifiedString(group.getName());
        } else {
            return (parentModule != null ? parentModule.getPrettyVirtualPath() + PATH_SEPARATOR : "") + StringUtils.simplifiedString(group.getName());
        }
    }

    @Override
    public PageGroup clone() {
        PageGroup pg = (PageGroup) super.clone();
        pg.setListeners(listeners);
        return pg;
    }

    @Override
    public String toString() {
        return getName();
    }

    public Page getFirstPage() {
        return getPages().stream().findFirst().orElse(null);
    }

    @Override
    protected String getLocalizedText(Locale locale, String sufix, String defaultValue) {
        if (getParentModule() != null) {
            return getParentModule().findLocalizedTextByKey(locale, msgKey(sufix), defaultValue);
        }
        return defaultValue;
    }

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

    public List<Page> findFeaturedPages() {
        return getPages().stream().filter(Page::isFeatured).collect(Collectors.toList());
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }
}
