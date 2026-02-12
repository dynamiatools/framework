package tools.dynamia.navigation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NavigationNode implements Serializable {


    private String id;
    private String name;
    private String longName;
    private String type;
    private String description;
    private String icon;

    private String internalPath;
    private String path;

    private Double position;
    private Boolean featured;

    @JsonIgnore
    private NavigationNode parent;
    private List<NavigationNode> children;

    private Map<String, Object> attributes;
    private String file;


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

        this.position = element.getPosition() != 0.0 ? element.getPosition() : null;
        this.type = element.getClass().getSimpleName();
        this.featured = element instanceof Page p ? p.isFeatured() : null;
        this.file = element instanceof Page p ? p.getPath() : null;
        if (element.getAttributes() != null && !element.getAttributes().isEmpty()) {
            this.attributes = new HashMap<>(element.getAttributes());
        }
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

    public Double getPosition() {
        return position;
    }

    public void setPosition(Double position) {
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

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
