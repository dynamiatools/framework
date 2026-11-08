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
import tools.dynamia.ui.NavigationProvider;

/**
 * ZK implementation of {@link NavigationProvider}: redirects the browser of the current execution.
 */
public class ZKNavigationProvider implements NavigationProvider {

    /**
     * Opens the page with the ZK navigation manager of the desktop, the same way a menu item does.
     */
    @Override
    public void openPage(String path, java.util.Map<String, Object> params) {
        var manager = tools.dynamia.navigation.NavigationManager.getCurrent();
        if (manager == null) {
            throw new IllegalStateException("No navigation manager to open the page " + path);
        }
        var serializable = new java.util.HashMap<String, java.io.Serializable>();
        params.forEach((key, value) -> {
            if (value instanceof java.io.Serializable s) {
                serializable.put(key, s);
            }
        });
        manager.navigateTo(path, serializable);
    }

    /**
     * Reloads the current page with the navigation manager.
     */
    @Override
    public void refresh() {
        var manager = tools.dynamia.navigation.NavigationManager.getCurrent();
        if (manager == null) {
            throw new IllegalStateException("No navigation manager to refresh the view");
        }
        manager.refresh();
    }

    @Override
    public void open(String url, boolean newWindow) {
        var execution = Executions.getCurrent();
        if (execution == null) {
            throw new IllegalStateException("No ZK execution to navigate from");
        }
        if (newWindow) {
            execution.sendRedirect(url, "_blank");
        } else {
            execution.sendRedirect(url);
        }
    }
}
