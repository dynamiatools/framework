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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import tools.dynamia.domain.jpa.JpaUtils;
import tools.dynamia.domain.query.DataPaginator;
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
import java.util.List;
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
            for (Class<?> entityClass : ordered) {
                if (token != null && token.isCancelled()) {
                    logger.info("[Migration/Export] Cancelled at entity: {}", entityClass.getSimpleName());
                    break;
                }

                var count = exportEntityType(gen, entityClass, accountId, options, token);
                processed++;

                if (listener != null) {
                    listener.onProgress(new MigrationProgress(processed, totalEntities,
                            "Exported " + entityClass.getSimpleName() + " (" + count + " records)"));
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

    private long exportEntityType(JsonGenerator gen, Class<?> entityClass, Long accountId,
                                  AccountExportOptions options, CancellationToken token)
            throws IOException {

        long processed = 0;
        int chunkSize = resolveChunkSize(options);

        gen.writeName(entityClass.getName());
        gen.writeStartArray();

        try {
            EntityType<?> entityType = emf.getMetamodel().entity(entityClass);

            if (Account.class.equals(entityClass)) {
                // Account is already serialized in the header; produce an empty array here
                // to keep the format consistent (importers can find it if needed)
                gen.writeEndArray();
                return 0;
            }


            logger.info("Reading entity {} data", entityClass);
            EntityManager em = (EntityManager) crudService.getDelgate();
            QueryParameters qp = QueryParameters.with("accountId", accountId)
                    .paginate(chunkSize)
                    .setHint(JpaCrudService.HINT_FETCH_GRAPH,em.createEntityGraph(entityClass))
                    .orderBy("id", true);

            @SuppressWarnings("unchecked")
            List<Object> entities = (List<Object>) crudService.find(entityClass, qp);
            if (entities != null && !entities.isEmpty()) {
                logger.info("Exporting {} records of type {} for accountId={}", entities.size(), entityClass.getSimpleName(), accountId);
                for (Object entity : entities) {
                    if (token != null && token.isCancelled()) break;
                    if (entity != null) {
                        writeEntity(gen, entity, entityType);
                    }
                    processed++;
                }
            }
        } catch (IllegalArgumentException e) {
            logger.warn("[Migration/Export] Entity not in JPA metamodel, writing raw: {}", entityClass.getName());
        }

        gen.writeEndArray();
        logger.debug("[Migration/Export] {} records exported for {}", processed, entityClass.getSimpleName());
        return processed;
    }

    private void writeEntity(JsonGenerator gen, Object entity, EntityType<?> entityType)
            throws IOException {
        gen.writeStartObject();

        // Write ID explicitly
        try {
            Serializable id = JpaUtils.getJPAIdValue(entity);
            gen.writeName("id");
            gen.writePOJO(id);
        } catch (Exception e) {
            logger.debug("[Migration/Export] Could not write ID for {}", entity.getClass().getSimpleName());
        }

        // Write all singular attributes
        for (SingularAttribute<?, ?> attr : entityType.getSingularAttributes()) {
            String name = attr.getName();
            if ("id".equals(name)) continue; // already written

            // Skip fields annotated with @ExportIgnore
            if (hasExportIgnore(entityType.getJavaType(), name)) continue;

            try {
                Field field = findField(entityType.getJavaType(), name);
                if (field == null) continue;
                field.setAccessible(true);
                Object value = field.get(entity);

                PersistentAttributeType pt = attr.getPersistentAttributeType();

                if (pt == PersistentAttributeType.MANY_TO_ONE
                        || pt == PersistentAttributeType.ONE_TO_ONE) {
                    if (value != null) {
                        Serializable refId = JpaUtils.getJPAIdValue(value);
                        gen.writeName(name + ExportConstants.REF_ID_SUFFIX);
                        gen.writePOJO(refId);
                    }
                } else if (pt == PersistentAttributeType.ONE_TO_MANY
                        || pt == PersistentAttributeType.MANY_TO_MANY
                        || pt == PersistentAttributeType.ELEMENT_COLLECTION) {
                    // Skip collections — they are reconstructed via child entities
                } else {
                    // BASIC or EMBEDDED
                    gen.writeName(name);
                    objectMapper.writeValue(gen, value);
                }

            } catch (IllegalAccessException e) {
                logger.debug("[Migration/Export] Cannot access field {} on {}: {}",
                        name, entityType.getJavaType().getSimpleName(), e.getMessage());
            } catch (Exception e) {
                logger.debug("[Migration/Export] Skipping field {} due to: {}", name, e.getMessage());
            }
        }

        gen.writeEndObject();
    }

    private boolean hasExportIgnore(Class<?> clazz, String fieldName) {
        Field field = findField(clazz, fieldName);
        return field != null && field.isAnnotationPresent(ExportIgnore.class);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private int resolveChunkSize(AccountExportOptions options) {
        int size = options.getChunkSize();
        return size > 0 ? size : properties.getChunkSize();
    }

}



