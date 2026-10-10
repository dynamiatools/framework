package tools.dynamia.actions.arch;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Makes the action inventory of {@code docs/next/dynamia-ui-inventory.md} executable.
 * <p>
 * Every {@code @InstallAction} class that is ZK-bound (it or an ancestor imports ZK) must be listed in
 * {@code ui/zk-bound-actions.baseline}. The test fails when a new ZK-bound action appears (use a UI facade instead) and
 * when a listed action no longer is ZK-bound (remove it from the baseline: progress is recorded, never lost).
 * <p>
 * Regenerate the baseline with {@code -Dui.baseline.update=true}.
 */
class ActionInventoryTest {

    private static final String BASELINE = "src/test/resources/ui/zk-bound-actions.baseline";

    @Test
    void zkBoundActionsMatchTheBaseline() throws IOException {
        var repo = new RepoSources();
        var actions = repo.sources().stream().filter(RepoSources.Source::installAction).toList();
        var bound = actions.stream().filter(repo::isZkBound).map(RepoSources.Source::path)
                .collect(Collectors.toCollection(TreeSet::new));

        System.out.printf("Actions: %d, ZK-bound: %d, ZK-free: %d%n", actions.size(), bound.size(), actions.size() - bound.size());

        Path file = Path.of(BASELINE);
        if (Boolean.getBoolean("ui.baseline.update")) {
            Files.createDirectories(file.getParent());
            Files.write(file, bound);
            return;
        }

        var expected = new TreeSet<>(Files.readAllLines(file).stream().filter(l -> !l.isBlank() && !l.startsWith("#")).toList());
        var added = new TreeSet<>(bound);
        added.removeAll(expected);
        var fixed = new TreeSet<>(expected);
        fixed.removeAll(bound);

        assertTrue(added.isEmpty(), "New ZK-bound actions. Use the UI facades (docs/next/dynamia-ui.md) or, if it really needs ZK, "
                + "discuss it first:\n" + String.join("\n", added));
        assertTrue(fixed.isEmpty(), "These actions are no longer ZK-bound, remove them from " + BASELINE
                + " (or regenerate it with -Dui.baseline.update=true):\n" + String.join("\n", fixed));
    }
}
