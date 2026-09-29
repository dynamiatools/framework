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
package tools.dynamia.navigation;

import java.util.function.Consumer;

/**
 * Minimal concrete {@link BaseNavigationManager} used only to exercise its behavior in tests, since
 * {@link BaseNavigationManager} itself is abstract and its only real implementation
 * ({@code ZKNavigationManager}) lives in the {@code zk} module and requires a ZK desktop.
 */
public class TestNavigationManager extends BaseNavigationManager {

    public TestNavigationManager(ModuleContainer container) {
        super(container);
    }

    public TestNavigationManager() {
        this(new ModuleContainer());
    }

    @Override
    public void closePage(Page page) {
        // no-op for tests
    }

    @Override
    public void sendEvent(PageEvent evt) {
        // no-op for tests
    }

    @Override
    public void onPageEvent(Page page, Consumer<PageEvent> evt) {
        // no-op for tests
    }

    @Override
    public void clearPageEvents(Page page) {
        // no-op for tests
    }
}
