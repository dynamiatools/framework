package tools.dynamia.web.pwa;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PWAManifest {

    public String name;
    @JsonProperty("short_name")
    public String shortName;

    @JsonProperty("start_url")
    public String startUrl;
    public String display;
    @JsonProperty("background_color")
    public String backgroundColor;
    @JsonProperty("theme_color")
    public String themeColor;
    public String orientation;
    public List<PWAIcon> icons;


    private void initIcons() {
        if (icons == null) {
            icons = new java.util.ArrayList<>();
        }
    }

    public String description;
    public String lang;
    public String scope;
    public List<String> categories;
    public String dir;
    @JsonProperty("prefer_related_applications")
    public Boolean preferRelatedApplications;
    @JsonProperty("iarc_rating_id")
    public String iarcRatingId;
    public List<PWAScreenshot> screenshots;
    public List<PWAShortcut> shortcuts;
    @JsonProperty("related_applications")
    public List<PWARelatedApplication> relatedApplications;

    public PWAManifest() {
    }


    public PWAManifest(String name, String shortName, String startUrl) {
        this.name = name;
        this.shortName = shortName;
        this.startUrl = startUrl;
    }

    // Métodos de utilidad
    public void addIcon(PWAIcon icon) {
        initIcons();
        icons.add(icon);
    }

    public void addScreenshot(PWAScreenshot screenshot) {
        if (screenshots == null) {
            screenshots = new java.util.ArrayList<>();
        }
        screenshots.add(screenshot);
    }

    public void addShortcut(PWAShortcut shortcut) {
        if (shortcuts == null) {
            shortcuts = new java.util.ArrayList<>();
        }
        shortcuts.add(shortcut);
    }

    public void addRelatedApplication(PWARelatedApplication app) {
        if (relatedApplications == null) {
            relatedApplications = new java.util.ArrayList<>();
        }
        relatedApplications.add(app);
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

    public String getStartUrl() {
        return startUrl;
    }

    public void setStartUrl(String startUrl) {
        this.startUrl = startUrl;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public List<PWAIcon> getIcons() {
        return icons;
    }

    public void setIcons(List<PWAIcon> icons) {
        this.icons = icons;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Builder para PWAManifest
    public static class Builder {
        private final PWAManifest manifest;

        public Builder() {
            manifest = new PWAManifest();
        }

        public Builder name(String name) {
            manifest.setName(name);
            return this;
        }

        public Builder shortName(String shortName) {
            manifest.setShortName(shortName);
            return this;
        }

        public Builder startUrl(String startUrl) {
            manifest.setStartUrl(startUrl);
            return this;
        }

        public Builder display(String display) {
            manifest.setDisplay(display);
            return this;
        }

        public Builder backgroundColor(String backgroundColor) {
            manifest.setBackgroundColor(backgroundColor);
            return this;
        }

        public Builder themeColor(String themeColor) {
            manifest.setThemeColor(themeColor);
            return this;
        }

        public Builder orientation(String orientation) {
            manifest.setOrientation(orientation);
            return this;
        }

        public Builder icons(List<PWAIcon> icons) {
            manifest.setIcons(icons);
            return this;
        }

        public Builder addIcon(PWAIcon icon) {
            if (manifest.icons == null) manifest.icons = new java.util.ArrayList<>();
            manifest.icons.add(icon);
            return this;
        }

        public Builder description(String description) {
            manifest.description = description;
            return this;
        }

        public Builder lang(String lang) {
            manifest.lang = lang;
            return this;
        }

        public Builder scope(String scope) {
            manifest.scope = scope;
            return this;
        }

        public Builder categories(List<String> categories) {
            manifest.categories = categories;
            return this;
        }

        public Builder addCategory(String category) {
            if (manifest.categories == null) manifest.categories = new java.util.ArrayList<>();
            manifest.categories.add(category);
            return this;
        }

        public Builder dir(String dir) {
            manifest.dir = dir;
            return this;
        }

        public Builder preferRelatedApplications(Boolean prefer) {
            manifest.preferRelatedApplications = prefer;
            return this;
        }

        public Builder iarcRatingId(String id) {
            manifest.iarcRatingId = id;
            return this;
        }

        public Builder screenshots(List<PWAScreenshot> screenshots) {
            manifest.screenshots = screenshots;
            return this;
        }

        public Builder addScreenshot(PWAScreenshot screenshot) {
            if (manifest.screenshots == null) manifest.screenshots = new java.util.ArrayList<>();
            manifest.screenshots.add(screenshot);
            return this;
        }

        public Builder shortcuts(List<PWAShortcut> shortcuts) {
            manifest.shortcuts = shortcuts;
            return this;
        }

        public Builder addShortcut(PWAShortcut shortcut) {
            if (manifest.shortcuts == null) manifest.shortcuts = new java.util.ArrayList<>();
            manifest.shortcuts.add(shortcut);
            return this;
        }

        public Builder relatedApplications(List<PWARelatedApplication> apps) {
            manifest.relatedApplications = apps;
            return this;
        }

        public Builder addRelatedApplication(PWARelatedApplication app) {
            if (manifest.relatedApplications == null) manifest.relatedApplications = new java.util.ArrayList<>();
            manifest.relatedApplications.add(app);
            return this;
        }

        public PWAManifest build() {
            return manifest;
        }
    }
}
