package tools.dynamia.actions.arch;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ZK lives in its own corner: only the modules in {@link #ZK_CORNER} may use {@code org.zkoss} or
 * {@code tools.dynamia.zk}, in code or as a Maven dependency (see {@code docs/next/dynamia-ui.md}).
 * <p>
 * Violations that exist today are listed in {@code ui/zk-corner-violations.baseline} and only shrink: a new one fails the
 * test, and so does a listed one that was fixed but not removed. Regenerate with {@code -Dui.baseline.update=true}.
 */
class ZkCornerRuleTest {

    private static final String BASELINE = "src/test/resources/ui/zk-corner-violations.baseline";

    /** Path patterns (relative to the repository root) where ZK is allowed. */
    private static final List<Pattern> ZK_CORNER = List.of(
            Pattern.compile("^platform/ui/zk/.*"),
            Pattern.compile("^platform/starters/zk-starter/.*"),
            Pattern.compile("^themes/theme-dynamical/.*"),
            Pattern.compile("^extensions/[^/]+/sources/ui/.*"),
            Pattern.compile("^extensions/dashboard/sources/zk/.*"),
            Pattern.compile("^examples/demo-zk-[^/]+/.*"));

    private static final Pattern ZK_DEPENDENCY = Pattern.compile("<artifactId>tools\\.dynamia\\.zk</artifactId>");

    @Test
    void zkStaysInItsCorner() throws IOException {
        var repo = new RepoSources();
        var violations = new TreeSet<String>();

        repo.sources().stream().filter(RepoSources.Source::importsZk).map(RepoSources.Source::path)
                .filter(ZkCornerRuleTest::outsideCorner).forEach(violations::add);

        try (Stream<Path> walk = Files.walk(repo.root())) {
            for (Path pom : walk.filter(p -> p.getFileName().toString().equals("pom.xml")).toList()) {
                String rel = repo.root().relativize(pom).toString().replace('\\', '/');
                if (!rel.contains("node_modules/") && !rel.contains("/target/") && outsideCorner(rel)
                        && ZK_DEPENDENCY.matcher(Files.readString(pom)).find()) {
                    violations.add(rel);
                }
            }
        }

        Path file = Path.of(BASELINE);
        if (Boolean.getBoolean("ui.baseline.update")) {
            Files.createDirectories(file.getParent());
            Files.write(file, violations);
            return;
        }

        var expected = new TreeSet<>(Files.readAllLines(file).stream().filter(l -> !l.isBlank() && !l.startsWith("#")).toList());
        var added = new TreeSet<>(violations);
        added.removeAll(expected);
        var fixed = new TreeSet<>(expected);
        fixed.removeAll(violations);

        assertTrue(added.isEmpty(), "ZK used outside its corner. Move the code to a ZK module or use a UI facade:\n"
                + String.join("\n", added));
        assertTrue(fixed.isEmpty(), "Fixed, remove from " + BASELINE + ":\n" + String.join("\n", fixed));
    }

    private static boolean outsideCorner(String path) {
        return ZK_CORNER.stream().noneMatch(p -> p.matcher(path).matches());
    }
}
