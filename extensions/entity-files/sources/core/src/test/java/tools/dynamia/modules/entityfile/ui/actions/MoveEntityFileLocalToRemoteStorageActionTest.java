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
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.modules.entityfile.EntityFileStorage;
import tools.dynamia.modules.entityfile.local.LocalEntityFileStorage;
import tools.dynamia.ui.testing.ActionTester;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tools.dynamia.ui.testing.UIInteraction.Type.CHOICE;
import static tools.dynamia.ui.testing.UIInteraction.Type.CONFIRM;
import static tools.dynamia.ui.testing.UIInteraction.Type.NOTIFY;
import static tools.dynamia.ui.testing.UIInteraction.Type.PROGRESS;

/**
 * Moving the local entity files to another storage: choose the storage, confirm, run with progress.
 */
class MoveEntityFileLocalToRemoteStorageActionTest {

    private final CrudService crudService = mock(CrudService.class);
    private final LocalEntityFileStorage local = mock(LocalEntityFileStorage.class);
    private final EntityFileStorage remote = mock(EntityFileStorage.class);

    private ActionTester tester() {
        when(remote.getId()).thenReturn("s3");
        when(remote.getName()).thenReturn("S3 bucket");
        when(crudService.executeQuery(org.mockito.ArgumentMatchers.any(tools.dynamia.domain.util.QueryBuilder.class)))
                .thenReturn(List.of(5L));
        return ActionTester.of(new MoveEntityFileLocalToRemoteStorageAction(local, crudService))
                .bean(remote);
    }

    @Test
    void theUserPicksTheStorageConfirmsAndTheFilesMove() {
        var result = tester()
                .user(u -> u.choose("S3 bucket").confirm(true))
                .runEverywhere();

        assertNull(result.exception());
        assertEquals(List.of(CHOICE, CONFIRM, PROGRESS, NOTIFY), result.types());
        assertEquals("Moving files completed", result.notifications().get(0).message());
        verify(remote, times(2)).reloadParams();
    }

    @Test
    void nothingMovesWhenTheUserDoesNotConfirm() {
        var result = tester()
                .user(u -> u.choose("S3 bucket").confirm(false))
                .runEverywhere();

        assertEquals(List.of(CHOICE, CONFIRM), result.types());
        verify(remote, never()).reloadParams();
    }
}
