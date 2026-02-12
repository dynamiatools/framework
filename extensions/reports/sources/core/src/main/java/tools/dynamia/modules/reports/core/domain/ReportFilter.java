package tools.dynamia.modules.reports.core.domain;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.contraints.NotEmpty;
import tools.dynamia.domain.jdbc.JdbcDataSet;
import tools.dynamia.domain.jdbc.JdbcHelper;
import tools.dynamia.domain.jdbc.Row;
import tools.dynamia.modules.saas.jpa.SimpleEntitySaaS;
import tools.dynamia.modules.reports.core.ReportDataSource;
import tools.dynamia.modules.reports.core.ReportFilterOption;
import tools.dynamia.modules.reports.core.ReportsException;
import tools.dynamia.modules.reports.core.ReportsUtils;
import tools.dynamia.modules.reports.core.domain.enums.DataType;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Entity
@Table(name = "rpt_reports_filters")
@JsonFilter("ignoreIds")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportFilter extends SimpleEntitySaaS {

    private final static LoggingService LOGGER = new SLF4JLoggingService(ReportFilter.class);

    @ManyToOne
    @JsonIgnore
    private Report report;

    @NotEmpty
    private String name;

    @NotEmpty
    private String label;

    @Column(name = "filterCondition")
    private String condition;

    private String defaultValue;

    private DataType dataType = DataType.TEXT;

    @Column(name = "filterValues")
    private String values;

    private String enumClassName;

    private String entityClassName;

    @Column(name = "filterOrder")
    private int order;

    private boolean required;

    private Boolean hideLabel;

    private String queryValues;

    public ReportFilter() {
    }

    public ReportFilter(String name) {
        this.name = name;
    }

    public List<ReportFilterOption> loadOptions(ReportDataSource dataSource) {
        if (dataType == DataType.ENUM && enumClassName != null) {
            return loadEnumOptions();
        } else if (dataType == DataType.ENTITY && entityClassName != null) {
            return loadEntityOptions();
        } else if (queryValues != null && !queryValues.isEmpty()) {
            return queryAndLoadOptions(dataSource);
        } else {
            return Collections.emptyList();
        }
    }

    private List<ReportFilterOption> loadEnumOptions() {
        var provider = ReportsUtils.findEnumFilterProvider(enumClassName);
        if (provider != null) {
            return Stream.of(provider.getValues())
                    .map(value -> new ReportFilterOption(this, value.name().replace("_", " "), value))
                    .toList();
        } else {
            throw new ReportsException("Cannot find enum provider for " + enumClassName);
        }
    }

    private List<ReportFilterOption> loadEntityOptions() {
        return Collections.emptyList();
    }

    private List<ReportFilterOption> queryAndLoadOptions(ReportDataSource dataSource) {
        List<ReportFilterOption> options = new ArrayList<>();
        if ("sql".equals(report.getQueryLang())) {
            try (Connection connection = ReportsUtils.getJdbcConnection(dataSource)) {
                JdbcHelper jdbc = new JdbcHelper(connection);
                JdbcDataSet result = jdbc.query(ReportsUtils.checkQuery(queryValues));

                for (Row row : result) {
                    row.loadAll(result.getColumnsLabels());
                    Object value = row.col(result.getColumnsLabels().get(0));
                    if (!result.getColumnsLabels().isEmpty()) {
                        options.add(new ReportFilterOption(this, row.col(result.getColumnsLabels().get(1)).toString(), value));
                    } else {
                        options.add(new ReportFilterOption(this, value.toString(), value));
                    }
                }
                result.close();
            } catch (Exception e) {
                LOGGER.error("Error loading options using SQL for filter " + name, e);
            }
        } else if ("jpql".equals(report.getQueryLang())) {
            ;
            try (EntityManager em = ReportsUtils.getJpaEntityManager(dataSource)) {
                List result = em.createQuery(queryValues).getResultList();
                result.forEach(b -> {
                    if (b.getClass().isArray()) {
                        Object[] values = (Object[]) b;
                        if (values.length > 1) {
                            options.add(new ReportFilterOption(this, values[1].toString(), values[0]));
                        } else {
                            options.add(new ReportFilterOption(this, values[0].toString(), values[0]));
                        }
                    } else {
                        options.add(new ReportFilterOption(this, b.toString(), b));
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Error loading options using JPQL for filter " + name, e);
            }
        }
        return options;
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

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getEnumClassName() {
        return enumClassName;
    }

    public void setEnumClassName(String enumClassName) {
        this.enumClassName = enumClassName;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Boolean getHideLabel() {
        if (hideLabel == null) {
            hideLabel = false;
        }
        return hideLabel;
    }

    public void setHideLabel(Boolean hideLabel) {
        this.hideLabel = hideLabel;
    }

    public String getQueryValues() {
        return queryValues;
    }

    public void setQueryValues(String queryValues) {
        this.queryValues = queryValues;
    }

    @Override
    public String toString() {
        return name;
    }
}
