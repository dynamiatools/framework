package tools.dynamia.app.metadata;

import tools.dynamia.app.ApplicationInfo;
import tools.dynamia.navigation.NavigationNode;
import tools.dynamia.navigation.NavigationTree;

import java.util.List;

public class ApplicationMetadata extends BasicMetadata {

    private String title;
    private String version;
    private String logo;
    private String template;
    private List<EntityMetadata> entities;
    private List<NavigationNode> navigation;
    private List<ActionMetadata> globalActions;

    public ApplicationMetadata() {
    }

    public ApplicationMetadata(ApplicationInfo info) {
        setName(info.getName());
        setTitle(info.getName());
        setDescription(info.getDescription());
        setIcon(info.getDefaultIcon());
        setLogo(info.getDefaultLogo());
        setTemplate(info.getTemplate());
        setNavigation(NavigationTree.buildDefault().getNavigation());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<EntityMetadata> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityMetadata> entities) {
        this.entities = entities;
    }

    public List<NavigationNode> getNavigation() {
        return navigation;
    }

    public void setNavigation(List<NavigationNode> navigation) {
        this.navigation = navigation;
    }

    public List<ActionMetadata> getGlobalActions() {
        return globalActions;
    }

    public void setGlobalActions(List<ActionMetadata> globalActions) {
        this.globalActions = globalActions;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public EntityMetadata getEntityMetadata(String className) {
        return entities != null ? entities.stream().filter(e -> e.getClassName().equals(className))
                .findFirst().orElse(null) : null;
    }
}
