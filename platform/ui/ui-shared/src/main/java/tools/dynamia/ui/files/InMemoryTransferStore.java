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
package tools.dynamia.ui.files;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link TransferStore} that keeps files in memory. For tests and small single-process uses; production uses the disk
 * store of the {@code app} module.
 */
public class InMemoryTransferStore implements TransferStore {

    private record Entry(TransferRef ref, TransferMeta meta, Instant expiresAt, byte[] content) implements StoredTransfer {
        @Override
        public InputStream openStream() {
            return new ByteArrayInputStream(content);
        }
    }

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private final Duration ttl;

    /** A store whose files live 30 minutes. */
    public InMemoryTransferStore() {
        this(Duration.ofMinutes(30));
    }

    /**
     * @param ttl how long files are kept
     */
    public InMemoryTransferStore(Duration ttl) {
        this.ttl = ttl;
    }

    @Override
    public TransferRef put(InputStream data, TransferMeta meta, long maxBytes) {
        try {
            var out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            long total = 0;
            int read;
            while ((read = data.read(buffer)) != -1) {
                total += read;
                if (total > maxBytes) {
                    throw new TransferTooLargeException(maxBytes);
                }
                out.write(buffer, 0, read);
            }
            String id = UUID.randomUUID().toString();
            var ref = new TransferRef(id, meta.name(), meta.contentType(), total);
            entries.put(id, new Entry(ref, meta, Instant.now().plus(ttl), out.toByteArray()));
            return ref;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<StoredTransfer> get(String id, FlowPrincipal owner) {
        Entry entry = id == null ? null : entries.get(id);
        if (entry == null || entry.expiresAt().isBefore(Instant.now()) || !entry.meta().isOwnedBy(owner)) {
            return Optional.empty();
        }
        return Optional.of(entry);
    }

    @Override
    public void delete(String id) {
        if (id != null) {
            entries.remove(id);
        }
    }

    @Override
    public void purgeExpired() {
        Instant now = Instant.now();
        entries.values().removeIf(entry -> entry.expiresAt().isBefore(now));
    }

    /** @return how many files are held, expired or not */
    public int size() {
        return entries.size();
    }
}
