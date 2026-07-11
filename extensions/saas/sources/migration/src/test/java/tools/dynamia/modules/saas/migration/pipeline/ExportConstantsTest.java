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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Tests that the JSON format constants used by ExportPipeline and ImportPipeline
 * match the documented export file format (ARCHITECTURE.md §8).
 */
public class ExportConstantsTest {

    @Test
    public void formatVersionIsThree() {
        Assert.assertEquals("3", ExportConstants.FORMAT_VERSION);
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
        Assert.assertEquals("fields",           ExportConstants.FIELD_FIELDS);
        Assert.assertEquals("rows",             ExportConstants.FIELD_ROWS);
    }

    @Test
    public void v3ConstantsAreCorrect() {
        Assert.assertEquals("manifest.json", ExportConstants.MANIFEST_FILE);
        Assert.assertEquals("entityClass",   ExportConstants.FIELD_ENTITY_CLASS);
        Assert.assertEquals("file",          ExportConstants.MANIFEST_ENTITY_FILE);
    }

    @Test
    public void refIdSuffixProducesCorrectFieldName() {
        String refField = "category" + ExportConstants.REF_ID_SUFFIX;
        Assert.assertEquals("category_ref_id", refField);
    }

    @Test
    public void manifestJsonStructureIsValid() throws IOException {
        // Verify that the manifest format (v3) can be written and read back correctly
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        var gen = mapper.createGenerator(out);

        gen.writeStartObject();
        gen.writeStringProperty(ExportConstants.FIELD_VERSION, ExportConstants.FORMAT_VERSION);
        gen.writeStringProperty(ExportConstants.FIELD_EXPORTED_AT, "2026-06-15T10:00:00");
        gen.writeNumberProperty(ExportConstants.FIELD_SOURCE_ACCOUNT_ID, 42L);
        gen.writeStringProperty(ExportConstants.FIELD_IDENTITY_STRATEGY, "KEEP_IDS");
        gen.writeName(ExportConstants.FIELD_ACCOUNT);
        gen.writeStartObject();
        gen.writeEndObject();
        gen.writeName(ExportConstants.FIELD_ENTITIES);
        gen.writeStartArray();
        gen.writeStartObject();
        gen.writeStringProperty(ExportConstants.MANIFEST_ENTITY_FILE, "Account42_Customer.json");
        gen.writeStringProperty(ExportConstants.FIELD_ENTITY_CLASS, "com.example.Customer");
        gen.writeEndObject();
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();

        JsonNode root = mapper.readTree(out.toByteArray());
        Assert.assertEquals("3",        root.get(ExportConstants.FIELD_VERSION).asText());
        Assert.assertEquals(42L,        root.get(ExportConstants.FIELD_SOURCE_ACCOUNT_ID).asLong());
        Assert.assertEquals("KEEP_IDS", root.get(ExportConstants.FIELD_IDENTITY_STRATEGY).asText());
        Assert.assertTrue(root.has(ExportConstants.FIELD_ACCOUNT));
        Assert.assertTrue(root.get(ExportConstants.FIELD_ENTITIES).isArray());
        Assert.assertEquals(1, root.get(ExportConstants.FIELD_ENTITIES).size());

        JsonNode entry = root.get(ExportConstants.FIELD_ENTITIES).get(0);
        Assert.assertEquals("Account42_Customer.json", entry.get(ExportConstants.MANIFEST_ENTITY_FILE).asText());
        Assert.assertEquals("com.example.Customer",    entry.get(ExportConstants.FIELD_ENTITY_CLASS).asText());
    }

    @Test
    public void entityFileJsonStructureIsValid() throws IOException {
        // Verify that the per-entity JSON file format (v3) can be written and parsed
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        var gen = mapper.createGenerator(out);

        gen.writeStartObject();
        gen.writeStringProperty(ExportConstants.FIELD_ENTITY_CLASS, "com.example.Customer");
        gen.writeName(ExportConstants.FIELD_FIELDS);
        gen.writeStartArray();
        gen.writeString("id");
        gen.writeString("name");
        gen.writeString("category_ref_id");
        gen.writeEndArray();
        gen.writeName(ExportConstants.FIELD_ROWS);
        gen.writeStartArray();
        gen.writeStartArray();
        gen.writeNumber(1L);
        gen.writeString("John");
        gen.writeNumber(5L);
        gen.writeEndArray();
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();

        JsonNode root = mapper.readTree(out.toByteArray());
        Assert.assertEquals("com.example.Customer", root.get(ExportConstants.FIELD_ENTITY_CLASS).asText());
        Assert.assertTrue(root.get(ExportConstants.FIELD_FIELDS).isArray());
        Assert.assertEquals(3, root.get(ExportConstants.FIELD_FIELDS).size());
        Assert.assertTrue(root.get(ExportConstants.FIELD_ROWS).isArray());
        Assert.assertEquals(1, root.get(ExportConstants.FIELD_ROWS).size());
    }

    @Test
    public void zipContainsManifestAsFirstEntry() throws IOException {
        // Verify that a ZIP archive produced with the expected naming convention
        // has manifest.json as its first entry
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(buf)) {
            zipOut.putNextEntry(new ZipEntry(ExportConstants.MANIFEST_FILE));
            zipOut.write("{}".getBytes());
            zipOut.closeEntry();
            zipOut.putNextEntry(new ZipEntry("Account42_Customer.json"));
            zipOut.write("{}".getBytes());
            zipOut.closeEntry();
        }

        ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(buf.toByteArray()));
        ZipEntry first = zipIn.getNextEntry();
        Assert.assertNotNull(first);
        Assert.assertEquals(ExportConstants.MANIFEST_FILE, first.getName());
    }
}
