package tools.dynamia.modules.email.api;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

class EmailTemplatesApiControllerTest {

    private EmailTemplatesApiService service;
    private boolean allowed;
    private MockMvc mvc;

    private final EmailTemplatePolicy policy = new EmailTemplatePolicy() {
        @Override
        public boolean canEdit(HttpServletRequest request) {
            return allowed;
        }
    };

    @BeforeEach
    void setUp() {
        service = mock(EmailTemplatesApiService.class);
        allowed = true;
        mvc = MockMvcBuilders.standaloneSetup(new EmailTemplatesApiController(service, () -> List.of(policy))).build();
    }

    private static MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder request, String user) {
        return request.principal(() -> user);
    }

    private String body(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request).andReturn().getResponse().getContentAsString();
    }

    private int status(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request).andReturn().getResponse().getStatus();
    }

    @Test
    void anonymousCallsAreRejectedBeforeAnythingRuns() throws Exception {
        assertEquals(401, status(get("/api/email/templates/1")));
        assertEquals(401, status(get("/api/email/templates/variables")));
        assertEquals(401, status(post("/api/email/templates").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"x\"}")));
        verify(service, never()).get(any());
    }

    @Test
    void anAuthenticatedUserThePolicyRefusesGetsForbidden() throws Exception {
        allowed = false;
        var request = as(get("/api/email/templates/1"), "ada@x.com");
        assertEquals(403, status(request));
        assertTrue(body(request).contains("ACCESS_DENIED"));
        verify(service, never()).get(any());
        assertEquals(403, status(as(post("/api/email/templates/1/migrate?dryRun=false"), "ada@x.com")));
        assertEquals(403, status(as(post("/api/email/templates/1/revert?confirm=true"), "ada@x.com")));
        assertEquals(403, status(as(post("/api/email/templates/1/test-send").contentType(MediaType.APPLICATION_JSON).content("{}"), "ada@x.com")));
    }

    @Test
    void getReturnsTheTemplate() throws Exception {
        when(service.get(1L)).thenReturn(new EmailTemplatesApiService.TemplateData(1L, "welcome", "Hi", "DYNAMIA", true,
                "{\"version\":1,\"blocks\":[]}", "<p/>", false, null));
        var json = body(as(get("/api/email/templates/1"), "ada@x.com"));
        assertTrue(json.contains("\"name\":\"welcome\""), json);
        assertTrue(json.contains("\"engine\":\"DYNAMIA\""), json);
    }

    @Test
    void serviceErrorsKeepTheirStatusAndCode() throws Exception {
        when(service.get(2L)).thenThrow(new EmailApiException(404, "NOT_FOUND", "Template not found"));
        var request = as(get("/api/email/templates/2"), "ada@x.com");
        assertEquals(404, status(request));
        assertTrue(body(request).contains("NOT_FOUND"));
    }

    @Test
    void unexpectedErrorsDoNotLeakDetails() throws Exception {
        when(service.get(3L)).thenThrow(new IllegalStateException("secret sql detail"));
        var request = as(get("/api/email/templates/3"), "ada@x.com");
        assertEquals(500, status(request));
        assertTrue(!body(request).contains("secret"));
    }

    @Test
    void saveDesignPassesTheBodyToTheService() throws Exception {
        var json = "{\"design\":\"{\\\"version\\\":1,\\\"blocks\\\":[]}\",\"content\":\"<p/>\",\"subject\":\"S\"}";
        assertEquals(200, status(as(put("/api/email/templates/7/design").contentType(MediaType.APPLICATION_JSON).content(json), "ada@x.com")));
        verify(service).saveDesign(7L, "{\"version\":1,\"blocks\":[]}", "<p/>", "S");
    }

    @Test
    void anUnreadableBodyIsABadRequest() throws Exception {
        assertEquals(400, status(as(put("/api/email/templates/7/design").contentType(MediaType.APPLICATION_JSON).content("{oops"), "ada@x.com")));
        verify(service, never()).saveDesign(any(), any(), any(), any());
    }

    @Test
    void testSendUsesTheAddressOfTheUserNeverOneFromTheRequest() throws Exception {
        var json = "{\"source\":\"s\",\"data\":{\"a\":1},\"to\":\"victim@x.com\"}";
        status(as(post("/api/email/templates/4/test-send").contentType(MediaType.APPLICATION_JSON).content(json), "ada@x.com"));
        var address = ArgumentCaptor.forClass(String.class);
        verify(service).testSend(eq(4L), address.capture(), eq("s"), eq(Map.of("a", 1)));
        assertEquals("ada@x.com", address.getValue());
    }

    @Test
    void testSendWithAUserThatIsNotAnEmailPassesNoAddress() throws Exception {
        status(as(post("/api/email/templates/4/test-send").contentType(MediaType.APPLICATION_JSON).content("{}"), "ada"));
        verify(service).testSend(eq(4L), eq(null), eq(null), eq(null));
    }

    @Test
    void migrateIsADryRunUnlessToldOtherwise() throws Exception {
        status(as(post("/api/email/templates/5/migrate"), "ada@x.com"));
        verify(service).migrate(5L, true);
        status(as(post("/api/email/templates/5/migrate?dryRun=false"), "ada@x.com"));
        verify(service).migrate(5L, false);
    }

    @Test
    void revertNeedsConfirmationToLoseEdits() throws Exception {
        status(as(post("/api/email/templates/5/revert"), "ada@x.com"));
        verify(service).revert(5L, false);
        status(as(post("/api/email/templates/5/revert?confirm=true"), "ada@x.com"));
        verify(service).revert(5L, true);
    }

    @Test
    void thePolicyRolesDecideAccess() {
        var roles = new RolesEmailTemplatePolicy(" ADMIN , editor ");
        var yes = mock(HttpServletRequest.class);
        when(yes.isUserInRole("ROLE_editor")).thenReturn(true);
        var no = mock(HttpServletRequest.class);
        assertTrue(roles.canEdit(yes));
        assertTrue(!roles.canEdit(no));
        assertTrue(!new RolesEmailTemplatePolicy("").canEdit(yes), "no roles configured means nobody");
    }
}
