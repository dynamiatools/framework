package tools.dynamia.modules.email.ui;

import org.junit.jupiter.api.Test;
import tools.dynamia.modules.email.ui.actions.DesignEmailTemplateAction;
import tools.dynamia.modules.email.ui.actions.MigrateEmailTemplateAction;
import tools.dynamia.modules.email.ui.actions.PreviewEmailTemplateAction;
import tools.dynamia.modules.email.ui.actions.RevertEmailTemplateAction;
import tools.dynamia.modules.email.domain.EmailTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailEditorLocationTest {

    @Test
    void theUrlOpensTheEditorOnATemplate() {
        assertEquals("/dynamia-email/editor?id=7&locale=es", EmailEditorLocation.url("", 7L, "es"));
        assertEquals("/erp/dynamia-email/editor?id=7", EmailEditorLocation.url("/erp", 7L, null));
        assertEquals("/dynamia-email/editor?id=7", EmailEditorLocation.url("/", 7L, " "));
        assertEquals("/dynamia-email/editor?id=7&locale=zh-CN", EmailEditorLocation.url(null, 7L, "zh-CN"));
        assertEquals("/dynamia-email/editor?id=7&locale=a%26b", EmailEditorLocation.url(null, 7L, "a&b"));
    }

    @Test
    void theControllerPointsTheRelativeAssetsToTheEditorFolder() {
        var page = "<html><head><title>x</title></head></html>";
        assertEquals("<html><head><base href=\"/dynamia-email/editor/\" /><title>x</title></head></html>",
                EmailEditorController.withBase(page, ""));
        assertEquals("<html><head><base href=\"/erp/dynamia-email/editor/\" /><title>x</title></head></html>",
                EmailEditorController.withBase(page, "/erp"));
        assertEquals("no head", EmailEditorController.withBase("no head", "/erp"));
    }

    @Test
    void theEditorPageIsDetectedOnTheClasspath() {
        // present only when the module was built without -Dskip.editor, so both answers are fine: it must not throw
        assertEquals(EmailEditorLocation.class.getResource("/META-INF/resources/dynamia-email/editor/index.html") != null,
                EmailEditorLocation.isInstalled());
    }

    @Test
    void theActionsApplyToEmailTemplates() {
        for (var action : new tools.dynamia.crud.AbstractCrudAction[]{new DesignEmailTemplateAction(), new MigrateEmailTemplateAction(),
                new RevertEmailTemplateAction(), new PreviewEmailTemplateAction()}) {
            assertEquals(EmailTemplate.class, action.getApplicableClass(), action.getClass().getSimpleName());
        }
    }

    @Test
    void theSandboxedFrameHasNoPermissionsAndEscapesTheEmail() {
        var markup = SandboxedFrame.markup("<p class=\"a\">Tom & <script>alert(1)</script></p>");
        assertTrue(markup.startsWith("<iframe sandbox=\"\""), markup);
        assertTrue(markup.contains("srcdoc=\"&lt;p class=&quot;a&quot;&gt;Tom &amp; &lt;script&gt;alert(1)&lt;/script&gt;&lt;/p&gt;\""), markup);
        assertFalse(markup.contains("allow-"), markup);
        assertEquals(1, markup.split("<iframe", -1).length - 1, "a value cannot add another tag");
        assertTrue(SandboxedFrame.markup(null).contains("srcdoc=\"\""));
    }

    @Test
    void theEditorOpensInAWindowUnlessTabIsRequested() {
        assertFalse(EmailEditorWindow.opensInTab(null));
        assertFalse(EmailEditorWindow.opensInTab("window"));
        assertTrue(EmailEditorWindow.opensInTab(" TAB "));
        assertEquals("/erp/dynamia-email/editor?id=3&locale=es", EmailEditorWindow.frame("/erp/dynamia-email/editor?id=3&locale=es").getSrc());
    }
}
