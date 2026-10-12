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
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The rules find what they are meant to find: front end use by import and by fully qualified name, inheritance by fully
 * qualified name, and the runtime of an action without inheriting it.
 */
class RepoSourcesTest {

    @TempDir
    Path root;

    private void source(String path, String code) throws IOException {
        Path file = root.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, code);
    }

    private RepoSources scan() {
        var config = new ArchRulesConfig(root, List.of(Pattern.compile("^zk/.*")), List.of(), List.of("org.zkoss.", "tools.dynamia.zk."),
                List.of(Pattern.compile("tools\\.dynamia\\.zk")), List.of("org.zkoss"), List.of(), List.of("ZK", "Zk"), root);
        return new RepoSources(config);
    }

    private RepoSources.Source find(RepoSources repo, String name) {
        return repo.sources().stream().filter(s -> s.name().equals(name)).findFirst().orElseThrow();
    }

    @Test
    void anImportIsAUseOfTheFrontendButAMentionInACommentOrAStringIsNot() throws IOException {
        source("a/Imports.java", "package a;\nimport org.zkoss.zul.Window;\nclass Imports { }\n");
        source("a/Mentions.java", "package a;\n/** See {@link org.zkoss.zul.Window} and tools.dynamia.zk.ui.Viewer */\n"
                + "class Mentions { String s = \"org.zkoss.zul.Window\"; // org.zkoss.zk.ui.Executions\n }\n");
        var repo = scan();

        assertTrue(find(repo, "Imports").importsFrontend());
        assertFalse(find(repo, "Mentions").importsFrontend());
        assertFalse(find(repo, "Mentions").usesFrontendQualified());
    }

    @Test
    void aFullyQualifiedNameInTheBodyIsAUseWithoutAnImport() throws IOException {
        source("a/Qualified.java", "package a;\nclass Qualified { Object w = new org.zkoss.zul.Window(); }\n");

        assertTrue(find(scan(), "Qualified").usesFrontendQualified());
    }

    @Test
    void inheritanceIsResolvedByFullyQualifiedNameNotBySimpleName() throws IOException {
        source("zkside/BaseAction.java", "package zkside;\nimport org.zkoss.zul.Window;\npublic class BaseAction { }\n");
        source("clean/BaseAction.java", "package clean;\npublic class BaseAction { }\n");
        source("clean/UsesClean.java", "package clean;\n@InstallAction\npublic class UsesClean extends BaseAction { }\n");
        source("other/UsesZk.java", "package other;\nimport zkside.BaseAction;\n@InstallAction\npublic class UsesZk extends BaseAction { }\n");
        source("other/UsesWildcard.java", "package other;\nimport zkside.*;\n@InstallAction\npublic class UsesWildcard extends BaseAction { }\n");
        var repo = scan();

        assertFalse(repo.isFrontendBound(find(repo, "UsesClean")), "same simple name, other package");
        assertTrue(repo.isFrontendBound(find(repo, "UsesZk")));
        assertTrue(repo.isFrontendBound(find(repo, "UsesWildcard")));
    }

    @Test
    void theRuntimeIsDeclaredOnTheConcreteClassAndNeverInherited() throws IOException {
        source("a/Declared.java", "package a;\n@InstallAction\n@RunsOn(ActionRuntime.HEADLESS)\npublic class Declared extends AbstractCrudAction { }\n");
        source("a/Sub.java", "package a;\n@InstallAction\npublic class Sub extends Declared { }\n");
        source("a/Marked.java", "package a;\n@InstallAction\npublic class Marked extends AbstractCrudAction implements HeadlessCapable { }\n");
        source("a/SubMarked.java", "package a;\n@InstallAction\npublic class SubMarked extends Marked { }\n");
        source("a/Flow.java", "package a;\n@InstallAction\npublic class Flow extends AbstractRemoteAction implements FlowRemoteAction { }\n");
        source("a/Remote.java", "package a;\n@InstallAction\npublic class Remote extends AbstractRemoteAction { }\n");
        source("a/Search.java", "package a;\n@InstallAction\n@RunsOn(ActionRuntime.FRONTEND)\npublic class Search extends AbstractCrudAction { }\n");
        var repo = scan();

        assertEquals("HEADLESS", repo.runtime(find(repo, "Declared")));
        assertEquals("UNDECLARED", repo.runtime(find(repo, "Sub")));
        assertEquals("HEADLESS", repo.runtime(find(repo, "Marked")));
        assertEquals("UNDECLARED", repo.runtime(find(repo, "SubMarked")));
        assertEquals("FLOW", repo.runtime(find(repo, "Flow")));
        assertEquals("REMOTE", repo.runtime(find(repo, "Remote")));
        assertEquals("FRONTEND", repo.runtime(find(repo, "Search")));
    }

    @Test
    void aPublishedActionBoundToTheFrontendThroughAnAncestorIsFound() throws IOException {
        source("a/ZkBase.java", "package a;\nimport org.zkoss.zul.Window;\npublic abstract class ZkBase extends AbstractCrudAction { }\n");
        source("a/Leaky.java", "package a;\n@InstallAction\n@RunsOn(ActionRuntime.HEADLESS)\npublic class Leaky extends ZkBase { }\n");
        var repo = scan();

        var leaky = find(repo, "Leaky");
        assertEquals("HEADLESS", repo.runtime(leaky));
        assertTrue(repo.isFrontendBound(leaky));
    }
}
