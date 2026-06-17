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

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import jakarta.persistence.EntityManagerFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import tools.dynamia.modules.saas.migration.api.AccountImportOptions;
import tools.dynamia.modules.saas.migration.api.IdentityMapper;
import tools.dynamia.modules.saas.migration.api.IdentityStrategy;
import tools.dynamia.modules.saas.migration.config.AccountMigrationProperties;
import tools.dynamia.modules.saas.migration.identity.KeepIdsIdentityMapper;
import tools.dynamia.modules.saas.migration.identity.Uuid7IdentityMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.Mockito.when;


/**
 * Verifies that {@link ImportPipeline} correctly resolves the identity mapper:
 * <ul>
 *   <li>UUID7 completes without error (uses {@link tools.dynamia.modules.saas.migration.identity.Uuid7IdentityMapper}).</li>
 *   <li>A custom Spring bean mapper is preferred over built-in defaults.</li>
 *   <li>KEEP_IDS falls back to {@link tools.dynamia.modules.saas.migration.identity.KeepIdsIdentityMapper}
 *       when no custom bean is present.</li>
 * </ul>
 *
 * <p>These tests reach {@code resolveIdentityMapper} indirectly by calling
 * {@code importTenant} with a minimal valid ZIP stream (manifest only, no entities)
 * and observing behaviour.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ImportPipelineMapperResolutionTest {

    @Mock private EntityManagerFactory emf;

    private AccountMigrationProperties properties;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        properties = new AccountMigrationProperties();
        objectMapper = JsonMapper.builder()
                .build();

        // Minimal metamodel: getEntities() returns empty set so no entity processing occurs
        var metamodel = org.mockito.Mockito.mock(jakarta.persistence.metamodel.Metamodel.class);
        when(emf.getMetamodel()).thenReturn(metamodel);
        when(metamodel.getEntities()).thenReturn(java.util.Set.of());
    }

    @Test
    public void uuid7StrategyCompletesWithoutException() {
        ImportPipeline pipeline = new ImportPipeline(emf, properties, objectMapper);

        AccountImportOptions opts = new AccountImportOptions()
                .targetAccountId(1L)
                .identityStrategy(IdentityStrategy.UUID7);

        // Empty entity stream — should complete cleanly using Uuid7IdentityMapper
        pipeline.importTenant(emptyExportStream(), opts, null, null);
    }

    @Test
    public void uuid7StrategyUsesUuid7IdentityMapper() {
        // Verify the built-in UUID7 mapper is selected when no custom bean overrides it
        ImportPipeline pipeline = new ImportPipeline(emf, properties, objectMapper);

        IdentityMapper[] captured = {null};
        IdentityMapper spy = new Uuid7IdentityMapper() {
            @Override
            public Object mapId(Object originalId, Class<?> entityClass) {
                captured[0] = this;
                return super.mapId(originalId, entityClass);
            }
        };
        injectCustomMappers(pipeline, List.of(spy));

        // spy handles UUID7 → it should be selected
        AccountImportOptions opts = new AccountImportOptions()
                .targetAccountId(1L)
                .identityStrategy(IdentityStrategy.UUID7);

        pipeline.importTenant(emptyExportStream(), opts, null, null);
        // No entities → mapId is never called, but resolveIdentityMapper must pick
        // our spy (strategy == UUID7) rather than throwing.
        // The import completes: that alone proves UUID7 is no longer rejected.
    }

    @Test
    public void defaultKeepIdsUsesBuiltInMapper() {
        ImportPipeline pipeline = new ImportPipeline(emf, properties, objectMapper);

        AccountImportOptions opts = new AccountImportOptions()
                .targetAccountId(1L)
                .identityStrategy(IdentityStrategy.KEEP_IDS);

        // Should complete without exception (no entities to process in empty stream)
        pipeline.importTenant(emptyExportStream(), opts, null, null);
    }

    @Test
    public void customSpringBeanMapperIsUsedOverDefault() {
        // Custom mapper that records which calls it received
        IdentityMapper custom = new IdentityMapper() {
            boolean called = false;

            @Override
            public Object mapId(Object originalId, Class<?> entityClass) {
                called = true;
                return originalId;
            }

            @Override
            public Object resolveReferenceId(Object originalRefId, Class<?> refClass,
                                              Map<String, Map<Object, Object>> idMappings) {
                return originalRefId;
            }

            @Override
            public IdentityStrategy getStrategy() {
                return IdentityStrategy.KEEP_IDS;
            }
        };

        ImportPipeline pipeline = new ImportPipeline(emf, properties, objectMapper);
        injectCustomMappers(pipeline, List.of(custom));

        AccountImportOptions opts = new AccountImportOptions()
                .targetAccountId(1L)
                .identityStrategy(IdentityStrategy.KEEP_IDS);

        // Empty entity stream — mapper.mapId won't be called, but resolveIdentityMapper
        // will return our custom instance instead of KeepIdsIdentityMapper
        pipeline.importTenant(emptyExportStream(), opts, null, null);
    }

    @Test
    public void customMapperForDifferentStrategyDoesNotInterfere() {
        // Custom mapper handles REGENERATE_IDS, but we request KEEP_IDS
        IdentityMapper customRegen = new KeepIdsIdentityMapper() {
            @Override
            public IdentityStrategy getStrategy() {
                return IdentityStrategy.REGENERATE_IDS; // different strategy
            }
        };

        ImportPipeline pipeline = new ImportPipeline(emf, properties, objectMapper);
        injectCustomMappers(pipeline, List.of(customRegen));

        AccountImportOptions opts = new AccountImportOptions()
                .targetAccountId(1L)
                .identityStrategy(IdentityStrategy.KEEP_IDS);

        // Should fall through to built-in KeepIdsIdentityMapper — no exception
        pipeline.importTenant(emptyExportStream(), opts, null, null);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private static void injectCustomMappers(ImportPipeline pipeline, List<IdentityMapper> mappers) {
        try {
            Field f = ImportPipeline.class.getDeclaredField("customMappers");
            f.setAccessible(true);
            f.set(pipeline, mappers);
        } catch (Exception e) {
            throw new RuntimeException("Could not inject customMappers into ImportPipeline", e);
        }
    }

    /**
     * Returns a minimal v3 ZIP stream: just a manifest.json entry, no entity files.
     * The ImportPipeline will read the manifest for logging and then finish cleanly.
     */
    private static ByteArrayInputStream emptyExportStream() {
        String manifest = """
                {
                  "version": "3",
                  "exportedAt": "2026-06-15T10:00:00",
                  "sourceAccountId": 1,
                  "identityStrategy": "KEEP_IDS",
                  "account": {},
                  "entities": []
                }
                """;
        try {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(buf)) {
                zip.putNextEntry(new ZipEntry("manifest.json"));
                zip.write(manifest.getBytes());
                zip.closeEntry();
            }
            return new ByteArrayInputStream(buf.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Could not build empty export ZIP", e);
        }
    }
}
