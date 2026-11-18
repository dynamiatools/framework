package tools.dynamia.modules.email.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailTemplateTest {

    @Test
    void aTemplateWithNoEngineIsVelocityAndDoesNotEscape() {
        var template = new EmailTemplate();
        assertEquals(EmailTemplateEngine.VELOCITY, template.getEngine());
        assertFalse(template.isDynamia());
        assertFalse(template.isEscapeValues());
        assertFalse(template.hasBackup());
        assertNull(template.getDesign());
    }

    @Test
    void theDynamiaEngineAndEscapingAreOptIn() {
        var template = new EmailTemplate();
        template.setEngine(EmailTemplateEngine.DYNAMIA);
        template.setEscapeValues(true);
        assertTrue(template.isDynamia());
        assertTrue(template.isEscapeValues());
    }

    @Test
    void aBackupExistsOnlyWithABackupEngine() {
        var template = new EmailTemplate();
        template.setBackupContent("<p>$name</p>");
        assertFalse(template.hasBackup());
        template.setBackupEngine(EmailTemplateEngine.VELOCITY);
        assertTrue(template.hasBackup());
    }
}
