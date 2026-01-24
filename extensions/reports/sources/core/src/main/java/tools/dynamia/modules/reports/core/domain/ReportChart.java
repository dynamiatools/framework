package tools.dynamia.modules.reports.core.domain;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.domain.contraints.NotEmpty;
import tools.dynamia.modules.saas.jpa.SimpleEntitySaaS;

@Entity
@Table(name = "rpt_reports_charts")
@JsonFilter("ignoreIds")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportChart extends SimpleEntitySaaS {


    @ManyToOne
    @JsonIgnore
    private Report report;
    @NotEmpty
    @NotNull
    private String title;
    @NotEmpty
    private String labelField;
    @NotEmpty
    private String valueField;

    private String type;
    @Column(name = "fieldOrder")

    private int order = 0;
    private boolean grouped;

    @Override
    public String toString() {
        return title;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLabelField() {
        return labelField;
    }

    public void setLabelField(String labelField) {
        this.labelField = labelField;
    }

    public String getValueField() {
        return valueField;
    }

    public void setValueField(String valueField) {
        this.valueField = valueField;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean getGrouped() {
        return grouped;
    }

    public boolean isGrouped() {
        return grouped;
    }

    public void setGrouped(boolean grouped) {
        this.grouped = grouped;
    }

}
