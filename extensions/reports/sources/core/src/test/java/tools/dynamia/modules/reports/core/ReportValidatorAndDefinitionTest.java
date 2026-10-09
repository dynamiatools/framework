package tools.dynamia.modules.reports.core;

import org.junit.jupiter.api.Test;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportGroup;
import tools.dynamia.modules.reports.core.domain.enums.DataType;
import tools.dynamia.modules.reports.core.services.impl.ReportsServiceImpl;
import tools.dynamia.modules.saas.api.AccountServiceAPI;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ReportValidatorAndDefinitionTest {

    private final ReportValidator validator = new ReportValidator();

    private static Report validReport() {
        var report = TestDb.sqlReport("ok", "select id from sales");
        var group = new ReportGroup();
        group.setName("g");
        report.setGroup(group);
        return report;
    }

    @Test
    void acceptsAValidReport() {
        assertDoesNotThrow(() -> validator.validate(validReport()));
    }

    @Test
    void rejectsModifyingQueryScripts() {
        var report = validReport();
        report.setQueryScript("update sales set total = 0");
        assertThrows(ValidationError.class, () -> validator.validate(report));
    }

    @Test
    void rejectsDangerousFilterConditionsAndOptionQueries() {
        var report = validReport();
        TestDb.filter(report, "x", "1=1; delete from sales", DataType.TEXT);
        assertThrows(ValidationError.class, () -> validator.validate(report));

        var other = validReport();
        TestDb.filter(other, "x", "a = :x", DataType.TEXT).setQueryValues("drop table sales");
        assertThrows(ValidationError.class, () -> validator.validate(other));
    }

    @Test
    void endpointRequiresGroupAndNames() {
        var report = validReport();
        report.setExportEndpoint(true);
        assertThrows(ValidationError.class, () -> validator.validate(report), "group has no endpoint name");

        var withoutGroup = TestDb.sqlReport("x", "select 1");
        withoutGroup.setExportEndpoint(true);
        assertThrows(ValidationError.class, () -> validator.validate(withoutGroup), "must not fail with a NullPointerException");
    }

    @Test
    void fullEndpointDoesNotFailWithoutGroupOrFlag() {
        var report = new Report();
        assertEquals("", report.getFullEndpoint());
        report.setExportEndpoint(true);
        report.setEndpointName("monthly");
        assertEquals("", report.getFullEndpoint());
    }

    @Test
    void exportedDefinitionHasNoIdsAndKeepsTheAccessRoles() throws Exception {
        var report = validReport();
        report.setId(10L);
        report.setAccountId(3L);
        report.setAccessRoles("FINANCE");
        report.setActive(true);
        var service = new ReportsServiceImpl(mock(AccountServiceAPI.class));

        File file = service.exportReport(report);
        try {
            var json = Files.readString(file.toPath());
            var tree = StringPojoParser.createJsonMapper().readTree(json);
            assertFalse(tree.has("id"), json);
            assertFalse(tree.has("accountId"), json);
            assertEquals("FINANCE", tree.get("accessRoles").asText());
            assertEquals("select id from sales", tree.get("queryScript").asText());
            assertTrue(tree.get("active").asBoolean(), "flags are exported once, from the is accessor");
            assertTrue(tree.has("autofields") && tree.has("chartable"), json);
        } finally {
            file.delete();
        }
    }

    @Test
    void exportedDefinitionNeverContainsTheDatasourcePassword() throws Exception {
        var report = validReport();
        var config = new tools.dynamia.modules.reports.core.domain.ReportDataSourceConfig();
        config.setName("ext");
        config.setPassword("top-secret");
        report.setDataSourceConfig(config);
        var service = new ReportsServiceImpl(mock(AccountServiceAPI.class));

        File file = service.exportReport(report);
        try {
            assertFalse(Files.readString(file.toPath()).contains("top-secret"));
        } finally {
            file.delete();
        }
    }

    @Test
    void importFailsClearlyWhenTheFileHasNoGroup() throws Exception {
        var service = new ReportsServiceImpl(mock(AccountServiceAPI.class));
        File file = File.createTempFile("report-", ".json");
        try {
            Files.writeString(file.toPath(), "{\"name\":\"x\",\"queryScript\":\"select 1\"}");
            var error = assertThrows(ReportsException.class, () -> service.importReport(file));
            assertTrue(error.getMessage().contains("group"), error.getMessage());
        } finally {
            file.delete();
        }
    }

    @Test
    void importFailsClearlyWhenTheFileIsNotAReport() throws Exception {
        var service = new ReportsServiceImpl(mock(AccountServiceAPI.class));
        File file = File.createTempFile("report-", ".json");
        try {
            Files.writeString(file.toPath(), "null");
            assertThrows(ReportsException.class, () -> service.importReport(file));
        } finally {
            file.delete();
        }
    }
}
