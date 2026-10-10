/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.arch;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Source level view of a repository. It reads Java sources, not classes, so it needs no module built. Comments and string
 * literals are removed first, so a mention in Javadoc is not a use.
 * <p>
 * Inheritance is resolved by <b>fully qualified name</b> (package, imports, same package), never by simple name, so two
 * classes called {@code BaseAction} in different packages are not confused.
 */
public final class RepoSources {

    private static final Pattern PACKAGE = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);
    private static final Pattern IMPORT = Pattern.compile("^\\s*import\\s+(static\\s+)?([\\w.]+?)(\\.\\*)?\\s*;", Pattern.MULTILINE);
    private static final Pattern TYPE_DECL = Pattern.compile(
            "(?:^|\\n)[ \\t]*(?:@\\w+(?:\\.\\w+)*(?:\\([^)]*\\))?\\s*)*(?:(?:public|protected|private|abstract|final|static|sealed|non-sealed)\\s+)*"
                    + "(class|interface|enum|record|@interface)\\s+(\\w+)([^{;]*)\\{", Pattern.DOTALL);
    private static final Pattern RUNS_ON = Pattern.compile("@RunsOn\\s*\\(\\s*(?:value\\s*=\\s*)?(?:\\w+\\.)*ActionRuntime\\.(\\w+)\\s*\\)");
    private static final Pattern RUNS_ON_STATIC = Pattern.compile("@RunsOn\\s*\\(\\s*(?:value\\s*=\\s*)?(\\w+)\\s*\\)");

    /** One Java source file. */
    public record Source(String path, String packageName, String name, String kind, String header, String annotations,
                         String superFqn, List<String> interfaces, boolean installAction, boolean importsFrontend,
                         boolean usesFrontendQualified, String declaredRuntime, List<String> identifiers) {

        /** @return package and name */
        public String fqn() {
            return packageName.isEmpty() ? name : packageName + "." + name;
        }
    }

    /** A source read but not yet resolved: names of parents and interfaces still depend on files not read yet. */
    private record Pending(String path, String packageName, String name, String kind, String header, String annotations,
                           String superName, List<String> interfaceNames, boolean installAction, boolean importsFrontend,
                           boolean qualified, String runtime, List<String> identifiers, Map<String, String> explicit,
                           List<String> wildcards) {
        String fqn() {
            return packageName.isEmpty() ? name : packageName + "." + name;
        }
    }

    private final ArchRulesConfig config;
    private final List<Source> sources = new ArrayList<>();
    private final Map<String, Source> byFqn = new HashMap<>();
    private final List<Pending> pending = new ArrayList<>();
    private final Set<String> known = new HashSet<>();

    /**
     * @param config where the repository is and what the front end is
     */
    public RepoSources(ArchRulesConfig config) {
        this.config = config;
        try (Stream<Path> walk = Files.walk(config.root())) {
            walk.filter(p -> p.toString().endsWith(".java")).filter(this::isMainSource).forEach(this::read);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        for (Pending p : pending) {
            String superFqn = p.superName() == null ? null : resolve(p.superName(), p.packageName(), p.explicit(), p.wildcards());
            var interfaces = p.interfaceNames().stream().map(n -> resolve(n, p.packageName(), p.explicit(), p.wildcards())).toList();
            var src = new Source(p.path(), p.packageName(), p.name(), p.kind(), p.header(), p.annotations(), superFqn, interfaces,
                    p.installAction(), p.importsFrontend(), p.qualified(), p.runtime(), p.identifiers());
            sources.add(src);
            byFqn.put(src.fqn(), src);
        }
    }

    /** @return the repository root */
    public Path root() {
        return config.root();
    }

    /** @return every main Java source */
    public List<Source> sources() {
        return sources;
    }

    /** @return the {@code @InstallAction} classes */
    public List<Source> actions() {
        return sources.stream().filter(Source::installAction).toList();
    }

    /** @return the parent of {@code source} when it is in the repository */
    public Optional<Source> parent(Source source) {
        return source.superFqn() == null ? Optional.empty() : Optional.ofNullable(byFqn.get(source.superFqn()));
    }

    /** A class is front-end bound when it, or any ancestor found in the repository, uses the front end. */
    public boolean isFrontendBound(Source source) {
        return isFrontendBound(source, new HashSet<>());
    }

    private boolean isFrontendBound(Source source, Set<String> visited) {
        if (source.importsFrontend() || source.usesFrontendQualified()) {
            return true;
        }
        if (!visited.add(source.fqn())) {
            return false;
        }
        return parent(source).map(p -> isFrontendBound(p, visited)).orElse(false);
    }

    /**
     * The runtime the class gets, with the same rules as {@code ActionRuntimes}: what the concrete class declares with
     * {@code @RunsOn}; otherwise {@code FLOW} or {@code REMOTE} when the type says so (also through ancestors, since the type
     * is the contract); otherwise {@code HEADLESS} when the class itself lists {@code HeadlessCapable}; else {@code UNDECLARED}.
     *
     * @param source an action class
     * @return {@code HEADLESS}, {@code FLOW}, {@code REMOTE}, {@code FRONTEND} or {@code UNDECLARED}
     */
    public String runtime(Source source) {
        if (source.declaredRuntime() != null) {
            return source.declaredRuntime();
        }
        String derived = derivedFromType(source, new HashSet<>());
        if (derived != null) {
            return derived;
        }
        if (source.interfaces().stream().anyMatch(i -> simple(i).equals("HeadlessCapable"))) {
            return "HEADLESS";
        }
        return "UNDECLARED";
    }

    private String derivedFromType(Source source, Set<String> visited) {
        if (!visited.add(source.fqn())) {
            return null;
        }
        for (String type : source.interfaces()) {
            String s = simple(type);
            if (s.equals("FlowRemoteAction")) {
                return "FLOW";
            }
            if (s.endsWith("RemoteAction")) {
                return "REMOTE";
            }
        }
        if (source.superFqn() != null) {
            String s = simple(source.superFqn());
            if (s.equals("FlowRemoteAction")) {
                return "FLOW";
            }
            if (s.endsWith("RemoteAction")) {
                return "REMOTE";
            }
        }
        return parent(source).map(p -> derivedFromType(p, visited)).orElse(null);
    }

    private static String simple(String name) {
        return name.substring(name.lastIndexOf('.') + 1);
    }

    private boolean isMainSource(Path p) {
        String s = config.root().relativize(p).toString().replace('\\', '/');
        return !(s.contains("/src/test/") || s.contains("/target/") || s.contains("node_modules/") || s.startsWith(".")
                || s.endsWith("module-info.java") || s.contains("/.git/") || config.ignored(s));
    }

    private void read(Path p) {
        try {
            String raw = Files.readString(p);
            String text = stripCommentsAndStrings(raw);
            var pkg = PACKAGE.matcher(text);
            String packageName = pkg.find() ? pkg.group(1) : "";

            var explicit = new HashMap<String, String>();
            var wildcards = new ArrayList<String>();
            boolean importsFrontend = false;
            var imports = IMPORT.matcher(text);
            while (imports.find()) {
                String name = imports.group(2);
                if (imports.group(3) != null) {
                    wildcards.add(name);
                    importsFrontend |= isFrontend(name + ".");
                } else {
                    explicit.put(simple(name), name);
                    importsFrontend |= isFrontend(name);
                }
            }

            var type = TYPE_DECL.matcher(text);
            if (!type.find()) {
                return;
            }
            String header = type.group(3);
            String superName = null;
            var interfaces = new ArrayList<String>();
            var ext = Pattern.compile("\\bextends\\s+([\\w.]+)").matcher(header);
            var impl = Pattern.compile("\\b(?:implements|extends)\\s+([^{]*?)(?=\\bextends\\b|\\bimplements\\b|\\bpermits\\b|$)", Pattern.DOTALL).matcher(header);
            if (ext.find() && type.group(1).equals("class")) {
                superName = ext.group(1);
            }
            while (impl.find()) {
                for (String part : impl.group(1).split(",")) {
                    String n = part.replaceAll("<.*", "").trim();
                    if (!n.isEmpty() && !(superName != null && n.equals(superName))) {
                        interfaces.add(n);
                    }
                }
            }

            String before = text.substring(0, type.start(2));
            String annotations = before.substring(Math.max(0, before.lastIndexOf('}') + 1));
            String runtime = null;
            var runs = RUNS_ON.matcher(annotations);
            if (runs.find()) {
                runtime = runs.group(1);
            } else {
                var staticRuns = RUNS_ON_STATIC.matcher(annotations);
                if (staticRuns.find()) {
                    runtime = staticRuns.group(1);
                }
            }
            boolean qualified = qualifiedUse(text);
            var pendingSource = new Pending(config.root().relativize(p).toString().replace('\\', '/'), packageName, type.group(2),
                    type.group(1), header, annotations, superName, interfaces, annotations.contains("@InstallAction"),
                    importsFrontend, qualified, runtime, identifiers(text), explicit, wildcards);
            pending.add(pendingSource);
            known.add(pendingSource.fqn());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A front end name written in full in the body (not in an import): {@code new org.zkoss.zul.Window()}. */
    private boolean qualifiedUse(String text) {
        String body = IMPORT.matcher(PACKAGE.matcher(text).replaceAll("")).replaceAll("");
        for (String prefix : config.frontendPackages()) {
            if (Pattern.compile("(?<![\\w.])" + Pattern.quote(prefix) + "[A-Za-z]").matcher(body).find()) {
                return true;
            }
        }
        return false;
    }

    private boolean isFrontend(String name) {
        return config.frontendPackages().stream().anyMatch(name::startsWith);
    }

    private String resolve(String name, String packageName, Map<String, String> explicit, List<String> wildcards) {
        if (name.contains(".")) {
            return name;
        }
        if (explicit.containsKey(name)) {
            return explicit.get(name);
        }
        String samePackage = packageName.isEmpty() ? name : packageName + "." + name;
        if (byFqnPending(samePackage)) {
            return samePackage;
        }
        for (String wildcard : wildcards) {
            String candidate = wildcard + "." + name;
            if (byFqnPending(candidate)) {
                return candidate;
            }
        }
        return samePackage;
    }

    /** Whether a class of the repository has this fully qualified name; resolution runs after every file was read. */
    private boolean byFqnPending(String fqn) {
        return known.contains(fqn);
    }

    private static List<String> identifiers(String text) {
        var found = new ArrayList<String>();
        var m = Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b").matcher(text);
        while (m.find()) {
            found.add(m.group());
        }
        return found;
    }

    /** Removes comments and the content of string and char literals, keeping the line structure. */
    static String stripCommentsAndStrings(String source) {
        var out = new StringBuilder(source.length());
        int i = 0;
        int n = source.length();
        while (i < n) {
            char c = source.charAt(i);
            if (c == '/' && i + 1 < n && source.charAt(i + 1) == '/') {
                while (i < n && source.charAt(i) != '\n') {
                    i++;
                }
            } else if (c == '/' && i + 1 < n && source.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < n && !(source.charAt(i) == '*' && source.charAt(i + 1) == '/')) {
                    if (source.charAt(i) == '\n') {
                        out.append('\n');
                    }
                    i++;
                }
                i += 2;
            } else if (c == '"' || c == '\'') {
                char quote = c;
                if (quote == '"' && source.startsWith("\"\"\"", i)) {
                    i += 3;
                    while (i < n && !source.startsWith("\"\"\"", i)) {
                        if (source.charAt(i) == '\n') {
                            out.append('\n');
                        }
                        i++;
                    }
                    i += 3;
                } else {
                    i++;
                    while (i < n && source.charAt(i) != quote) {
                        if (source.charAt(i) == '\\') {
                            i++;
                        }
                        i++;
                    }
                    i++;
                }
                out.append(quote).append(quote);
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }
}
