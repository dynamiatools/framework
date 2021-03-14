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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.dynamia.commons.SimpleCache;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Ing. Mario Serrano Leones
 */
@Component
public final class ModuleContainer implements Serializable {

    private final transient LoggingService LOGGER = new SLF4JLoggingService(ModuleContainer.class);

    private final Collection<Module> modules;
    private final Collection<Page> featuredPages;

    @Autowired
    private Collection<ModuleProvider> providers;
    private final SimpleCache<String, Page> INDEX = new SimpleCache<>();


    public ModuleContainer() {
        LOGGER.info("Creating " + getClass());
        modules = new ArrayList<>();
        featuredPages = new ArrayList<>();

    }

    @PostConstruct
    private void loadModules() {

        if (providers != null) {
            List<Module> modulesReferences = new ArrayList<>();

            for (ModuleProvider moduleProvider : providers) {
                Module module = moduleProvider.getModule();
                if (module != null) {
                    if (module.getBaseClass() == null && !module.isReference()) {
                        module.setBaseClass(moduleProvider.getClass());
                    }

                    if (module.isReference()) {
                        modulesReferences.add(module);
                    } else {
                        installModule(module);
                    }
                }
            }

            for (Module moduleRef : modulesReferences) {
                installModule(moduleRef);
            }
        }
    }

    public void installModule(Module module) {
        if (module != null) {
            LOGGER.info("Installing new WebModule ");
            install(module);
            LOGGER.info("Web module " + module.getName() + " installed");
        }
    }

    private void install(Module newMod) {

        Module oldModule = this.getModuleById(newMod.getId());
        if (oldModule == null) {
            modules.add(newMod);
        } else {
            if (newMod.getBaseClass() != null) {
                oldModule.addBaseClass(newMod.getBaseClass());
            }

            newMod.getDefaultPageGroup().getPages().forEach(page -> {
                oldModule.addPage(page);
                index(page);
            });

            for (PageGroup newGroup : newMod.getPageGroups()) {
                PageGroup oldGroup = oldModule.getPageGroupById(newGroup.getId());
                if (oldGroup == null) {
                    oldModule.addPageGroup(newGroup);
                } else {
                    for (Page newPage : newGroup.getPages()) {
                        index(newPage);
                        Page oldPage = oldGroup.getPageById(newPage.getId());
                        if (oldPage == null) {
                            oldGroup.addPage(newPage);
                        } else {
                            LOGGER.warn("Page " + newPage.getVirtualPath()
                                    + " already exists in the module container, another page has same ID. Cannot install  "
                                    + newPage.getClass().getSimpleName());
                        }
                    }
                }
            }
        }

    }

    private void index(Page page) {
        INDEX.add(page.getVirtualPath(), page);
        INDEX.add(page.getPrettyVirtualPath(), page);
        if (page.isFeatured()) {
            featuredPages.add(page);
        }
    }

    void reloadModule(Module module) {
        try {
            module.reload();
        } catch (Exception ex) {
            LOGGER.error("Error reloading web module " + module.getName(), ex);
        }
    }

    public Collection<Module> getModules() {
        if (modules.isEmpty()) {
            loadModules();
        }
        return modules;
    }

    public Module getModuleById(String id) {
        for (Module webModule : modules) {
            if (webModule.getId().equalsIgnoreCase(id)) {
                return webModule;
            }
        }
        return null;
    }

    public int getModuleCount() {
        return modules.size();
    }

    public Module getFirstModule() {
        for (Module module : getModules()) {
            return module;
        }
        return null;
    }

    public Page findPage(String path) {
        Page page = INDEX.get(path);

        try {
            if (page == null) {
                String p[] = path.split("/");
                Module pathModule = getModuleById(p[0]);
                if (pathModule != null) {
                    if (p.length == 2) {
                        page = pathModule.getDefaultPageGroup().getPageById(p[1]);
                    } else if (p.length == 3) {
                        page = pathModule.getPageGroupById(p[1]).getPageById(p[2]);
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }

        if (page != null) {
            return page;
        } else {
            throw new PageNotFoundException("Cannot be found page for path " + path + " in container ");
        }
    }

    public Page findPageByPrettyVirtualPath(String prettyPath) {
        Page page = INDEX.get(prettyPath);
        if (page == null) {
            for (Module module : modules) {

                List<PageGroup> modulePageGroups = new ArrayList<>();
                modulePageGroups.add(module.getDefaultPageGroup());
                modulePageGroups.addAll(module.getPageGroups());

                for (PageGroup pageGroup : modulePageGroups) {
                    for (Page p : pageGroup.getPages()) {
                        if (p.getPrettyVirtualPath().equals(prettyPath)) {
                            page = p;
                            index(page);
                            break;
                        }
                    }
                }
            }
        }
        if (page != null) {
            return page;
        } else {
            throw new PageNotFoundException("Cannot be found page for path " + prettyPath);
        }
    }

    public List<Page> findPagesByName(String name) {

        List<Page> result = new ArrayList<>();

        if (name == null || name.isEmpty()) {
            return result;
        }
        name = name.toLowerCase();
        for (Module module : modules) {

            List<PageGroup> modulePageGroups = new ArrayList<>();
            modulePageGroups.add(module.getDefaultPageGroup());
            modulePageGroups.addAll(module.getPageGroups());

            for (PageGroup pageGroup : modulePageGroups) {
                for (Page page : pageGroup.getPages()) {
                    if (page.getName().toLowerCase().contains(name) || (pageGroup.getName() != null && pageGroup.getName().toLowerCase().contains(name))) {
                        result.add(page);
                    }
                }

                for (PageGroup subgroup : pageGroup.getPageGroups()) {
                    for (Page page : subgroup.getPages()) {
                        if (page.getName().toLowerCase().contains(name) || (subgroup.getName() != null && subgroup.getName().toLowerCase().contains(name))) {
                            result.add(page);
                        }
                    }
                }
            }
        }
        return result;
    }

    public Collection<Page> getFeaturedPages() {
        return featuredPages;
    }

    public NavigationElement findElement(String path) {
        NavigationElement elem = INDEX.get(path);
        if (elem != null) {
            return elem;
        }


        Module pathModule = null;

        if (path.contains("/")) {
            try {
                String p[] = path.split("/");
                pathModule = getModuleById(p[0]);
            } catch (Exception e) {
            }
        } else {
            pathModule = getModuleById(path);
        }

        if (pathModule == null) {
            return null;
        }

        if (path.equals(pathModule.getVirtualPath())) {
            return pathModule;
        }


        if (path.equals(pathModule.getDefaultPageGroup().getVirtualPath())) {
            return pathModule.getDefaultPageGroup();
        }

        var page = pathModule.getDefaultPageGroup().getPages().stream().filter(pg -> path.equals(pg.getVirtualPath())).findFirst();
        if (page.isPresent()) {
            return page.get();
        }

        var group = pathModule.getPageGroups().stream().filter(pg -> path.equals(pg.getVirtualPath())).findFirst();
        if (group.isPresent()) {
            return group.get();
        }

        var pageGroups = pathModule.getPageGroups();
        var pg = findPage(path, pageGroups);
        if (pg != null) {
            index(pg);
        }

        return pg;
    }


    private Page findPage(String path, Collection<PageGroup> pageGroups) {
        for (PageGroup grp : pageGroups) {
            for (Page pg : grp.getPages()) {
                if (path.equals(pg.getVirtualPath())) {
                    return pg;
                }
            }
            if (grp.getPageGroups() != null && !grp.getPageGroups().isEmpty()) {
                return findPage(path, grp.getPageGroups());
            }
        }
        return null;
    }
}
