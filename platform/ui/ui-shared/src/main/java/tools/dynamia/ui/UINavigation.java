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

/**
 * Facade for sending the user somewhere, whatever front end it is on (see {@link UIFacades}).
 * <p>
 * Headless, the redirect is the last thing that happens: it ends the action and the client navigates after the action
 * finished. Anything the action does after calling {@code open} still runs, but the user is already on the way.
 */
public final class UINavigation {

    private UINavigation() {
    }

    /**
     * Sends the user to {@code url} in the current window.
     */
    public static void open(String url) {
        UIFacades.port(NavigationProvider.class).open(url, false);
    }

    /**
     * Sends the user to a page of the application by its virtual path.
     *
     * @param path   virtual path of the page, such as {@code ventas/ventas}
     * @param params parameters for the page; values must be {@code String}, {@code Number} or {@code Boolean}, because they
     *               travel to the client (an entity or any other object is refused)
     * @throws IllegalArgumentException when {@code path} is not a valid page path (a URL, for example) or a parameter has a
     *                                  null key or value, or a value of another type
     */
    public static void openPage(String path, java.util.Map<String, Object> params) {
        if (!NavigationProvider.isValidPagePath(path)) {
            throw new IllegalArgumentException("Not a page path: " + path + " (use UINavigation.open for URLs)");
        }
        var checked = new java.util.LinkedHashMap<String, Object>();
        if (params != null) {
            params.forEach((key, value) -> {
                if (key == null || !(value instanceof String || value instanceof Number || value instanceof Boolean)) {
                    throw new IllegalArgumentException("Page parameter " + key + " must be a String, Number or Boolean, not "
                            + (value == null ? "null" : value.getClass().getName()));
                }
                checked.put(key, value);
            });
        }
        UIFacades.port(NavigationProvider.class).openPage(path, java.util.Collections.unmodifiableMap(checked));
    }

    /**
     * Sends the user to a page of the application by its virtual path.
     *
     * @param path virtual path of the page
     */
    public static void openPage(String path) {
        openPage(path, java.util.Map.of());
    }

    /**
     * Asks the front end to reload what the user is looking at. In a remote run it is a flag of the final response, so
     * it happens once, after the action ended.
     */
    public static void refresh() {
        UIFacades.port(NavigationProvider.class).refresh();
    }

    /**
     * Sends the user to {@code url} in a new window or tab.
     */
    public static void openInNewWindow(String url) {
        UIFacades.port(NavigationProvider.class).open(url, true);
    }
}
