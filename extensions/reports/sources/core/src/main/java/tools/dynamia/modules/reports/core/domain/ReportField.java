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
import tools.dynamia.modules.reports.core.domain.enums.DataType;
import tools.dynamia.modules.reports.core.domain.enums.TextAlign;

@Entity
@Table(name = "rpt_reports_fields")
@JsonFilter("ignoreIds")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportField extends SimpleEntitySaaS {

    @ManyToOne
    @JsonIgnore
    private Report report;
    @NotEmpty
    @NotNull
    private String name;
    @NotEmpty
    private String label;
    private DataType dataType = DataType.TEXT;
    @Column(name = "fieldOrder")
    private int order = 0;
    private TextAlign align = TextAlign.LEFT;
    private String description;
    private String format;
    private String width;
    private String cellStyle;
    private String columnStyle;
    private boolean upperCase;
    private String jsonPath;



    @Override
    public String toString() {
        return name;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public TextAlign getAlign() {
        return align;
    }

    public void setAlign(TextAlign align) {
        this.align = align;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getCellStyle() {
        return cellStyle;
    }

    public void setCellStyle(String cellStyle) {
        this.cellStyle = cellStyle;
    }

    public String getColumnStyle() {
        return columnStyle;
    }

    public void setColumnStyle(String columnStyle) {
        this.columnStyle = columnStyle;
    }

    public boolean getUpperCase() {
        return upperCase;
    }

    public boolean isUpperCase() {
        return upperCase;
    }

    public void setUpperCase(boolean upperCase) {
        this.upperCase = upperCase;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }
}
