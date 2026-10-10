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
import tools.dynamia.commons.Callback;
import tools.dynamia.integration.Containers;
import tools.dynamia.ui.ProgressRunner;
import tools.dynamia.ui.ProgressTask;
import tools.dynamia.ui.files.FlowPrincipal;
import tools.dynamia.ui.jobs.JobRegistry;
import tools.dynamia.ui.jobs.JobStatus;

import java.util.function.Consumer;

/**
 * {@link ProgressRunner} of a headless run. The task does not run inside the request: it is handed to the
 * {@link JobRegistry}, which runs it in the background with the context of the caller (tenant, security) and no UI, and the
 * pass stops at a {@code PROGRESS} step carrying the job id. The client follows the job and answers when it stops waiting;
 * the next pass reads the real state of the job, and if it finished runs {@code onFinish} or {@code onError} <b>without
 * running the task again</b>. If the job still runs, the same step is asked again.
 */
public final class ReplayProgressRunner implements ProgressRunner {

    private final ReplaySession session;

    public ReplayProgressRunner(ReplaySession session) {
        this.session = session;
    }

    @Override
    public void run(String title, String messageTemplate, ProgressTask task, Callback onFinish, Consumer<Throwable> onError) {
        var step = ActionFlowStep.progress(title, null);
        boolean started = session.interact(step, answer -> {
            String jobId = jobIdOf(answer);
            var registry = registry();
            JobStatus status = registry.status(jobId, FlowPrincipal.current()).orElseThrow(() ->
                    new ReplayRetry("The task is no longer available. Please ask for it again."));
            if (!status.state().isFinished()) {
                step.setData(ActionFlowStep.progressData(title, jobId)); // still running: wait for the same job
                throw new ReplayRetry(null);
            }
            session.consumeJob(jobId);
            if (status.state() == JobStatus.State.FAILED) {
                var failure = new JobFailedException(status.error());
                session.fail(failure); // the action handles the error, but what the pass did is not committed
                onError.accept(failure);
            } else if (onFinish != null) {
                onFinish.doSomething();
            }
        });
        if (started) {
            // a new question: start the work now, once. Earlier passes stopped before reaching here, and this one stops here.
            String jobId = registry().start(title, task, FlowPrincipal.current());
            step.setData(ActionFlowStep.progressData(title, jobId));
        }
    }

    private static JobRegistry registry() {
        JobRegistry registry = Containers.get().findObject(JobRegistry.class);
        if (registry == null) {
            throw new IllegalStateException("No JobRegistry is registered: UIProgress for remote clients needs one");
        }
        return registry;
    }

    private static String jobIdOf(Object answer) {
        if (answer instanceof java.util.Map<?, ?> map && map.get("jobId") != null) {
            return String.valueOf(map.get("jobId"));
        }
        throw new ReplayRetry("The answer of a PROGRESS step must carry the jobId");
    }

    /** The error a background task ended with, as the action sees it in its {@code onError}. */
    public static final class JobFailedException extends RuntimeException {
        JobFailedException(String message) {
            super(message);
        }
    }
}
