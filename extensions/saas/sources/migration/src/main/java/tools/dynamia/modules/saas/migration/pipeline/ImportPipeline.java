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
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ReflectionUtils;
import tools.dynamia.domain.jpa.JpaUtils;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.saas.migration.api.AccountImportOptions;
import tools.dynamia.modules.saas.migration.api.CancellationToken;
import tools.dynamia.modules.saas.migration.api.IdentityMapper;
import tools.dynamia.modules.saas.migration.api.IdentityStrategy;
import tools.dynamia.modules.saas.migration.api.MigrationException;
import tools.dynamia.modules.saas.migration.api.MigrationProgress;
import tools.dynamia.modules.saas.migration.api.MigrationProgressListener;
import tools.dynamia.modules.saas.migration.config.AccountMigrationProperties;
import tools.dynamia.modules.saas.migration.identity.KeepIdsIdentityMapper;
import tools.dynamia.modules.saas.migration.identity.RegenerateIdsIdentityMapper;
import tools.dynamia.modules.saas.migration.identity.Uuid7IdentityMapper;
import tools.dynamia.modules.saas.domain.Account;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.UUID;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Streaming import pipeline — format v3 (ZIP multi-file).
 *
 * <h3>ZIP import flow</h3>
 * <ol>
 *   <li>Open {@link ZipInputStream}; first entry is {@code manifest.json} (metadata only).</li>
 *   <li>For every subsequent {@code *.json} entry, parse
 *       {@code entityClass} → {@code fields} → {@code rows} in streaming mode.</li>
 *   <li>Rows are accumulated into chunks and persisted via
 *       {@link #persistChunk} in isolated transactions.</li>
 *
 * </ol>
 *
 * <p>Entities arrive in the same topological order they were written by
 * {@link ExportPipeline} — parents before children — so reference
 * resolution via the {@code idMappings} table normally finds the parent
 * already persisted.
 *
 * <h3>Dangling references (second pass)</h3>
 * Two situations break that assumption: entities in a dependency cycle (the child arrives before its
 * parent) and children whose parent was not exported (soft-deleted rows have a negative
 * {@code accountId}). For a reference to a class that <em>is</em> in the export but is not mapped yet,
 * see {@link ReferenceResolver}: optional references are persisted as {@code null} and patched once all
 * entities are imported; rows with a mandatory reference are held (by their JSON row only) and retried
 * until no more progress is made. What is still unresolved afterwards points to something that was never
 * cloned: it is cleared (optional) or the row is skipped (mandatory) when
 * {@link AccountImportOptions#isSkipOrphans()} is set. References to classes outside the export keep
 * their original id.
 *
 * @author Mario Serrano Leones
 */
@Service
public class ImportPipeline {

    private static final Logger log = LoggerFactory.getLogger(ImportPipeline.class);

    @PersistenceContext
    private EntityManager em;

    /**
     * Custom SPI mappers registered as Spring beans; queried before built-in defaults.
     */
    @Autowired(required = false)
    private List<IdentityMapper> customMappers;

    /**
     * Field cache: class → (fieldName → accessible Field).
     * {@link Optional#empty()} is stored when a field doesn't exist, avoiding repeated failed lookups.
     */
    private final Map<Class<?>, Map<String, Optional<Field>>> fieldCache = new ConcurrentHashMap<>();

    private final EntityManagerFactory emf;
    private final AccountMigrationProperties properties;
    private final ObjectMapper objectMapper;

    public ImportPipeline(EntityManagerFactory emf,
                          AccountMigrationProperties properties,
                          @Qualifier("migrationObjectMapper") ObjectMapper objectMapper) {
        this.emf = emf;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Imports all entity data from {@code input} into the target account.
     *
     * <p>Input must be a ZIP archive in format v3 (produced by {@link ExportPipeline}).
     *
     * @param input    ZIP export stream
     * @param options  import configuration
     * @param listener optional progress callback
     * @param token    optional cancellation token
     */
    public void importTenant(InputStream input,
                             AccountImportOptions options,
                             MigrationProgressListener listener,
                             CancellationToken token) {

        IdentityMapper identityMapper = resolveIdentityMapper(options);
        Map<String, Map<Object, Object>> idMappings = new HashMap<>();

        try {
            importFromZip(input, options, identityMapper, idMappings, listener, token);
        } catch (IOException e) {
            throw new MigrationException("Import failed", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ZIP import (v3)
    // ─────────────────────────────────────────────────────────────────────────

    private void importFromZip(InputStream source,
                               AccountImportOptions options,
                               IdentityMapper identityMapper,
                               Map<String, Map<Object, Object>> idMappings,
                               MigrationProgressListener listener,
                               CancellationToken token) throws IOException {

        ZipInputStream zipIn = new ZipInputStream(source);
        ZipEntry entry;
        long total = 0;
        ImportRun run = newRun(Set.of(), options, identityMapper, idMappings);

        while ((entry = zipIn.getNextEntry()) != null) {
            if (token != null && token.isCancelled()) {
                log.info("[Migration/Import] Cancelled");
                break;
            }

            String name = entry.getName();

            if (ExportConstants.MANIFEST_FILE.equals(name)) {
                Set<String> exported = logManifestInfo(zipIn);
                run = newRun(exported, options, identityMapper, idMappings);
            } else if (name.endsWith(".json")) {
                long count = importZipEntityEntry(zipIn, options, identityMapper, idMappings, run, listener, token);
                total += count;
            }

            zipIn.closeEntry();
        }

        if (token == null || !token.isCancelled()) {
            total += resolveDeferredRows(run, options, identityMapper, idMappings, listener, token);
            applyPendingReferences(run, options, listener);
        }

        ReferenceResolver resolver = run.resolver;
        String summary = "Import complete";
        if (resolver.skippedRows() > 0 || resolver.clearedReferences() > 0) {
            summary += " (%d rows skipped because a mandatory parent was not cloned, %d optional references cleared)"
                    .formatted(resolver.skippedRows(), resolver.clearedReferences());
            log.warn("[Migration/Import] Orphan references: {} rows skipped {}, {} optional references cleared",
                    resolver.skippedRows(), resolver.skippedByClass(), resolver.clearedReferences());
        }
        if (listener != null) {
            listener.onProgress(MigrationProgress.of(total, total, summary, total));
        }
        log.info("[Migration/Import] ZIP import complete — {} total records", total);
    }

    private ImportRun newRun(Set<String> exportedClasses,
                             AccountImportOptions options,
                             IdentityMapper identityMapper,
                             Map<String, Map<Object, Object>> idMappings) {
        ImportRun run = new ImportRun();
        run.resolver = new ReferenceResolver(identityMapper, exportedClasses, idMappings,
                this::isSharedRow, options.isSkipOrphans());
        return run;
    }

    /**
     * Reads and logs metadata from manifest.json and returns the names of the entity classes that are
     * part of the export (needed to tell "not cloned" apart from "not part of the export").
     * {@link Account} is excluded: it has no rows in the export, so it is never mapped.
     */
    private Set<String> logManifestInfo(ZipInputStream zipIn) {
        Set<String> exported = new HashSet<>();
        try {
            JsonParser parser = objectMapper.createParser(new NoCloseInputStream(zipIn));
            String version = null;
            String sourceAccountId = null;
            if (parser.nextToken() == JsonToken.START_OBJECT) {
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String field = parser.currentName();
                    parser.nextToken();
                    switch (field) {
                        case ExportConstants.FIELD_VERSION -> version = parser.getText();
                        case ExportConstants.FIELD_SOURCE_ACCOUNT_ID -> sourceAccountId = parser.getValueAsString();
                        case ExportConstants.FIELD_ENTITIES -> readManifestEntities(parser, exported);
                        default -> parser.skipChildren();
                    }
                }
            }
            parser.close();
            log.info("[Migration/Import] manifest: version={}, sourceAccountId={}, entityTypes={}",
                    version, sourceAccountId, exported.size());
        } catch (Exception e) {
            log.debug("[Migration/Import] Could not read manifest metadata: {}", e.getMessage());
        }
        exported.remove(Account.class.getName());
        return exported;
    }

    private void readManifestEntities(JsonParser parser, Set<String> exported) {
        if (parser.currentToken() != JsonToken.START_ARRAY) {
            parser.skipChildren();
            return;
        }
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (parser.currentToken() != JsonToken.START_OBJECT) {
                parser.skipChildren();
                continue;
            }
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                parser.nextToken();
                if (ExportConstants.FIELD_ENTITY_CLASS.equals(field)) {
                    exported.add(parser.getText());
                } else {
                    parser.skipChildren();
                }
            }
        }
    }

    /**
     * Imports one entity entry from the ZIP stream.
     *
     * <p>The parser reads from the {@link ZipInputStream} via a {@link NoCloseInputStream}
     * wrapper so that closing the parser does not close the underlying zip stream.
     * {@link ZipInputStream} naturally reports EOF at the end of each entry, so Jackson
     * stops reading exactly at the entry boundary.
     */
    private long importZipEntityEntry(ZipInputStream zipIn,
                                      AccountImportOptions options,
                                      IdentityMapper identityMapper,
                                      Map<String, Map<Object, Object>> idMappings,
                                      ImportRun run,
                                      MigrationProgressListener listener,
                                      CancellationToken token) throws IOException {

        JsonParser parser = objectMapper.createParser(new NoCloseInputStream(zipIn));
        try {
            return parseEntityEntry(parser, options, identityMapper, idMappings, run, listener, token);
        } finally {
            parser.close(); // flushes parser buffers; NoCloseInputStream.close() is a no-op
        }
    }

    /**
     * Parses a single entity JSON file:
     * {@code {"entityClass":"...", "fields":[...], "rows":[...]}}
     */
    private long parseEntityEntry(JsonParser parser,
                                  AccountImportOptions options,
                                  IdentityMapper identityMapper,
                                  Map<String, Map<Object, Object>> idMappings,
                                  ImportRun run,
                                  MigrationProgressListener listener,
                                  CancellationToken token) throws IOException {

        expectToken(parser, JsonToken.START_OBJECT, "entity entry");

        String entityClassName = null;
        List<String> fields = null;
        long total = 0;
        int chunkSize = options.getChunkSize() > 0 ? options.getChunkSize() : properties.getChunkSize();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();

            switch (fieldName) {
                case ExportConstants.FIELD_ENTITY_CLASS -> entityClassName = parser.getText();

                case ExportConstants.FIELD_FIELDS -> {
                    fields = new ArrayList<>();
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        fields.add(parser.getText());
                    }
                }

                case ExportConstants.FIELD_ROWS -> {
                    if (entityClassName == null || fields == null) {
                        throw new MigrationException(
                                "'entityClass' and 'fields' must appear before 'rows' in entity entry");
                    }
                    total = importRowsFromParser(
                            parser, entityClassName, fields, options,
                            identityMapper, idMappings, run, listener, token, chunkSize);
                }

                default -> parser.skipChildren();
            }
        }

        return total;
    }

    private long importRowsFromParser(JsonParser parser,
                                      String entityClassName,
                                      List<String> fields,
                                      AccountImportOptions options,
                                      IdentityMapper identityMapper,
                                      Map<String, Map<Object, Object>> idMappings,
                                      ImportRun run,
                                      MigrationProgressListener listener,
                                      CancellationToken token,
                                      int chunkSize) throws IOException {

        Class<?> entityClass;
        try {
            entityClass = Class.forName(entityClassName);
        } catch (ClassNotFoundException e) {
            log.warn("[Migration/Import] Entity class not found, skipping: {}", entityClassName);
            parser.skipChildren();
            return 0;
        }

        // parser is positioned at START_ARRAY of rows
        expectToken(parser, JsonToken.START_ARRAY, "rows for " + entityClass.getSimpleName());

        List<JsonNode> chunk = new ArrayList<>(chunkSize);
        long total = 0;

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (token != null && token.isCancelled()) break;

            chunk.add(rowToObjectNode(parser, fields));

            if (chunk.size() >= chunkSize) {
                total += persistChunk(chunk, entityClass, options, identityMapper, idMappings, run);
                chunk.clear();
            }
        }

        if (!chunk.isEmpty()) {
            total += persistChunk(chunk, entityClass, options, identityMapper, idMappings, run);
        }

        log.info("[Migration/Import] Imported {} records for {}", total, entityClass.getSimpleName());
        if (listener != null) {
            listener.onProgress(MigrationProgress.of(total, 0,
                    "Imported " + entityClass.getSimpleName() + " (" + total + " records)", total));
        }

        return total;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Chunk persistence (transactional boundary)
    // ─────────────────────────────────────────────────────────────────────────


    public int persistChunk(List<JsonNode> chunk,
                            Class<?> entityClass,
                            AccountImportOptions options,
                            IdentityMapper identityMapper,
                            Map<String, Map<Object, Object>> idMappings) {
        // No manifest information: legacy behaviour (unmapped references keep the original id).
        return persistChunk(chunk, entityClass, options, identityMapper, idMappings,
                newRun(Set.of(), options, identityMapper, idMappings));
    }

    private int persistChunk(List<JsonNode> chunk,
                             Class<?> entityClass,
                             AccountImportOptions options,
                             IdentityMapper identityMapper,
                             Map<String, Map<Object, Object>> idMappings,
                             ImportRun run) {
        AtomicInteger count = new AtomicInteger();
        EntityType<?> entityType;
        try {
            entityType = emf.getMetamodel().entity(entityClass);
        } catch (IllegalArgumentException e) {
            log.warn("[Migration/Import] Entity not in JPA metamodel, skipping: {}", entityClass.getName());
            return 0;
        }

        if (chunk.isEmpty()) return 0;

        var crud = DomainUtils.lookupCrudService();
        crud.executeWithinTransaction(() -> {
            for (JsonNode node : chunk) {
                try {
                    List<ReferenceResolver.Ref> refs = new ArrayList<>();
                    Object entity = deserializeEntity(node, entityClass, entityType,
                            options.getTargetAccountId(), refs);

                    ReferenceResolver.RowResult resolved =
                            run.resolver.resolveRow(refs, run.finalPhase, entityClass.getSimpleName());
                    if (resolved.verdict() == ReferenceResolver.Verdict.SKIP) {
                        log.debug("[Migration/Import] Skipping orphan {} row id={}: mandatory parent was not cloned",
                                entityClass.getSimpleName(), node.get("id"));
                        continue;
                    }
                    if (resolved.verdict() == ReferenceResolver.Verdict.DEFER) {
                        // Keep only the JSON row; the entity is rebuilt on retry.
                        run.deferred.computeIfAbsent(entityClass, k -> new ArrayList<>()).add(node);
                        continue;
                    }
                    applyReferences(entity, refs, resolved.ids());

                    Object originalId = readId(node);
                    Object mappedId = identityMapper.mapId(originalId, entityClass);

                    if (mappedId != null) {
                        setField(entity, "id", mappedId);
                        em.persist(entity);
                    } else {
                        setField(entity, "id", null);
                        em.persist(entity);
                        em.flush();
                    }

                    Object generatedId = JpaUtils.getJPAIdValue(entity);
                    if (originalId != null && generatedId != null) {
                        idMappings.computeIfAbsent(entityClass.getName(), k -> new HashMap<>())
                                .put(originalId, generatedId);
                    }
                    if (generatedId != null) {
                        for (ReferenceResolver.Ref pendingRef : resolved.pending()) {
                            run.pending.add(new PendingRef(entityClass, generatedId, pendingRef));
                        }
                    }

                    count.getAndIncrement();

                } catch (Exception e) {
                    if (options.isFailOnEntityError()) {
                        throw new MigrationException(
                                "Error persisting " + entityClass.getSimpleName(), e);
                    }
                    log.warn("[Migration/Import] Skipping entity due to error in {}: {}",
                            entityClass.getSimpleName(), e.getMessage());
                    log.debug("[Migration/Import] Stack trace:", e);
                }
            }
        });
        return count.get();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Entity deserialization
    // ─────────────────────────────────────────────────────────────────────────

    private Object deserializeEntity(JsonNode node,
                                     Class<?> entityClass,
                                     EntityType<?> entityType,
                                     Serializable targetAccountId,
                                     List<ReferenceResolver.Ref> refs) throws Exception {

        Object entity = entityClass.getDeclaredConstructor().newInstance();

        for (SingularAttribute<?, ?> attr : entityType.getSingularAttributes()) {
            String name = attr.getName();
            if ("id".equals(name)) continue;

            PersistentAttributeType pt = attr.getPersistentAttributeType();

            if ("accountId".equals(name)) {
                setField(entity, "accountId", targetAccountId);
                continue;
            }

            if (pt == PersistentAttributeType.MANY_TO_ONE
                    || pt == PersistentAttributeType.ONE_TO_ONE) {

                String refKey = name + ExportConstants.REF_ID_SUFFIX;
                JsonNode refIdNode = node.get(refKey);
                if (refIdNode != null && !refIdNode.isNull()) {
                    // Resolved later by the ReferenceResolver (needs the whole row to decide).
                    refs.add(new ReferenceResolver.Ref(name, attr.getJavaType(), refIdNode.asLong(),
                            attr.isOptional()));
                }

            } else if (pt != PersistentAttributeType.ONE_TO_MANY
                    && pt != PersistentAttributeType.MANY_TO_MANY
                    && pt != PersistentAttributeType.ELEMENT_COLLECTION) {

                JsonNode valueNode = node.get(name);
                if (valueNode != null && !valueNode.isNull()) {
                    try {
                        Object value = objectMapper.treeToValue(valueNode, attr.getJavaType());
                        setField(entity, name, value);
                    } catch (Exception e) {
                        log.debug("[Migration/Import] Could not set field {}={}: {}",
                                name, valueNode, e.getMessage());
                    }
                }
            }
        }

        return entity;
    }

    private void applyReferences(Object entity, List<ReferenceResolver.Ref> refs, Map<String, Object> ids) {
        for (ReferenceResolver.Ref ref : refs) {
            Object resolvedId = ids.get(ref.attribute());
            if (resolvedId == null) continue;
            try {
                Object proxy = em.getReference(ref.refClass(), coerceId(resolvedId, ref.refClass()));
                setField(entity, ref.attribute(), proxy);
            } catch (Exception e) {
                log.debug("[Migration/Import] Could not create reference proxy for {}={}: {}",
                        ref.attribute() + ExportConstants.REF_ID_SUFFIX, resolvedId, e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Second pass: deferred rows and pending references
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retries the rows held back because a mandatory parent was not mapped yet. Rounds repeat while they
     * make progress (a held row may wait for another held row); the last round runs in final phase, where
     * the parents that are still missing are known to have never been cloned.
     */
    private long resolveDeferredRows(ImportRun run,
                                     AccountImportOptions options,
                                     IdentityMapper identityMapper,
                                     Map<String, Map<Object, Object>> idMappings,
                                     MigrationProgressListener listener,
                                     CancellationToken token) {
        long total = 0;
        while (!run.deferred.isEmpty()) {
            if (token != null && token.isCancelled()) break;

            Map<Class<?>, List<JsonNode>> batch = run.deferred;
            run.deferred = new LinkedHashMap<>();
            int before = countRows(batch);
            log.info("[Migration/Import] Resolving {} deferred rows (cyclic or late parents)", before);
            if (listener != null) {
                listener.onProgress(MigrationProgress.of(0, 0,
                        "Resolving " + before + " rows with late references", total));
            }

            total += persistDeferred(batch, options, identityMapper, idMappings, run);

            int after = countRows(run.deferred);
            if (after > 0 && after >= before) {
                // No progress: whatever is missing will never exist. Decide with the final rules.
                batch = run.deferred;
                run.deferred = new LinkedHashMap<>();
                run.finalPhase = true;
                try {
                    total += persistDeferred(batch, options, identityMapper, idMappings, run);
                } finally {
                    run.finalPhase = false;
                }
            }
        }
        return total;
    }

    private long persistDeferred(Map<Class<?>, List<JsonNode>> batch,
                                 AccountImportOptions options,
                                 IdentityMapper identityMapper,
                                 Map<String, Map<Object, Object>> idMappings,
                                 ImportRun run) {
        int chunkSize = options.getChunkSize() > 0 ? options.getChunkSize() : properties.getChunkSize();
        long total = 0;
        for (Map.Entry<Class<?>, List<JsonNode>> e : batch.entrySet()) {
            List<JsonNode> rows = e.getValue();
            for (int i = 0; i < rows.size(); i += chunkSize) {
                List<JsonNode> chunk = new ArrayList<>(rows.subList(i, Math.min(rows.size(), i + chunkSize)));
                total += persistChunk(chunk, e.getKey(), options, identityMapper, idMappings, run);
            }
        }
        return total;
    }

    private static int countRows(Map<Class<?>, List<JsonNode>> rows) {
        return rows.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Patches the optional references that were persisted as {@code null} because their parent was not
     * mapped yet. Uses bulk updates by id so no entity is loaded; unresolved ones stay {@code null}.
     */
    private void applyPendingReferences(ImportRun run,
                                        AccountImportOptions options,
                                        MigrationProgressListener listener) {
        if (run.pending.isEmpty()) return;

        log.info("[Migration/Import] Patching {} late optional references", run.pending.size());
        if (listener != null) {
            listener.onProgress(MigrationProgress.of(0, 0,
                    "Patching " + run.pending.size() + " late references", 0));
        }

        int chunkSize = options.getChunkSize() > 0 ? options.getChunkSize() : properties.getChunkSize();
        var crud = DomainUtils.lookupCrudService();
        for (int i = 0; i < run.pending.size(); i += chunkSize) {
            List<PendingRef> slice = run.pending.subList(i, Math.min(run.pending.size(), i + chunkSize));
            crud.executeWithinTransaction(() -> {
                for (PendingRef p : slice) {
                    try {
                        ReferenceResolver.Resolution r = run.resolver.resolvePending(p.ref());
                        if (r.status() != ReferenceResolver.Status.RESOLVED) continue;

                        String entityName = emf.getMetamodel().entity(p.entityClass()).getName();
                        em.createQuery("UPDATE " + entityName + " e SET e." + p.ref().attribute()
                                        + " = :ref WHERE e.id = :id")
                                .setParameter("ref", em.getReference(p.ref().refClass(),
                                        coerceId(r.id(), p.ref().refClass())))
                                .setParameter("id", p.entityId())
                                .executeUpdate();
                    } catch (Exception e) {
                        if (options.isFailOnEntityError()) {
                            throw new MigrationException("Error patching reference "
                                    + p.entityClass().getSimpleName() + "." + p.ref().attribute(), e);
                        }
                        log.warn("[Migration/Import] Could not patch {}.{} of id={}: {}",
                                p.entityClass().getSimpleName(), p.ref().attribute(), p.entityId(), e.getMessage());
                    }
                }
            });
        }
        run.pending.clear();
    }

    /**
     * Whether the original row (of a class that is in the export but was not mapped) is shared by every
     * account ({@code accountId} 0 or null) and so must keep its id. A row that is gone or belongs to
     * another account (soft-deleted rows have a negative accountId) is an orphan. If the lookup fails,
     * the legacy behaviour (keep the id) is preferred over nulling data.
     */
    private boolean isSharedRow(Class<?> refClass, Object originalId) {
        EntityManager local = emf.createEntityManager();
        try {
            String entityName = emf.getMetamodel().entity(refClass).getName();
            List<?> result = local.createQuery("SELECT e.accountId FROM " + entityName + " e WHERE e.id = :id")
                    .setParameter("id", coerceId(originalId, refClass))
                    .getResultList();
            if (result.isEmpty()) return false;
            Object owner = result.get(0);
            return owner == null || (owner instanceof Number n && n.longValue() == 0L);
        } catch (Exception e) {
            log.warn("[Migration/Import] Could not check owner of {}#{}, keeping original id: {}",
                    refClass.getSimpleName(), originalId, e.getMessage());
            return true;
        } finally {
            local.close();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────────────────────────────────

    private JsonNode rowToObjectNode(JsonParser parser, List<String> fields) throws IOException {
        ObjectNode node = objectMapper.createObjectNode();
        int i = 0;
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (i < fields.size()) {
                node.set(fields.get(i), parser.readValueAsTree());
            } else {
                parser.skipChildren();
            }
            i++;
        }
        return node;
    }

    private void setField(Object entity, String fieldName, Object value) {
        Optional<Field> cached = fieldCache
                .computeIfAbsent(entity.getClass(), c -> new ConcurrentHashMap<>())
                .computeIfAbsent(fieldName, fn -> {
                    Field f = ReflectionUtils.findField(entity.getClass(), fn);
                    if (f != null) ReflectionUtils.makeAccessible(f);
                    return Optional.ofNullable(f);
                });
        cached.ifPresent(field -> {
            try {
                field.set(entity, value);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                log.debug("[Migration/Import] Cannot set field {}: {}", fieldName, e.getMessage());
            }
        });
    }

    private static Object readId(JsonNode node) {
        JsonNode idNode = node.get("id");
        if (idNode == null || idNode.isNull()) return null;
        if (idNode.isLong() || idNode.isInt()) return idNode.asLong();
        return idNode.asText();
    }

    private static Object coerceId(Object id, Class<?> refClass) {
        if (id instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
            }
            try {
                return UUID.fromString(s);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return id;
    }

    private IdentityMapper resolveIdentityMapper(AccountImportOptions options) {
        IdentityStrategy strategy = options.getIdentityStrategy();
        if (customMappers != null) {
            for (IdentityMapper mapper : customMappers) {
                if (mapper.getStrategy() == strategy) {
                    return mapper;
                }
            }
        }
        return switch (strategy) {
            case KEEP_IDS -> new KeepIdsIdentityMapper();
            case UUID7 -> new Uuid7IdentityMapper();
            default -> new RegenerateIdsIdentityMapper();
        };
    }

    private static void expectToken(JsonParser parser, JsonToken expected, String context)
            throws IOException {
        JsonToken actual = parser.currentToken();
        if (actual == null) {
            parser.nextToken();
            actual = parser.currentToken();
        }
        if (actual != expected) {
            throw new MigrationException(
                    "Expected %s for %s but got %s".formatted(expected, context, actual));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal utilities
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Per-import mutable state of the second pass. Single-threaded.
     */
    private static final class ImportRun {
        ReferenceResolver resolver;
        /** True only while retrying held rows in the last round. */
        boolean finalPhase;
        /** Rows (JSON only) with a mandatory parent that is not mapped yet, by entity class. */
        Map<Class<?>, List<JsonNode>> deferred = new LinkedHashMap<>();
        /** Optional references persisted as null, to patch once all entities exist. Ids only. */
        final List<PendingRef> pending = new ArrayList<>();
    }

    private record PendingRef(Class<?> entityClass, Object entityId, ReferenceResolver.Ref ref) {
    }

    /**
     * Wraps an {@link InputStream} and swallows {@link #close()} calls.
     * Used so that closing a {@link JsonParser} does not close the
     * underlying {@link ZipInputStream} between entries.
     */
    private static final class NoCloseInputStream extends FilterInputStream {
        NoCloseInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() {
            // intentionally a no-op — ZipInputStream lifecycle is managed by the caller
        }
    }
}
