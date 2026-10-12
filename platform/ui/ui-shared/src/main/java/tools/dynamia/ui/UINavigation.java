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
     * Sends the user to {@code url} in a new window or tab.
     */
    public static void openInNewWindow(String url) {
        UIFacades.port(NavigationProvider.class).open(url, true);
    }
}
