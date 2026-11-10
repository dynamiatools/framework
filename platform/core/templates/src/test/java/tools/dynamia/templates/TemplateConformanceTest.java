package tools.dynamia.templates;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Runs the cases of {@code platform/contract/fixtures/templates/conformance.json}, the same file the TypeScript engine
 * (@dynamia-tools/template-engine) runs. This engine is the reference: a case changes only together with both engines.
 */
class TemplateConformanceTest {

    private static final String FILE = "platform/contract/fixtures/templates/conformance.json";

    private final DynamiaTemplateEngine engine = new DynamiaTemplateEngine();

    @TestFactory
    Stream<DynamicTest> conformance() throws IOException {
        var root = new JsonMapper().readValue(findFile(), new TypeReference<Map<String, Object>>() {
        });
        @SuppressWarnings("unchecked")
        var cases = (List<Map<String, Object>>) root.get("cases");
        assertFalse(cases.isEmpty());
        return cases.stream().map(c -> dynamicTest(c.get("group") + ": " + c.get("name"), () -> {
            @SuppressWarnings("unchecked")
            var data = new HashMap<>((Map<String, Object>) c.get("data"));
            if (c.get("locale") != null) {
                data.put("locale", c.get("locale"));
            }
            assertEquals(c.get("expected"), engine.evaluate((String) c.get("template"), data), String.valueOf(c.get("template")));
        }));
    }

    private static Path findFile() {
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null) {
            var file = dir.resolve(FILE);
            if (Files.isRegularFile(file)) {
                return file;
            }
            dir = dir.getParent();
        }
        throw new UncheckedIOException(new IOException(FILE + " not found from " + Paths.get("").toAbsolutePath()));
    }
}
