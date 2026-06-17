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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ReflectionUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.domain.jpa.JpaCrudService;
import tools.dynamia.domain.jpa.JpaUtils;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
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
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Streaming export pipeline — format v3.
 *
 * <p>Produces a ZIP archive where every tenant entity type is written as a
 * separate JSON entry ({@code Account{id}_{SimpleName}.json}) preceded by a
 * {@code manifest.json} that carries metadata and the ordered entity list.
 *
 * <p>Each entity JSON file uses the columnar format introduced in v2:
 * <pre>
 * {
 *   "entityClass": "com.example.Customer",
 *   "fields": ["id", "name", "type_ref_id"],
 *   "rows":  [[1,"John",5],[2,"Jane",5]]
 * }
 * </pre>
 *
 * <h3>Serialization rules per attribute type</h3>
 * <ul>
 *   <li>{@code BASIC} / {@code EMBEDDED} — value written directly.</li>
 *   <li>{@code MANY_TO_ONE} / {@code ONE_TO_ONE} — written as
 *       {@code {fieldName}_ref_id: <pk>}.</li>
 *   <li>{@code ONE_TO_MANY} / {@code MANY_TO_MANY} — skipped.</li>
 * </ul>
 *
 * <p>Fields annotated with {@link ExportIgnore} are silently skipped.
 * The output is always a ZIP archive; the caller need not add any
 * additional compression layer.
 *
 * @author Mario Serrano Leones
 */
@Service
public class ExportPipeline {

    private static final LoggingService logger = LoggingService.get(ExportPipeline.class);

    /** ZIP compression level: BEST_SPEED gives good ratio for JSON with minimal CPU overhead. */
    private static final int ZIP_LEVEL = Deflater.BEST_SPEED;

    /** Write buffer size for the ZIP output stream. */
    private static final int BUFFER_SIZE = 256 * 1024;

    /**
     * Column definitions cached per entity class; built once, reused across export runs.
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
     * <p>The archive always starts with {@code manifest.json} followed by one
     * JSON entry per entity type in topological dependency order (parents first).
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
        long totalTypes = ordered.size();

        logger.info("[Migration/Export] Starting export for accountId={} — {} entity types", accountId, totalTypes);

        ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(output, BUFFER_SIZE));
        zipOut.setLevel(ZIP_LEVEL);

        try {
            // ── Manifest (first entry) ────────────────────────────────────────
            zipOut.putNextEntry(new ZipEntry(ExportConstants.MANIFEST_FILE));
            writeManifest(zipOut, account, accountId, options, ordered);
            zipOut.closeEntry();

            // ── Entity entries ────────────────────────────────────────────────
            long processedTypes = 0;
            long totalRecords = 0;

            for (Class<?> entityClass : ordered) {
                if (token != null && token.isCancelled()) {
                    logger.info("[Migration/Export] Cancelled at entity: {}", entityClass.getSimpleName());
                    break;
                }

                String entryName = "Account" + accountId + "_" + entityClass.getSimpleName() + ".json";
                zipOut.putNextEntry(new ZipEntry(entryName));
                long count = exportEntityToEntry(zipOut, entityClass, accountId, options, token);
                zipOut.closeEntry();

                totalRecords += count;
                processedTypes++;

                if (listener != null) {
                    listener.onProgress(new MigrationProgress(processedTypes, totalTypes,
                            "Exported " + entityClass.getSimpleName() + " (" + count + " records)",
                            totalRecords));
                }
            }

            zipOut.finish(); // write ZIP central directory without closing the caller's stream
            logger.info("[Migration/Export] ZIP complete: {} types, {} total records", processedTypes, totalRecords);

        } catch (IOException e) {
            throw new MigrationException("Export failed while writing ZIP", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Manifest
    // ─────────────────────────────────────────────────────────────────────────

    private void writeManifest(ZipOutputStream zipOut, Account account, Long accountId,
                               AccountExportOptions options, List<Class<?>> ordered) throws IOException {
        // NoCloseOutputStream prevents gen.close() from closing the ZipOutputStream
        try (JsonGenerator gen = objectMapper.createGenerator(new NoCloseOutputStream(zipOut))) {
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
                String fileName = "Account" + accountId + "_" + entityClass.getSimpleName() + ".json";
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
    // Entity entry
    // ─────────────────────────────────────────────────────────────────────────

    private long exportEntityToEntry(ZipOutputStream zipOut, Class<?> entityClass,
                                     Long accountId, AccountExportOptions options,
                                     CancellationToken token) throws IOException {

        try (JsonGenerator gen = objectMapper.createGenerator(new NoCloseOutputStream(zipOut))) {
            gen.writeStartObject();
            gen.writeStringProperty(ExportConstants.FIELD_ENTITY_CLASS, entityClass.getName());

            EntityType<?> entityType;
            try {
                entityType = emf.getMetamodel().entity(entityClass);
            } catch (IllegalArgumentException e) {
                logger.warn("[Migration/Export] Entity not in JPA metamodel, skipping: {}", entityClass.getName());
                gen.writeName(ExportConstants.FIELD_FIELDS);
                gen.writeStartArray();
                gen.writeEndArray();
                gen.writeName(ExportConstants.FIELD_ROWS);
                gen.writeStartArray();
                gen.writeEndArray();
                gen.writeEndObject();
                return 0;
            }

            List<ColumnDef> columns = buildColumns(entityType);

            gen.writeName(ExportConstants.FIELD_FIELDS);
            gen.writeStartArray();
            for (ColumnDef col : columns) {
                gen.writeString(col.columnName());
            }
            gen.writeEndArray();

            gen.writeName(ExportConstants.FIELD_ROWS);
            gen.writeStartArray();
            long processed = writeEntityRows(gen, entityClass, accountId, options, token);
            gen.writeEndArray();

            gen.writeEndObject();
            return processed;
        }
    }

    /**
     * Writes all rows for {@code entityClass} using keyset pagination to avoid
     * OFFSET degradation on large tables.
     * {@link Account} is skipped — its data lives in the manifest.
     */
    private long writeEntityRows(JsonGenerator gen, Class<?> entityClass, Long accountId,
                                 AccountExportOptions options, CancellationToken token)
            throws IOException {

        if (Account.class.equals(entityClass)) {
            return 0;
        }

        int chunkSize = resolveChunkSize(options);
        EntityManager em = (EntityManager) crudService.getDelgate();
        List<ColumnDef> columns = columnCache.get(entityClass); // already cached at this point

        long lastId = 0;
        long processed = 0;
        List<Object> page;

        do {
            QueryParameters qp = QueryParameters.with("accountId", accountId)
                    .add("id", QueryConditions.gt(lastId))
                    .paginate(chunkSize)
                    .setHint(JpaCrudService.HINT_FETCH_GRAPH, em.createEntityGraph(entityClass))
                    .setReadOnly(true)
                    .orderBy("id", true);

            @SuppressWarnings("unchecked")
            List<Object> fetched = (List<Object>) crudService.findReadOnly(entityClass, qp);
            page = fetched != null ? fetched : List.of();

            for (Object entity : page) {
                if (token != null && token.isCancelled()) break;
                if (entity != null) {
                    writeEntityRow(gen, entity, columns);
                    Object idVal = JpaUtils.getJPAIdValue(entity);
                    if (idVal instanceof Number n) {
                        lastId = n.longValue();
                    }
                }
                processed++;
            }

            em.clear();

        } while (page.size() == chunkSize && (token == null || !token.isCancelled()));

        logger.debug("[Migration/Export] {} records exported for {}", processed, entityClass.getSimpleName());
        return processed;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Column building & row serialization
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Describes one exported column. The {@link Field} is resolved and made accessible once
     * (in {@link #buildColumns}) and cached — never looked up again per row.
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

    private boolean hasExportIgnore(Class<?> clazz, String fieldName) {
        Field field = ReflectionUtils.findField(clazz, fieldName);
        return field != null && field.isAnnotationPresent(ExportIgnore.class);
    }

    private int resolveChunkSize(AccountExportOptions options) {
        int size = options.getChunkSize();
        return size > 0 ? size : properties.getChunkSize();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal utilities
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Wraps an {@link OutputStream} and swallows {@link #close()} calls.
     * Used to prevent a {@link JsonGenerator} from closing the underlying
     * {@link ZipOutputStream} when the generator itself is closed.
     */
    private static final class NoCloseOutputStream extends FilterOutputStream {
        NoCloseOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        @Override
        public void close() throws IOException {
            out.flush(); // flush but do not close
        }
    }
}
