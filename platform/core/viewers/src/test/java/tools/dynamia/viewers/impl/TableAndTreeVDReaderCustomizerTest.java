package tools.dynamia.viewers.impl;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TableAndTreeVDReaderCustomizerTest {

    private static final tools.dynamia.io.Resource RESOURCE = (tools.dynamia.io.Resource) java.lang.reflect.Proxy.newProxyInstance(
            tools.dynamia.io.Resource.class.getClassLoader(), new Class[]{tools.dynamia.io.Resource.class},
            (proxy, method, args) -> method.getName().equals("getFilename") ? "test.yml" : null);

    private static String yaml(String view) {
        return """
                view: %s
                frozenColumns: 2
                actions:
                  FindAction:
                    enabled: false
                """.formatted(view);
    }

    private static tools.dynamia.viewers.ViewDescriptor read(String yaml, tools.dynamia.viewers.ViewDescriptorReaderCustomizer<?> customizer) {
        return new YamlViewDescriptorReader().read(RESOURCE, new StringReader(yaml),
                List.of(customizer));
    }

    @Test
    void tableCustomizerAppliesFrozenColumnsAndActions() {
        var descriptor = read(yaml("table"), new TableVDReaderCustomizer());

        assertEquals("2", descriptor.getParams().get("frozenColumns"));
        assertEquals(Map.of("FindAction", Map.of("enabled", false)), descriptor.getParams().get("actions"));
    }

    @Test
    void tableCustomizerIgnoresOtherViewTypes() {
        var descriptor = read(yaml("form"), new TableVDReaderCustomizer());

        assertFalse(descriptor.getParams().containsKey("frozenColumns"));
    }

    @Test
    void treeCustomizerAppliesFrozenColumns() {
        var descriptor = read(yaml("tree"), new TreeVDReaderCustomizer());

        assertEquals("2", descriptor.getParams().get("frozenColumns"));
    }
}
