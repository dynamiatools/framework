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

import tools.dynamia.commons.Callback;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.ui.ProgressRunner;
import tools.dynamia.ui.ProgressTask;

import java.util.function.Consumer;

/**
 * {@link ProgressRunner} of a headless run: there is no window to show progress in, so the task runs right away, inside
 * the request, and the action continues when it ends. It marks the session so that nothing can be asked after it (see
 * {@link ReplaySession#markNonRepeatable(String)}).
 */
public final class ReplayProgressRunner implements ProgressRunner {

    private final ReplaySession session;

    public ReplayProgressRunner(ReplaySession session) {
        this.session = session;
    }

    @Override
    public void run(String title, String messageTemplate, ProgressTask task, Callback onFinish, Consumer<Throwable> onError) {
        if (session.isPending()) {
            return; // the action already stopped at a question: this pass does not do the work
        }
        session.markNonRepeatable("UIProgress");
        try {
            task.run(new ProgressMonitor());
        } catch (Throwable e) {
            onError.accept(e);
            return;
        }
        if (onFinish != null) {
            onFinish.doSomething();
        }
    }
}
