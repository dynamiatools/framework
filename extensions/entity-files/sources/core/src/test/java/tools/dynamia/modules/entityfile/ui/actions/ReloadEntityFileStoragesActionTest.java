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
import tools.dynamia.modules.entityfile.EntityFileStorage;
import tools.dynamia.ui.testing.ActionTester;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static tools.dynamia.ui.testing.UIInteraction.Type.NOTIFY;

/**
 * Reloading the parameters of every storage tells the user, the same in ZK and for remote clients.
 */
class ReloadEntityFileStoragesActionTest {

    @Test
    void theStorageReloadsItsParametersAndTheUserIsTold() {
        var storage = mock(EntityFileStorage.class);

        var result = ActionTester.of(new ReloadEntityFileStoragesAction())
                .bean(storage)
                .runEverywhere();

        assertNull(result.exception());
        assertEquals(List.of(NOTIFY), result.types());
        assertEquals("Reloaded", result.notifications().get(0).message());
        verify(storage, times(2)).reloadParams(); // once in each execution
    }

    @Test
    void withoutStoragesItJustTellsTheUser() {
        var result = ActionTester.of(new ReloadEntityFileStoragesAction()).runEverywhere();

        assertEquals(List.of(NOTIFY), result.types());
    }
}
