package tools.dynamia.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultApplicationTemplateTest {

    @Test
    void isNotBoundToZk() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("tools.dynamia.zk.ui.Viewer"));
    }

    @Test
    void providesTheDefaultTemplateWithASingleDefaultSkin() {
        var template = new DefaultApplicationTemplate(null);

        assertEquals("Default", template.getName());
        assertFalse(template.getSkins().isEmpty());
        assertEquals("default", template.getSkins().get(0).getId());
    }
}
