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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Tests that the JSON format constants used by ExportPipeline and ImportPipeline
 * match the documented export file format (ARCHITECTURE.md §7).
 */
public class ExportConstantsTest {

    @Test
    public void formatVersionIsOne() {
        Assert.assertEquals("1", ExportConstants.FORMAT_VERSION);
    }

    @Test
    public void refIdSuffixIsUnderscoredRefId() {
        Assert.assertEquals("_ref_id", ExportConstants.REF_ID_SUFFIX);
    }

    @Test
    public void fieldNamesMatchArchitectureSpec() {
        Assert.assertEquals("version",          ExportConstants.FIELD_VERSION);
        Assert.assertEquals("exportedAt",       ExportConstants.FIELD_EXPORTED_AT);
        Assert.assertEquals("sourceAccountId",  ExportConstants.FIELD_SOURCE_ACCOUNT_ID);
        Assert.assertEquals("identityStrategy", ExportConstants.FIELD_IDENTITY_STRATEGY);
        Assert.assertEquals("account",          ExportConstants.FIELD_ACCOUNT);
        Assert.assertEquals("entities",         ExportConstants.FIELD_ENTITIES);
    }

    @Test
    public void refIdSuffixProducesCorrectFieldName() {
        // e.g. "category" field becomes "category_ref_id" in the JSON
        String refField = "category" + ExportConstants.REF_ID_SUFFIX;
        Assert.assertEquals("category_ref_id", refField);
    }

    @Test
    public void minimalExportJsonStructureIsValid() throws IOException {
        // Build the minimal JSON skeleton that ImportPipeline expects
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        var gen = mapper.getFactory().createGenerator(out);

        gen.writeStartObject();
        gen.writeStringField(ExportConstants.FIELD_VERSION, ExportConstants.FORMAT_VERSION);
        gen.writeStringField(ExportConstants.FIELD_EXPORTED_AT, "2026-06-15T10:00:00");
        gen.writeNumberField(ExportConstants.FIELD_SOURCE_ACCOUNT_ID, 1L);
        gen.writeStringField(ExportConstants.FIELD_IDENTITY_STRATEGY, "KEEP_IDS");
        gen.writeObjectFieldStart(ExportConstants.FIELD_ACCOUNT);
        gen.writeEndObject();
        gen.writeObjectFieldStart(ExportConstants.FIELD_ENTITIES);
        gen.writeEndObject();
        gen.writeEndObject();
        gen.close();

        JsonNode root = mapper.readTree(out.toByteArray());
        Assert.assertEquals("1",        root.get(ExportConstants.FIELD_VERSION).asText());
        Assert.assertEquals(1L,         root.get(ExportConstants.FIELD_SOURCE_ACCOUNT_ID).asLong());
        Assert.assertEquals("KEEP_IDS", root.get(ExportConstants.FIELD_IDENTITY_STRATEGY).asText());
        Assert.assertTrue(root.has(ExportConstants.FIELD_ACCOUNT));
        Assert.assertTrue(root.has(ExportConstants.FIELD_ENTITIES));
    }
}
