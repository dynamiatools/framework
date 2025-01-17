package tools.dynamia.app.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.app.ApplicationInfo;
import tools.dynamia.navigation.NavigationNode;
import tools.dynamia.navigation.NavigationTree;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationMetadata extends BasicMetadata {

    private String title;
    private String version;
    private String logo;
    private String template;
    private String author;

    public ApplicationMetadata() {
    }

    public ApplicationMetadata(ApplicationInfo info) {
        setName(info.getName());
        setTitle(info.getName());
        setDescription(info.getDescription());
        setIcon(info.getDefaultIcon());
        setLogo(info.getDefaultLogo());
        setTemplate(info.getTemplate());
        setVersion(info.getVersion());
        setAuthor(info.getAuthor());
        setEndpoint("/api/app/metadata");
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
