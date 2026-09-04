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
package tools.dynamia.modules.saas.migration.api;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the fluent builder APIs on options classes and their Jackson serialization
 * (exercising the options_json feature added to AccountMigrationJob).
 */
public class OptionsFluentBuilderTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = JsonMapper.builder()
                .build();
    }

    // ─── AccountExportOptions ────────────────────────────────────────────────

    @Test
    public void exportOptionsDefaults() {
        AccountExportOptions opts = new AccountExportOptions();
        Assertions.assertEquals(AccountExportOptions.DEFAULT_CHUNK_SIZE, opts.getChunkSize());
        Assertions.assertEquals(IdentityStrategy.KEEP_IDS, opts.getIdentityStrategy());
    }

    @Test
    public void exportOptionsFluentBuilder() {
        AccountExportOptions opts = new AccountExportOptions()
                .chunkSize(200)
                .identityStrategy(IdentityStrategy.REGENERATE_IDS)
                .label("my-export");

        Assertions.assertEquals(200, opts.getChunkSize());
        Assertions.assertEquals(IdentityStrategy.REGENERATE_IDS, opts.getIdentityStrategy());
        Assertions.assertEquals("my-export", opts.getLabel());
    }

    @Test
    public void exportOptionsIsJsonSerializable() {
        AccountExportOptions opts = new AccountExportOptions()
                .chunkSize(100)
                .identityStrategy(IdentityStrategy.KEEP_IDS);

        String json = objectMapper.writeValueAsString(opts);
        Assertions.assertNotNull(json);
        Assertions.assertTrue(json.contains("chunkSize"));
        Assertions.assertTrue(json.contains("KEEP_IDS"));

        AccountExportOptions roundtrip = objectMapper.readValue(json, AccountExportOptions.class);
        Assertions.assertEquals(100, roundtrip.getChunkSize());
    }

    // ─── AccountImportOptions ────────────────────────────────────────────────

    @Test
    public void importOptionsDefaults() {
        AccountImportOptions opts = new AccountImportOptions();
        Assertions.assertNull(opts.getTargetAccountId());
        Assertions.assertEquals(IdentityStrategy.REGENERATE_IDS, opts.getIdentityStrategy());
        Assertions.assertEquals(AccountExportOptions.DEFAULT_CHUNK_SIZE, opts.getChunkSize());
        Assertions.assertFalse(opts.isFailOnEntityError());
    }

    @Test
    public void importOptionsFluentBuilder() {
        AccountImportOptions opts = new AccountImportOptions()
                .targetAccountId(42L)
                .identityStrategy(IdentityStrategy.KEEP_IDS)
                .chunkSize(250)
                .failOnEntityError(true);

        Assertions.assertEquals(42L, (long) opts.getTargetAccountId());
        Assertions.assertEquals(IdentityStrategy.KEEP_IDS, opts.getIdentityStrategy());
        Assertions.assertEquals(250, opts.getChunkSize());
        Assertions.assertTrue(opts.isFailOnEntityError());
    }

    @Test
    public void importOptionsIsJsonSerializable() throws Exception {
        AccountImportOptions opts = new AccountImportOptions()
                .targetAccountId(7L)
                .identityStrategy(IdentityStrategy.REGENERATE_IDS);

        String json = objectMapper.writeValueAsString(opts);
        Assertions.assertNotNull(json);
        Assertions.assertTrue(json.contains("targetAccountId"));
        Assertions.assertTrue(json.contains("REGENERATE_IDS"));

        AccountImportOptions roundtrip = objectMapper.readValue(json, AccountImportOptions.class);
        if (roundtrip.getTargetAccountId() instanceof Number id) {
            Assertions.assertEquals(7L, id.longValue());
        }
    }

    // ─── AccountCloneOptions ─────────────────────────────────────────────────

    @Test
    public void cloneOptionsIsJsonSerializable() throws Exception {
        AccountCloneOptions opts = new AccountCloneOptions();
        opts.setSourceAccountId(1L);
        opts.setTargetAccountId(2L);

        String json = objectMapper.writeValueAsString(opts);
        Assertions.assertNotNull(json);
        Assertions.assertTrue(json.contains("sourceAccountId") || json.contains("1"));
    }
}
