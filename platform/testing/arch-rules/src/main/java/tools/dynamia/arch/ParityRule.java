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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * R6, parity: every standard action (the ones of the platform and of the extensions, not the applications) must be available
 * to a remote client such as the Vue backoffice, the same way it is in ZK. A standard action is available when
 * <ul>
 *     <li>it is published ({@code HEADLESS}, {@code FLOW} or {@code REMOTE}) and does not use the front end, or</li>
 *     <li>a TypeScript {@code ClientAction} with the id of its class exists (any {@code id: 'XxxAction'} registered in the
 *     packages, found by {@link #clientActionIds()}).</li>
 * </ul>
 * The rest are gaps, listed with the reason in {@code parity-gaps.baseline}, which only shrinks. The ideal is an empty file:
 * standard actions that run everywhere and depend on no front end.
 */
public abstract class ParityRule {

    /** An id registered in TypeScript: {@code id: 'FindAction'}. */
    private static final Pattern TS_ID = Pattern.compile("\\bid:\\s*['\"]([A-Za-z0-9_]+)['\"]");
    private static final Set<String> PUBLISHABLE = Set.of("HEADLESS", "FLOW", "REMOTE");

    /** @return the configuration of the repository under test */
    protected abstract ArchRulesConfig config();

    /**
     * @param path a path relative to the root
     * @return whether the action there is a standard one; by default everything but the examples
     */
    protected boolean standard(String path) {
        return !path.startsWith("examples/");
    }

    /**
     * @return the ids of the client actions implemented in TypeScript. By default every id found in the TypeScript sources of
     * the packages folders (platform and extensions) that mention client actions.
     */
    protected Set<String> clientActionIds() {
        var root = config().root();
        var ids = new TreeSet<String>();
        for (Path file : RepoFiles.list(root, p -> {
            String rel = root.relativize(p).toString().replace('\\', '/');
            return rel.endsWith(".ts") && rel.contains("/src/") && rel.contains("/packages/") && !rel.endsWith(".d.ts");
        })) {
            try {
                String text = Files.readString(file);
                if (text.contains("ClientAction")) {
                    var matcher = TS_ID.matcher(text);
                    while (matcher.find()) {
                        ids.add(matcher.group(1));
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return ids;
    }

    @Test
    void standardActionsAreAvailableToRemoteClients() {
        var config = config();
        var repo = new RepoSources(config);
        var client = clientActionIds();
        var gaps = repo.actions().stream()
                .filter(a -> standard(a.path()))
                .filter(a -> !client.contains(a.name()))
                .map(a -> a.path() + " | " + gap(repo, a))
                .filter(line -> !line.endsWith("| ok"))
                .collect(Collectors.toCollection(TreeSet::new));

        System.out.printf("Client actions in TypeScript: %d, standard actions that are not available to remote clients: %d%n",
                client.size(), gaps.size());

        ArchBaseline.check(config.baseline("parity-gaps.baseline"), gaps,
                "New standard actions that a remote client cannot use. Declare @RunsOn(HEADLESS/FLOW) and remove the front end "
                        + "calls (UI facades), or register a ClientAction with the id of the class in TypeScript");
    }

    private static String gap(RepoSources repo, RepoSources.Source action) {
        String runtime = repo.runtime(action);
        if (repo.isFrontendBound(action)) {
            return "uses the front end (" + runtime + ")";
        }
        if (PUBLISHABLE.contains(runtime)) {
            return "ok";
        }
        return "FRONTEND".equals(runtime) ? "FRONTEND without a TypeScript client action" : "runtime " + runtime;
    }
}
