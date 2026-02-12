package tools.dynamia.modules.reports.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.reports.api.EntityFilterProvider;
import tools.dynamia.modules.reports.api.EnumFilterProvider;
import tools.dynamia.modules.reports.core.domain.Report;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ReportsUtils {

    public static Connection getJdbcConnection(ReportDataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        if (connection == null) {
            throw new ReportsException("Error obtaining database connection from report datasource " + dataSource);
        }
        return connection;
    }

    public static EntityManager getJpaEntityManager(ReportDataSource dataSource) {
        if (dataSource.getDelegate() instanceof EntityManager) {
            return (EntityManager) dataSource.getDelegate();
        } else if (dataSource.getDelegate() instanceof EntityManagerFactory) {
            return ((EntityManagerFactory) dataSource.getDelegate()).createEntityManager();
        } else {
            throw new ReportsException("Error getting entity manager from datasource " + dataSource);
        }
    }

    public static EnumFilterProvider findEnumFilterProvider(String className) {
        return Containers.get().findObjects(EnumFilterProvider.class).stream()
                .filter(provider -> provider.getEnumClassName().equals(className)).findFirst().orElse(null);
    }

    public static EntityFilterProvider findEntityFilterProvider(String className) {
        return Containers.get().findObjects(EntityFilterProvider.class).stream()
                .filter(provider -> provider.getEntityClassName().equals(className)).findFirst().orElse(null);
    }

    public static List<EnumFilterProvider> findEnumFiltersProviders() {
        return Containers.get().findObjects(EnumFilterProvider.class).stream().toList();
    }

    public static List<EntityFilterProvider> findEntityFiltersProvider() {
        return Containers.get().findObjects(EntityFilterProvider.class).stream().toList();
    }

    public static String checkQuery(String query) {
        if (query == null) {
            throw new ValidationError("Invalid Query");
        }
        if (query.toLowerCase().contains("delete ") || query.toLowerCase().contains("update ")) {
            throw new ValidationError("Danger query detected: " + query);
        }

        if (query.contains(":accountId")) {
            AccountServiceAPI accountServiceAPI = Containers.get().findObject(AccountServiceAPI.class);
            if (accountServiceAPI != null) {
                query = query.replaceAll(":accountId", String.valueOf(accountServiceAPI.getCurrentAccountId()));
            }
        }
        return query;
    }

    public static ReportDataSource findDatasource(Report report) {
        if ("sql".equals(report.getQueryLang())) {
            if (report.getDataSourceConfig() != null) {
                return new ReportDataSource(report.getDataSourceConfig().getName(), report.getDataSourceConfig());
            } else {
                DataSource dataSource = Containers.get().findObject(DataSource.class);
                return new ReportDataSource("Database", dataSource);
            }
        } else {
            EntityManagerFactory em = Containers.get().findObject(EntityManagerFactory.class);
            return new ReportDataSource("EntityManager", em);
        }

    }
}
