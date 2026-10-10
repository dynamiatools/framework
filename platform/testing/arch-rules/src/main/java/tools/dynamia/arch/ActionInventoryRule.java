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

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The action inventory of {@code docs/next/dynamia-ui-inventory.md} made executable. Every {@code @InstallAction} class is
 * looked at with the same rules as {@code ActionRuntimes}: inheritance is resolved by fully qualified name and a runtime is
 * never inherited.
 * <ul>
 *     <li><b>R2, inventory:</b> the actions bound to the front end (the class or an ancestor uses it) are listed in
 *     {@code frontend-bound-actions.baseline}, which only shrinks.</li>
 *     <li><b>R3, publication:</b> no action with a publishable runtime ({@code HEADLESS}, {@code FLOW}, {@code REMOTE}) may be
 *     bound to the front end, ancestors included. No baseline: it is always an error.</li>
 *     <li><b>R4, declaration:</b> the actions nobody declared ({@code UNDECLARED}) are listed in
 *     {@code undeclared-actions.baseline}, which only shrinks as each one is reviewed and declared.</li>
 * </ul>
 */
public abstract class ActionInventoryRule {

    /** @return the configuration of the repository under test */
    protected abstract ArchRulesConfig config();

    private static final Set<String> PUBLISHABLE = Set.of("HEADLESS", "FLOW", "REMOTE");

    @Test
    void actionsBoundToTheFrontendMatchTheBaseline() {
        var config = config();
        var repo = new RepoSources(config);
        var actions = repo.actions();
        var bound = actions.stream().filter(repo::isFrontendBound).map(RepoSources.Source::path)
                .collect(Collectors.toCollection(TreeSet::new));

        System.out.printf("Actions: %d, bound to the front end: %d, free: %d%n", actions.size(), bound.size(), actions.size() - bound.size());

        ArchBaseline.check(config.baseline("frontend-bound-actions.baseline"), bound,
                "New actions bound to the front end. Use the UI facades (docs/next/dynamia-ui.md) or, if it really needs the "
                        + "front end, declare it @RunsOn(ActionRuntime.FRONTEND) and discuss it first");
    }

    @Test
    void noPublishedActionIsBoundToTheFrontend() {
        var repo = new RepoSources(config());
        var offenders = repo.actions().stream()
                .filter(a -> PUBLISHABLE.contains(repo.runtime(a)))
                .filter(repo::isFrontendBound)
                .map(a -> a.path() + " (" + repo.runtime(a) + ")")
                .collect(Collectors.toCollection(TreeSet::new));

        assertTrue(offenders.isEmpty(), "An action published to remote clients cannot use the front end, itself or through an "
                + "ancestor. Replace the front end calls with UI facades or declare it FRONTEND:\n" + String.join("\n", offenders));
    }

    @Test
    void undeclaredActionsMatchTheBaseline() {
        var config = config();
        var repo = new RepoSources(config);
        var undeclared = repo.actions().stream().filter(a -> "UNDECLARED".equals(repo.runtime(a))).map(RepoSources.Source::path)
                .collect(Collectors.toCollection(TreeSet::new));

        ArchBaseline.check(config.baseline("undeclared-actions.baseline"), undeclared,
                "New actions without a declared runtime. Add @RunsOn(...) to the concrete class after reviewing its restrictions "
                        + "(a runtime is never inherited)");
    }
}
