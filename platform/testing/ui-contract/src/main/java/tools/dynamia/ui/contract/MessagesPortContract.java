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
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** {@code UIMessages}: questions, inputs and notifications. */
public abstract class MessagesPortContract extends PortContract {

    @Test
    void yesCallsOnYesAndOnlyThat() {
        var o = driver().run(fx -> UIMessages.showQuestion("Sure?", () -> fx.add("yes"), () -> fx.add("no")), List.of(Reply.yes()));

        assertEquals(List.of("yes"), o.effects());
        assertEquals(List.of(Outcome.Asked.of("CONFIRM", "Sure?")), o.asked());
        assertNull(o.failure());
    }

    @Test
    void noCallsOnNoWhenThereIsOne() {
        var o = driver().run(fx -> UIMessages.showQuestion("Sure?", () -> fx.add("yes"), () -> fx.add("no")), List.of(Reply.no()));

        assertEquals(List.of("no"), o.effects());
    }

    @Test
    void noWithoutAnOnNoDoesNothing() {
        var o = driver().run(fx -> UIMessages.showQuestion("Sure?", () -> fx.add("yes")), List.of(Reply.no()));

        assertEquals(List.of(), o.effects());
        assertNull(o.failure());
    }

    @Test
    void closingTheQuestionIsNo() {
        var o = driver().run(fx -> UIMessages.showQuestion("Sure?", () -> fx.add("yes"), () -> fx.add("no")), List.of(Reply.cancel()));

        assertEquals(List.of("no"), o.effects());
    }

    @Test
    void anInputIsConvertedToTheValueClass() {
        var o = driver().run(fx -> UIMessages.<Integer>showInput("How many?", Integer.class, n -> fx.add("n=" + (n + 1))),
                List.of(Reply.input("12")));

        assertEquals(List.of("n=13"), o.effects());
        assertEquals(List.of(Outcome.Asked.of("INPUT", "How many?")), o.asked());
    }

    @Test
    void cancellingAnInputNeverCallsTheCallback() {
        var o = driver().run(fx -> UIMessages.<String>showInput("Reason", String.class, reason -> fx.add(reason)),
                List.of(Reply.cancel()));

        assertEquals(List.of(), o.effects());
        assertNull(o.failure());
    }

    @Test
    void theCallbackIsTheContinuationAndRunsOnce() {
        var o = driver().run(fx -> {
            UIMessages.showQuestion("First?", () -> {
                fx.add("first");
                UIMessages.showQuestion("Second?", () -> fx.add("second"));
            });
        }, List.of(Reply.yes(), Reply.yes()));

        assertEquals(List.of("first", "second"), o.effects());
        assertEquals(List.of(Outcome.Asked.of("CONFIRM", "First?"), Outcome.Asked.of("CONFIRM", "Second?")), o.asked());
    }

    @Test
    void notificationsDoNotAskAnythingAndAreReported() {
        var o = driver().run(fx -> {
            UIMessages.showMessage("Heads up", MessageType.WARNING);
            UIMessages.showMessage("Done");
        }, List.of());

        assertEquals(List.of(), o.asked());
        assertEquals(List.of("WARNING Heads up", "NORMAL Done"), o.notices());
    }
}
