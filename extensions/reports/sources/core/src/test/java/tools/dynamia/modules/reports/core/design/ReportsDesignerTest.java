package tools.dynamia.modules.reports.core.design;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.reports.api.v2.ReportPreviewRequest;
import tools.dynamia.modules.reports.core.ReportNotFoundException;
import tools.dynamia.modules.reports.core.ReportsSettings;
import tools.dynamia.modules.reports.core.api.ApiFixtures;
import tools.dynamia.modules.reports.core.controllers.ReportsDesignerController;
import tools.dynamia.modules.reports.core.domain.ReportDataSourceConfig;
import tools.dynamia.modules.reports.core.security.ReportAccessDeniedException;
import tools.dynamia.modules.reports.core.security.ReportDesigners;
import tools.dynamia.modules.reports.core.security.ReportDesignerPolicy;
import tools.dynamia.modules.reports.core.security.RolesReportDesignerPolicy;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportsDesignerTest {

    private static final Principal USER = () -> "user";

    private ApiFixtures.InMemoryReports reports;
    private ReportsSettings settings;
    private boolean designer;
    private ReportsDesignerService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        reports = new ApiFixtures.InMemoryReports();
        reports.add(1L, "Sales", "Sales list", "select id from sales");
        settings = new ReportsSettings();
        settings.setPreviewLimit(2);
        designer = true;
        service = new ReportsDesignerService(reports, settings) {
            @Override
            protected void checkDesigner() {
                if (!designer) {
                    throw new ReportAccessDeniedException("designer");
                }
            }

            @Override
            protected tools.dynamia.domain.services.CrudService crudService() {
                var crud = org.mockito.Mockito.mock(tools.dynamia.domain.services.CrudService.class);
                return crud;
            }

            @Override
            protected Long currentAccountId() {
                return 2L;
            }
        };
        mvc = MockMvcBuilders.standaloneSetup(new ReportsDesignerController(service, settings)).build();
    }

    private static ReportPreviewRequest preview(String lang, String query, Map<String, Object> parameters) {
        return new ReportPreviewRequest(lang, query, null, parameters);
    }

    // ---- service

    @Test
    void previewReturnsTheFirstRowsAndFlagsTruncation() {
        var result = service.preview(preview("sql", "select id, region from sales order by id", null));

        assertEquals(List.of("ID", "REGION"), result.columns());
        assertEquals(2, result.rows().size());
        assertTrue(result.truncated());
        assertEquals(1, result.rows().get(0).get("ID"));
    }

    @Test
    void previewBindsParametersAndUsesTheCurrentAccount() {
        var byRegion = service.preview(preview("sql", "select id from sales where region = :region", Map.of("region", "north")));
        assertEquals(2, byRegion.rows().size());
        assertFalse(byRegion.truncated());

        var byAccount = service.preview(preview("sql", "select id from sales where account_id = :accountId order by id", null));
        assertEquals(List.of(3, 4), byAccount.rows().stream().map(r -> r.get("ID")).toList());
    }

    @Test
    void previewRejectsDangerousQueriesAndLanguages() {
        assertThrows(ValidationError.class, () -> service.preview(preview("sql", "delete from sales", null)));
        assertThrows(ValidationError.class, () -> service.preview(preview("sql", "select 1; drop table sales", null)));
        assertThrows(ValidationError.class, () -> service.preview(preview("groovy", "select 1", null)));
        assertThrows(ValidationError.class, () -> service.preview(preview(null, "select 1", null)));
    }

    @Test
    void previewWithUnknownDatasourceFails() {
        assertThrows(ReportNotFoundException.class, () -> service.preview(new ReportPreviewRequest("sql", "select 1", 9L, null)));
    }

    @Test
    void everyOperationNeedsTheDesignerPermission() {
        designer = false;
        assertThrows(ReportAccessDeniedException.class, () -> service.preview(preview("sql", "select 1", null)));
        assertThrows(ReportAccessDeniedException.class, () -> service.exportDefinition(1L));
        assertThrows(ReportAccessDeniedException.class, () -> service.importDefinition("{}"));
        assertThrows(ReportAccessDeniedException.class, () -> service.testDataSource(1L));
    }

    @Test
    void exportedDefinitionIsJsonWithoutIds() {
        var json = service.exportDefinition(1L);
        assertTrue(json.contains("\"name\" : \"Sales list\"") || json.contains("\"name\":\"Sales list\""), json);
        assertFalse(json.contains("\"id\""));
        assertThrows(ReportNotFoundException.class, () -> service.exportDefinition(77L));
    }

    @Test
    void importPassesTheDefinitionToTheReportsService() {
        var id = service.importDefinition("{\"name\":\"x\",\"queryScript\":\"select 1\"}");
        assertEquals(99L, id);
        assertTrue(reports.lastImported.contains("\"queryScript\""));
    }

    @Test
    void importRejectsEmptyHugeAndInvalidJson() {
        assertThrows(ValidationError.class, () -> service.importDefinition(" "));
        assertThrows(ValidationError.class, () -> service.importDefinition("{not json"));
        assertThrows(ValidationError.class, () -> service.importDefinition("x".repeat(ReportsDesignerService.MAX_DEFINITION_SIZE + 1)));
    }

    // ---- policy

    @Test
    void nobodyIsADesignerByDefault() {
        var policy = new RolesReportDesignerPolicy(new ReportsSettings());
        assertFalse(policy.canDesign());
        assertFalse(ReportDesigners.canDesign(List.of(policy)));
    }

    @Test
    void anyPolicyThatAllowsMakesTheUserADesigner() {
        ReportDesignerPolicy deny = () -> false;
        ReportDesignerPolicy allow = () -> true;
        assertTrue(ReportDesigners.canDesign(List.of(deny, allow)));
        assertFalse(ReportDesigners.canDesign(List.of(deny)));
        assertFalse(ReportDesigners.canDesign(List.of()));
        assertThrows(ReportAccessDeniedException.class, ReportDesigners::check, "no container: fail closed");
    }

    @Test
    void rolesPolicyChecksTheRequestUser() {
        var settings = new ReportsSettings();
        settings.setDesignerRoles("ADMIN, REPORTER");
        var policy = new RolesReportDesignerPolicy(settings);
        assertFalse(policy.canDesign(), "no request");

        var request = org.mockito.Mockito.mock(jakarta.servlet.http.HttpServletRequest.class);
        org.mockito.Mockito.when(request.isUserInRole("ROLE_REPORTER")).thenReturn(true);
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request));
        try {
            assertTrue(policy.canDesign());
        } finally {
            org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        }
    }

    // ---- controller

    @Test
    void controllerRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/reports/v2/design/info")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/reports/v2/design/preview").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
    }

    @Test
    void infoTellsIfTheUserCanDesign() throws Exception {
        // ReportDesigners has no container in this test: nobody is a designer
        mvc.perform(get("/api/reports/v2/design/info").principal(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed", is(false)))
                .andExpect(jsonPath("$.previewLimit", is(2)));
    }

    @Test
    void controllerPreviewDefinitionImportAndTest() throws Exception {
        mvc.perform(post("/api/reports/v2/design/preview").principal(USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"queryLang\":\"sql\",\"queryScript\":\"select id from sales order by id\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows", hasSize(2)))
                .andExpect(jsonPath("$.truncated", is(true)));

        mvc.perform(post("/api/reports/v2/design/preview").principal(USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"queryLang\":\"sql\",\"queryScript\":\"drop table sales\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/reports/v2/design/1/definition").principal(USER))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", startsWith("application/json")))
                .andExpect(jsonPath("$.name", is("Sales list")));

        mvc.perform(get("/api/reports/v2/design/55/definition").principal(USER)).andExpect(status().isNotFound());

        mvc.perform(post("/api/reports/v2/design/import").principal(USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"queryScript\":\"select 1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(99)));
    }

    @Test
    void controllerDeniesNonDesigners() throws Exception {
        designer = false;
        mvc.perform(post("/api/reports/v2/design/preview").principal(USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"queryLang\":\"sql\",\"queryScript\":\"select 1\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("ACCESS_DENIED")));
        mvc.perform(get("/api/reports/v2/design/1/definition").principal(USER)).andExpect(status().isForbidden());
        mvc.perform(post("/api/reports/v2/design/datasources/1/test").principal(USER)).andExpect(status().isForbidden());
    }
}
