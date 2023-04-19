package tools.dynamia.navigation;

import java.util.ArrayList;
import java.util.List;

public class NavigationTree {

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
}
