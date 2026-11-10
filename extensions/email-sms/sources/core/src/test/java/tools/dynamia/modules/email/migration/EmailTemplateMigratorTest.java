package tools.dynamia.modules.email.migration;

import org.junit.jupiter.api.Test;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.email.domain.EmailTemplateEngine;
import tools.dynamia.templates.DynamiaTemplateEngine;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailTemplateMigratorTest {

    private final EmailTemplateMigrator migrator = new EmailTemplateMigrator();

    private static EmailTemplate velocityTemplate() {
        var t = new EmailTemplate();
        t.setName("invoice");
        t.setSubject("Invoice $invoice.number");
        t.setContent("<html><body><p>Hi $user.name</p>#if($vip)<b>VIP</b>#end</body></html>");
        t.setTo("$user.email");
        t.setCc("billing@x.com");
        t.setReplyTo("$replyTo");
        t.setSendSMS(true);
        t.setSmsNumber("$user.phone");
        t.setSmsText("Hi $user.name, invoice $invoice.number");
        return t;
    }

    @Test
    void analyzeShowsTheConvertedFieldsAndChangesNothing() {
        var t = velocityTemplate();
        var report = migrator.analyze(t);

        assertTrue(report.portable());
        assertFalse(report.applied());
        assertFalse(report.alreadyMigrated());
        assertEquals("Invoice ${invoice.number}", report.converted().get("subject"));
        assertEquals("${user.email}", report.converted().get("to"));
        assertEquals("Invoice $invoice.number", t.getSubject());
        assertEquals(EmailTemplateEngine.VELOCITY, t.getEngine());
        assertTrue(report.warnings().contains(EmailTemplateMigrator.DIRECT_EVALUATION_WARNING));
    }

    @Test
    void migrateConvertsEveryFieldBacksUpAndBuildsAnHtmlBlockDesign() throws Exception {
        var t = velocityTemplate();
        var report = migrator.migrate(t);

        assertTrue(report.applied());
        assertEquals(EmailTemplateEngine.DYNAMIA, t.getEngine());
        assertFalse(t.isEscapeValues(), "Velocity never escaped, so a migrated template does not either");
        assertEquals("Invoice ${invoice.number}", t.getSubject());
        assertEquals("${user.email}", t.getTo());
        assertEquals("billing@x.com", t.getCc());
        assertEquals("${replyTo}", t.getReplyTo());
        assertEquals("${user.phone}", t.getSmsNumber());
        assertEquals("Hi ${user.name}, invoice ${invoice.number}", t.getSmsText());
        assertTrue(t.hasBackup());
        assertEquals(EmailTemplateEngine.VELOCITY, t.getBackupEngine());

        var design = new JsonMapper().readTree(t.getDesign());
        assertEquals(1, design.path("version").asInt());
        var block = design.path("blocks").get(0);
        assertEquals("html", block.path("type").asString());
        assertEquals(t.getContent(), block.path("props").path("html").asString());
        assertTrue(block.path("props").path("fullDocument").asBoolean());

        var data = Map.<String, Object>of("user", Map.of("name", "Ada"), "vip", true, "invoice", Map.of("number", 7));
        assertEquals("<html><body><p>Hi Ada</p><b>VIP</b></body></html>", new DynamiaTemplateEngine().evaluate(t.getContent(), data));
    }

    @Test
    void aFragmentIsNotMarkedAsAFullDocument() throws Exception {
        var t = new EmailTemplate();
        t.setSubject("s");
        t.setContent("<p>$name</p>");
        migrator.migrate(t);
        assertFalse(new JsonMapper().readTree(t.getDesign()).path("blocks").get(0).path("props").path("fullDocument").asBoolean());
    }

    @Test
    void aTemplateWithAnythingNotPortableIsLeftUntouched() {
        var t = velocityTemplate();
        t.setSmsText("#set($x = 1)Hi $x");
        t.setContent(t.getContent() + "$list.size()");
        var before = t.getContent();

        var report = migrator.migrate(t);

        assertFalse(report.portable());
        assertFalse(report.applied());
        assertEquals(2, report.issues().size());
        assertEquals("smsText", report.issues().get(0).field().equals("content") ? report.issues().get(1).field() : report.issues().get(0).field());
        assertEquals(EmailTemplateEngine.VELOCITY, t.getEngine());
        assertEquals(before, t.getContent());
        assertEquals("Invoice $invoice.number", t.getSubject());
        assertNull(t.getDesign());
        assertFalse(t.hasBackup());
    }

    @Test
    void anAlreadyMigratedTemplateIsReportedAndNotTouched() {
        var t = velocityTemplate();
        migrator.migrate(t);
        var content = t.getContent();
        var report = migrator.migrate(t);
        assertTrue(report.alreadyMigrated());
        assertFalse(report.applied());
        assertEquals(content, t.getContent());
    }

    @Test
    void literalDoubleBracesAreNotPortableBecauseTheNewEngineWouldReadThem() {
        var t = new EmailTemplate();
        t.setContent("<p>$name</p>\n<script>var a = {{ x }};</script>");
        var report = migrator.migrate(t);
        assertFalse(report.portable());
        assertFalse(report.applied());
        assertEquals("LITERAL_BRACES", report.issues().get(0).kind());
        assertEquals(2, report.issues().get(0).line());
        assertEquals(EmailTemplateEngine.VELOCITY, t.getEngine());
    }

    @Test
    void aTemplateWithoutContentIsNotPortable() {
        var t = new EmailTemplate();
        t.setSubject("Hi $name");
        assertFalse(migrator.migrate(t).applied());
        assertEquals(EmailTemplateEngine.VELOCITY, t.getEngine());
    }

    @Test
    void revertRestoresEveryFieldTheEngineAndTheDesign() {
        var t = velocityTemplate();
        var original = velocityTemplate();
        migrator.migrate(t);

        var result = migrator.revert(t, false);

        assertTrue(result.reverted());
        assertFalse(result.designEdited());
        assertEquals(EmailTemplateEngine.VELOCITY, t.getEngine());
        assertEquals(original.getContent(), t.getContent());
        assertEquals(original.getSubject(), t.getSubject());
        assertEquals(original.getTo(), t.getTo());
        assertEquals(original.getReplyTo(), t.getReplyTo());
        assertEquals(original.getSmsNumber(), t.getSmsNumber());
        assertEquals(original.getSmsText(), t.getSmsText());
        assertNull(t.getDesign());
        assertFalse(t.hasBackup());
        assertNull(t.getBackupContent());
    }

    @Test
    void revertIsRefusedWhenTheDesignWasEditedUnlessConfirmed() {
        var t = velocityTemplate();
        migrator.migrate(t);
        t.setDesign(t.getDesign().replace("html-1", "html-2")); // the editor changed the design
        t.setContent("<p>edited</p>");

        var refused = migrator.revert(t, false);
        assertFalse(refused.reverted());
        assertTrue(refused.designEdited());
        assertEquals(EmailTemplateEngine.DYNAMIA, t.getEngine(), "nothing changed");
        assertEquals("<p>edited</p>", t.getContent());
        assertTrue(t.hasBackup());

        var confirmed = migrator.revert(t, true);
        assertTrue(confirmed.reverted());
        assertTrue(confirmed.designEdited());
        assertEquals(velocityTemplate().getContent(), t.getContent());
        assertEquals(EmailTemplateEngine.VELOCITY, t.getEngine());
    }

    @Test
    void revertWithoutABackupDoesNothing() {
        var t = velocityTemplate();
        var result = migrator.revert(t, true);
        assertFalse(result.reverted());
        assertEquals(EmailTemplateEngine.VELOCITY, t.getEngine());
    }

    @Test
    void aTemplateCanBeMigratedAgainAfterARevert() {
        var t = velocityTemplate();
        migrator.migrate(t);
        migrator.revert(t, false);
        assertTrue(migrator.migrate(t).applied());
        assertEquals(EmailTemplateEngine.DYNAMIA, t.getEngine());
    }
}
