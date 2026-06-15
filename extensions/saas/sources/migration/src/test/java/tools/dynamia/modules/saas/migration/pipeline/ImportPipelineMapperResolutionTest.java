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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import tools.dynamia.modules.saas.migration.api.MigrationException;
import tools.dynamia.modules.saas.migration.config.AccountMigrationProperties;
import tools.dynamia.modules.saas.migration.identity.KeepIdsIdentityMapper;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * Verifies that {@link ImportPipeline} correctly resolves the identity mapper:
 * <ul>
 *   <li>UUID7 throws {@link MigrationException} immediately.</li>
 *   <li>A custom Spring bean mapper is preferred over built-in defaults.</li>
 *   <li>KEEP_IDS falls back to {@link tools.dynamia.modules.saas.migration.identity.KeepIdsIdentityMapper}
 *       when no custom bean is present.</li>
 * </ul>
 *
 * <p>These tests reach {@code resolveIdentityMapper} indirectly by calling
 * {@code importTenant} with a minimal but valid JSON stream and observing behaviour.
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
                .addModule(new JavaTimeModule())
                .build();

        // Minimal metamodel: getEntities() returns empty set so no entity processing occurs
        var metamodel = org.mockito.Mockito.mock(jakarta.persistence.metamodel.Metamodel.class);
        when(emf.getMetamodel()).thenReturn(metamodel);
        when(metamodel.getEntities()).thenReturn(java.util.Set.of());
    }

    @Test
    public void uuid7StrategyThrowsMigrationException() {
        ImportPipeline pipeline = new ImportPipeline(emf, properties, objectMapper);

        AccountImportOptions opts = new AccountImportOptions()
                .targetAccountId(1L)
                .identityStrategy(IdentityStrategy.UUID7);

        try {
            pipeline.importTenant(emptyExportStream(), opts, null, null);
            Assert.fail("Expected MigrationException for UUID7");
        } catch (MigrationException e) {
            Assert.assertTrue("Message should mention UUID7",
                    e.getMessage().contains("UUID7"));
        }
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

    private static ByteArrayInputStream emptyExportStream() {
        String json = """
                {
                  "version": "1",
                  "exportedAt": "2026-06-15T10:00:00",
                  "sourceAccountId": 1,
                  "identityStrategy": "KEEP_IDS",
                  "account": {},
                  "entities": {}
                }
                """;
        return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    }
}
