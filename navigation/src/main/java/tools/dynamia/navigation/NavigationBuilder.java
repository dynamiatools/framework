package tools.dynamia.navigation;

import java.util.ArrayList;
import java.util.List;

/**
 * Navigation Builder base class
 */
public abstract class NavigationBuilder {

    private Class<NavigationViewBuilder> viewBuilderClass;
    private NavigationViewBuilder viewBuilder;

    private final NavigationTree navigationTree = new NavigationTree();

    public void init() {
        buildNavigation();
        NavigationManager.getCurrent().setCurrentNavigationBuilder(this);
    }


    public void buildNavigation() {
        if (viewBuilder == null) {
            this.viewBuilder = defaultViewVuilder();
        }
        List<Module> modules = new ArrayList<>(ModuleContainer.getInstance().getModules());
        NavigationManager.getCurrent().getAvailablesPages().clear();
        modules.sort(new ModuleComparator());
        for (Module module : modules) {
            if (NavigationRestrictions.allowAccess(module) && hasPagesWithAccess(module)) {
                viewBuilder.createModuleView(module);

                var moduleNode = new NavigationNode(module);
                navigationTree.addNode(moduleNode);

                buildPages(module.getDefaultPageGroup(), moduleNode);
                buildPageGroups(module, moduleNode);
            }
        }
        showNavigation(viewBuilder.getNavigationView());
    }

    protected abstract void showNavigation(Object navigationView);


    protected boolean hasPagesWithAccess(Module module) {
        boolean allowed = module.getDefaultPageGroup().getPages().stream().anyMatch(NavigationRestrictions::allowAccess);

        if (!allowed) {
            for (PageGroup pg : module.getPageGroups()) {
                allowed = pg.getPages().stream().anyMatch(NavigationRestrictions::allowAccess);
                if (allowed) {
                    break;
                }
            }
        }

        return allowed;
    }

    protected void buildPageGroups(Module module, NavigationNode parentNode) {
        var groups = new ArrayList<>(module.getPageGroups());
        for (PageGroup pageGroup : groups) {
            buildPageGroupAndSubgroups(pageGroup, parentNode);
        }
    }

    protected void buildPageGroupAndSubgroups(PageGroup pageGroup, NavigationNode parentNode) {
        if (NavigationRestrictions.allowAccess(pageGroup) && pageGroup.isVisible()) {
            var pageGroupNode = new NavigationNode(pageGroup);
            parentNode.addChild(pageGroupNode);

            viewBuilder.createPageGroupView(pageGroup);
            if (!pageGroup.getPageGroups().isEmpty()) {
                for (PageGroup subgroup : pageGroup.getPageGroups()) {
                    buildPageGroupAndSubgroups(subgroup, pageGroupNode);
                }
            }
            buildPages(pageGroup, pageGroupNode);
        }
    }

    protected void buildPages(PageGroup pageGroup, NavigationNode parentNode) {
        var pages = new ArrayList<>(pageGroup.getPages());
        var navManager = NavigationManager.getCurrent();
        for (Page p : pages) {
            if (NavigationRestrictions.allowAccess(p) && p.isVisible()) {

                var pageNode = new NavigationNode(p);
                parentNode.addChild(pageNode);
                viewBuilder.createPageView(p);
                navManager.addAvailablePage(p);
            }
        }
    }


    public void setViewBuilderClass(String viewBuilderClass) {
        instanceNavigationBuilder(viewBuilderClass);
    }

    public void setViewBuilderClass(Class<NavigationViewBuilder> viewBuilderClass) {
        this.viewBuilderClass = viewBuilderClass;
    }

    public void setViewBuilder(NavigationViewBuilder viewBuilder) {
        this.viewBuilder = viewBuilder;
    }

    private void instanceNavigationBuilder(String viewBuilderClass) {
        if (viewBuilderClass != null && !viewBuilderClass.isEmpty()) {
            try {
                Class clazz = Class.forName(viewBuilderClass);
                //noinspection unchecked
                this.viewBuilder = (NavigationViewBuilder) clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Navigation view builder cannot be created: " + viewBuilderClass, e);
            }
        } else {
            this.viewBuilder = defaultViewVuilder();
        }

    }

    protected abstract NavigationViewBuilder defaultViewVuilder();

    public Class<NavigationViewBuilder> getViewBuilderClass() {
        if (viewBuilderClass == null && viewBuilder != null) {
            //noinspection unchecked
            viewBuilderClass = (Class<NavigationViewBuilder>) viewBuilder.getClass();
        }
        return viewBuilderClass;
    }

    public NavigationViewBuilder getViewBuilder() {
        return viewBuilder;
    }

    public NavigationTree getNavigationTree() {
        return navigationTree;
    }
}
