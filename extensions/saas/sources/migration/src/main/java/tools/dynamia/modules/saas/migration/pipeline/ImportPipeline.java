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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import tools.dynamia.domain.jpa.JpaUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
 * resolution via the {@code idMappings} table always finds the parent
 * already persisted.
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

        while ((entry = zipIn.getNextEntry()) != null) {
            if (token != null && token.isCancelled()) {
                log.info("[Migration/Import] Cancelled");
                break;
            }

            String name = entry.getName();

            if (ExportConstants.MANIFEST_FILE.equals(name)) {
                logManifestInfo(zipIn);
            } else if (name.endsWith(".json")) {
                long count = importZipEntityEntry(zipIn, options, identityMapper, idMappings, listener, token);
                total += count;
            }

            zipIn.closeEntry();
        }

        if (listener != null) {
            listener.onProgress(MigrationProgress.of(total, total, "Import complete", total));
        }
        log.info("[Migration/Import] ZIP import complete — {} total records", total);
    }

    /**
     * Reads and logs metadata from manifest.json without loading it into memory.
     */
    private void logManifestInfo(ZipInputStream zipIn) {
        try {
            JsonParser parser = objectMapper.createParser(new NoCloseInputStream(zipIn));
            // Read only version and sourceAccountId for logging; skip everything else
            String version = null;
            String sourceAccountId = null;
            if (parser.nextToken() == JsonToken.START_OBJECT) {
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String field = parser.currentName();
                    parser.nextToken();
                    switch (field) {
                        case ExportConstants.FIELD_VERSION -> version = parser.getText();
                        case ExportConstants.FIELD_SOURCE_ACCOUNT_ID -> sourceAccountId = parser.getValueAsString();
                        default -> parser.skipChildren();
                    }
                    if (version != null && sourceAccountId != null) break;
                }
            }
            parser.close();
            log.info("[Migration/Import] manifest: version={}, sourceAccountId={}", version, sourceAccountId);
        } catch (Exception e) {
            log.debug("[Migration/Import] Could not read manifest metadata: {}", e.getMessage());
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
                                      MigrationProgressListener listener,
                                      CancellationToken token) throws IOException {

        JsonParser parser = objectMapper.createParser(new NoCloseInputStream(zipIn));
        try {
            return parseEntityEntry(parser, options, identityMapper, idMappings, listener, token);
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
                            identityMapper, idMappings, listener, token, chunkSize);
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
                total += persistChunk(chunk, entityClass, options, identityMapper, idMappings);
                chunk.clear();
            }
        }

        if (!chunk.isEmpty()) {
            total += persistChunk(chunk, entityClass, options, identityMapper, idMappings);
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int persistChunk(List<JsonNode> chunk,
                            Class<?> entityClass,
                            AccountImportOptions options,
                            IdentityMapper identityMapper,
                            Map<String, Map<Object, Object>> idMappings) {
        int count = 0;
        EntityType<?> entityType;
        try {
            entityType = emf.getMetamodel().entity(entityClass);
        } catch (IllegalArgumentException e) {
            log.warn("[Migration/Import] Entity not in JPA metamodel, skipping: {}", entityClass.getName());
            return 0;
        }

        for (JsonNode node : chunk) {
            try {
                Object entity = deserializeEntity(node, entityClass, entityType,
                        options.getTargetAccountId(), identityMapper, idMappings);

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

                count++;

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
        return count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Entity deserialization
    // ─────────────────────────────────────────────────────────────────────────

    private Object deserializeEntity(JsonNode node,
                                     Class<?> entityClass,
                                     EntityType<?> entityType,
                                     Serializable targetAccountId,
                                     IdentityMapper identityMapper,
                                     Map<String, Map<Object, Object>> idMappings) throws Exception {

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
                    Object originalRefId = refIdNode.asLong();
                    Class<?> refClass = attr.getJavaType();
                    Object resolvedId = identityMapper.resolveReferenceId(
                            originalRefId, refClass, idMappings);
                    if (resolvedId != null) {
                        try {
                            Object ref = em.getReference(refClass, coerceId(resolvedId, refClass));
                            setField(entity, name, ref);
                        } catch (Exception e) {
                            log.debug("[Migration/Import] Could not create reference proxy for {}={}: {}",
                                    refKey, resolvedId, e.getMessage());
                        }
                    }
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
