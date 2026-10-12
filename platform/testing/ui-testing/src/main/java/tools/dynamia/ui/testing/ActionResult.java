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

import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.ui.testing.UIInteraction.Type;

import java.util.List;

/**
 * What happened when an action ran under {@link ActionTester}.
 */
public final class ActionResult {

    private final String mode;
    private final List<UIInteraction> interactions;
    private final List<CapturedDownload> downloads;
    private final String redirectUrl;
    private final Throwable exception;
    private final TestCrud<?> crud;
    private final List<ActionFlowStep> steps;
    private final int maxTokenLength;
    private ActionResult remote;

    ActionResult(String mode, List<UIInteraction> interactions, List<CapturedDownload> downloads, String redirectUrl,
                 Throwable exception, TestCrud<?> crud, List<ActionFlowStep> steps, int maxTokenLength) {
        this.mode = mode;
        this.interactions = List.copyOf(interactions);
        this.downloads = List.copyOf(downloads);
        this.redirectUrl = redirectUrl;
        this.exception = exception;
        this.crud = crud;
        this.steps = List.copyOf(steps);
        this.maxTokenLength = maxTokenLength;
    }

    /** @return {@code "direct"} or {@code "remote"} */
    public String mode() {
        return mode;
    }

    /** @return every interaction, in order */
    public List<UIInteraction> interactions() {
        return interactions;
    }

    /** @return the type of every interaction, in order */
    public List<Type> types() {
        return interactions.stream().map(UIInteraction::type).toList();
    }

    /** @return the messages shown without a question */
    public List<UIInteraction> notifications() {
        return interactions.stream().filter(i -> i.type() == Type.NOTIFY).toList();
    }

    /** @return the files given to the user */
    public List<CapturedDownload> downloads() {
        return downloads;
    }

    /** @return where the user was sent, or null */
    public String redirectUrl() {
        return redirectUrl;
    }

    /** @return the exception the action ended with, or null */
    public Throwable exception() {
        return exception;
    }

    /** @return whether the action ended with an exception */
    public boolean failed() {
        return exception != null;
    }

    /**
     * @param <E> entity type
     * @return the CRUD the action ran against; fails if the test configured none
     */
    @SuppressWarnings("unchecked")
    public <E> TestCrud<E> crud() {
        if (crud == null) {
            throw new IllegalStateException("The test did not configure a CRUD: call ActionTester.crud(...)");
        }
        return (TestCrud<E>) crud;
    }

    /** @return in remote mode, the protocol steps the client received, one per pass that stopped */
    public List<ActionFlowStep> steps() {
        return steps;
    }

    /** @return in remote mode, the length of the largest resume token */
    public int maxTokenLength() {
        return maxTokenLength;
    }

    /** @return when run with {@code runEverywhere()}, the result of the remote run; otherwise null */
    public ActionResult remote() {
        return remote;
    }

    void attachRemote(ActionResult remote) {
        this.remote = remote;
    }
}
