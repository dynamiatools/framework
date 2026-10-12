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

import tools.dynamia.ui.files.FlowPrincipal;
import tools.dynamia.ui.files.StoredTransfer;
import tools.dynamia.ui.files.TransferMeta;
import tools.dynamia.ui.files.TransferRef;
import tools.dynamia.ui.files.TransferStore;
import tools.dynamia.ui.files.TransferTooLargeException;
import tools.dynamia.ui.files.UploadRejectedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * {@link TransferStore} on the local disk: one data file and one metadata file per reference. Content is copied by
 * streaming with a hard limit, so a file bigger than allowed is cut and deleted, and nothing is ever read fully into memory.
 * <p>
 * With several nodes behind a balancer each node would only see its own files: use a shared directory or another store.
 */
public class LocalTransferStore implements TransferStore {

    private static final Pattern ID = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    private final Path directory;
    private final Duration ttl;
    private final long quota;

    /**
     * @param directory where files are kept; created if missing
     * @param ttl       how long a file lives
     * @param quota     most bytes all files together may take, or 0 for no limit
     */
    public LocalTransferStore(Path directory, Duration ttl, long quota) {
        this.directory = directory;
        this.ttl = ttl;
        this.quota = quota;
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public TransferRef put(InputStream data, TransferMeta meta, long maxBytes) {
        if (quota > 0 && usedBytes() >= quota) {
            throw new UploadRejectedException("The transfer area is full, try again later");
        }
        String id = UUID.randomUUID().toString();
        Path content = dataFile(id);
        long total = 0;
        try {
            try (OutputStream out = Files.newOutputStream(content)) {
                byte[] buffer = new byte[64 * 1024];
                int read;
                while ((read = data.read(buffer)) != -1) {
                    total += read;
                    if (total > maxBytes) {
                        throw new TransferTooLargeException(maxBytes);
                    }
                    out.write(buffer, 0, read);
                }
            }
            var ref = new TransferRef(id, meta.name(), meta.contentType(), total);
            writeMeta(id, meta, ref, Instant.now().plus(ttl));
            return ref;
        } catch (IOException e) {
            deleteQuietly(id);
            throw new UncheckedIOException(e);
        } catch (RuntimeException e) {
            deleteQuietly(id);
            throw e;
        }
    }

    @Override
    public Optional<StoredTransfer> get(String id, FlowPrincipal owner) {
        if (id == null || !ID.matcher(id).matches() || !Files.exists(metaFile(id)) || !Files.exists(dataFile(id))) {
            return Optional.empty();
        }
        try {
            var properties = new Properties();
            try (var in = Files.newBufferedReader(metaFile(id), StandardCharsets.UTF_8)) {
                properties.load(in);
            }
            Instant expiresAt = Instant.parse(properties.getProperty("expiresAt"));
            var meta = new TransferMeta(properties.getProperty("name"), properties.getProperty("contentType"),
                    TransferMeta.Direction.valueOf(properties.getProperty("direction")),
                    properties.getProperty("subject"), properties.getProperty("tenant"));
            if (expiresAt.isBefore(Instant.now()) || !meta.isOwnedBy(owner)) {
                return Optional.empty();
            }
            var ref = new TransferRef(id, meta.name(), meta.contentType(), Long.parseLong(properties.getProperty("size")));
            return Optional.of(new Stored(ref, meta, expiresAt, dataFile(id)));
        } catch (IOException | RuntimeException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String id) {
        if (id != null && ID.matcher(id).matches()) {
            deleteQuietly(id);
        }
    }

    @Override
    public void purgeExpired() {
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(file -> file.getFileName().toString().endsWith(".meta")).forEach(this::purgeIfExpired);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void purgeIfExpired(Path metaFile) {
        String name = metaFile.getFileName().toString();
        String id = name.substring(0, name.length() - ".meta".length());
        try {
            var properties = new Properties();
            try (var in = Files.newBufferedReader(metaFile, StandardCharsets.UTF_8)) {
                properties.load(in);
            }
            if (Instant.parse(properties.getProperty("expiresAt")).isBefore(Instant.now())) {
                deleteQuietly(id);
            }
        } catch (IOException | RuntimeException e) {
            deleteQuietly(id); // unreadable metadata: the file can never be served
        }
    }

    private long usedBytes() {
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(file -> file.getFileName().toString().endsWith(".bin")).mapToLong(file -> {
                try {
                    return Files.size(file);
                } catch (IOException e) {
                    return 0;
                }
            }).sum();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeMeta(String id, TransferMeta meta, TransferRef ref, Instant expiresAt) throws IOException {
        var properties = new Properties();
        properties.setProperty("name", meta.name() == null ? "" : meta.name());
        if (meta.contentType() != null) {
            properties.setProperty("contentType", meta.contentType());
        }
        properties.setProperty("direction", meta.direction().name());
        properties.setProperty("subject", meta.subject());
        if (meta.tenant() != null) {
            properties.setProperty("tenant", meta.tenant());
        }
        properties.setProperty("size", String.valueOf(ref.size()));
        properties.setProperty("expiresAt", expiresAt.toString());
        try (var out = Files.newBufferedWriter(metaFile(id), StandardCharsets.UTF_8)) {
            properties.store(out, null);
        }
    }

    private void deleteQuietly(String id) {
        try {
            Files.deleteIfExists(metaFile(id));
            Files.deleteIfExists(dataFile(id));
        } catch (IOException e) {
            // the next purge tries again
        }
    }

    private Path dataFile(String id) {
        return directory.resolve(id + ".bin");
    }

    private Path metaFile(String id) {
        return directory.resolve(id + ".meta");
    }

    private record Stored(TransferRef ref, TransferMeta meta, Instant expiresAt, Path file) implements StoredTransfer {
        @Override
        public InputStream openStream() {
            try {
                return Files.newInputStream(file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
