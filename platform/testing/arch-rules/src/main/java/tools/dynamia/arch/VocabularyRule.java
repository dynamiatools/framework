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

import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <b>R5, vocabulary.</b> The neutral core names no front end: no type, constant, method or annotation in the paths of
 * {@link ArchRulesConfig#vocabularyPaths()} has the front end's name ({@code ZK}, {@code Zk}) in an identifier. The core
 * says {@code FRONTEND}, not {@code ZK_ONLY}. No baseline: it is always an error.
 */
public abstract class VocabularyRule {

    /** @return the configuration of the repository under test */
    protected abstract ArchRulesConfig config();

    @Test
    void theNeutralCoreNamesNoFrontend() {
        var config = config();
        var repo = new RepoSources(config);
        var offenders = new TreeSet<String>();
        for (var source : repo.sources()) {
            if (config.vocabularyPaths().stream().noneMatch(p -> p.matcher(source.path()).matches())) {
                continue;
            }
            for (String identifier : source.identifiers()) {
                if (config.vocabularyWords().stream().anyMatch(identifier::contains)) {
                    offenders.add(source.path() + ": " + identifier);
                }
            }
        }

        assertTrue(offenders.isEmpty(), "The neutral core must not name a front end:\n" + String.join("\n", offenders));
    }
}
