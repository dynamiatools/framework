package tools.dynamia.web.pwa;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PWARelatedApplication {
    public String platform;
    public String url;
    public String id;

    public PWARelatedApplication() {
    }

    public PWARelatedApplication(String platform, String url, String id) {
        this.platform = platform;
        this.url = url;
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Builder para PWARelatedApplication
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String platform;
        private String url;
        private String id;

        public Builder platform(String platform) {
            this.platform = platform;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public PWARelatedApplication build() {
            return new PWARelatedApplication(platform, url, id);
        }
    }
}
