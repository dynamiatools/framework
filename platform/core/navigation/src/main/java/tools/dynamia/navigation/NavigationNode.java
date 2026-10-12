package tools.dynamia.navigation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.dynamia.commons.LocalizedMessagesProvider;
import tools.dynamia.commons.Messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "name", "longName", "type", "description", "icon", "internalPath", "path", "position", "featured", "url", "attributes", "children"})
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

    /**
     * Address a browser can load for this page: only for pages whose path is an absolute {@code http(s)://} URL or a
     * root relative one ({@code /reports/sales.html}, {@code /widgets/chart.js}), typically an {@link ExternalPage}.
     * Internal paths ({@code classpath:} resources, bean names, entity class names) are never exposed.
     */
    private String url;

    @JsonIgnore
    private NavigationNode parent;
    private List<NavigationNode> children;

    private Map<String, Object> attributes;


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
        this(element, NavigationLabels.providers());
    }

    /**
     * Creates a node reusing already looked up label providers, so a whole tree build finds and sorts the providers
     * once instead of once per node.
     *
     * @param element   the navigation element
     * @param providers the localized messages providers sorted by priority, see {@code NavigationLabels.providers()}
     */
    NavigationNode(NavigationElement element, List<LocalizedMessagesProvider> providers) {
        this.element = element;
        this.id = element.getId();
        var locale = Messages.getDefaultLocale();
        this.name = NavigationLabels.resolve(providers, element.getVirtualPath(), locale, element.getLocalizedName(locale));
        this.longName = element.getLongNameSupplier() != null ? (String) element.getLongNameSupplier().get() : element.getLongName();
        var defaultDescription = element.getLocalizedDescription(locale);
        this.description = defaultDescription == null || defaultDescription.isBlank() ? defaultDescription
                : NavigationLabels.resolve(providers, element.getVirtualPath() + ".description", locale, defaultDescription);
        this.icon = element.getIcon();
        this.internalPath = element.getVirtualPath();
        this.path = element.getPrettyVirtualPath();

        this.position = element.getPosition() != 0.0 ? element.getPosition() : null;
        this.type = element.getClass().getSimpleName();
        this.featured = element instanceof Page p ? p.isFeatured() : null;
        this.url = element instanceof Page p ? browsableUrl(p.getPath()) : null;
        if (element.getAttributes() != null && !element.getAttributes().isEmpty()) {
            this.attributes = new HashMap<>(element.getAttributes());
        }
    }

    private static final java.util.regex.Pattern BROWSABLE_URL = java.util.regex.Pattern.compile("^(https?://|/(?!/)).*", java.util.regex.Pattern.CASE_INSENSITIVE);

    /** The path when a browser can load it as is, otherwise {@code null}. */
    static String browsableUrl(String path) {
        return path != null && BROWSABLE_URL.matcher(path).matches() ? path : null;
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


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
