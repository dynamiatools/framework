package tools.dynamia.web.pwa;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PWAShortcut {
    public String name;
    @JsonProperty("short_name")
    public String shortName;
    public String description;
    public String url;
    public List<PWAIcon> icons;

    public PWAShortcut() {
    }

    public PWAShortcut(String name, String shortName, String description, String url, List<PWAIcon> icons) {
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.url = url;
        this.icons = icons;
    }

    public void addIcon(PWAIcon icon) {
        if (icons == null) {
            icons = new java.util.ArrayList<>();
        }
        icons.add(icon);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<PWAIcon> getIcons() {
        return icons;
    }

    public void setIcons(List<PWAIcon> icons) {
        this.icons = icons;
    }

    // Builder para PWAShortcut
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String shortName;
        private String description;
        private String url;
        private List<PWAIcon> icons;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder shortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder icons(List<PWAIcon> icons) {
            this.icons = icons;
            return this;
        }

        public Builder addIcon(PWAIcon icon) {
            if (this.icons == null) this.icons = new java.util.ArrayList<>();
            this.icons.add(icon);
            return this;
        }

        public PWAShortcut build() {
            return new PWAShortcut(name, shortName, description, url, icons);
        }
    }
}
