/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package tools.dynamia.modules.saas.migration.pipeline;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ReflectionUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.domain.jpa.JpaUtils;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.saas.api.ExportIgnore;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.migration.api.AccountExportOptions;
import tools.dynamia.modules.saas.migration.api.CancellationToken;
import tools.dynamia.modules.saas.migration.api.MigrationException;
import tools.dynamia.modules.saas.migration.api.MigrationProgress;
import tools.dynamia.modules.saas.migration.api.MigrationProgressListener;
import tools.dynamia.modules.saas.migration.config.AccountMigrationProperties;
import tools.dynamia.modules.saas.migration.discovery.AccountEntityDiscovery;
import tools.dynamia.modules.saas.migration.graph.EntityDependencyGraph;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Streaming export pipeline — format v3.
 *
 * <h3>Strategy</h3>
 * <ol>
 *   <li>Creates a temporary directory for the export session.</li>
 *   <li>Writes {@code manifest.json} synchronously (entity list known upfront).</li>
 *   <li>Exports each entity type to its own {@code Account{id}_{Simple}.json} file
 *       using up to {@link AccountMigrationProperties#getExportParallelism()} concurrent
 *       virtual threads. Each thread creates its own {@link EntityManager}.</li>
 *   <li>Zips the temp directory to the caller's {@link OutputStream} in topological
 *       order (manifest first, then entities parent-before-child).</li>
 *   <li>Deletes the temp directory unconditionally in a {@code finally} block.</li>
 * </ol>
 *
 * <h3>Per-entity JSON format</h3>
 * <pre>
 * {
 *   "entityClass": "com.example.Customer",
 *   "fields": ["id", "name", "category_ref_id"],
 *   "rows":  [[1,"John",5],[2,"Jane",null]]
 * }
 * </pre>
 *
 * <h3>Serialization rules per attribute type</h3>
 * <ul>
 *   <li>{@code BASIC} / {@code EMBEDDED} — value written directly.</li>
 *   <li>{@code MANY_TO_ONE} / {@code ONE_TO_ONE} — written as
 *       {@code {fieldName}_ref_id: <pk>} (proxy ID extracted without initializing the proxy).</li>
 *   <li>{@code ONE_TO_MANY} / {@code MANY_TO_MANY} — skipped.</li>
 * </ul>
 *
 * <p>Fields annotated with {@link ExportIgnore} are silently skipped.
 *
 * @author Mario Serrano Leones
 */
@Service
public class ExportPipeline {

    private static final LoggingService logger = LoggingService.get(ExportPipeline.class);

    /**
     * ZIP compression level — BEST_SPEED gives good ratio for JSON with minimal CPU overhead.
     */
    private static final int ZIP_LEVEL = Deflater.BEST_SPEED;

    /**
     * ZIP output buffer size.
     */
    private static final int ZIP_BUFFER_SIZE = 256 * 1024;

    /**
     * Per-entity file write buffer size.
     */
    private static final int ENTITY_BUFFER_SIZE = 64 * 1024;
    public static final String JAKARTA_PERSISTENCE_FETCHGRAPH = "jakarta.persistence.fetchgraph";

    /**
     * Column definitions cached per entity class; built once on first export,
     * reused across parallel tasks and subsequent export runs.
     */
    private final Map<Class<?>, List<ColumnDef>> columnCache = new ConcurrentHashMap<>();

    private final EntityManagerFactory emf;
    private final CrudService crudService;
    private final AccountEntityDiscovery discovery;
    private final EntityDependencyGraph dependencyGraph;
    private final AccountMigrationProperties properties;
    private final ObjectMapper objectMapper;

    public ExportPipeline(EntityManagerFactory emf,
                          CrudService crudService,
                          AccountEntityDiscovery discovery,
                          EntityDependencyGraph dependencyGraph,
                          AccountMigrationProperties properties,
                          @Qualifier("migrationObjectMapper") ObjectMapper objectMapper) {
        this.emf = emf;
        this.crudService = crudService;
        this.discovery = discovery;
        this.dependencyGraph = dependencyGraph;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Exports all tenant data for {@code accountId} to {@code output} as a ZIP archive.
     *
     * <p>Entity JSON files are written in parallel to a temporary directory, then
     * zipped to {@code output} in topological order. The temp directory is always
     * deleted before this method returns.
     *
     * @param accountId ID of the account to export
     * @param output    destination stream; ownership is NOT transferred — the caller must close it
     * @param options   export configuration
     * @param listener  optional progress callback
     * @param token     optional cancellation token
     */
    public void export(Long accountId,
                       OutputStream output,
                       AccountExportOptions options,
                       MigrationProgressListener listener,
                       CancellationToken token) {

        Account account = crudService.find(Account.class, accountId);
        if (account == null) {
            throw new MigrationException("Account not found: " + accountId);
        }

        List<Class<?>> candidates = discovery.discoverExportableEntities();
        if (options.getEntities() != null && !options.getEntities().isEmpty()) {
            candidates.removeIf(c -> !options.getEntities().contains(c.getSimpleName()));
            logger.info("[Migration/Export] Filtered entities, {} remaining", candidates.size());
        }

        List<Class<?>> ordered = dependencyGraph.topologicalSort(candidates);
        int parallelism = Math.max(1, properties.getExportParallelism());

        logger.info("[Migration/Export] Starting export accountId={} — {} entity types, parallelism={}",
                accountId, ordered.size(), parallelism);

        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("saas-export-" + accountId + "-");
        } catch (IOException e) {
            throw new MigrationException("Cannot create temp directory for export", e);
        }

        try {
            // ── 1. Manifest (synchronous, entity list is known upfront) ────────
            writeManifestToFile(tempDir.resolve(ExportConstants.MANIFEST_FILE),
                    account, accountId, options, ordered);

            // ── 2. Entity files in parallel ────────────────────────────────────
            exportEntitiesInParallel(tempDir, ordered, accountId, options, token,
                    parallelism, listener);

            // ── 3. Zip temp dir → output (topological order) ──────────────────
            if (token == null || !token.isCancelled()) {
                zipToOutput(tempDir, ordered, accountId, output);
                logger.info("[Migration/Export] ZIP written successfully for accountId={}", accountId);
            }

        } catch (IOException e) {
            throw new MigrationException("Export failed", e);
        } finally {
            deleteDirectory(tempDir);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Manifest
    // ─────────────────────────────────────────────────────────────────────────

    private void writeManifestToFile(Path manifestPath, Account account, Long accountId,
                                     AccountExportOptions options, List<Class<?>> ordered)
            throws IOException {

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(manifestPath));
             JsonGenerator gen = objectMapper.createGenerator(out)) {

            gen.writeStartObject();
            gen.writeStringProperty(ExportConstants.FIELD_VERSION, ExportConstants.FORMAT_VERSION);
            gen.writeStringProperty(ExportConstants.FIELD_EXPORTED_AT, LocalDateTime.now().toString());
            gen.writeNumberProperty(ExportConstants.FIELD_SOURCE_ACCOUNT_ID, accountId);
            gen.writeStringProperty(ExportConstants.FIELD_IDENTITY_STRATEGY,
                    options.getIdentityStrategy().name());

            gen.writeName(ExportConstants.FIELD_ACCOUNT);
            objectMapper.writeValue(gen, account.toDTO());

            gen.writeName(ExportConstants.FIELD_ENTITIES);
            gen.writeStartArray();
            for (Class<?> entityClass : ordered) {
                String fileName = entityFileName(accountId, entityClass);
                gen.writeStartObject();
                gen.writeStringProperty(ExportConstants.MANIFEST_ENTITY_FILE, fileName);
                gen.writeStringProperty(ExportConstants.FIELD_ENTITY_CLASS, entityClass.getName());
                gen.writeEndObject();
            }
            gen.writeEndArray();

            gen.writeEndObject();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Parallel entity export
    // ─────────────────────────────────────────────────────────────────────────

    private void exportEntitiesInParallel(Path tempDir,
                                          List<Class<?>> ordered,
                                          Long accountId,
                                          AccountExportOptions options,
                                          CancellationToken token,
                                          int parallelism,
                                          MigrationProgressListener listener) {

        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
        Semaphore semaphore = new Semaphore(parallelism);
        List<Future<Long>> futures = new ArrayList<>(ordered.size());

        for (Class<?> entityClass : ordered) {
            futures.add(pool.submit(() -> {
                semaphore.acquire();
                try {
                    if (token != null && token.isCancelled()) return 0L;
                    return exportEntityToFile(tempDir, entityClass, accountId, options, token);
                } finally {
                    semaphore.release();
                }
            }));
        }
        pool.shutdown();

        // Collect results in topological order (for progress reporting)
        long processedTypes = 0;
        long totalTypes = ordered.size();
        long totalRecords = 0;

        for (int i = 0; i < ordered.size(); i++) {
            Class<?> entityClass = ordered.get(i);
            try {
                long count = futures.get(i).get();
                totalRecords += count;
                processedTypes++;

                if (listener != null) {
                    listener.onProgress(new MigrationProgress(processedTypes, totalTypes,
                            "Exported " + entityClass.getSimpleName() + " (" + count + " records)",
                            totalRecords));
                }
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                logger.error("[Migration/Export] Failed to export {}: {}",
                        entityClass.getSimpleName(), cause.getMessage(), cause);
                if (properties.isFailOnEntityError()) {
                    pool.shutdownNow();
                    throw new MigrationException(
                            "Export failed for entity " + entityClass.getSimpleName(), cause);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                pool.shutdownNow();
                throw new MigrationException("Export interrupted", e);
            }
        }

        logger.info("[Migration/Export] All entities exported — {} types, {} total records",
                processedTypes, totalRecords);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Single entity → file
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Writes one entity JSON file to {@code tempDir}.
     * Opens its own {@link EntityManager} so this method is safe to call
     * concurrently from multiple virtual threads.
     */
    private long exportEntityToFile(Path tempDir, Class<?> entityClass,
                                    Long accountId, AccountExportOptions options,
                                    CancellationToken token) throws IOException {

        Path filePath = tempDir.resolve(entityFileName(accountId, entityClass));
        EntityManager localEm = emf.createEntityManager();
        try {
            EntityType<?> entityType;
            try {
                entityType = localEm.getMetamodel().entity(entityClass);
            } catch (IllegalArgumentException e) {
                logger.warn("[Migration/Export] Entity not in JPA metamodel, skipping: {}",
                        entityClass.getName());
                writeEmptyEntityFile(filePath, entityClass);
                return 0;
            }

            List<ColumnDef> columns = buildColumns(entityType);

            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(filePath), ENTITY_BUFFER_SIZE);
                 JsonGenerator gen = objectMapper.createGenerator(out)) {

                gen.writeStartObject();
                gen.writeStringProperty(ExportConstants.FIELD_ENTITY_CLASS, entityClass.getName());

                gen.writeName(ExportConstants.FIELD_FIELDS);
                gen.writeStartArray();
                for (ColumnDef col : columns) {
                    gen.writeString(col.columnName());
                }
                gen.writeEndArray();

                gen.writeName(ExportConstants.FIELD_ROWS);
                gen.writeStartArray();
                long processed = writeEntityRows(gen, entityClass, accountId, options, token, localEm, columns);
                gen.writeEndArray();

                gen.writeEndObject();

                logger.debug("[Migration/Export] {} → {} records written", entityClass.getSimpleName(), processed);
                return processed;
            }
        } finally {
            localEm.close();
        }
    }

    private void writeEmptyEntityFile(Path filePath, Class<?> entityClass) throws IOException {
        try (OutputStream out = Files.newOutputStream(filePath);
             JsonGenerator gen = objectMapper.createGenerator(out)) {
            gen.writeStartObject();
            gen.writeStringProperty(ExportConstants.FIELD_ENTITY_CLASS, entityClass.getName());
            gen.writeName(ExportConstants.FIELD_FIELDS);
            gen.writeStartArray();
            gen.writeEndArray();
            gen.writeName(ExportConstants.FIELD_ROWS);
            gen.writeStartArray();
            gen.writeEndArray();
            gen.writeEndObject();
        }
    }

    /**
     * Pages through all rows for {@code entityClass} using keyset pagination and writes
     * each row to {@code gen}.
     *
     * <p>ID type-agnostic: {@code null} is used as the first-page sentinel so no
     * type-specific "zero" value is required. The JPA provider receives the actual
     * ID object (Long, UUID, String, …) on subsequent pages and handles coercion.
     *
     * <p>{@link Account} rows are skipped — account data lives in the manifest.
     */
    private long writeEntityRows(JsonGenerator gen,
                                 Class<?> entityClass,
                                 Long accountId,
                                 AccountExportOptions options,
                                 CancellationToken token,
                                 EntityManager localEm,
                                 List<ColumnDef> columns) throws IOException {
        if (Account.class.equals(entityClass)) {
            return 0;
        }

        String simpleName = entityClass.getSimpleName();
        int chunkSize = resolveChunkSize(options);
        Object lastId = null;   // null = first page; avoids assuming ID type
        long processed = 0;

        EntityGraph<?> emptyGraph = localEm.createEntityGraph(entityClass); //to avoid errors with multiple eagers calls


        do {
            @SuppressWarnings("unchecked")
            List<Object> page = (lastId == null)
                    ? localEm.createQuery(
                            "SELECT e FROM " + simpleName +
                            " e WHERE e.accountId = :accountId ORDER BY e.id ASC")
                    .setParameter("accountId", accountId)
                    .setMaxResults(chunkSize)
                    .setHint(JAKARTA_PERSISTENCE_FETCHGRAPH, emptyGraph)
                    .getResultList()
                    : localEm.createQuery(
                            "SELECT e FROM " + simpleName +
                            " e WHERE e.accountId = :accountId AND e.id > :lastId ORDER BY e.id ASC")
                    .setParameter("accountId", accountId)
                    .setParameter("lastId", lastId)
                    .setMaxResults(chunkSize)
                    .setHint(JAKARTA_PERSISTENCE_FETCHGRAPH, emptyGraph)
                    .getResultList();

            for (Object entity : page) {
                if (token != null && token.isCancelled()) break;
                if (entity != null) {
                    writeEntityRow(gen, entity, columns);
                    Object idVal = JpaUtils.getJPAIdValue(entity);
                    if (idVal != null) {
                        lastId = idVal; // preserve exact type: Long, UUID, String, …
                    }
                }
                processed++;
            }

            localEm.clear();

            if (page.size() < chunkSize || (token != null && token.isCancelled())) break;

        } while (true);

        return processed;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ZIP assembly
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Streams the temp directory contents to {@code output} as a ZIP archive.
     * Entries are added in topological order: manifest first, then entities.
     */
    private void zipToOutput(Path tempDir, List<Class<?>> ordered, Long accountId,
                             OutputStream output) throws IOException {

        // Keep reference to the buffer so we can flush it after finish().
        // ZipOutputStream.finish() writes the central directory into the BufferedOutputStream
        // buffer but does NOT flush it — without the explicit flush() below the last bytes
        // never reach `output` and the ZIP is corrupt.
        BufferedOutputStream buffered = new BufferedOutputStream(output, ZIP_BUFFER_SIZE);
        ZipOutputStream zipOut = new ZipOutputStream(buffered);
        zipOut.setLevel(ZIP_LEVEL);

        addFileToZip(zipOut, tempDir.resolve(ExportConstants.MANIFEST_FILE), ExportConstants.MANIFEST_FILE);

        for (Class<?> entityClass : ordered) {
            String fileName = entityFileName(accountId, entityClass);
            Path filePath = tempDir.resolve(fileName);
            if (Files.exists(filePath)) {
                addFileToZip(zipOut, filePath, fileName);
            }
        }

        zipOut.finish();   // writes ZIP central directory into buffered
        buffered.flush();  // pushes buffered bytes to the caller's output stream
    }

    private static void addFileToZip(ZipOutputStream zipOut, Path file, String entryName)
            throws IOException {
        zipOut.putNextEntry(new ZipEntry(entryName));
        Files.copy(file, zipOut); // streams without loading file into RAM
        zipOut.closeEntry();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Column building & row serialization
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Describes one exported column. The {@link Field} is resolved and made accessible once
     * and cached — never looked up again per row.
     */
    private record ColumnDef(String columnName, Field field, PersistentAttributeType type) {
    }

    private List<ColumnDef> buildColumns(EntityType<?> entityType) {
        return columnCache.computeIfAbsent(entityType.getJavaType(), cls -> {
            List<ColumnDef> cols = new ArrayList<>();

            Field idField = ReflectionUtils.findField(cls, "id");
            if (idField != null) {
                ReflectionUtils.makeAccessible(idField);
                cols.add(new ColumnDef("id", idField, PersistentAttributeType.BASIC));
            }

            for (SingularAttribute<?, ?> attr : entityType.getSingularAttributes()) {
                String name = attr.getName();
                if ("id".equals(name)) continue;
                if (hasExportIgnore(cls, name)) continue;

                PersistentAttributeType pt = attr.getPersistentAttributeType();
                if (pt == PersistentAttributeType.ONE_TO_MANY
                        || pt == PersistentAttributeType.MANY_TO_MANY
                        || pt == PersistentAttributeType.ELEMENT_COLLECTION) continue;

                Field field = ReflectionUtils.findField(cls, name);
                if (field == null) continue;
                ReflectionUtils.makeAccessible(field);

                String colName = (pt == PersistentAttributeType.MANY_TO_ONE
                        || pt == PersistentAttributeType.ONE_TO_ONE)
                        ? name + ExportConstants.REF_ID_SUFFIX : name;
                cols.add(new ColumnDef(colName, field, pt));
            }
            return Collections.unmodifiableList(cols);
        });
    }

    private void writeEntityRow(JsonGenerator gen, Object entity, List<ColumnDef> columns)
            throws IOException {
        gen.writeStartArray();
        for (ColumnDef col : columns) {
            try {
                Object value = col.field().get(entity);

                if (col.type() == PersistentAttributeType.MANY_TO_ONE
                        || col.type() == PersistentAttributeType.ONE_TO_ONE) {
                    if (value != null) {
                        gen.writePOJO(JpaUtils.getJPAIdValue(value));
                    } else {
                        gen.writeNull();
                    }
                } else {
                    objectMapper.writeValue(gen, value);
                }
            } catch (IllegalAccessException e) {
                logger.debug("[Migration/Export] Cannot access field {} on {}: {}",
                        col.field().getName(), entity.getClass().getSimpleName(), e.getMessage());
                gen.writeNull();
            } catch (Exception e) {
                logger.debug("[Migration/Export] Skipping field {} due to: {}",
                        col.field().getName(), e.getMessage());
                gen.writeNull();
            }
        }
        gen.writeEndArray();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────────────────────────────────

    private static String entityFileName(Long accountId, Class<?> entityClass) {
        return "Account" + accountId + "_" + entityClass.getSimpleName() + ".json";
    }

    private boolean hasExportIgnore(Class<?> clazz, String fieldName) {
        Field field = ReflectionUtils.findField(clazz, fieldName);
        return field != null && field.isAnnotationPresent(ExportIgnore.class);
    }

    private int resolveChunkSize(AccountExportOptions options) {
        int size = options.getChunkSize();
        return size > 0 ? size : properties.getChunkSize();
    }

    private static void deleteDirectory(Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException exc) throws IOException {
                    Files.delete(directory);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.warn("[Migration/Export] Could not delete temp directory {}: {}", dir, e.getMessage());
        }
    }
}
