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

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
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
import tools.dynamia.modules.saas.migration.api.CancellationToken;
import tools.dynamia.modules.saas.migration.api.IdentityMapper;
import tools.dynamia.modules.saas.migration.api.IdentityStrategy;
import tools.dynamia.modules.saas.migration.api.MigrationException;
import tools.dynamia.modules.saas.migration.api.MigrationProgress;
import tools.dynamia.modules.saas.migration.api.MigrationProgressListener;
import tools.dynamia.modules.saas.migration.api.AccountImportOptions;
import tools.dynamia.modules.saas.migration.config.AccountMigrationProperties;
import tools.dynamia.modules.saas.migration.identity.KeepIdsIdentityMapper;
import tools.dynamia.modules.saas.migration.identity.RegenerateIdsIdentityMapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

/**
 * Streaming import pipeline.
 *
 * <p>Reads the JSON export format produced by {@link ExportPipeline} using
 * Jackson's {@link JsonParser} in streaming mode. Entities are persisted in
 * topological order (guaranteed by the export format) using chunked transactions
 * so no single transaction loads the full dataset.
 *
 * <p>For each entity record:
 * <ol>
 *   <li>Read JSON object as a {@link JsonNode}.</li>
 *   <li>Instantiate the entity class via its no-arg constructor.</li>
 *   <li>Set primitive / embedded fields from the JSON node.</li>
 *   <li>Resolve {@code _ref_id} references via the running {@code idMappings}
 *       table using the configured {@link IdentityMapper}.</li>
 *   <li>Set {@code accountId} to the target account.</li>
 *   <li>Persist the entity; record original→new ID mapping.</li>
 * </ol>
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
     * @param input    export stream (auto-detected: GZIP or plain JSON)
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

        InputStream source;
        try {
            source = detectAndWrapGzip(input);
        } catch (IOException e) {
            throw new MigrationException("Failed to open input stream", e);
        }

        try (JsonParser parser = objectMapper.createParser(source)) {

            expectToken(parser, JsonToken.START_OBJECT, "root object");
            long totalProcessed = 0;
            long records = 0;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (token != null && token.isCancelled()) break;

                String fieldName = parser.currentName();
                parser.nextToken(); // move to value

                switch (fieldName) {
                    case ExportConstants.FIELD_VERSION,
                         ExportConstants.FIELD_EXPORTED_AT,
                         ExportConstants.FIELD_SOURCE_ACCOUNT_ID,
                         ExportConstants.FIELD_IDENTITY_STRATEGY -> {
                        // Read and discard header metadata (validated if needed in future)
                    }
                    case ExportConstants.FIELD_ACCOUNT -> {
                        parser.skipChildren(); // Account handled externally
                    }
                    case ExportConstants.FIELD_ENTITIES -> {
                        totalProcessed = importEntitiesSection(parser, options, identityMapper, idMappings, listener, token);
                    }
                    default -> parser.skipChildren();
                }
            }

            if (listener != null) {
                records += totalProcessed;
                listener.onProgress(new MigrationProgress(
                        totalProcessed, totalProcessed, "Import complete", records));
            }

        } catch (IOException e) {
            throw new MigrationException("Import failed while reading JSON stream", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Entities section
    // ─────────────────────────────────────────────────────────────────────────

    private long importEntitiesSection(JsonParser parser,
                                       AccountImportOptions options,
                                       IdentityMapper identityMapper,
                                       Map<String, Map<Object, Object>> idMappings,
                                       MigrationProgressListener listener,
                                       CancellationToken token) throws IOException {

        expectToken(parser, JsonToken.START_OBJECT, "entities object");
        long total = 0;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (token != null && token.isCancelled()) {
                log.info("[Migration/Import] Cancelled");
                break;
            }

            String entityClassName = parser.currentName();
            parser.nextToken(); // START_ARRAY

            try {
                Class<?> entityClass = Class.forName(entityClassName);
                long count = importEntityArray(
                        parser, entityClass, options, identityMapper, idMappings, listener, token);
                total += count;
                log.info("[Migration/Import] Imported {} records for {}", count, entityClass.getSimpleName());

                if (listener != null) {
                    listener.onProgress(new MigrationProgress(total, 0,
                            "Imported " + entityClass.getSimpleName() + " (" + count + " records)",total));
                }

            } catch (ClassNotFoundException e) {
                log.warn("[Migration/Import] Entity class not found in classpath, skipping: {}", entityClassName);
                parser.skipChildren();
            }
        }

        return total;
    }

    private long importEntityArray(JsonParser parser,
                                   Class<?> entityClass,
                                   AccountImportOptions options,
                                   IdentityMapper identityMapper,
                                   Map<String, Map<Object, Object>> idMappings,
                                   MigrationProgressListener listener,
                                   CancellationToken token) throws IOException {

        // v2 columnar format: each entity section is an object with "fields" and "rows"
        expectToken(parser, JsonToken.START_OBJECT, "entity section for " + entityClass.getSimpleName());

        int chunkSize = options.getChunkSize() > 0 ? options.getChunkSize() : properties.getChunkSize();
        List<JsonNode> chunk = new ArrayList<>(chunkSize);
        List<String> fields = null;
        long total = 0;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String sectionName = parser.currentName();
            parser.nextToken(); // move to value

            if (ExportConstants.FIELD_FIELDS.equals(sectionName)) {
                expectToken(parser, JsonToken.START_ARRAY, "fields array for " + entityClass.getSimpleName());
                fields = new ArrayList<>();
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    fields.add(parser.getText());
                }

            } else if (ExportConstants.FIELD_ROWS.equals(sectionName)) {
                if (fields == null) {
                    throw new MigrationException("'rows' section appeared before 'fields' for " + entityClass.getName());
                }
                expectToken(parser, JsonToken.START_ARRAY, "rows array for " + entityClass.getSimpleName());
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    if (token != null && token.isCancelled()) break;

                    JsonNode node = rowToObjectNode(parser, fields);
                    chunk.add(node);

                    if (chunk.size() >= chunkSize) {
                        total += persistChunk(chunk, entityClass, options, identityMapper, idMappings);
                        chunk.clear();
                    }
                }
                if (!chunk.isEmpty()) {
                    total += persistChunk(chunk, entityClass, options, identityMapper, idMappings);
                }

            } else {
                parser.skipChildren();
            }
        }

        return total;
    }

    private JsonNode rowToObjectNode(JsonParser parser, List<String> fields) throws IOException {
        // parser is positioned at the START_ARRAY token for this row
        ObjectNode node = objectMapper.createObjectNode();
        int i = 0;
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (i < fields.size()) {
                node.set(fields.get(i), objectMapper.readTree(parser));
            } else {
                parser.skipChildren();
            }
            i++;
        }
        return node;
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
                    // KEEP_IDS: set the original ID before persisting
                    setField(entity, "id", mappedId);
                    em.persist(entity);
                } else {
                    // REGENERATE_IDS: clear ID and let JPA assign a new one
                    setField(entity, "id", null);
                    em.persist(entity);
                    em.flush(); // force ID generation
                }

                // Record mapping for downstream reference resolution
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
                                     Long targetAccountId,
                                     IdentityMapper identityMapper,
                                     Map<String, Map<Object, Object>> idMappings) throws Exception {

        Object entity = entityClass.getDeclaredConstructor().newInstance();

        for (SingularAttribute<?, ?> attr : entityType.getSingularAttributes()) {
            String name = attr.getName();
            if ("id".equals(name)) continue; // handled outside

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
            } catch (IllegalAccessException e) {
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
        // If id is a String that looks like a Long, convert it
        if (id instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return id;
    }

    private static InputStream detectAndWrapGzip(InputStream in) throws IOException {
        if (!in.markSupported()) {
            return in; // cannot detect — use as-is
        }
        in.mark(2);
        int b1 = in.read();
        int b2 = in.read();
        in.reset();
        if (b1 == 0x1f && b2 == 0x8b) {
            return new GZIPInputStream(in);
        }
        return in;
    }

    private IdentityMapper resolveIdentityMapper(AccountImportOptions options) {
        IdentityStrategy strategy = options.getIdentityStrategy();
        if (strategy == IdentityStrategy.UUID7) {
            throw new MigrationException(
                    "IdentityStrategy.UUID7 is not yet supported (planned for v3). " +
                            "Use KEEP_IDS or REGENERATE_IDS.");
        }
        if (customMappers != null) {
            for (IdentityMapper mapper : customMappers) {
                if (mapper.getStrategy() == strategy) {
                    return mapper;
                }
            }
        }
        return switch (strategy) {
            case KEEP_IDS -> new KeepIdsIdentityMapper();
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
}



