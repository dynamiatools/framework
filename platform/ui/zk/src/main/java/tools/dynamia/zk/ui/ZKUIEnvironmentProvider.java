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
package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.Executions;
import tools.dynamia.ui.UIEnvironment;
import tools.dynamia.ui.UIEnvironmentProvider;

/**
 * Activates {@link ZKUIEnvironment} while the code runs inside a ZK execution (an event listener, a server push
 * activation...). Outside it, for example in a scheduler thread or a REST request, the UI ports are not available and
 * facades fail with a clear error instead of reaching for ZK.
 */
public class ZKUIEnvironmentProvider implements UIEnvironmentProvider {

    private final ZKUIEnvironment environment = new ZKUIEnvironment();

    @Override
    public boolean isActive() {
        return Executions.getCurrent() != null;
    }

    @Override
    public UIEnvironment environment() {
        return environment;
    }
}
