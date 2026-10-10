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
package tools.dynamia.app.jobs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.dynamia.ui.ProgressTask;
import tools.dynamia.ui.files.FlowPrincipal;
import tools.dynamia.ui.jobs.InMemoryJobRegistry;
import tools.dynamia.ui.jobs.JobRegistry;
import tools.dynamia.ui.jobs.JobStatus;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wires the background jobs of {@code UIProgress} for remote clients: an in-memory {@link JobRegistry} unless the application
 * defines another (with several nodes it must be a shared one, see D2 in {@code docs/next/dynamia-ui.md}), purged every five
 * minutes.
 */
@Configuration
public class JobsConfiguration {

    /**
     * @return the registry
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(JobRegistry.class)
    public PurgingJobs jobRegistry() {
        return new PurgingJobs(new InMemoryJobRegistry());
    }

    /** A registry that purges itself on a daemon thread and stops it when the context closes. */
    public static final class PurgingJobs implements JobRegistry, AutoCloseable {

        private final JobRegistry delegate;
        private final ScheduledExecutorService purger = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "job-registry-purge");
            thread.setDaemon(true);
            return thread;
        });

        PurgingJobs(JobRegistry delegate) {
            this.delegate = delegate;
            purger.scheduleWithFixedDelay(() -> {
                try {
                    delegate.purgeExpired();
                } catch (RuntimeException e) {
                    // keep purging
                }
            }, 5, 5, TimeUnit.MINUTES);
        }

        @Override
        public String start(String title, ProgressTask task, FlowPrincipal owner) {
            return delegate.start(title, task, owner);
        }

        @Override
        public Optional<JobStatus> status(String id, FlowPrincipal owner) {
            return delegate.status(id, owner);
        }

        @Override
        public void remove(String id) {
            delegate.remove(id);
        }

        @Override
        public void purgeExpired() {
            delegate.purgeExpired();
        }

        @Override
        public void close() {
            purger.shutdownNow();
        }
    }
}
