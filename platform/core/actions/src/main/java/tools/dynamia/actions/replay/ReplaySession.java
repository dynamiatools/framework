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

import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIFacades;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The state of one headless pass of an action: the answers the user already gave, how many were consumed, the first
 * question without an answer, and the messages shown on the way.
 * <p>
 * It is what every headless implementation of a UI facade shares. {@link ReplayInteractions} (messages and questions)
 * is one; the implementation of any other facade (forms, downloads, choices...) takes the session of the execution
 * with {@link #current()} and calls {@link #interact(ActionFlowStep, Consumer)} with the step the client must show.
 * That keeps the numbering of the interactions in one place, which is what makes replay line the answers up.
 */
public final class ReplaySession {

    private final List<Object> answers;
    private final List<ReplayInteractions.Notification> notifications = new ArrayList<>();
    private int cursor;
    private ActionFlowStep pending;

    /**
     * @param answers the answers of the user so far, in the order of the interactions
     */
    public ReplaySession(List<Object> answers) {
        this.answers = answers;
    }

    /**
     * @return the session of the headless execution running on this thread, or {@code null} when the code is not
     * running under {@link ReplayExecutor} (for example in ZK)
     */
    public static ReplaySession current() {
        return UIFacades.bound(ReplaySession.class);
    }

    /**
     * Runs {@code work} with {@code session} as the {@link #current()} one.
     */
    public static <T> T run(ReplaySession session, java.util.function.Supplier<T> work) {
        return UIFacades.with(ReplaySession.class, session, work);
    }

    /**
     * Registers the n-th interaction of the action. If the user already answered it, {@code onAnswer} runs right
     * here with the answer; otherwise {@code question} becomes {@link #pending()} and nothing else of this pass
     * reaches the user. Interactions after the pending one are ignored.
     *
     * @param question what the client must show to get the answer
     * @param onAnswer what the action does with the answer
     */
    public void interact(ActionFlowStep question, Consumer<Object> onAnswer) {
        if (pending != null) {
            return; // the action already stopped at an earlier question
        }
        if (cursor < answers.size()) {
            onAnswer.accept(answers.get(cursor++));
        } else {
            pending = question;
        }
    }

    /**
     * Records something the action told the user that is not a question. Ignored once the pass has stopped.
     */
    public void notify(String message, String title, MessageType type) {
        if (pending == null) {
            notifications.add(new ReplayInteractions.Notification(message, title, type));
        }
    }

    /** @return the question waiting for an answer, or {@code null} when the action ran to the end */
    public ActionFlowStep pending() {
        return pending;
    }

    public boolean isPending() {
        return pending != null;
    }

    /** @return the messages of this pass, in order; they are only meaningful when the pass completed */
    public List<ReplayInteractions.Notification> notifications() {
        return notifications;
    }
}
