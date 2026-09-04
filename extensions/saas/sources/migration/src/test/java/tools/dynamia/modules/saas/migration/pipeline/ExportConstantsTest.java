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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals("3", ExportConstants.FORMAT_VERSION);
    }

    @Test
    public void refIdSuffixIsUnderscoredRefId() {
        Assertions.assertEquals("_ref_id", ExportConstants.REF_ID_SUFFIX);
    }

    @Test
    public void fieldNamesMatchArchitectureSpec() {
        Assertions.assertEquals("version",          ExportConstants.FIELD_VERSION);
        Assertions.assertEquals("exportedAt",       ExportConstants.FIELD_EXPORTED_AT);
        Assertions.assertEquals("sourceAccountId",  ExportConstants.FIELD_SOURCE_ACCOUNT_ID);
        Assertions.assertEquals("identityStrategy", ExportConstants.FIELD_IDENTITY_STRATEGY);
        Assertions.assertEquals("account",          ExportConstants.FIELD_ACCOUNT);
        Assertions.assertEquals("entities",         ExportConstants.FIELD_ENTITIES);
        Assertions.assertEquals("fields",           ExportConstants.FIELD_FIELDS);
        Assertions.assertEquals("rows",             ExportConstants.FIELD_ROWS);
    }

    @Test
    public void v3ConstantsAreCorrect() {
        Assertions.assertEquals("manifest.json", ExportConstants.MANIFEST_FILE);
        Assertions.assertEquals("entityClass",   ExportConstants.FIELD_ENTITY_CLASS);
        Assertions.assertEquals("file",          ExportConstants.MANIFEST_ENTITY_FILE);
    }

    @Test
    public void refIdSuffixProducesCorrectFieldName() {
        String refField = "category" + ExportConstants.REF_ID_SUFFIX;
        Assertions.assertEquals("category_ref_id", refField);
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
        Assertions.assertEquals("3",        root.get(ExportConstants.FIELD_VERSION).asText());
        Assertions.assertEquals(42L,        root.get(ExportConstants.FIELD_SOURCE_ACCOUNT_ID).asLong());
        Assertions.assertEquals("KEEP_IDS", root.get(ExportConstants.FIELD_IDENTITY_STRATEGY).asText());
        Assertions.assertTrue(root.has(ExportConstants.FIELD_ACCOUNT));
        Assertions.assertTrue(root.get(ExportConstants.FIELD_ENTITIES).isArray());
        Assertions.assertEquals(1, root.get(ExportConstants.FIELD_ENTITIES).size());

        JsonNode entry = root.get(ExportConstants.FIELD_ENTITIES).get(0);
        Assertions.assertEquals("Account42_Customer.json", entry.get(ExportConstants.MANIFEST_ENTITY_FILE).asText());
        Assertions.assertEquals("com.example.Customer",    entry.get(ExportConstants.FIELD_ENTITY_CLASS).asText());
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
        Assertions.assertEquals("com.example.Customer", root.get(ExportConstants.FIELD_ENTITY_CLASS).asText());
        Assertions.assertTrue(root.get(ExportConstants.FIELD_FIELDS).isArray());
        Assertions.assertEquals(3, root.get(ExportConstants.FIELD_FIELDS).size());
        Assertions.assertTrue(root.get(ExportConstants.FIELD_ROWS).isArray());
        Assertions.assertEquals(1, root.get(ExportConstants.FIELD_ROWS).size());
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
        Assertions.assertNotNull(first);
        Assertions.assertEquals(ExportConstants.MANIFEST_FILE, first.getName());
    }
}
