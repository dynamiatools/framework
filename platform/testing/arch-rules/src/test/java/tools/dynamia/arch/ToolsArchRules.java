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

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The architecture rules applied to {@code dynamia-tools} itself, with the baselines of {@code src/test/resources/arch}.
 * <p>
 * {@code dynamia-erp} does the same in a test module of its own: it extends the three rules with an
 * {@link ArchRulesConfig} of its repository and keeps its baselines next to its tests (see the README of this module).
 */
final class ToolsArchRules {

    /** Where the front end (ZK) may be used in {@code dynamia-tools}. */
    static final List<Pattern> CORNER = List.of(
            Pattern.compile("^platform/ui/zk/.*"),
            Pattern.compile("^platform/starters/zk-starter/.*"),
            Pattern.compile("^themes/theme-dynamical/.*"),
            Pattern.compile("^extensions/dashboard/sources/zk/.*"),
            Pattern.compile("^examples/demo-zk-[^/]+/.*"));

    private ToolsArchRules() {
    }

    static ArchRulesConfig config() {
        return ArchRulesConfig.forZk(Path.of("src/test/resources/arch"), CORNER);
    }
}
