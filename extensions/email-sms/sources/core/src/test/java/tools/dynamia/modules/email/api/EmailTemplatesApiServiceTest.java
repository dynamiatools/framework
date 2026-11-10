package tools.dynamia.modules.email.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.modules.email.EmailMessage;
import tools.dynamia.modules.email.EmailSendResult;
import tools.dynamia.modules.email.EmailTemplateRenderer;
import tools.dynamia.modules.email.TemplateVariable;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.email.domain.EmailTemplateEngine;
import tools.dynamia.modules.email.migration.EmailTemplateMigrator;
import tools.dynamia.modules.email.services.EmailService;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.templates.TemplateEngine;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailTemplatesApiServiceTest {

    static class NoVelocity implements TemplateEngine {
        @Override
        public String evaluate(String content, Map<String, Object> params) {
            throw new AssertionError("Velocity must not run: " + content);
        }

        @Override
        public String evaluate(String content, Object target) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void evaluate(Reader reader, Writer writer, Map<String, Object> params) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void evaluate(Reader reader, Writer writer, Object target) {
            throw new UnsupportedOperationException();
        }
    }

    private CrudService crud;
    private AccountServiceAPI accounts;
    private EmailService emailService;
    private EmailTemplatesApiService service;

    @BeforeEach
    void setUp() {
        crud = mock(CrudService.class);
        accounts = mock(AccountServiceAPI.class);
        emailService = mock(EmailService.class);
        when(accounts.getCurrentAccountId()).thenReturn(5L);
        when(crud.save(any(EmailTemplate.class))).thenAnswer(i -> i.getArgument(0));
        when(crud.create(any(EmailTemplate.class))).thenAnswer(i -> i.getArgument(0));
        service = new EmailTemplatesApiService(crud, accounts, new EmailTemplateMigrator(), emailService,
                new EmailTemplateRenderer(new NoVelocity()));
    }

    private EmailTemplate stored(EmailTemplateEngine engine) {
        var t = new EmailTemplate();
        t.setId(9L);
        t.setAccountId(5L);
        t.setName("welcome");
        t.setSubject("Hi ${name}");
        t.setContent("<p>${name}</p>");
        t.setEngine(engine);
        t.setEscapeValues(engine == EmailTemplateEngine.DYNAMIA);
        when(crud.findSingle(eq(EmailTemplate.class), any(QueryParameters.class))).thenReturn(t);
        return t;
    }

    private static int status(Runnable call) {
        return assertThrows(EmailApiException.class, call::run).getStatus();
    }

    @Test
    void aTemplateIsLoadedByIdAndTheCurrentAccountOnly() {
        stored(EmailTemplateEngine.DYNAMIA);
        service.get(9L);

        var params = ArgumentCaptor.forClass(QueryParameters.class);
        verify(crud).findSingle(eq(EmailTemplate.class), params.capture());
        assertEquals(9L, params.getValue().get("id"));
        assertEquals(5L, params.getValue().get("accountId"));
    }

    @Test
    void aTemplateOfAnotherOrTheSystemAccountIsNotFound() {
        when(crud.findSingle(eq(EmailTemplate.class), any(QueryParameters.class))).thenReturn(null);
        assertEquals(404, status(() -> service.get(9L)));
        assertEquals(404, status(() -> service.saveDesign(9L, "{\"version\":1,\"blocks\":[]}", "<p/>", null)));
        assertEquals(404, status(() -> service.migrate(9L, false)));
        assertEquals(404, status(() -> service.revert(9L, true)));
        assertEquals(404, status(() -> service.preview(9L, null, null, null, null)));
        assertEquals(404, status(() -> service.testSend(9L, "a@x.com", null, null)));
        verify(crud, never()).save(any(EmailTemplate.class));
    }

    @Test
    void withoutACurrentAccountNothingIsFound() {
        when(accounts.getCurrentAccountId()).thenReturn(null);
        assertEquals(404, status(() -> service.get(9L)));
        assertEquals(404, status(() -> service.create("x", null)));
        verify(crud, never()).findSingle(eq(EmailTemplate.class), any(QueryParameters.class));
    }

    @Test
    void createMakesAnEscapingDynamiaTemplateInTheCurrentAccount() {
        var data = service.create(" Welcome ", null);
        assertEquals("DYNAMIA", data.engine());
        assertTrue(data.escapeValues());
        assertEquals("Welcome", data.name());
        assertEquals("Welcome", data.subject());
        assertEquals(EmailTemplatesApiService.EMPTY_DESIGN, data.design());
        var saved = ArgumentCaptor.forClass(EmailTemplate.class);
        verify(crud).create(saved.capture());
        assertEquals(5L, saved.getValue().getAccountId());
        assertEquals(400, status(() -> service.create("  ", null)));
        assertEquals(400, status(() -> service.create("x".repeat(201), null)));
    }

    @Test
    void saveDesignStoresTheDesignAndTheCompiledHtml() {
        var t = stored(EmailTemplateEngine.DYNAMIA);
        var data = service.saveDesign(9L, "{\"version\":1,\"blocks\":[]}", "<p>hi</p>", "New subject");
        assertEquals("<p>hi</p>", t.getContent());
        assertEquals("{\"version\":1,\"blocks\":[]}", t.getDesign());
        assertEquals("New subject", data.subject());
        assertTrue(t.isDynamia());
        service.saveDesign(9L, "{\"version\":1,\"blocks\":[]}", "<p>again</p>", " ");
        assertEquals("New subject", t.getSubject(), "a blank subject keeps the old one");
    }

    @Test
    void aVelocityTemplateCannotBeSavedThroughTheEditor() {
        var t = stored(EmailTemplateEngine.VELOCITY);
        assertEquals(409, status(() -> service.saveDesign(9L, "{\"version\":1,\"blocks\":[]}", "<p/>", null)));
        assertEquals("<p>${name}</p>", t.getContent());
        verify(crud, never()).save(any(EmailTemplate.class));
    }

    @Test
    void invalidOrOversizedDesignsAreRejected() {
        stored(EmailTemplateEngine.DYNAMIA);
        assertEquals(400, status(() -> service.saveDesign(9L, "not json", "<p/>", null)));
        assertEquals(400, status(() -> service.saveDesign(9L, "[]", "<p/>", null)));
        assertEquals(400, status(() -> service.saveDesign(9L, "{\"blocks\":[]}", "<p/>", null)));
        assertEquals(400, status(() -> service.saveDesign(9L, "{\"version\":1,\"blocks\":{}}", "<p/>", null)));
        assertEquals(400, status(() -> service.saveDesign(9L, "{\"version\":1,\"blocks\":[]}", " ", null)));
        assertEquals(400, status(() -> service.saveDesign(9L, null, "<p/>", null)));
        assertEquals(413, status(() -> service.saveDesign(9L, "{\"version\":1,\"blocks\":[]}",
                "x".repeat(EmailTemplatesApiService.MAX_CHARS), null)));
        verify(crud, never()).save(any(EmailTemplate.class));
    }

    @Test
    void previewRendersUnsavedContentWithTheDynamiaEngineAndEscaping() {
        stored(EmailTemplateEngine.DYNAMIA);
        var preview = service.preview(9L, "<b>${name}</b>", "Re: ${name}", null, Map.of("name", "A&B"));
        assertEquals("<b>A&amp;B</b>", preview.html());
        assertEquals("Re: A&B", preview.subject());
    }

    @Test
    void previewOfUnsavedContentIsRefusedForVelocityTemplates() {
        stored(EmailTemplateEngine.VELOCITY);
        assertEquals(409, status(() -> service.preview(9L, "#set($x=1)", null, null, Map.of())));
        assertEquals(409, status(() -> service.preview(9L, null, "$x", null, Map.of())));
    }

    @Test
    void previewOfTheStoredTemplateUsesTheServiceRenderWithItsLayout() {
        var t = stored(EmailTemplateEngine.DYNAMIA);
        when(emailService.render(eq(t), any())).thenReturn("<html>rendered</html>");
        var preview = service.preview(9L, null, null, null, Map.of("name", "Ada"));
        assertEquals("<html>rendered</html>", preview.html());
        assertEquals("Hi Ada", preview.subject());
    }

    @Test
    void testSendGoesOnlyToTheGivenAddressAndDropsTheTemplateRecipients() {
        var t = stored(EmailTemplateEngine.DYNAMIA);
        t.setTo("customer@x.com");
        t.setCc("boss@x.com");
        t.setBcc("audit@x.com");
        t.setSendSMS(true);
        var sent = ArgumentCaptor.forClass(EmailMessage.class);
        when(emailService.sendAndWait(sent.capture())).thenAnswer(i -> new EmailSendResult(i.getArgument(0), true, null));

        var result = service.testSend(9L, "me@x.com", null, Map.of("name", "Ada"));

        assertTrue(result.sent());
        var message = sent.getValue();
        assertEquals(List.of("me@x.com"), List.copyOf(message.getTos()));
        assertTrue(message.getCcs().isEmpty());
        assertTrue(message.getBccs().isEmpty());
        assertFalse(message.getTemplate().isSendSMS());
        assertEquals("customer@x.com", t.getTo(), "the stored template is not changed");
    }

    @Test
    void testSendNeedsAnAddress() {
        stored(EmailTemplateEngine.DYNAMIA);
        assertEquals(400, status(() -> service.testSend(9L, null, null, null)));
        assertEquals(400, status(() -> service.testSend(9L, " ", null, null)));
        verify(emailService, never()).sendAndWait(any());
    }

    @Test
    void aFailedTestSendReportsTheCause() {
        stored(EmailTemplateEngine.DYNAMIA);
        when(emailService.sendAndWait(any())).thenAnswer(i -> new EmailSendResult(i.getArgument(0), false, "no account"));
        var result = service.testSend(9L, "me@x.com", null, null);
        assertFalse(result.sent());
        assertEquals("no account", result.message());
    }

    @Test
    void migrateDryRunDoesNotSaveAndApplyDoes() {
        var t = stored(EmailTemplateEngine.VELOCITY);
        t.setContent("<p>$name</p>");
        t.setSubject("Hi $name");

        var dry = service.migrate(9L, true);
        assertTrue(dry.portable());
        assertFalse(dry.applied());
        assertEquals(EmailTemplateEngine.VELOCITY, t.getEngine());
        verify(crud, never()).save(any(EmailTemplate.class));

        var applied = service.migrate(9L, false);
        assertTrue(applied.applied());
        assertEquals(EmailTemplateEngine.DYNAMIA, t.getEngine());
        verify(crud).save(t);
    }

    @Test
    void aNonPortableMigrationIsNotSaved() {
        var t = stored(EmailTemplateEngine.VELOCITY);
        t.setContent("#set($x = 1)$x");
        assertFalse(service.migrate(9L, false).applied());
        verify(crud, never()).save(any(EmailTemplate.class));
    }

    @Test
    void revertSavesOnlyWhenItRestored() {
        var t = stored(EmailTemplateEngine.VELOCITY);
        t.setContent("<p>$name</p>");
        assertFalse(service.revert(9L, true).reverted());
        verify(crud, never()).save(any(EmailTemplate.class));

        service.migrate(9L, false);
        assertTrue(service.revert(9L, false).reverted());
        assertEquals("<p>$name</p>", t.getContent());
    }

    @Test
    void sampleDataIsNestedFromTheVariablePaths() {
        var data = EmailTemplatesApiService.sampleData(List.of(
                new TemplateVariable("user.name", null, "Ada"),
                new TemplateVariable("user.email"),
                new TemplateVariable("total", "Total", 10),
                new TemplateVariable("user.name.first")));
        assertEquals(Map.of("name", "Ada", "email", "[user.email]"), data.get("user"));
        assertEquals(10, data.get("total"));
    }
}
