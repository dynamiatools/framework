package tools.dynamia.actions.arch;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Source level view of the repository, used by the architecture tests of the Dynamia UI design
 * ({@code docs/next/dynamia-ui.md}). It reads Java sources instead of classes so it needs no other module built.
 */
final class RepoSources {

    private static final Pattern ZK_IMPORT = Pattern.compile("^import\\s+(?:static\\s+)?(?:org\\.zkoss|tools\\.dynamia\\.zk)\\.", Pattern.MULTILINE);
    private static final Pattern CLASS_DECL = Pattern.compile("^\\s*(?:public\\s+|protected\\s+|abstract\\s+|final\\s+)*class\\s+(\\w+)(?:<[^{]*?>)?(?:\\s+extends\\s+([\\w.]+))?", Pattern.MULTILINE);

    /** One Java source file. */
    record Source(String path, String className, String superName, boolean installAction, boolean importsZk) {
    }

    private final Path root;
    private final List<Source> sources = new ArrayList<>();
    private final Map<String, List<Source>> byClassName = new HashMap<>();

    RepoSources() {
        this.root = findRoot();
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(p -> p.toString().endsWith(".java")).filter(this::isMainSource).forEach(this::read);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    Path root() {
        return root;
    }

    List<Source> sources() {
        return sources;
    }

    /** A class is ZK-bound when it, or any ancestor found in the repository, imports ZK. */
    boolean isZkBound(Source source) {
        return isZkBound(source, new HashSet<>());
    }

    private boolean isZkBound(Source source, Set<String> visited) {
        if (source.importsZk()) {
            return true;
        }
        if (source.superName() == null || !visited.add(source.className())) {
            return false;
        }
        for (Source parent : byClassName.getOrDefault(source.superName(), List.of())) {
            if (isZkBound(parent, visited)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMainSource(Path p) {
        String s = root.relativize(p).toString().replace('\\', '/');
        return !(s.contains("/src/test/") || s.contains("/target/") || s.contains("node_modules/") || s.startsWith(".")
                || s.endsWith("module-info.java") || s.contains("/.git/"));
    }

    private void read(Path p) {
        try {
            String text = Files.readString(p);
            var m = CLASS_DECL.matcher(text);
            if (!m.find()) {
                return;
            }
            String superName = m.group(2) == null ? null : m.group(2).substring(m.group(2).lastIndexOf('.') + 1);
            var src = new Source(root.relativize(p).toString().replace('\\', '/'), m.group(1), superName,
                    text.contains("@InstallAction"), ZK_IMPORT.matcher(text).find());
            sources.add(src);
            byClassName.computeIfAbsent(src.className(), k -> new ArrayList<>()).add(src);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path findRoot() {
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null) {
            if (Files.isDirectory(dir.resolve("platform")) && Files.isDirectory(dir.resolve("extensions"))
                    && Files.exists(dir.resolve("pom.xml"))) {
                return dir;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("Repository root not found from " + Paths.get("").toAbsolutePath());
    }
}
