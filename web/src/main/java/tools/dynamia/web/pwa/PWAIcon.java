package tools.dynamia.web.pwa;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PWAIcon {
    public String src;
    public String sizes;
    public String type;

    public PWAIcon() {
    }

    public PWAIcon(String src, String sizes, String type) {
        this.src = src;
        this.sizes = sizes;
        this.type = type;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getSizes() {
        return sizes;
    }

    public void setSizes(String sizes) {
        this.sizes = sizes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Builder para PWAIcon
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String src;
        private String sizes;
        private String type;

        public Builder src(String src) {
            this.src = src;
            return this;
        }

        public Builder sizes(String sizes) {
            this.sizes = sizes;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public PWAIcon build() {
            return new PWAIcon(src, sizes, type);
        }
    }
}
