package tools.dynamia.viewers;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent an action reference
 */
public class ActionRef {
    private String id;
    private String width;
    private boolean visible;
    private String label;
    private String icon;
    private String description;
    private Map<String, Object> params;

    public ActionRef() {
    }

    public ActionRef(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return id;
    }

    public void addParam(String key, Object value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
    }

    public Object getParam(String key) {
        return params != null ? params.get(key) : null;
    }
}
