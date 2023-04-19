package tools.dynamia.navigation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NavigationNode {


    private String id;
    private String name;
    private String longName;
    private String type;
    private String description;
    private String icon;

    private String internalPath;
    private String path;

    private double position = 0;

    @JsonIgnore
    private NavigationNode parent;
    private List<NavigationNode> children;




    @JsonIgnore
    private NavigationElement element;

    public NavigationNode() {
    }

    public NavigationNode(String id, String name, String internalPath) {
        this.id = id;
        this.name = name;
        this.internalPath = internalPath;
    }

    public NavigationNode(NavigationElement element) {
        this.element = element;
        this.id = element.getId();
        this.name = element.getLocalizedName();
        this.longName = element.getLongNameSupplier() != null ? (String) element.getLongNameSupplier().get() : element.getLongName();
        this.description = element.getLocalizedDescription();
        this.icon = element.getIcon();
        this.internalPath = element.getVirtualPath();
        this.path = element.getPrettyVirtualPath();

        this.position = element.getPosition();
        this.type = element.getClass().getSimpleName();
    }

    public void addChild(NavigationNode node) {
        if (node == null) {
            return;
        }

        if (children == null) {
            children = new ArrayList<>();
        }

        if (node.getParent() != null && node.getParent().getChildren() != null) {
            node.getParent().removeChild(node);
        }

        if (!children.contains(node)) {
            node.parent = this;
            children.add(node);
        }
    }

    public void removeChild(NavigationNode node) {
        if (children != null) {
            children.remove(node);
        }
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<NavigationNode> getChildren() {
        return children;
    }

    public void setChildren(List<NavigationNode> children) {
        this.children = children;
    }

    public NavigationElement getElement() {
        return element;
    }

    public void setElement(NavigationElement element) {
        this.element = element;
    }

    public String getInternalPath() {
        return internalPath;
    }

    public void setInternalPath(String internalPath) {
        this.internalPath = internalPath;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public NavigationNode getParent() {
        return parent;
    }

    public void setParent(NavigationNode parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return internalPath;
    }
}
