package tools.dynamia.modules.reports.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.dynamia.modules.reports.core.domain.ReportFilter;

import java.util.*;
import java.util.stream.Collectors;

public class ReportFilters {


    private List<ReportFilterOption> options = new ArrayList<>();

    public ReportFilters() {
    }

    public ReportFilters(List<ReportFilterOption> options) {
        this.options = options;
    }

    public void add(ReportFilter filter, Object value) {
        if (exists(filter.getName())) {
            options.stream().filter(f -> f.getName().equals(filter.getName())).findFirst().ifPresent(op -> {
                op.setValue(value);
            });
        } else {
            options.add(new ReportFilterOption(filter, filter.getName(), value));
        }
    }

    public Object getValue(String filterName) {
        return options.stream().filter(f -> f.getName().equals(filterName))
                .map(ReportFilterOption::getValue)
                .findFirst().orElse(null);
    }

    public ReportFilter getFilter(String filterName) {
        return options.stream().filter(f -> f.getName().equals(filterName))
                .map(ReportFilterOption::getFilter)
                .findFirst().orElse(null);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return options.isEmpty();
    }

    public boolean exists(String filterName) {
        return options.stream().anyMatch(f -> f.getName().equals(filterName));
    }

    @JsonIgnore
    public Set<String> getFiltersNames() {
        return options.stream().map(ReportFilterOption::getName).collect(Collectors.toSet());
    }

    @JsonIgnore
    public Map<String, Object> getValues() {
        Map<String, Object> values = new HashMap<>();
        options.forEach(f -> values.put(f.getName(), f.getValue()));
        return values;
    }

    public List<ReportFilterOption> getOptions() {
        return options;
    }

    public void setOptions(List<ReportFilterOption> options) {
        this.options = options;
    }
}
