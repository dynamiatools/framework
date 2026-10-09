package tools.dynamia.modules.reports.core.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.dynamia.modules.reports.core.api.ApiFixtures;
import tools.dynamia.modules.reports.core.api.ReportsApiService;
import tools.dynamia.modules.reports.core.domain.ReportFilter;
import tools.dynamia.modules.reports.core.domain.enums.DataType;

import java.security.Principal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportsApiControllerTest {

    private static final Principal USER = () -> "user";

    private ApiFixtures.InMemoryReports reports;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        reports = new ApiFixtures.InMemoryReports();
        var sales = reports.add(1L, "Sales", "By region", "select id, region, total from sales order by id");
        var filter = new ReportFilter("min");
        filter.setReport(sales);
        filter.setLabel("Minimum");
        filter.setCondition("total >= :min");
        filter.setDataType(DataType.NUMBER);
        sales.getFilters().add(filter);
        reports.chart(sales, "Totals", "bar", "region", "total", true);
        mvc = MockMvcBuilders.standaloneSetup(new ReportsApiController(new ReportsApiService(reports))).build();
    }

    @Test
    void everyEndpointRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/reports/v2/catalog")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error", is("UNAUTHORIZED")));
        mvc.perform(get("/api/reports/v2/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/reports/v2/1/filters/min/options")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/reports/v2/1/run").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/reports/v2/1/export").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
    }

    @Test
    void catalog() throws Exception {
        mvc.perform(get("/api/reports/v2/catalog").principal(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Sales")))
                .andExpect(jsonPath("$[0].reports[0].id", is(1)))
                .andExpect(jsonPath("$[0].reports[0].chartable", is(true)));
    }

    @Test
    void definition() throws Exception {
        mvc.perform(get("/api/reports/v2/1").principal(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report.name", is("By region")))
                .andExpect(jsonPath("$.filters[0].name", is("min")))
                .andExpect(jsonPath("$.filters[0].dataType", is("NUMBER")))
                .andExpect(jsonPath("$.filters[0].optionsSource", is("NONE")))
                .andExpect(jsonPath("$.charts[0].type", is("bar")))
                .andExpect(jsonPath("$.exportFormats", contains("xlsx", "csv", "pdf")));
    }

    @Test
    void definitionDoesNotLeakClassNamesOrQueries() throws Exception {
        var json = mvc.perform(get("/api/reports/v2/1").principal(USER)).andReturn().getResponse().getContentAsString();
        assertTrue(!json.contains("select ") && !json.contains("tools.dynamia"), json);
    }

    @Test
    void unknownReportIs404AndNonNumericIdDoesNotMatch() throws Exception {
        mvc.perform(get("/api/reports/v2/99").principal(USER)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
        mvc.perform(get("/api/reports/v2/abc").principal(USER)).andExpect(status().isNotFound());
    }

    @Test
    void restrictedReportIs403() throws Exception {
        reports.reports.get(0).setAccessRoles("HR");
        mvc.perform(get("/api/reports/v2/1").principal(USER)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("ACCESS_DENIED")));
        mvc.perform(post("/api/reports/v2/1/run").principal(USER)).andExpect(status().isForbidden());
    }

    @Test
    void runWithoutBody() throws Exception {
        mvc.perform(post("/api/reports/v2/1/run").principal(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(4)))
                .andExpect(jsonPath("$.rows", hasSize(4)))
                .andExpect(jsonPath("$.columns[0].name", is("ID")))
                .andExpect(jsonPath("$.charts[0].labels", contains("north", "south", "east")));
    }

    @Test
    void runWithFiltersPagingAndSort() throws Exception {
        var body = "{\"filters\":{\"min\":\"100\"},\"page\":0,\"size\":2,\"sort\":\"TOTAL\",\"direction\":\"desc\"}";
        mvc.perform(post("/api/reports/v2/1/run").principal(USER).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(3)))
                .andExpect(jsonPath("$.rows", hasSize(2)))
                .andExpect(jsonPath("$.rows[0].ID", is(3)))
                .andExpect(jsonPath("$.size", is(2)));
    }

    @Test
    void invalidFilterAndSortAre400() throws Exception {
        mvc.perform(post("/api/reports/v2/1/run").principal(USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filters\":{\"min\":\"abc\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("min")));
        mvc.perform(post("/api/reports/v2/1/run").principal(USER).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sort\":\"nope\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void executionErrorsDoNotLeakDetails() throws Exception {
        reports.reports.get(0).setQueryScript("select * from secret_table");
        mvc.perform(post("/api/reports/v2/1/run").principal(USER))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.details.reference", notNullValue()))
                .andExpect(content().string(not(containsString("secret_table"))));
    }

    @Test
    void options() throws Exception {
        var filter = reports.reports.get(0).findFilter("min");
        filter.setQueryValues("select distinct region from sales order by region");

        mvc.perform(get("/api/reports/v2/1/filters/min/options?q=so&limit=5").principal(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].label", is("south")));
        mvc.perform(get("/api/reports/v2/1/filters/zzz/options").principal(USER)).andExpect(status().isNotFound());
    }

    @Test
    void malformedBodiesAndMediaTypesAreClientErrors() throws Exception {
        mvc.perform(post("/api/reports/v2/1/run").principal(USER).contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error", is("INVALID_REQUEST")));
        mvc.perform(post("/api/reports/v2/1/run").principal(USER).contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(get("/api/reports/v2/1/run").principal(USER)).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void exportDownloadsAFile() throws Exception {
        var result = mvc.perform(post("/api/reports/v2/1/export?format=csv").principal(USER))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", startsWith("text/csv")))
                .andExpect(header().string("Content-Disposition", allOf(startsWith("attachment"), containsString(".csv"))))
                .andExpect(header().doesNotExist("X-Report-Truncated"))
                .andReturn();
        assertEquals(5, result.getResponse().getContentAsString().split("\r\n").length);
    }

    @Test
    void exportWithTruncatedDataSendsAHeader() throws Exception {
        reports.settings.setMaxRows(2);
        mvc.perform(post("/api/reports/v2/1/export?format=csv").principal(USER))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Report-Truncated", "true"));
    }

    @Test
    void exportFormatsAndErrors() throws Exception {
        mvc.perform(post("/api/reports/v2/1/export").principal(USER))
                .andExpect(status().isOk()).andExpect(header().string("Content-Type", containsString("spreadsheetml")));
        mvc.perform(post("/api/reports/v2/1/export?format=pdf").principal(USER))
                .andExpect(status().isOk()).andExpect(header().string("Content-Type", is("application/pdf")));
        mvc.perform(post("/api/reports/v2/1/export?format=docx").principal(USER))
                .andExpect(status().isBadRequest());
    }
}
