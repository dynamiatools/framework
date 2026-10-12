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
package tools.dynamia.actions.replay;

import tools.dynamia.ui.NavigationProvider;

/**
 * {@link NavigationProvider} of a headless run: the redirect is recorded in the {@link ReplaySession} and, when the
 * action ends, becomes the final {@code REDIRECT} step the client follows.
 */
public final class ReplayNavigationProvider implements NavigationProvider {

    private final ReplaySession session;

    public ReplayNavigationProvider(ReplaySession session) {
        this.session = session;
    }

    @Override
    public void open(String url, boolean newWindow) {
        session.redirect(url, newWindow);
    }
}
