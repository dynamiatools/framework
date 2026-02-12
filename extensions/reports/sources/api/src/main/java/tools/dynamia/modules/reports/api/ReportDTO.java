package tools.dynamia.modules.reports.api;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportDTO implements Serializable {

    private String title;
    private String subtitle;
    private String name;
    private String description;
    private String group;
    private String endpoint;
    private List<ReportFilterDTO> filters;


    public ReportDTO() {
    }

    public ReportDTO(String title, String subtitle, String name, String description, String group, String endpoint) {
        this.title = title;
        this.subtitle = subtitle;
        this.name = name;
        this.description = description;
        this.group = group;
        this.endpoint = endpoint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<ReportFilterDTO> getFilters() {
        return filters;
    }

    public void setFilters(List<ReportFilterDTO> filters) {
        this.filters = filters;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
}
