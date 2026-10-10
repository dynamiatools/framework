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
package tools.dynamia.modules.entityfile.ui.actions;

import org.junit.jupiter.api.Test;
import tools.dynamia.actions.AbstractLocalAction;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.testing.ActionTester;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static tools.dynamia.ui.testing.UIInteraction.Type.NOTIFY;
import static tools.dynamia.ui.testing.UIInteraction.Type.REDIRECT;

/**
 * Downloading a file sends the user to its URL in a new window, the same in ZK and for remote clients.
 * <p>
 * The action reaches its file through a controller that is a ZK class, so the test hands it the event it would build and
 * exercises the action itself.
 */
class DownloadFileActionTest {

    private static ActionTester downloading(EntityFile file) {
        var action = new DownloadFileAction();
        var entry = new AbstractLocalAction() {
            {
                setId("download");
            }

            @Override
            public void actionPerformed(ActionEvent evt) {
                action.actionPerformed(new EntityFileActionEvent(null, file, this, null, null));
            }
        };
        return ActionTester.of(entry);
    }

    @Test
    void theUserIsSentToTheUrlOfTheFileInANewWindow() {
        var file = new EntityFile();
        file.setRemoteURL("https://files.example.com/report.pdf");

        var result = downloading(file).runEverywhere();

        assertNull(result.exception());
        assertEquals(List.of(REDIRECT), result.types());
        assertEquals("https://files.example.com/report.pdf", result.redirectUrl());
        assertEquals(true, result.interactions().get(0).payload().get("newWindow"));
    }

    @Test
    void withoutASelectedFileTheUserIsAskedToSelectOne() {
        var result = downloading(null).runEverywhere();

        assertEquals(List.of(NOTIFY), result.types());
        assertEquals(MessageType.WARNING, result.notifications().get(0).messageType());
        assertNull(result.redirectUrl());
    }
}
