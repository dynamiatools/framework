package tools.dynamia.modules.reports.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.modules.reports.core.domain.ReportFilter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportFilterOption {

    private ReportFilter filter;
    private String name;
    private Object value;


    public ReportFilterOption() {
    }

    public ReportFilterOption(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public ReportFilterOption(ReportFilter filter, String name, Object value) {
        this.filter = filter;
        this.name = name;
        this.value = value;
    }

    public ReportFilter getFilter() {
        return filter;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }


    public void setFilter(ReportFilter filter) {
        this.filter = filter;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
