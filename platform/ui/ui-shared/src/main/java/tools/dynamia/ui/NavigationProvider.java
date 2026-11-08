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
package tools.dynamia.ui;

import java.util.Map;

/**
 * SPI behind {@link UINavigation}. ZK redirects the browser; a headless run ends the action with a {@code REDIRECT} step.
 */
@UIPort(name = "navigation", steps = {"REDIRECT"})
public interface NavigationProvider {

    /**
     * Sends the user to {@code url}.
     *
     * @param url       relative or http(s) URL
     * @param newWindow whether to open it in a new window or tab
     */
    void open(String url, boolean newWindow);

    /**
     * Sends the user to a page of the application's navigation (a {@code CrudPage}, for example) by its virtual path.
     * The path is not a URL: each front end resolves it with its own navigation. The default implementation refuses, so
     * an adapter that has no page navigation fails loudly instead of ignoring the request.
     *
     * @param path   virtual path of the page, such as {@code ventas/ventas}; letters, digits, {@code _ - . /} only
     * @param params parameters for the page; may be empty
     * @throws UnsupportedOperationException when the front end cannot open pages
     */
    default void openPage(String path, Map<String, Object> params) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " cannot open the page " + path);
    }

    /**
     * Asks the front end to reload what the user is looking at (the list of the current CRUD page, for example). The
     * default implementation refuses.
     *
     * @throws UnsupportedOperationException when the front end cannot refresh
     */
    default void refresh() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " cannot refresh the view");
    }

    /**
     * Tells whether {@code path} is a valid virtual page path: relative, made of letters, digits, {@code _ - . /}, and
     * without {@code ..} segments. It is checked before a path leaves the server.
     *
     * @param path the candidate path
     * @return {@code true} when it can be sent to a front end
     */
    static boolean isValidPagePath(String path) {
        return path != null && !path.isBlank() && path.matches("[A-Za-z0-9_\\-./]+")
                && !path.startsWith("/") && !path.contains("..") && !path.contains("//");
    }
}
