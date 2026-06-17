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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the fluent builder APIs on options classes and their Jackson serialization
 * (exercising the options_json feature added to AccountMigrationJob).
 */
public class OptionsFluentBuilderTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = JsonMapper.builder()
                .build();
    }

    // ─── AccountExportOptions ────────────────────────────────────────────────

    @Test
    public void exportOptionsDefaults() {
        AccountExportOptions opts = new AccountExportOptions();
        Assert.assertEquals(AccountExportOptions.DEFAULT_CHUNK_SIZE, opts.getChunkSize());
        Assert.assertFalse(opts.isCompressionEnabled());
        Assert.assertEquals(IdentityStrategy.KEEP_IDS, opts.getIdentityStrategy());
    }

    @Test
    public void exportOptionsFluentBuilder() {
        AccountExportOptions opts = new AccountExportOptions()
                .chunkSize(200)
                .compressionEnabled(true)
                .identityStrategy(IdentityStrategy.REGENERATE_IDS)
                .label("my-export");

        Assert.assertEquals(200, opts.getChunkSize());
        Assert.assertTrue(opts.isCompressionEnabled());
        Assert.assertEquals(IdentityStrategy.REGENERATE_IDS, opts.getIdentityStrategy());
        Assert.assertEquals("my-export", opts.getLabel());
    }

    @Test
    public void exportOptionsIsJsonSerializable() throws Exception {
        AccountExportOptions opts = new AccountExportOptions()
                .chunkSize(100)
                .compressionEnabled(true)
                .identityStrategy(IdentityStrategy.KEEP_IDS);

        String json = objectMapper.writeValueAsString(opts);
        Assert.assertNotNull(json);
        Assert.assertTrue(json.contains("chunkSize"));
        Assert.assertTrue(json.contains("KEEP_IDS"));

        AccountExportOptions roundtrip = objectMapper.readValue(json, AccountExportOptions.class);
        Assert.assertEquals(100, roundtrip.getChunkSize());
        Assert.assertTrue(roundtrip.isCompressionEnabled());
    }

    // ─── AccountImportOptions ────────────────────────────────────────────────

    @Test
    public void importOptionsDefaults() {
        AccountImportOptions opts = new AccountImportOptions();
        Assert.assertNull(opts.getTargetAccountId());
        Assert.assertEquals(IdentityStrategy.REGENERATE_IDS, opts.getIdentityStrategy());
        Assert.assertEquals(500, opts.getChunkSize());
        Assert.assertFalse(opts.isFailOnEntityError());
    }

    @Test
    public void importOptionsFluentBuilder() {
        AccountImportOptions opts = new AccountImportOptions()
                .targetAccountId(42L)
                .identityStrategy(IdentityStrategy.KEEP_IDS)
                .chunkSize(250)
                .failOnEntityError(true);

        Assert.assertEquals(42L, (long) opts.getTargetAccountId());
        Assert.assertEquals(IdentityStrategy.KEEP_IDS, opts.getIdentityStrategy());
        Assert.assertEquals(250, opts.getChunkSize());
        Assert.assertTrue(opts.isFailOnEntityError());
    }

    @Test
    public void importOptionsIsJsonSerializable() throws Exception {
        AccountImportOptions opts = new AccountImportOptions()
                .targetAccountId(7L)
                .identityStrategy(IdentityStrategy.REGENERATE_IDS);

        String json = objectMapper.writeValueAsString(opts);
        Assert.assertNotNull(json);
        Assert.assertTrue(json.contains("targetAccountId"));
        Assert.assertTrue(json.contains("REGENERATE_IDS"));

        AccountImportOptions roundtrip = objectMapper.readValue(json, AccountImportOptions.class);
        Assert.assertEquals(7L, (long) roundtrip.getTargetAccountId());
    }

    // ─── AccountCloneOptions ─────────────────────────────────────────────────

    @Test
    public void cloneOptionsIsJsonSerializable() throws Exception {
        AccountCloneOptions opts = new AccountCloneOptions();
        opts.setSourceAccountId(1L);
        opts.setTargetAccountId(2L);

        String json = objectMapper.writeValueAsString(opts);
        Assert.assertNotNull(json);
        Assert.assertTrue(json.contains("sourceAccountId") || json.contains("1"));
    }
}
