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
package tools.dynamia.ui.testing;

import org.junit.jupiter.api.Test;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.LocalAction;
import tools.dynamia.crud.CrudState;
import tools.dynamia.crud.actions.DeleteAction;
import tools.dynamia.crud.actions.SaveAction;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIFiles;
import tools.dynamia.ui.UIChoices;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.UINavigation;
import tools.dynamia.ui.UIProgress;
import tools.dynamia.ui.UIViews;
import tools.dynamia.ui.FormOptions;
import tools.dynamia.ui.UIFacades;
import tools.dynamia.ui.UIUnavailableException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static tools.dynamia.ui.testing.UIInteraction.Type.CHOICE;
import static tools.dynamia.ui.testing.UIInteraction.Type.CONFIRM;
import static tools.dynamia.ui.testing.UIInteraction.Type.DIALOG;
import static tools.dynamia.ui.testing.UIInteraction.Type.INPUT;
import static tools.dynamia.ui.testing.UIInteraction.Type.NOTIFY;
import static tools.dynamia.ui.testing.UIInteraction.Type.PROGRESS;
import static tools.dynamia.ui.testing.UIInteraction.Type.REDIRECT;
import static tools.dynamia.ui.testing.UIInteraction.Type.UPLOAD;

/**
 * The test platform itself: strict scripts, retries on validation, the contract of progress, and direct against remote
 * execution.
 */
class ActionTesterTest {

    public static class Customer {
        public String name;
    }

    private static String text(tools.dynamia.ui.files.UploadedFile file) {
        try (var in = file.openStream()) {
            return new String(in.readAllBytes());
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    private static LocalAction action(String id, java.util.function.Consumer<ActionEvent> body) {
        return new tools.dynamia.actions.AbstractLocalAction() {
            {
                setId(id);
            }

            @Override
            public void actionPerformed(ActionEvent evt) {
                body.accept(evt);
            }
        };
    }

    @Test
    void anUnansweredInteractionFailsNamingIt() {
        var tester = ActionTester.of(action("ask", e -> UIMessages.showQuestion("Void sale 123?", () -> {
        })));

        var failure = assertThrows(AssertionError.class, tester::run);

        assertEquals("Unanswered CONFIRM 'Void sale 123?' at interaction #1", failure.getMessage());
    }

    @Test
    void leftoverAnswersFail() {
        var tester = ActionTester.of(action("none", e -> UIMessages.showMessage("hi"))).user(u -> u.confirm(true));

        var failure = assertThrows(AssertionError.class, tester::run);

        assertTrue(failure.getMessage().contains("never asked for"));
    }

    @Test
    void lenientScriptsAcceptBoth() {
        var log = new ArrayList<String>();
        var result = ActionTester.of(action("lenient", e -> {
            UIMessages.showQuestion("Sure?", () -> log.add("yes"));
            UIMessages.showMessage("hi");
        })).lenient().user(u -> u.confirm(true).confirm(true)).run();

        assertEquals(List.of("yes"), log);
        assertEquals(List.of(CONFIRM, NOTIFY), result.types());
    }

    @Test
    void anAnswerOfTheWrongKindFails() {
        var tester = ActionTester.of(action("kind", e -> UIMessages.showQuestion("Sure?", () -> {
        }))).user(u -> u.input("x"));

        assertThrows(AssertionError.class, tester::run);
    }

    @Test
    void questionRulesAnswerByTextWithoutConsumingTheOrder() {
        var log = new ArrayList<String>();
        ActionTester.of(action("rules", e -> {
            UIMessages.showQuestion("Void sale 7?", () -> log.add("voided"));
            UIMessages.<String>showInput("Reason", String.class, reason -> log.add(reason));
        })).user(u -> u.whenQuestion(UIScript.containing("Void")).confirm(true).input("Mistake")).run();

        assertEquals(List.of("voided", "Mistake"), log);
    }

    @Test
    void cancellingNeverCallsTheSuccessCallback() {
        var log = new ArrayList<String>();
        var result = ActionTester.of(action("cancel", e -> {
            UIMessages.<Integer>showInput("How many?", Integer.class, n -> log.add("n=" + n));
            UIMessages.showQuestion("Sure?", () -> log.add("yes"), () -> log.add("no"));
        })).user(u -> u.cancel().cancel()).run();

        assertEquals(List.of("no"), log);
        assertEquals(List.of(INPUT, CONFIRM), result.types());
    }

    @Test
    void aValidationErrorInOnSubmitOpensTheFormAgainWithTheError() {
        var seen = new ArrayList<String>();
        var result = ActionTester.of(action("form", e -> UIViews.showForm(
                FormOptions.of("New customer", Customer.class, new Customer()), (customer, dialog) -> {
                    seen.add(customer.name);
                    if (customer.name == null || customer.name.isBlank()) {
                        throw new ValidationError("Name is required");
                    }
                    dialog.close();
                }))).user(u -> u.<Customer>submitForm(c -> c.name = "").<Customer>submitForm(c -> c.name = "Ana")).run();

        assertEquals(List.of("", "Ana"), seen);
        assertEquals(List.of(DIALOG, DIALOG), result.types());
        var second = result.interactions().get(1);
        assertEquals("Name is required", second.message());
        assertEquals(MessageType.ERROR, second.messageType());
    }

    @Test
    void progressRunsWithoutUiInsideTheTask() {
        var seen = new ArrayList<String>();
        var result = ActionTester.of(action("progress", e -> UIProgress.run("Working", monitor -> {
            seen.add(UIFacades.current().name());
            assertThrows(UIUnavailableException.class, () -> UIMessages.showQuestion("In task?", () -> {
            }));
        }, () -> UIMessages.showMessage("Done")))).run();

        assertEquals(List.of("none"), seen);
        assertEquals(List.of(PROGRESS, NOTIFY), result.types());
    }

    @Test
    void aFailingProgressTaskCallsOnError() {
        var errors = new ArrayList<String>();
        ActionTester.of(action("progress-error", e -> UIProgress.run("Working", null,
                monitor -> {
                    throw new IllegalStateException("boom");
                }, () -> errors.add("finished"), t -> errors.add(t.getMessage())))).run();

        assertEquals(List.of("boom"), errors);
    }

    @Test
    void filesChoicesDownloadsAndRedirectsAreScripted() {
        var read = new ArrayList<String>();
        var result = ActionTester.of(action("everything", e -> {
            UIFiles.uploadOne(".txt", file -> read.add(text(file)));
            UIChoices.chooseOne("Format", List.of("PDF", "CSV"), s -> s, format -> {
                UIFiles.download("out." + format, "text/plain", format.getBytes());
                UINavigation.open("/done");
            });
        })).user(u -> u.upload(TestFiles.text("in.txt", "hello")).choose("CSV")).run();

        assertEquals(List.of("hello"), read);
        assertEquals(List.of(UPLOAD, CHOICE, REDIRECT), result.types());
        assertEquals("out.CSV", result.downloads().get(0).name());
        assertEquals("CSV", result.downloads().get(0).asString());
        assertEquals("/done", result.redirectUrl());
    }

    @Test
    void runEverywhereComparesDirectAndRemote() {
        var result = ActionTester.of(action("same", e -> {
            UIMessages.showQuestion("Delete?", () -> {
                UIMessages.<String>showInput("Reason", String.class, reason -> UIMessages.showMessage("Deleted: " + reason));
            });
        })).user(u -> u.confirm(true).input("duplicate")).runEverywhere();

        assertEquals(List.of(CONFIRM, INPUT, NOTIFY), result.types());
        assertEquals("remote", result.remote().mode());
        assertEquals(2, result.remote().steps().size());
        assertTrue(result.remote().maxTokenLength() > 0);
    }

    @Test
    void runEverywhereFailsWhenAnActionIsNotDeterministic() {
        var counter = new int[1];
        var tester = ActionTester.of(action("flaky", e -> {
            UIMessages.showMessage("run " + counter[0]++);
        }));

        var failure = assertThrows(AssertionError.class, tester::runEverywhere);

        assertTrue(failure.getMessage().contains("does not behave the same"));
    }

    // -- the crud actions of the framework -------------------------------------------------------------------------

    @Test
    void deleteActionAsksAndDeletesTheSameInDirectAndRemote() {
        var service = mock(CrudService.class);
        var result = ActionTester.of(new DeleteAction())
                .crud(Customer.class, service)
                .on(new Customer())
                .user(u -> u.confirm(true))
                .runEverywhere();

        assertEquals(List.of(CONFIRM, NOTIFY), result.types());
        assertTrue(result.<Customer>crud().deleted());
    }

    @Test
    void deleteActionDoesNothingWhenTheUserSaysNo() {
        var service = mock(CrudService.class);
        var result = ActionTester.of(new DeleteAction())
                .crud(Customer.class, service)
                .on(new Customer())
                .user(u -> u.confirm(false))
                .runEverywhere();

        assertEquals(List.of(CONFIRM), result.types());
        assertFalse(result.<Customer>crud().deleted());
        verify(service, never()).delete(any(Object.class));
    }

    @Test
    void saveActionSavesAndGoesBackToRead() {
        var service = mock(CrudService.class);
        var customer = new Customer();
        var result = ActionTester.of(new SaveAction())
                .crud(Customer.class, service)
                .on(customer)
                .user(u -> {
                })
                .runEverywhere();

        assertEquals(List.of(NOTIFY), result.types());
        assertEquals(CrudState.READ, result.crud().state());
        assertTrue(result.<Customer>crud().saved());
    }

    @Test
    void theModuleHasNoZkDependency() throws Exception {
        var classpath = System.getProperty("java.class.path");
        assertFalse(classpath.contains("zkoss"), "ui-testing must not bring ZK");
        assertInstanceOf(Class.class, Class.forName("tools.dynamia.ui.testing.ActionTester"));
    }
}
