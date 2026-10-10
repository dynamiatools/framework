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

import tools.dynamia.ui.files.UploadedFile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The "user" of a test: what to answer, in order, when the action asks something. Answers are matched with the
 * interactions that wait for the user (questions, inputs, forms, choices, uploads); notifications, views, progress and
 * redirects need no answer.
 * <p>
 * The script is <b>strict</b> by default: an interaction nobody answered, or an answer of the wrong kind, fails the test
 * with a message that names the interaction, and so do answers left over when the action ended. {@link #lenient()}
 * turns that off.
 *
 * <pre>{@code
 * u.confirm(true).input("Customer returned it").confirm(true);
 * u.whenQuestion(containing("Void")).confirm(true);
 * }</pre>
 */
public final class UIScript {

    sealed interface Answer {
    }

    record Confirm(boolean yes) implements Answer {
    }

    record Input(Object value) implements Answer {
    }

    record Form(Map<String, Object> values, Consumer<Object> edit) implements Answer {
    }

    record Choose(List<Object> selection) implements Answer {
    }

    record Upload(List<UploadedFile> files) implements Answer {
    }

    record Cancel() implements Answer {
    }

    private record Rule(Predicate<String> question, boolean yes) {
    }

    private final Deque<Answer> answers = new ArrayDeque<>();
    private final List<Rule> rules = new ArrayList<>();
    private boolean lenient;

    /**
     * Matches a question whose text contains {@code text}.
     *
     * @param text the text to look for
     * @return the matcher
     */
    public static Predicate<String> containing(String text) {
        return question -> question != null && question.contains(text);
    }

    /** @return this script, no longer failing on unanswered interactions or leftover answers */
    public UIScript lenient() {
        lenient = true;
        return this;
    }

    /**
     * Answers the next question yes or no.
     *
     * @param yes the answer
     * @return this script
     */
    public UIScript confirm(boolean yes) {
        answers.add(new Confirm(yes));
        return this;
    }

    /**
     * Answers the next input with {@code value}.
     *
     * @param value the value; converted to the type the action asks for
     * @return this script
     */
    public UIScript input(Object value) {
        answers.add(new Input(value));
        return this;
    }

    /**
     * Fills the next form with {@code values} (by field name) and submits it. Works in direct and remote execution.
     *
     * @param values field values
     * @return this script
     */
    public UIScript fillForm(Map<String, Object> values) {
        answers.add(new Form(values, null));
        return this;
    }

    /**
     * Edits the bean of the next form and submits it. Direct execution only: remote execution needs plain values, so use
     * {@link #fillForm(Map)} when the action must also run remotely.
     *
     * @param edit receives the bean the form shows
     * @param <T>  bean type
     * @return this script
     */
    @SuppressWarnings("unchecked")
    public <T> UIScript submitForm(Consumer<T> edit) {
        answers.add(new Form(null, (Consumer<Object>) edit));
        return this;
    }

    /**
     * Chooses options of the next choice by label.
     *
     * @param labels the labels, as the user sees them
     * @return this script
     */
    public UIScript choose(String... labels) {
        answers.add(new Choose(new ArrayList<>(Arrays.asList((Object[]) labels))));
        return this;
    }

    /**
     * Chooses options of the next choice by position.
     *
     * @param positions zero based positions
     * @return this script
     */
    public UIScript chooseAt(int... positions) {
        var selection = new ArrayList<Object>();
        for (int position : positions) {
            selection.add(position);
        }
        answers.add(new Choose(selection));
        return this;
    }

    /**
     * Uploads files to the next upload request.
     *
     * @param files the files
     * @return this script
     */
    public UIScript upload(UploadedFile... files) {
        answers.add(new Upload(List.of(files)));
        return this;
    }

    /**
     * Cancels the next interaction: a question counts as no, anything else is closed without an answer.
     *
     * @return this script
     */
    public UIScript cancel() {
        answers.add(new Cancel());
        return this;
    }

    /**
     * Answers every question that matches with the same answer, without consuming the ordered answers.
     *
     * @param question matcher for the text of the question, see {@link #containing(String)}
     * @return the builder that gives the answer
     */
    public WhenQuestion whenQuestion(Predicate<String> question) {
        return yes -> {
            rules.add(new Rule(question, yes));
            return this;
        };
    }

    /** Gives the answer to a question rule. */
    @FunctionalInterface
    public interface WhenQuestion {

        /**
         * @param yes the answer
         * @return the script
         */
        UIScript confirm(boolean yes);
    }

    /**
     * @param interaction what the action asks
     * @param number      position of the interaction in the run, starting at 1
     * @return the answer, or {@code null} when the script is lenient and has none
     */
    Answer answer(UIInteraction interaction, int number) {
        if (interaction.type() == UIInteraction.Type.CONFIRM) {
            for (Rule rule : rules) {
                if (rule.question().test(interaction.message())) {
                    return new Confirm(rule.yes());
                }
            }
        }
        Answer next = answers.poll();
        if (next == null) {
            if (lenient) {
                return null;
            }
            throw new AssertionError("Unanswered " + interaction + " at interaction #" + number);
        }
        return next;
    }

    /** Fails if the action ended with answers nobody asked for. */
    void verifyConsumed() {
        if (!lenient && !answers.isEmpty()) {
            throw new AssertionError(answers.size() + " scripted answer(s) were never asked for: " + answers);
        }
    }

    static AssertionError mismatch(UIInteraction interaction, int number, Answer answer) {
        return new AssertionError("Interaction #" + number + " " + interaction + " cannot be answered with "
                + answer.getClass().getSimpleName().toLowerCase() + "(...)");
    }
}
