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
        assertEquals("/dynamia-email/editor/index.html?id=7&locale=es", EmailEditorLocation.url("", 7L, "es"));
        assertEquals("/erp/dynamia-email/editor/index.html?id=7", EmailEditorLocation.url("/erp", 7L, null));
        assertEquals("/dynamia-email/editor/index.html?id=7", EmailEditorLocation.url("/", 7L, " "));
        assertEquals("/dynamia-email/editor/index.html?id=7&locale=zh-CN", EmailEditorLocation.url(null, 7L, "zh-CN"));
        assertEquals("/dynamia-email/editor/index.html?id=7&locale=a%26b", EmailEditorLocation.url(null, 7L, "a&b"));
    }

    @Test
    void theEditorPageIsDetectedOnTheClasspath() {
        // present only when the module was built with -Peditor, so both answers are fine: it must not throw
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
}
