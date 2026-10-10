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
package tools.dynamia.modules.saas.ui.action;

import org.junit.jupiter.api.Test;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountPayment;
import tools.dynamia.modules.saas.services.AccountService;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.testing.ActionTester;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static tools.dynamia.ui.testing.UIInteraction.Type.CONFIRM;
import static tools.dynamia.ui.testing.UIInteraction.Type.DIALOG;
import static tools.dynamia.ui.testing.UIInteraction.Type.NOTIFY;

/**
 * Registering a payment for an account: form, confirmation, save, refresh. Same in ZK and for remote clients.
 */
class NewAccountPaymentActionTest {

    private final CrudService crudService = mock(CrudService.class);

    private ActionTester tester() {
        return ActionTester.of(new NewAccountPaymentAction())
                .crud(Account.class, crudService)
                .bean(mock(AccountService.class))
                .on(new Account());
    }

    @Test
    void theUserFillsTheFormConfirmsAndThePaymentIsSaved() {
        var result = tester()
                .user(u -> u.fillForm(Map.of("value", 150000, "reference", "TR-1")).confirm(true))
                .runEverywhere();

        assertNull(result.exception());
        assertEquals(List.of(DIALOG, CONFIRM, NOTIFY), result.types());
        verify(crudService, times(2)).save(any(AccountPayment.class));
        assertEquals(true, result.crud().queried());
    }

    @Test
    void theFormValuesReachThePayment() {
        var saved = new java.util.ArrayList<AccountPayment>();
        org.mockito.Mockito.when(crudService.save(any(AccountPayment.class))).thenAnswer(call -> {
            saved.add(call.getArgument(0));
            return call.getArgument(0);
        });

        tester().user(u -> u.fillForm(Map.of("value", 150000, "reference", "TR-1")).confirm(true)).runRemote();

        assertEquals(1, saved.size());
        assertEquals("TR-1", saved.get(0).getReference());
        assertEquals(0, new BigDecimal("150000").compareTo(saved.get(0).getValue()));
    }

    @Test
    void nothingIsSavedWhenTheUserDoesNotConfirm() {
        var result = tester()
                .user(u -> u.fillForm(Map.of("value", 1)).confirm(false))
                .runEverywhere();

        assertNull(result.exception());
        assertEquals(List.of(DIALOG, CONFIRM), result.types());
        verify(crudService, never()).save(any(AccountPayment.class));
    }

    @Test
    void anAccountMustBeSelected() {
        var result = ActionTester.of(new NewAccountPaymentAction())
                .crud(Account.class, crudService)
                .runEverywhere();

        assertEquals(List.of(NOTIFY), result.types());
        assertEquals(MessageType.WARNING, result.notifications().get(0).messageType());
    }
}
