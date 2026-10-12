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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * <b>R1, front end corner.</b> The front end (ZK) lives in its own corner: only the paths of
 * {@link ArchRulesConfig#cornerPaths()} may use it. It detects an {@code import}, a <b>fully qualified name in the body</b>
 * (no import needed) and a Maven dependency whose {@code groupId} or {@code artifactId} is the front end's.
 * <p>
 * Violations that exist today are in {@code frontend-corner.baseline} and only shrink. Extend this class in the test sources
 * of the repository and implement {@link #config()}.
 */
public abstract class FrontendCornerRule {

    private static final Pattern DEPENDENCY = Pattern.compile("<dependency>(.*?)</dependency>", Pattern.DOTALL);
    private static final Pattern GROUP = Pattern.compile("<groupId>\\s*([^<\\s]+)\\s*</groupId>");
    private static final Pattern ARTIFACT = Pattern.compile("<artifactId>\\s*([^<\\s]+)\\s*</artifactId>");

    /** @return the configuration of the repository under test */
    protected abstract ArchRulesConfig config();

    @Test
    void frontendStaysInItsCorner() throws IOException {
        var config = config();
        var repo = new RepoSources(config);
        var violations = new TreeSet<String>();

        repo.sources().stream()
                .filter(s -> s.importsFrontend() || s.usesFrontendQualified())
                .map(RepoSources.Source::path)
                .filter(path -> !config.inCorner(path))
                .forEach(violations::add);

        for (Path pom : RepoFiles.list(config.root(), p -> p.getFileName().toString().equals("pom.xml"))) {
            String rel = config.root().relativize(pom).toString().replace('\\', '/');
            if (config.ignored(rel) || config.inCorner(rel)) {
                continue;
            }
            if (dependsOnFrontend(Files.readString(pom), config)) {
                violations.add(rel);
            }
        }

        ArchBaseline.check(config.baseline("frontend-corner.baseline"), violations,
                "The front end is used outside its corner. Move the code to a front end module or use a UI facade");
    }

    private static boolean dependsOnFrontend(String pom, ArchRulesConfig config) {
        var dependencies = DEPENDENCY.matcher(pom);
        while (dependencies.find()) {
            String block = dependencies.group(1);
            var group = GROUP.matcher(block);
            var artifact = ARTIFACT.matcher(block);
            if (group.find() && config.frontendGroups().contains(group.group(1))) {
                return true;
            }
            if (artifact.find() && config.frontendArtifacts().stream().anyMatch(p -> p.matcher(artifact.group(1)).matches())) {
                return true;
            }
        }
        return false;
    }
}
