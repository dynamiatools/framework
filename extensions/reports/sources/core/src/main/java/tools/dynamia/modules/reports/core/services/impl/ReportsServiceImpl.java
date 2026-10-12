package tools.dynamia.modules.reports.core.services.impl;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Parameter;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.jdbc.JdbcDataSet;
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
import tools.dynamia.modules.reports.core.security.ReportAccess;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "reports")
public class ReportsServiceImpl extends AbstractService implements ReportsService {


    private static final String ACCOUNT_KEY = "#root.target.currentAccountKey()";

    private final AccountServiceAPI accountServiceAPI;
    private ReportsSettings settings = new ReportsSettings();

    public ReportsServiceImpl(AccountServiceAPI accountServiceAPI) {
        this.accountServiceAPI = accountServiceAPI;
    }

    @Autowired(required = false)
    public void setSettings(ReportsSettings settings) {
        this.settings = settings;
    }

    /**
     * Account used in the cache keys, so cached lists are never shared between accounts.
     */
    public Long currentAccountKey() {
        return accountServiceAPI.getCurrentAccountId();
    }

    @Override
    public ReportData execute(Report report, ReportFilters filters, ReportDataSource datasource) {
        return execute(report, filters, datasource, settings.getMaxRows());
    }

    @Override
    public ReportData execute(Report report, ReportFilters filters, ReportDataSource datasource, int maxRows) {
        ReportAccess.check(report);
        String lang = report.getQueryLang() == null ? "" : report.getQueryLang().toLowerCase();
        if (!"sql".equals(lang) && !"jpql".equals(lang)) {
            throw new ReportsException("Unsupported query language [" + report.getQueryLang() + "] in report " + report.getName());
        }
        if (filters == null) {
            filters = new ReportFilters();
        }
        log("Executing query for report: " + report.getName() + " - " + lang);
        long start = System.currentTimeMillis();
        loadDefaultFilters(report, filters);
        ReportData data = "sql".equals(lang) ? executeSQL(report, filters, datasource, maxRows) : executeJPQL(report, filters, datasource, maxRows);
        long end = System.currentTimeMillis();
        log("Report " + report.getName() + " executed in " + (end - start) + "ms" + (data.isTruncated() ? " (truncated)" : ""));
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
    @Cacheable(key = "'Report-' + #id + '-' + " + ACCOUNT_KEY)
    public Report loadReportModel(Long id) {
        return findSingleInAccounts(account -> QueryParameters.with("id", id).add("accountId", account), true);
    }

    private ReportData executeSQL(Report report, ReportFilters filters, ReportDataSource dataSource, int maxRows) {
        String sql = ReportQueryValidator.validateQuery(ReportQueryBuilder.build(report.getQueryScript(), filters), "sql");

        try (Connection connection = ReportsUtils.getJdbcConnection(dataSource)) {
            setReadOnly(connection, true);
            try {
                var template = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
                template.setQueryTimeout(settings.getQueryTimeoutSeconds());
                if (maxRows > 0) {
                    template.setMaxRows(maxRows == Integer.MAX_VALUE ? maxRows : maxRows + 1);
                }
                List<Map<String, Object>> rows = new NamedParameterJdbcTemplate(template).queryForList(sql, filters.getValues());
                boolean truncated = maxRows > 0 && rows.size() > maxRows;
                if (truncated) {
                    rows = new ArrayList<>(rows.subList(0, maxRows));
                }
                ReportData data = ReportData.build(report, new JdbcDataSet(rows));
                data.setTruncated(truncated);
                return data;
            } finally {
                setReadOnly(connection, false);
            }
        } catch (SQLException | DataAccessException e) {
            throw new ReportsException("Error executing report [" + report.getName() + "]: " + e.getMessage(), e);
        }
    }

    private void setReadOnly(Connection connection, boolean readOnly) {
        try {
            connection.setReadOnly(readOnly);
        } catch (SQLException | RuntimeException e) {
            // Some drivers do not support it. Queries are still validated and limited.
            if (readOnly) {
                logWarn("Cannot set report connection as read-only: " + e.getMessage());
            }
        }
    }

    private ReportData executeJPQL(Report report, ReportFilters filters, ReportDataSource dataSource, int maxRows) {
        String jpql = ReportQueryValidator.validateQuery(ReportQueryBuilder.build(report.getQueryScript(), filters), "jpql");
        boolean ownsEntityManager = dataSource.getDelegate() instanceof EntityManagerFactory;
        EntityManager em = ReportsUtils.getJpaEntityManager(dataSource);
        try {
            Query query = em.createQuery(jpql);
            Set<String> declared = query.getParameters().stream().map(Parameter::getName)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            filters.getValues().forEach((name, value) -> {
                if (declared.contains(name)) {
                    query.setParameter(name, value);
                }
            });
            if (maxRows > 0 && maxRows < Integer.MAX_VALUE) {
                query.setMaxResults(maxRows + 1);
            }
            hint(query, "jakarta.persistence.query.timeout", settings.getQueryTimeoutSeconds() * 1000);
            hint(query, "org.hibernate.readOnly", true);

            List<?> result = query.getResultList();
            boolean truncated = maxRows > 0 && result.size() > maxRows;
            if (truncated) {
                result = new ArrayList<>(result.subList(0, maxRows));
            }
            ReportData data = ReportData.build(report, result);
            data.setTruncated(truncated);
            return data;
        } catch (PersistenceException | IllegalArgumentException e) {
            throw new ReportsException("Error executing report [" + report.getName() + "]: " + e.getMessage(), e);
        } finally {
            if (ownsEntityManager) {
                em.close();
            }
        }
    }

    private static void hint(Query query, String name, Object value) {
        try {
            query.setHint(name, value);
        } catch (IllegalArgumentException ignored) {
            // hint not supported by the provider
        }
    }

    @Override
    @Cacheable(key = "'ActiveReport-' + " + ACCOUNT_KEY)
    public List<Report> findActives() {
        return findInAccounts(account -> QueryParameters.with("active", true)
                .add("group.active", true)
                .add("accountId", account)
                .orderBy("name"), false);
    }

    @Override
    @Cacheable(key = "'ActiveReportByGroup-' + #reportGroup.id + '-' + " + ACCOUNT_KEY)
    public List<Report> findActivesByGroup(ReportGroup reportGroup) {
        return findInAccounts(account -> QueryParameters.with("group.name", QueryConditions.eq(reportGroup.getName()))
                .add("active", true)
                .add("accountId", account)
                .orderBy("name"), false);
    }

    @Override
    public Report findByEndpoint(String endpoint) {
        return findSingleInAccounts(account -> QueryParameters.with("endpointName", QueryConditions.eq(endpoint))
                .add("accountId", account), true);
    }

    @Override
    public Report findByEndpoint(String group, String endpoint) {
        return findSingleInAccounts(account -> QueryParameters.with("endpointName", QueryConditions.eq(endpoint))
                .add("group.endpointName", QueryConditions.eq(group))
                .add("accountId", account), true);
    }

    /**
     * Reports are tenant entities: Hibernate only returns the rows of the tenant bound when the session opens, so the
     * reports of the system account are not visible from another account. Each account is read as itself, in its own
     * session, and the system reports are added to the ones of the current account.
     *
     * @param query      the query of the reports of one account
     * @param initialize true to load the lazy parts of every report (fields, filters and charts)
     * @return the reports of the system account and of the current account, sorted by name
     */
    private List<Report> findInAccounts(Function<Long, QueryParameters> query, boolean initialize) {
        List<Report> reports = new ArrayList<>();
        for (Long account : accountsToRead()) {
            reports.addAll(inAccount(account, () -> crudService().find(Report.class, query.apply(account)), initialize));
        }
        reports.sort(Comparator.comparing(Report::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
        return reports;
    }

    /**
     * Finds one report, looking first in the current account and then in the system account.
     */
    private Report findSingleInAccounts(Function<Long, QueryParameters> query, boolean initialize) {
        var accounts = accountsToRead();
        // the current account has priority over the system one
        Collections.reverse(accounts);
        for (Long account : accounts) {
            Report report = inAccount(account, () -> crudService().findSingle(Report.class, query.apply(account)), initialize);
            if (report != null) {
                return report;
            }
        }
        return null;
    }

    private List<Long> accountsToRead() {
        var accounts = new ArrayList<Long>();
        accounts.add(accountServiceAPI.getSystemAccountId());
        Long current = accountServiceAPI.getCurrentAccountId();
        if (current != null && !accounts.contains(current)) {
            accounts.add(current);
        }
        return accounts;
    }

    /**
     * Runs the work as the given account, in a new session and transaction, because the tenant of a session is fixed
     * when it opens. The reports leave the session fully initialized.
     */
    @SuppressWarnings("unchecked")
    private <T> T inAccount(Long account, Supplier<T> work, boolean initialize) {
        Object[] result = new Object[1];
        accountServiceAPI.withAccount(account, () -> {
            crudService().executeWithinTransaction(() -> {
                T value = work.get();
                initialize(value, initialize);
                result[0] = value;
            });
            return null;
        });
        return (T) result[0];
    }

    private static void initialize(Object value, boolean deep) {
        if (value instanceof Report report) {
            initialize(report, deep);
        } else if (value instanceof List<?> list) {
            list.forEach(r -> initialize((Report) r, deep));
        }
    }

    private static void initialize(Report report, boolean deep) {
        Hibernate.initialize(report.getGroup());
        if (deep) {
            Hibernate.initialize(report.getFields());
            Hibernate.initialize(report.getFilters());
            Hibernate.initialize(report.getCharts());
        }
    }

    @Override
    public File exportReport(Report report) {
        try {
            File file = File.createTempFile("report-" + StringUtils.simplifiedString(report.getName()) + "-", ".json");
            file.deleteOnExit();
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

            if (report == null) {
                throw new ReportsException("The file does not contain a report");
            }
            if (report.getGroup() == null || report.getGroup().getName() == null || report.getGroup().getName().isBlank()) {
                throw new ReportsException("The report file has no group");
            }

            report.setId(null);
            report.setName(report.getName() + " (imported)");
            report.setActive(false);
            report.setExportWithoutFormat(false);
            report.setExportEndpoint(false);
            report.setDataSourceConfig(null);
            report.setGroup(findGroup(report.getGroup().getName()));
            report.setAccountId(report.getGroup().getAccountId());

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

            validate(report);
            report.save();
            return report;
        } catch (ReportsException | ValidationError e) {
            throw e;
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
    @Cacheable(key = "'ExportableReports-' + #includeSystem + '-' + " + ACCOUNT_KEY)
    public List<Report> findExportableReports(boolean includeSystem) {
        return findInAccounts(account -> {
            var params = QueryParameters.with("exportEndpoint", true)
                    .add("active", true)
                    .add("accountId", account)
                    .add("group.active", true)
                    .orderBy("name");
            if (!includeSystem) {
                params.add("group.system", false);
            }
            return params;
        }, true);
    }
}
