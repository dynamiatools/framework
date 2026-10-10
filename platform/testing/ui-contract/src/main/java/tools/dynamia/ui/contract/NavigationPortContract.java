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
package tools.dynamia.ui.contract;

import org.junit.jupiter.api.Test;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.UINavigation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@code UINavigation}: terminal in practice; what the action does afterwards still runs. */
public abstract class NavigationPortContract extends PortContract {

    @Test
    void openSendsTheUserToTheUrl() {
        var o = driver().run(fx -> UINavigation.open("/sales/1"), List.of());

        assertEquals(new Outcome.Redirect("/sales/1", false), o.redirect());
    }

    @Test
    void openInANewWindowSaysSo() {
        var o = driver().run(fx -> UINavigation.openInNewWindow("/files/1/download"), List.of());

        assertEquals(new Outcome.Redirect("/files/1/download", true), o.redirect());
    }

    @Test
    void whatTheActionDoesAfterwardsStillRunsAndItsNotificationsAreKept() {
        var o = driver().run(fx -> {
            UIMessages.showMessage("Opening");
            UINavigation.open("/somewhere");
            fx.add("after");
        }, List.of());

        assertEquals(List.of("after"), o.effects());
        assertEquals(List.of("NORMAL Opening"), o.notices());
        assertTrue(o.redirect() != null);
    }
}
