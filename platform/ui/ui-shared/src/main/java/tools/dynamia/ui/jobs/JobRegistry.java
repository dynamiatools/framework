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
package tools.dynamia.ui.jobs;

import tools.dynamia.ui.ProgressTask;
import tools.dynamia.ui.files.FlowPrincipal;

import java.util.Optional;

/**
 * Runs the tasks of {@code UIProgress} for remote clients in the background and lets the client follow them. The request
 * that starts a task does not wait for it: the client polls {@code GET /api/app/jobs/{id}} and, when the job is finished,
 * answers the {@code PROGRESS} step so the action can go on, without running the task again.
 * <p>
 * The task runs with the context of the caller (tenant, security) and no UI. With several nodes behind a balancer the
 * registry must be shared (see the transfers store); the default keeps jobs in memory.
 */
public interface JobRegistry {

    /**
     * Starts {@code task} in the background.
     *
     * @param title what the user is told
     * @param task  the work
     * @param owner who started it; only this principal can see the job
     * @return the id of the job
     */
    String start(String title, ProgressTask task, FlowPrincipal owner);

    /**
     * @param id    the job id
     * @param owner who asks
     * @return the status, or empty when the job does not exist, expired, or belongs to someone else
     */
    Optional<JobStatus> status(String id, FlowPrincipal owner);

    /**
     * Forgets a job whose result was already consumed.
     *
     * @param id the job id; unknown ids are ignored
     */
    void remove(String id);

    /** Forgets the finished jobs nobody asked about for a while. */
    void purgeExpired();
}
