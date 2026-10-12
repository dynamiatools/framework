package tools.dynamia.modules.reports.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.reports.core.domain.ReportDataSourceConfig;

import java.sql.Connection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReportSecurityComponentsTest {

    @BeforeEach
    @AfterEach
    void reset() {
        ReportSecrets.configure(null);
        ReportDataSourceValidator.configureAllowedDrivers(null);
        ReportDataSourcePools.closeAll();
    }

    private static ReportDataSourceConfig h2Config(Long id) {
        var config = new ReportDataSourceConfig();
        config.setId(id);
        config.setName("h2");
        config.setDriverClassName("org.h2.Driver");
        config.setUrl("jdbc:h2:mem:pool" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        return config;
    }

    // ---- secrets

    @Test
    void secretsAreEncryptedWhenKeyIsConfigured() {
        ReportSecrets.configure("my key");
        var encrypted = ReportSecrets.encrypt("s3cret");

        assertTrue(ReportSecrets.isEncrypted(encrypted));
        assertFalse(encrypted.contains("s3cret"));
        assertEquals("s3cret", ReportSecrets.decrypt(encrypted));
        assertNotEquals(encrypted, ReportSecrets.encrypt("s3cret"), "each encryption uses a new IV");
    }

    @Test
    void plainValuesStayReadableAfterEnablingEncryption() {
        ReportSecrets.configure("my key");
        assertEquals("legacy", ReportSecrets.decrypt("legacy"));
        assertNull(ReportSecrets.decrypt(null));
        assertEquals(ReportSecrets.encrypt(""), "");
    }

    @Test
    void withoutKeyValuesAreStoredAsIs() {
        assertEquals("s3cret", ReportSecrets.encrypt("s3cret"));
    }

    @Test
    void encryptedValueCannotBeReadWithAnotherKeyOrWithoutKey() {
        ReportSecrets.configure("key one");
        var encrypted = ReportSecrets.encrypt("s3cret");

        ReportSecrets.configure("key two");
        assertThrows(ReportsException.class, () -> ReportSecrets.decrypt(encrypted));

        ReportSecrets.configure(null);
        assertThrows(ReportsException.class, () -> ReportSecrets.decrypt(encrypted));
    }

    @Test
    void converterEncryptsAndDecrypts() {
        ReportSecrets.configure("my key");
        var converter = new ReportSecretConverter();
        var stored = converter.convertToDatabaseColumn("pw");
        assertTrue(ReportSecrets.isEncrypted(stored));
        assertEquals("pw", converter.convertToEntityAttribute(stored));
    }

    @Test
    void settingsApplyTheSecurityConfiguration() {
        var settings = new ReportsSettings();
        settings.setEncryptionKey("k");
        settings.setAllowedDrivers("org.h2.Driver, org.sqlite.JDBC");
        settings.apply();

        assertTrue(ReportSecrets.isEncrypted(ReportSecrets.encrypt("x")));
        assertTrue(ReportDataSourceValidator.getAllowedDrivers().contains("org.h2.Driver"));
        assertTrue(ReportDataSourceValidator.getAllowedDrivers().contains("org.sqlite.JDBC"));
    }

    // ---- datasource validation

    @Test
    void commonServerDriversAreAllowedByDefault() {
        assertDoesNotThrow(() -> ReportDataSourceValidator.validate("org.postgresql.Driver", "jdbc:postgresql://localhost/db"));
        assertDoesNotThrow(() -> ReportDataSourceValidator.validate("com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost/db"));
    }

    @Test
    void unknownOrEmbeddedDriversAreRejectedByDefault() {
        assertThrows(ValidationError.class, () -> ReportDataSourceValidator.validate("org.h2.Driver", "jdbc:h2:mem:x"));
        assertThrows(ValidationError.class, () -> ReportDataSourceValidator.validate("com.evil.Driver", "jdbc:evil:x"));
        assertThrows(ValidationError.class, () -> ReportDataSourceValidator.validate(null, "jdbc:mysql://x"));
        assertThrows(ValidationError.class, () -> ReportDataSourceValidator.validate("org.postgresql.Driver", ""));
    }

    @Test
    void configuredDriversReplaceTheDefaults() {
        ReportDataSourceValidator.configureAllowedDrivers("org.h2.Driver");
        assertDoesNotThrow(() -> ReportDataSourceValidator.validate("org.h2.Driver", "jdbc:h2:mem:x"));
        assertThrows(ValidationError.class, () -> ReportDataSourceValidator.validate("org.postgresql.Driver", "jdbc:postgresql://x/db"));
    }

    @Test
    void urlMustBeJdbcWithoutDangerousOptions() {
        assertThrows(ValidationError.class, () -> ReportDataSourceValidator.validate("org.postgresql.Driver", "http://evil/x"));
        assertThrows(ValidationError.class, () -> ReportDataSourceValidator.validate("com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://h/db?autoDeserialize=true"));
        assertThrows(ValidationError.class, () -> ReportDataSourceValidator.validate("com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://h/db?allowLoadLocalInfile=true"));
        assertThrows(ValidationError.class, () -> ReportDataSourceValidator.validate("org.postgresql.Driver",
                "jdbc:postgresql://h/db?socketFactory=com.evil.Factory"));
    }

    @Test
    void newConnectionValidatesTheConfig() {
        var config = h2Config(null);
        assertThrows(ValidationError.class, () -> ReportDataSource.newConnection(config));
    }

    @Test
    void validatorProviderChecksTheConfig() {
        assertThrows(ValidationError.class, () -> new ReportDataSourceConfigValidator().validate(h2Config(1L)));
    }

    // ---- pools

    @Test
    void savedDatasourcesArePooledAndReused() throws Exception {
        ReportDataSourceValidator.configureAllowedDrivers("org.h2.Driver");
        var config = h2Config(7L);

        try (Connection first = ReportDataSourcePools.getConnection(config);
             Connection second = ReportDataSourcePools.getConnection(config)) {
            assertTrue(first.isValid(1));
            assertTrue(second.isValid(1));
        }
        assertEquals(1, ReportDataSourcePools.size());
    }

    @Test
    void poolIsReplacedWhenTheSettingsChangeAndClosedOnEvict() throws Exception {
        ReportDataSourceValidator.configureAllowedDrivers("org.h2.Driver");
        var config = h2Config(8L);
        ReportDataSourcePools.getConnection(config).close();

        config.setUrl("jdbc:h2:mem:other" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        ReportDataSourcePools.getConnection(config).close();
        assertEquals(1, ReportDataSourcePools.size());

        new ReportDataSourceConfigListener().afterUpdate(config);
        assertEquals(0, ReportDataSourcePools.size());
    }

    @Test
    void unsavedDatasourcesAreNotPooled() throws Exception {
        ReportDataSourceValidator.configureAllowedDrivers("org.h2.Driver");
        try (Connection connection = ReportDataSourcePools.getConnection(h2Config(null))) {
            assertTrue(connection.isValid(1));
        }
        assertEquals(0, ReportDataSourcePools.size());
    }

    @Test
    void passwordIsWriteOnlyInJson() throws Exception {
        var config = h2Config(1L);
        config.setPassword("top-secret");
        var json = tools.dynamia.commons.StringPojoParser.createJsonMapper().writeValueAsString(config);
        assertFalse(json.contains("top-secret"), json);
        assertFalse(json.contains("password"), json);
    }
}
