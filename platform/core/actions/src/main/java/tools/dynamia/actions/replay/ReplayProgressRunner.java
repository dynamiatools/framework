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
import tools.dynamia.ui.NoUIEnvironment;
import tools.dynamia.ui.ProgressRunner;
import tools.dynamia.ui.UIFacades;
import tools.dynamia.ui.ProgressTask;

import java.util.function.Consumer;

/**
 * {@link ProgressRunner} of a headless run: there is no window to show progress in, so the task runs right away, inside
 * the request, and the action continues when it ends. It marks the session so that nothing can be asked after it (see
 * {@link ReplaySession#markNonRepeatable(String)}).
 */
public final class ReplayProgressRunner implements ProgressRunner {

    /** Carries a checked exception of the task out of the transaction callback. */
    private static final class TaskFailure extends RuntimeException {
        TaskFailure(Throwable cause) {
            super(cause);
        }
    }

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
            // like in ZK, the task does not run inside the transaction of the pass: it manages its own
            ReplayTransactions.current().runOutside(() ->
                    // the contract of UIProgress: no UI facade can be used inside the task
                    UIFacades.with(NoUIEnvironment.INSTANCE, () -> {
                        try {
                            task.run(new ProgressMonitor());
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new TaskFailure(e);
                        }
                        return null;
                    }));
        } catch (TaskFailure failure) {
            session.fail(failure.getCause());
            onError.accept(failure.getCause());
            return;
        } catch (Throwable e) {
            session.fail(e); // the action handles the error, but what the pass did is not committed
            onError.accept(e);
            return;
        }
        if (onFinish != null) {
            onFinish.doSomething();
        }
    }
}
