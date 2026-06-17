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
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.domain.jpa.JpaCrudService;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JsonGenerator;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ReflectionUtils;
import tools.dynamia.domain.jpa.JpaUtils;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.saas.api.ExportIgnore;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.migration.api.CancellationToken;
import tools.dynamia.modules.saas.migration.api.MigrationException;
import tools.dynamia.modules.saas.migration.api.MigrationProgress;
import tools.dynamia.modules.saas.migration.api.MigrationProgressListener;
import tools.dynamia.modules.saas.migration.api.AccountExportOptions;
import tools.dynamia.modules.saas.migration.config.AccountMigrationProperties;
import tools.dynamia.modules.saas.migration.discovery.AccountEntityDiscovery;
import tools.dynamia.modules.saas.migration.graph.EntityDependencyGraph;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;

/**
 * Streaming export pipeline.
 *
 * <p>Writes tenant data to an {@link OutputStream} using Jackson's
 * {@link JsonGenerator}. Entities are processed in topological order, paginated
 * by chunks so that RAM usage is bounded regardless of dataset size.
 *
 * <h3>Serialization rules per attribute type</h3>
 * <ul>
 *   <li>{@code BASIC} / {@code EMBEDDED} — value written directly.</li>
 *   <li>{@code MANY_TO_ONE} / {@code ONE_TO_ONE} — written as
 *       {@code {fieldName}_ref_id: <pk>}.</li>
 *   <li>{@code ONE_TO_MANY} / {@code MANY_TO_MANY} — skipped; child entities
 *       include their own references back to the parent.</li>
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
     * Exports all tenant data for {@code accountId} to {@code output}.
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
            logger.info("[Migration/Export] Filtered entities based on options, {} remaining", candidates.size());
        }

        List<Class<?>> ordered = dependencyGraph.topologicalSort(candidates);

        long totalEntities = ordered.size() + 1; // number of entity types

        OutputStream target;
        try {
            target = options.isCompressionEnabled() ? new GZIPOutputStream(output) : output;
        } catch (IOException e) {
            throw new MigrationException("Failed to set up output stream", e);
        }

        try (JsonGenerator gen = objectMapper.createGenerator(target)) {
            logger.info("[Migration/Export] Writing export data for accountId={} with {} entity types", accountId, ordered.size());
            gen.writeStartObject();
            gen.writeStringProperty(ExportConstants.FIELD_VERSION, ExportConstants.FORMAT_VERSION);
            gen.writeStringProperty(ExportConstants.FIELD_EXPORTED_AT, LocalDateTime.now().toString());
            gen.writeNumberProperty(ExportConstants.FIELD_SOURCE_ACCOUNT_ID, accountId);
            gen.writeStringProperty(ExportConstants.FIELD_IDENTITY_STRATEGY,
                    options.getIdentityStrategy().name());

            // Serialize AccountDTO as the tenant descriptor
            gen.writeName(ExportConstants.FIELD_ACCOUNT);
            objectMapper.writeValue(gen, account.toDTO());

            // Entity section
            gen.writeName(ExportConstants.FIELD_ENTITIES);
            gen.writeStartObject();

            long processed = 0;
            long records = 0;
            for (Class<?> entityClass : ordered) {
                if (token != null && token.isCancelled()) {
                    logger.info("[Migration/Export] Cancelled at entity: {}", entityClass.getSimpleName());
                    break;
                }

                var count = exportEntityType(gen, entityClass, accountId, options, token);
                records += count;
                processed++;

                if (listener != null) {
                    listener.onProgress(new MigrationProgress(processed, totalEntities,
                            "Exported " + entityClass.getSimpleName() + " (" + count + " records)", records)
                    );
                }
            }

            gen.writeEndObject(); // entities
            gen.writeEndObject(); // root

            if (options.isCompressionEnabled()) {
                // Flush the GZIP stream
                ((GZIPOutputStream) target).finish();
            }

        } catch (IOException e) {
            throw new MigrationException("Export failed while writing JSON stream", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Describes one exported column. The {@link Field} is resolved and made accessible once
     * (in {@link #buildColumns}) and cached — never looked up again per row.
     */
    private record ColumnDef(String columnName, Field field, PersistentAttributeType type) {
    }

    private long exportEntityType(JsonGenerator gen, Class<?> entityClass, Long accountId,
                                  AccountExportOptions options, CancellationToken token)
            throws IOException {

        gen.writeName(entityClass.getName());
        gen.writeStartObject();

        try {
            EntityType<?> entityType = emf.getMetamodel().entity(entityClass);
            List<ColumnDef> columns = buildColumns(entityType);

            // Write column header
            gen.writeName(ExportConstants.FIELD_FIELDS);
            gen.writeStartArray();
            for (ColumnDef col : columns) {
                gen.writeString(col.columnName());
            }
            gen.writeEndArray();

            gen.writeName(ExportConstants.FIELD_ROWS);
            gen.writeStartArray();

            long processed = 0;

            if (!Account.class.equals(entityClass)) {
                logger.info("Reading entity {} data", entityClass);
                int chunkSize = resolveChunkSize(options);
                EntityManager em = (EntityManager) crudService.getDelgate();
                QueryParameters qp = QueryParameters.with("accountId", accountId)
                        .paginate(chunkSize)
                        .setHint(JpaCrudService.HINT_FETCH_GRAPH, em.createEntityGraph(entityClass))
                        .setReadOnly(true)
                        .orderBy("id", true);

                @SuppressWarnings("unchecked")
                List<Object> entities = (List<Object>) crudService.findReadOnly(entityClass, qp);
                if (entities != null && !entities.isEmpty()) {
                    logger.info("Exporting {} records of type {} for accountId={}", entities.size(), entityClass.getSimpleName(), accountId);
                    for (Object entity : entities) {
                        if (token != null && token.isCancelled()) break;
                        if (entity != null) {
                            writeEntityRow(gen, entity, columns);
                        }
                        processed++;
                    }
                    em.clear();
                }
            }

            gen.writeEndArray(); // rows
            logger.debug("[Migration/Export] {} records exported for {}", processed, entityClass.getSimpleName());
            gen.writeEndObject();
            return processed;

        } catch (IllegalArgumentException e) {
            logger.warn("[Migration/Export] Entity not in JPA metamodel, skipping: {}", entityClass.getName());
            // Still close the object cleanly with empty fields/rows
            gen.writeName(ExportConstants.FIELD_FIELDS);
            gen.writeStartArray();
            gen.writeEndArray();
            gen.writeName(ExportConstants.FIELD_ROWS);
            gen.writeStartArray();
            gen.writeEndArray();
            gen.writeEndObject();
            return 0;
        }
    }

    /**
     * Builds and caches the column list for an entity class.
     * Fields are resolved and made accessible here — once per entity class per JVM lifetime.
     */
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
                Object value = col.field().get(entity); // Field already accessible — no lookup here

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

}



