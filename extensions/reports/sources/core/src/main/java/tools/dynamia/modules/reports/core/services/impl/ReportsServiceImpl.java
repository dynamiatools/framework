package tools.dynamia.modules.reports.core.services.impl;

import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.domain.jdbc.JdbcHelper;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.AbstractService;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.reports.core.*;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportFilter;
import tools.dynamia.modules.reports.core.domain.ReportGroup;
import tools.dynamia.modules.reports.core.services.ReportsService;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@CacheConfig(cacheNames = "reports")
public class ReportsServiceImpl extends AbstractService implements ReportsService {


    private final AccountServiceAPI accountServiceAPI;

    public ReportsServiceImpl(AccountServiceAPI accountServiceAPI) {
        this.accountServiceAPI = accountServiceAPI;
    }

    @Override
    public ReportData execute(Report report, ReportFilters filters, ReportDataSource datasource) {
        log("Executing query for report: " + report.getName() + " - " + report.getQueryLang());
        long start = System.currentTimeMillis();
        ReportData data = null;
        loadDefaultFilters(report, filters);
        data = switch (report.getQueryLang()) {
            case "sql" -> executeSQL(report, filters, datasource);
            case "jpql" -> executeJPQL(report, filters, datasource);
            default -> data;
        };
        long end = System.currentTimeMillis();
        log("Report " + report.getName() + " executed in " + (end - start) + "ms");
        return data;
    }

    private void loadDefaultFilters(Report report, ReportFilters reportFilters) {
        boolean checkQuery = true;
        if (!reportFilters.isEmpty()) {
            ReportFilter filter = reportFilters.getFilter("accountId");
            if (filter != null) {
                reportFilters.add(filter, accountServiceAPI.getCurrentAccountId());
                checkQuery = false;
            }
        }

        if (checkQuery && report.getQueryScript().contains(":accountId")) {
            ReportFilter filter = new ReportFilter("accountId");
            Long systemAccountId = accountServiceAPI.getSystemAccountId();
            if (!Objects.equals(report.getAccountId(), systemAccountId)) {
                reportFilters.add(filter, report.getAccountId());
            } else {
                reportFilters.add(filter, accountServiceAPI.getCurrentAccountId());
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Cacheable(key = "'Report-' + #id")
    public Report loadReportModel(Long id) {
        Report report = crudService().findSingle(Report.class, QueryParameters.with("id", id).add("accountId", QueryConditions.isNotNull()));
        report.getFields().size();
        report.getFilters().size();
        report.getCharts().size();
        return report;
    }

    private static ReportData executeSQL(Report report, ReportFilters filters, ReportDataSource dataSource) {
        ReportData data = null;

        try (Connection connection = ReportsUtils.getJdbcConnection(dataSource)) {
            var jdbc = new JdbcHelper(new ReportDataSource("delegate", connection));
            jdbc.setShowSQL(false);
            String sql = buildSqlScript(report.getQueryScript(), filters);

            var result = filters.isEmpty() ? jdbc.query(sql) : jdbc.query(sql, filters.getValues());
            data = ReportData.build(report, result);
        } catch (SQLException e) {
            throw new ReportsException(e);
        }

        return data;
    }

    private static ReportData executeJPQL(Report report, ReportFilters filters, ReportDataSource dataSource) {
        EntityManager em = ReportsUtils.getJpaEntityManager(dataSource);
        String jpql = buildSqlScript(report.getQueryScript(), filters);
        Query query = em.createQuery(jpql);
        filters.getValues().forEach(query::setParameter);
        List result = query.getResultList();
        return ReportData.build(report, result);
    }

    private static String buildSqlScript(String query, ReportFilters filters) {
        query = query.replace("\n", " ").replace("\t", " ");
        StringBuilder filtersSql = new StringBuilder("");
        if (!filters.isEmpty() && !query.contains("where")) {
            filtersSql.append("where 1=1 ");
        }
        for (String filterName : filters.getFiltersNames()) {
            ReportFilter filter = filters.getFilter(filterName);
            if (filter.getCondition() != null) {
                filtersSql.append(" and ").append(filter.getCondition());
            }
        }
        if (query.contains("<FILTERS>")) {
            query = query.replace("<FILTERS>", filtersSql.toString());
        } else {
            query = query + " " + filtersSql;
        }
        return query;
    }

    @Cacheable(key = "'ActiveReport'")
    public List<Report> findActives() {
        List<Long> accounts = new ArrayList<>();
        accounts.add(accountServiceAPI.getSystemAccountId());
        accounts.add(accountServiceAPI.getCurrentAccountId());
        QueryParameters params = QueryParameters.with("active", true)
                .add("group.active", true)
                .add("accountId", QueryConditions.in(accounts))
                .orderBy("name");
        return crudService().find(Report.class, params);
    }

    @Cacheable(key = "'ActiveReportByGroup-' + #reportGroup.id")
    public List<Report> findActivesByGroup(ReportGroup reportGroup) {
        return crudService().find(Report.class, QueryParameters.with("group.name", QueryConditions.eq(reportGroup.getName()))
                .add("active", true)
                .add("accountId", accountServiceAPI.getSystemAccountId()).orderBy("name"));
    }

    @Override
    @Transactional
    public Report findByEndpoint(String endpoint) {
        var report = crudService().findSingle(Report.class, QueryParameters.with("endpointName", QueryConditions.eq(endpoint)));
        if (report == null) {
            //if not fount try to find report in system account
            report = crudService().findSingle(Report.class, QueryParameters.with("endpointName", QueryConditions.eq(endpoint))
                    .add("accountId", accountServiceAPI.getSystemAccountId()));
        }
        if (report != null) {
            Hibernate.initialize(report.getFields());
            Hibernate.initialize(report.getFilters());
            Hibernate.initialize(report.getCharts());
            Hibernate.initialize(report.getGroup());
        }
        return report;
    }


    @Transactional
    @Override
    public Report findByEndpoint(String group, String endpoint) {
        var report = crudService().findSingle(Report.class, QueryParameters.with("endpointName", QueryConditions.eq(endpoint))
                .add("group.endpointName", QueryConditions.eq(group)));

        if (report == null) {
            //if not fount try to find report in system account
            report = crudService().findSingle(Report.class, QueryParameters.with("endpointName", QueryConditions.eq(endpoint))
                    .add("group.endpointName", QueryConditions.eq(group))
                    .add("accountId", accountServiceAPI.getSystemAccountId()));
        }

        if (report != null) {
            Hibernate.initialize(report.getFields());
            Hibernate.initialize(report.getFilters());
            Hibernate.initialize(report.getCharts());
            Hibernate.initialize(report.getGroup());
        }
        return report;
    }

    @Override
    public File exportReport(Report report) {
        try {
            File file = File.createTempFile("report-" + StringUtils.simplifiedString(report.getName()) + "-", ".json");
            var ignoreIds = new SimpleFilterProvider();
            ignoreIds.addFilter("ignoreIds", SimpleBeanPropertyFilter.serializeAllExcept("id", "accountId"));

            StringPojoParser.createJsonMapper().writerFor(Report.class)
                    .with(ignoreIds)
                    .writeValue(file, report);
            return file;
        } catch (IOException e) {
            throw new ReportsException("Error exporting report: " + report, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Report importReport(File file) {
        try {
            Report report = StringPojoParser.createJsonMapper().readerFor(Report.class)
                    .readValue(file);

            if (report != null) {
                report.setId(null);
                report.setName(report.getName() + " (imported)");
                report.setActive(false);
                report.setExportWithoutFormat(false);
                report.setExportEndpoint(false);
                report.setDataSourceConfig(null);
                if (report.getGroup() != null) {
                    report.setGroup(findGroup(report.getGroup().getName()));
                    report.setAccountId(report.getGroup().getAccountId());
                }

                if (report.getFilters() != null) {
                    report.getFilters().forEach(f -> {
                        f.setId(null);
                        f.setAccountId(report.getAccountId());
                        f.setReport(report);
                    });
                }

                if (report.getFields() != null) {
                    report.getFields().forEach(f -> {
                        f.setId(null);
                        f.setAccountId(report.getAccountId());
                        f.setReport(report);
                    });
                }

                if (report.getCharts() != null) {
                    report.getCharts().forEach(c -> {
                        c.setId(null);
                        c.setAccountId(report.getAccountId());
                        c.setReport(report);
                    });
                }
            }
            validate(report);
            report.save();
            return report;
        } catch (Exception e) {
            log("Error importing", e);
            throw new ReportsException("Error importing report", e);
        }
    }

    public ReportGroup findGroup(String name) {
        var group = crudService().findSingle(ReportGroup.class, "name", QueryConditions.eq(name));
        if (group == null) {
            group = new ReportGroup();
            group.setName(name);
            group.setActive(true);
            group.save();
        }
        return group;
    }

    @Override
    @Cacheable(key = "'ExportableReports-'+#includeSystem")
    @Transactional
    public List<Report> findExportableReports(boolean includeSystem) {
        List<Long> accounts = new ArrayList<>();
        accounts.add(accountServiceAPI.getSystemAccountId());
        accounts.add(accountServiceAPI.getCurrentAccountId());
        var params = QueryParameters.with("exportEndpoint", true)
                .add("active", true)
                .add("accountId", QueryConditions.in(accounts))
                .add("group.active", true)
                .orderBy("name");


        if (!includeSystem) {
            params.add("group.system", false);
        }

        var reports = crudService().find(Report.class, params);

        reports.forEach(r -> {
            Hibernate.initialize(r.getGroup());
            Hibernate.initialize(r.getFields());
            Hibernate.initialize(r.getFilters());
            Hibernate.initialize(r.getCharts());
        });

        return reports;
    }
}
