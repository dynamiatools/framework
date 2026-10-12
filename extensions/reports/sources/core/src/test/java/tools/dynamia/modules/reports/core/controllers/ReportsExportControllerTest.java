package tools.dynamia.modules.reports.core.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.dynamia.domain.jdbc.JdbcDataSet;
import tools.dynamia.modules.reports.core.ReportData;
import tools.dynamia.modules.reports.core.ReportFilters;
import tools.dynamia.modules.reports.core.ReportsException;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportGroup;
import tools.dynamia.modules.reports.core.domain.enums.DataType;
import tools.dynamia.modules.reports.core.services.ReportsService;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportsExportControllerTest {

    private ReportsService service;
    private MockMvc mvc;
    private Report report;

    @BeforeEach
    void setUp() {
        service = mock(ReportsService.class);
        mvc = MockMvcBuilders.standaloneSetup(new ReportsExportController(service)).build();

        var group = new ReportGroup();
        group.setName("Sales");
        group.setEndpointName("sales");
        report = new Report();
        report.setName("Monthly");
        report.setGroup(group);
        report.setEndpointName("monthly");
        report.setExportEndpoint(true);
        report.setQueryScript("select 1");
        when(service.findByEndpoint("sales", "monthly")).thenReturn(report);
        when(service.execute(any(), any())).thenAnswer(inv -> data(report, List.of(
                Map.of("month", 1, "customer.name", "Ana"))));
    }

    private static ReportData data(Report report, List<Map<String, Object>> rows) {
        return ReportData.build(report, new JdbcDataSet(rows));
    }

    private void addFilter(String name, DataType type, boolean required) {
        var filter = TestFilters.filter(report, name, type, required);
        report.getFilters().add(filter);
    }

    @Test
    void catalogListsExportableReports() throws Exception {
        when(service.findExportableReports(false)).thenReturn(List.of(report));

        mvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Monthly")))
                .andExpect(jsonPath("$[0].endpoint", is("/api/reports/sales/monthly")));
    }

    @Test
    void catalogHidesReportsTheUserCannotAccess() throws Exception {
        var restricted = new Report();
        restricted.setName("Payroll");
        restricted.setAccessRoles("HR");
        when(service.findExportableReports(false)).thenReturn(List.of(report, restricted));

        mvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Monthly")));
    }

    @Test
    void executesWithGet() throws Exception {
        mvc.perform(get("/api/reports/sales/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].customer.name", is("Ana")));
    }

    @Test
    void executesWithPost() throws Exception {
        mvc.perform(post("/api/reports/sales/monthly").contentType(MediaType.APPLICATION_JSON).content("{\"options\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void passesConvertedFiltersToTheService() throws Exception {
        addFilter("year", DataType.NUMBER, false);
        addFilter("active", DataType.BOOLEAN, false);

        mvc.perform(get("/api/reports/sales/monthly?year=2025&active=true&unknown=x")).andExpect(status().isOk());

        var captor = org.mockito.ArgumentCaptor.forClass(ReportFilters.class);
        verify(service).execute(eq(report), captor.capture());
        var values = captor.getValue().getValues();
        org.junit.jupiter.api.Assertions.assertEquals(new java.math.BigDecimal("2025"), values.get("year"));
        org.junit.jupiter.api.Assertions.assertEquals(Boolean.TRUE, values.get("active"));
        org.junit.jupiter.api.Assertions.assertFalse(values.containsKey("unknown"), "filters not defined by the report are ignored");
    }

    @Test
    void unknownReportIs404() throws Exception {
        mvc.perform(get("/api/reports/sales/nothing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }

    @Test
    void inactiveReportIs403() throws Exception {
        report.setActive(false);
        mvc.perform(get("/api/reports/sales/monthly"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("REPORT_INACTIVE")));
    }

    @Test
    void reportNotExportedIs403() throws Exception {
        report.setExportEndpoint(false);
        mvc.perform(get("/api/reports/sales/monthly"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("REPORT_NOT_EXPORTED")));
    }

    @Test
    void reportRestrictedToRolesIs403WithoutRevealingAnythingElse() throws Exception {
        report.setAccessRoles("FINANCE");

        mvc.perform(get("/api/reports/sales/monthly"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("ACCESS_DENIED")))
                .andExpect(jsonPath("$.message", not(containsString("FINANCE"))));
        verify(service, never()).execute(any(), any());
    }

    @Test
    void missingRequiredFilterIs400() throws Exception {
        addFilter("year", DataType.NUMBER, true);

        mvc.perform(get("/api/reports/sales/monthly"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("year")));
        verify(service, never()).execute(any(), any());
    }

    @Test
    void invalidFilterValueIs400NamingTheFilter() throws Exception {
        addFilter("year", DataType.NUMBER, false);

        mvc.perform(get("/api/reports/sales/monthly?year=abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_REQUEST")))
                .andExpect(jsonPath("$.message", allOf(containsString("year"), containsString("number"))));
    }

    @Test
    void invalidDateAndBooleanAreRejected() throws Exception {
        addFilter("from", DataType.DATE, false);
        addFilter("active", DataType.BOOLEAN, false);

        mvc.perform(get("/api/reports/sales/monthly?from=31/12/2025")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("yyyy-MM-dd")));
        mvc.perform(get("/api/reports/sales/monthly?active=maybe")).andExpect(status().isBadRequest());
    }

    @Test
    void dateTimeAndTimeUse24HourClock() throws Exception {
        addFilter("at", DataType.DATE_TIME, false);
        addFilter("time", DataType.TIME, false);

        mvc.perform(get("/api/reports/sales/monthly").param("at", "2025-03-01 15:30:00").param("time", "18:45:10"))
                .andExpect(status().isOk());

        var captor = org.mockito.ArgumentCaptor.forClass(ReportFilters.class);
        verify(service).execute(eq(report), captor.capture());
        var at = (java.util.Date) captor.getValue().getValues().get("at");
        var calendar = java.util.Calendar.getInstance();
        calendar.setTime(at);
        org.junit.jupiter.api.Assertions.assertEquals(15, calendar.get(java.util.Calendar.HOUR_OF_DAY));
    }

    @Test
    void unexpectedErrorsDoNotLeakDetails() throws Exception {
        when(service.execute(any(), any())).thenThrow(new ReportsException("ORA-00942: table SECRET_TABLE does not exist"));

        mvc.perform(get("/api/reports/sales/monthly"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("REPORT_ERROR")))
                .andExpect(jsonPath("$.details.reference", notNullValue()))
                .andExpect(content().string(not(containsString("SECRET_TABLE"))))
                .andExpect(header().doesNotExist("X-Error-Message"));
    }

    @Test
    void outputKeepsColumnOrder() throws Exception {
        var rows = new java.util.ArrayList<Map<String, Object>>();
        var row = new java.util.LinkedHashMap<String, Object>();
        row.put("zeta", 1);
        row.put("alpha", 2);
        row.put("mid", 3);
        rows.add(row);
        when(service.execute(any(), any())).thenAnswer(inv -> data(report, rows));

        var json = mvc.perform(get("/api/reports/sales/monthly")).andReturn().getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(json.indexOf("zeta") < json.indexOf("alpha") && json.indexOf("alpha") < json.indexOf("mid"), json);
    }

    @Test
    void truncatedResultsAreFlagged() throws Exception {
        when(service.execute(any(), any())).thenAnswer(inv -> {
            var data = data(report, List.of(Map.of("a", 1)));
            data.setTruncated(true);
            return data;
        });

        mvc.perform(get("/api/reports/sales/monthly")).andExpect(jsonPath("$.truncated", is(true)));
    }
}
