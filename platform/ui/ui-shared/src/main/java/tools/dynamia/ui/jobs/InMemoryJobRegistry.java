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

import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.integration.scheduling.SchedulerUtil;
import tools.dynamia.ui.ProgressTask;
import tools.dynamia.ui.files.FlowPrincipal;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link JobRegistry} that keeps jobs in memory and runs them with {@link SchedulerUtil}, which carries the context of
 * the caller (tenant, security) into the task and runs it with no UI.
 */
public class InMemoryJobRegistry implements JobRegistry {

    private static final class Job {
        final String id = UUID.randomUUID().toString();
        final String title;
        final String subject;
        final String tenant;
        final ProgressMonitor monitor = new ProgressMonitor();
        volatile JobStatus.State state = JobStatus.State.RUNNING;
        volatile String error;
        volatile Instant finishedAt;

        Job(String title, FlowPrincipal owner) {
            this.title = title;
            // a snapshot: the principal of the container is live and answers for whoever calls it now
            this.subject = owner.subject();
            this.tenant = owner.tenant();
        }

        boolean isOwnedBy(FlowPrincipal other) {
            return other != null && java.util.Objects.equals(subject, other.subject())
                    && java.util.Objects.equals(tenant, other.tenant());
        }

        JobStatus status() {
            return new JobStatus(id, title, state, monitor.getCurrent(), monitor.getMax(), monitor.getMessage(), error);
        }
    }

    private final Map<String, Job> jobs = new ConcurrentHashMap<>();
    private final Duration retention;

    /** A registry that keeps a finished job 30 minutes. */
    public InMemoryJobRegistry() {
        this(Duration.ofMinutes(30));
    }

    /**
     * @param retention how long a finished job is kept for the client to read
     */
    public InMemoryJobRegistry(Duration retention) {
        this.retention = retention;
    }

    @Override
    public String start(String title, ProgressTask task, FlowPrincipal owner) {
        purgeExpired();
        var job = new Job(title, owner);
        jobs.put(job.id, job);
        SchedulerUtil.run(() -> {
            try {
                task.run(job.monitor);
                job.state = JobStatus.State.DONE;
            } catch (Throwable e) {
                job.error = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                job.state = JobStatus.State.FAILED;
            } finally {
                job.finishedAt = Instant.now();
            }
        });
        return job.id;
    }

    @Override
    public Optional<JobStatus> status(String id, FlowPrincipal owner) {
        Job job = id == null ? null : jobs.get(id);
        if (job == null || !job.isOwnedBy(owner)) {
            return Optional.empty();
        }
        return Optional.of(job.status());
    }

    @Override
    public void remove(String id) {
        if (id != null) {
            jobs.remove(id);
        }
    }

    @Override
    public void purgeExpired() {
        Instant limit = Instant.now().minus(retention);
        jobs.values().removeIf(job -> job.finishedAt != null && job.finishedAt.isBefore(limit));
    }
}
