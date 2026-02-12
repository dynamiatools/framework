package tools.dynamia.modules.reports.core;

import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import tools.dynamia.domain.ValidatorUtil;
import tools.dynamia.modules.reports.core.domain.ReportDataSourceConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ReportDataSource extends AbstractDataSource {


    private String name;
    private Object delegate;

    public ReportDataSource(String name, Object delegate) {
        this.name = name;
        this.delegate = delegate;

    }

    public Object getDelegate() {
        return delegate;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (delegate instanceof Connection connection) {
            return connection;
        } else if (delegate instanceof DataSource dataSource) {
            return dataSource.getConnection();
        } else if (delegate instanceof ReportDataSourceConfig config) {
            return newConnection(config);
        }

        return null;
    }

    public static Connection newConnection(ReportDataSourceConfig config) {
        ValidatorUtil.validateEmpty(config.getDriverClassName(), "Select datasource driver class");
        ValidatorUtil.validateEmpty(config.getUrl(), "Enter datasource jdbc valid URL");

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(config.getDriverClassName());
        dataSource.setUrl(config.getUrl());
        if (config.getUsername() != null && !config.getUsername().isBlank()) {
            dataSource.setUsername(config.getUsername());
        }
        if (config.getPassword() != null && !config.getPassword().isBlank()) {
            dataSource.setPassword(config.getPassword());
        }

        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new ReportsException("Cannot create database connection using datasource: " + config.getName() + ". " + e.getMessage());
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

}
