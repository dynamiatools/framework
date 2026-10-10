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
import tools.dynamia.ui.files.DownloadSource;

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
    private final List<DownloadSource> downloads = new ArrayList<>();
    private final List<String> consumedRefs = new ArrayList<>();
    private String redirectUrl;
    private boolean redirectInNewWindow;
    private String nonRepeatable;
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
        return UIFacades.current().port(ReplaySession.class).orElse(null);
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
            if (nonRepeatable != null) {
                throw new IllegalStateException(nonRepeatable + " already did its work in this pass, so it must be the last "
                        + "interaction of the action: the action runs again for every answer of the user");
            }
            pending = question;
        }
    }

    /**
     * Declares that the action did something that must not happen twice (long work done inside the request). Questions
     * asked later in the same pass fail, because answering them would run the action, and the work, again.
     *
     * @param what who did it, for the error message
     */
    public void markNonRepeatable(String what) {
        if (nonRepeatable == null) {
            nonRepeatable = what;
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

    /**
     * Records a file for the user; the content is read only when the flow ends. Ignored once the pass has stopped at a question; like messages, only the downloads
     * of the pass that ran to the end reach the client.
     */
    public void download(DownloadSource source) {
        if (pending == null) {
            downloads.add(source);
        }
    }

    /**
     * Records that the action read an uploaded file from the {@code TransferStore}. When the flow ends successfully the
     * runtime deletes the references; if it is abandoned, the store expires them.
     *
     * @param ref the reference id
     */
    public void consume(String ref) {
        if (!consumedRefs.contains(ref)) {
            consumedRefs.add(ref);
        }
    }

    /** @return the references of the uploads read in this pass */
    public List<String> consumedRefs() {
        return List.copyOf(consumedRefs);
    }

    /**
     * Records where the user must go when the action ends. Ignored once the pass has stopped at a question; the first
     * redirect wins.
     */
    public void redirect(String url, boolean newWindow) {
        if (pending == null && redirectUrl == null) {
            redirectUrl = url;
            redirectInNewWindow = newWindow;
        }
    }

    /** @return the URL the user must go to when the action ended, or {@code null} */
    public String redirectUrl() {
        return redirectUrl;
    }

    public boolean redirectInNewWindow() {
        return redirectInNewWindow;
    }

    /** @return the files of this pass, in order; they are only meaningful when the pass completed */
    public List<DownloadSource> downloads() {
        return downloads;
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
