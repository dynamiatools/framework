package tools.dynamia.navigation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NavigationTree implements Serializable {

    private List<NavigationNode> navigation;

    public NavigationTree() {
    }

    public NavigationTree(List<NavigationNode> nodes) {
        this.navigation = nodes;
    }

    public void addNode(NavigationNode node) {
        if (navigation == null) {
            navigation = new ArrayList<>();
        }
        navigation.add(node);
    }

    public List<NavigationNode> getNavigation() {
        return navigation;
    }

    public void setNavigation(List<NavigationNode> nodes) {
        this.navigation = nodes;
    }

    /**
     * Build default navigation tree
     *
     * @return navigation tree
     */
    public static NavigationTree buildDefault() {

        try {
            var builder = new DefaultNavigationBuilder() {
                @Override
                public void buildNavigation() {
                    setViewBuilder(defaultViewVuilder());
                    List<Module> modules = new ArrayList<>(ModuleContainer.getInstance().getModules());
                    modules.sort(new ModuleComparator());
                    for (Module module : modules) {
                        if (NavigationRestrictions.allowAccess(module) && hasPagesWithAccess(module)) {
                            var moduleNode = new NavigationNode(module);
                            getNavigationTree().addNode(moduleNode);
                            buildPages(module.getDefaultPageGroup(), moduleNode);
                            buildPageGroups(module, moduleNode);
                        }
                    }
                }

                @Override
                protected void buildPages(PageGroup pageGroup, NavigationNode parentNode) {
                    var pages = new ArrayList<>(pageGroup.getPages());
                    for (Page p : pages) {
                        if (NavigationRestrictions.allowAccess(p) && p.isVisible()) {
                            var pageNode = new NavigationNode(p);
                            parentNode.addChild(pageNode);
                        }
                    }
                }
            };
            builder.buildNavigation();
            return builder.getNavigationTree();
        } catch (Exception e) {
            return new NavigationTree(List.of(new NavigationNode("error", "Invalid Navigation Tree", null)));
        }
    }

}
