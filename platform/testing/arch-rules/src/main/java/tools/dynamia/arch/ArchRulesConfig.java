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
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

/**
 * What differs between repositories that use the architecture rules: where the root is, which paths are the front end
 * corner, which packages and artifacts are the front end, and where the baselines live. {@code dynamia-tools} and
 * {@code dynamia-erp} each build their own.
 *
 * @param root            repository root; every path below is relative to it
 * @param cornerPaths     patterns of the paths where the front end (ZK) may be used
 * @param ignoredPaths    patterns of paths that are never scanned (generated code, examples that are not part of the product)
 * @param frontendPackages package prefixes that name the front end, such as {@code org.zkoss.} and {@code tools.dynamia.zk.}
 * @param frontendArtifacts regular expressions that match the {@code artifactId} of a Maven dependency on the front end
 * @param frontendGroups   {@code groupId}s of Maven dependencies on the front end
 * @param vocabularyPaths  patterns of the paths where no name may contain the front end's name (the neutral core)
 * @param vocabularyWords  the front end's names that must not appear in those names, such as {@code ZK}
 * @param baselineDir      directory with the baseline files, relative to the working directory of the test
 */
public record ArchRulesConfig(Path root, List<Pattern> cornerPaths, List<Pattern> ignoredPaths, List<String> frontendPackages,
                              List<Pattern> frontendArtifacts, List<String> frontendGroups, List<Pattern> vocabularyPaths,
                              List<String> vocabularyWords, Path baselineDir) {

    /**
     * @param baselineDir directory with the baseline files
     * @param cornerPaths patterns of the paths where ZK may be used
     * @return the configuration for a Dynamia repository: ZK is {@code org.zkoss} and {@code tools.dynamia.zk}, the root is the
     * first parent of the working directory with a {@code pom.xml} next to {@code platform} and {@code extensions}
     */
    public static ArchRulesConfig forZk(Path baselineDir, List<Pattern> cornerPaths) {
        return new ArchRulesConfig(findRoot(), cornerPaths, List.of(), List.of("org.zkoss.", "tools.dynamia.zk."),
                List.of(Pattern.compile("tools\\.dynamia\\.zk"), Pattern.compile(".*zk-starter")), List.of("org.zkoss", "org.zkoss.zk"),
                List.of(Pattern.compile("^platform/core/.*"), Pattern.compile("^platform/ui/ui-shared/.*")),
                List.of("ZK", "Zk"), baselineDir);
    }

    /**
     * @param path a path relative to the root
     * @return whether it is a place where the front end may be used
     */
    public boolean inCorner(String path) {
        return cornerPaths.stream().anyMatch(p -> p.matcher(path).matches());
    }

    /**
     * @param path a path relative to the root
     * @return whether the scan skips it
     */
    public boolean ignored(String path) {
        return ignoredPaths.stream().anyMatch(p -> p.matcher(path).matches());
    }

    /**
     * @param name the file name of a baseline
     * @return its path
     */
    public Path baseline(String name) {
        return baselineDir.resolve(name);
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
        throw new UncheckedIOException(new IOException("Repository root not found from " + Paths.get("").toAbsolutePath()));
    }
}
