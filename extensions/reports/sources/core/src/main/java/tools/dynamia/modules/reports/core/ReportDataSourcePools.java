package tools.dynamia.modules.reports.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.modules.reports.core.domain.ReportDataSourceConfig;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

/**
 * Small connection pools for the external datasources configured in {@link ReportDataSourceConfig}: one read-only
 * pool per datasource, created on first use and replaced when the connection settings change. Datasources that were
 * not saved yet (no id) are not pooled.
 */
@Provider
public class ReportDataSourcePools {

    private static final Map<String, Entry> POOLS = new HashMap<>();

    private record Entry(String fingerprint, HikariDataSource dataSource) {
    }

    /**
     * Gets a connection from the pool of the datasource, creating the pool if needed.
     */
    public static Connection getConnection(ReportDataSourceConfig config) throws SQLException {
        ReportDataSourceValidator.validate(config);
        if (config.getId() == null) {
            return ReportDataSource.newConnection(config);
        }
        return pool(config).getConnection();
    }

    private static synchronized HikariDataSource pool(ReportDataSourceConfig config) {
        String key = String.valueOf(config.getId());
        String fingerprint = fingerprint(config);
        Entry entry = POOLS.get(key);
        if (entry != null && entry.fingerprint.equals(fingerprint) && !entry.dataSource.isClosed()) {
            return entry.dataSource;
        }
        if (entry != null) {
            entry.dataSource.close();
            POOLS.remove(key);
        }

        var hikari = new HikariConfig();
        hikari.setPoolName("reports-" + config.getId());
        hikari.setJdbcUrl(config.getUrl());
        hikari.setDriverClassName(config.getDriverClassName());
        if (config.getUsername() != null && !config.getUsername().isBlank()) {
            hikari.setUsername(config.getUsername());
        }
        if (config.getPassword() != null && !config.getPassword().isBlank()) {
            hikari.setPassword(config.getPassword());
        }
        hikari.setReadOnly(true);
        hikari.setMaximumPoolSize(4);
        hikari.setMinimumIdle(0);
        hikari.setIdleTimeout(60_000);
        hikari.setConnectionTimeout(10_000);
        hikari.setInitializationFailTimeout(-1);

        try {
            var dataSource = new HikariDataSource(hikari);
            POOLS.put(key, new Entry(fingerprint, dataSource));
            return dataSource;
        } catch (RuntimeException e) {
            throw new ReportsException("Cannot create connection pool for datasource: " + config.getName(), e);
        }
    }

    /**
     * Closes the pool of a datasource, for example after it was changed or deleted.
     */
    public static synchronized void evict(ReportDataSourceConfig config) {
        if (config != null && config.getId() != null) {
            Entry entry = POOLS.remove(String.valueOf(config.getId()));
            if (entry != null) {
                entry.dataSource.close();
            }
        }
    }

    /**
     * Number of open pools, for diagnostics and tests.
     */
    public static synchronized int size() {
        return POOLS.size();
    }

    @PreDestroy
    public void shutdown() {
        closeAll();
    }

    public static synchronized void closeAll() {
        POOLS.values().forEach(entry -> entry.dataSource.close());
        POOLS.clear();
    }

    private static String fingerprint(ReportDataSourceConfig config) {
        try {
            String text = String.join("\n", String.valueOf(config.getUrl()), String.valueOf(config.getUsername()),
                    String.valueOf(config.getPassword()), String.valueOf(config.getDriverClassName()));
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new ReportsException(e);
        }
    }
}
