package tools.dynamia.web.pwa;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PWAScreenshot {
    public String src;
    public String sizes;
    public String type;
    public String label;

    public PWAScreenshot() {
    }

    public PWAScreenshot(String src, String sizes, String type, String label) {
        this.src = src;
        this.sizes = sizes;
        this.type = type;
        this.label = label;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    // Builder para PWAScreenshot
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String src;
        private String sizes;
        private String type;
        private String label;

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

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public PWAScreenshot build() {
            return new PWAScreenshot(src, sizes, type, label);
        }
    }
}
