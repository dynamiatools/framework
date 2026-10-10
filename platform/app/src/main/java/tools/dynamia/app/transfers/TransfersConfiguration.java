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
package tools.dynamia.app.transfers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.dynamia.ui.files.TransferStore;
import tools.dynamia.ui.files.UploadDefaults;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wires the files that travel between remote clients and actions: the {@link TransferStore} (local disk unless the
 * application defines another) and the upload limits.
 * <ul>
 *     <li>{@code dynamia.ui.files.dir}: where files are kept, default {@code ${java.io.tmpdir}/dynamia-transfers}</li>
 *     <li>{@code dynamia.ui.files.ttl}: how long they live, default {@code PT30M}</li>
 *     <li>{@code dynamia.ui.files.quota}: total bytes allowed in the store, default 0 (no limit)</li>
 *     <li>{@code dynamia.ui.files.max-file-size}: default 104857600 (100 MB)</li>
 *     <li>{@code dynamia.ui.files.max-files}: default 10</li>
 * </ul>
 */
@Configuration
public class TransfersConfiguration {

    /**
     * @param dir         directory of the files
     * @param ttl         time to live
     * @param quota       total bytes allowed, 0 for no limit
     * @param maxFileSize default maximum size of one file
     * @param maxFiles    default maximum files of one upload
     * @return the store, purged every five minutes
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(TransferStore.class)
    public PurgingStore localTransferStore(
            @Value("${dynamia.ui.files.dir:#{systemProperties['java.io.tmpdir'] + '/dynamia-transfers'}}") String dir,
            @Value("${dynamia.ui.files.ttl:PT30M}") Duration ttl,
            @Value("${dynamia.ui.files.quota:0}") long quota,
            @Value("${dynamia.ui.files.max-file-size:104857600}") long maxFileSize,
            @Value("${dynamia.ui.files.max-files:10}") int maxFiles) {
        UploadDefaults.set(maxFileSize, maxFiles);
        return new PurgingStore(new LocalTransferStore(Path.of(dir), ttl, quota));
    }

    /** A store that purges itself on a daemon thread and stops it when the context closes. */
    public static final class PurgingStore implements TransferStore, AutoCloseable {

        private final TransferStore delegate;
        private final ScheduledExecutorService purger = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "transfer-store-purge");
            thread.setDaemon(true);
            return thread;
        });

        PurgingStore(TransferStore delegate) {
            this.delegate = delegate;
            purger.scheduleWithFixedDelay(() -> {
                try {
                    delegate.purgeExpired();
                } catch (RuntimeException e) {
                    // keep purging
                }
            }, 1, 5, TimeUnit.MINUTES);
        }

        @Override
        public tools.dynamia.ui.files.TransferRef put(java.io.InputStream data, tools.dynamia.ui.files.TransferMeta meta, long maxBytes) {
            return delegate.put(data, meta, maxBytes);
        }

        @Override
        public java.util.Optional<tools.dynamia.ui.files.StoredTransfer> get(String id, tools.dynamia.ui.files.FlowPrincipal owner) {
            return delegate.get(id, owner);
        }

        @Override
        public void delete(String id) {
            delegate.delete(id);
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
