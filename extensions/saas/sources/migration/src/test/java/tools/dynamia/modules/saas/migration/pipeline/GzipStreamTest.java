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

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Verifies ZIP stream properties relied on by {@link ImportPipeline} and {@link ExportPipeline}.
 *
 * <p>The export pipeline writes a temp-directory ZIP; the import pipeline reads it.
 * These tests confirm the ZIP contract: magic bytes, entry ordering, and that
 * {@link BufferedInputStream} supports {@code mark()} so the format can be detected
 * without consuming the stream.
 */
public class GzipStreamTest {

    @Test
    public void bufferedInputStreamSupportsMark() {
        var raw = new ByteArrayInputStream(new byte[]{1, 2, 3});
        var buffered = new BufferedInputStream(raw);
        Assert.assertTrue("BufferedInputStream must support mark()", buffered.markSupported());
    }

    @Test
    public void byteArrayInputStreamSupportsMark() {
        var bais = new ByteArrayInputStream(new byte[]{1, 2, 3});
        Assert.assertTrue(bais.markSupported());
    }

    @Test
    public void zipMagicBytesAreDetectable() throws IOException {
        byte[] zipData = zip("manifest.json", "{}");
        Assert.assertEquals("ZIP magic byte 0", 0x50, zipData[0] & 0xFF);
        Assert.assertEquals("ZIP magic byte 1", 0x4B, zipData[1] & 0xFF);
    }

    @Test
    public void bufferedStreamPreservesZipAfterMagicPeek() throws IOException {
        byte[] zipData = zip("manifest.json", "{}");
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(zipData));

        in.mark(4);
        int b1 = in.read();
        int b2 = in.read();
        in.reset();

        Assert.assertEquals(0x50, b1 & 0xFF);
        Assert.assertEquals(0x4B, b2 & 0xFF);

        // After reset the full ZIP is still readable
        ZipInputStream zipIn = new ZipInputStream(in);
        ZipEntry entry = zipIn.getNextEntry();
        Assert.assertNotNull("Entry must exist after reset", entry);
        Assert.assertEquals("manifest.json", entry.getName());
    }

    @Test
    public void zipEntriesAreReadInInsertionOrder() throws IOException {
        byte[] zipData = zip3("manifest.json", "{}", "Account1_Customer.json", "[]", "Account1_Order.json", "[]");
        ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(zipData));

        Assert.assertEquals("manifest.json",        zipIn.getNextEntry().getName());
        Assert.assertEquals("Account1_Customer.json", zipIn.getNextEntry().getName());
        Assert.assertEquals("Account1_Order.json",    zipIn.getNextEntry().getName());
        Assert.assertNull("No more entries", zipIn.getNextEntry());
    }

    @Test
    public void zipEntryReportsEofAtEntryBoundary() throws IOException {
        byte[] content = "hello".getBytes();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(buf)) {
            out.putNextEntry(new ZipEntry("a.json"));
            out.write(content);
            out.closeEntry();
            out.putNextEntry(new ZipEntry("b.json"));
            out.write("world".getBytes());
            out.closeEntry();
        }

        ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(buf.toByteArray()));
        zipIn.getNextEntry();
        byte[] read = zipIn.readAllBytes(); // reads only "a.json" content
        Assert.assertArrayEquals("entry content", content, read);

        // second entry is still accessible
        Assert.assertNotNull("b.json must follow", zipIn.getNextEntry());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private static byte[] zip(String name, String content) throws IOException {
        return zip3(name, content, null, null, null, null);
    }

    private static byte[] zip3(String n1, String c1, String n2, String c2, String n3, String c3)
            throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(buf)) {
            writeEntry(out, n1, c1);
            if (n2 != null) writeEntry(out, n2, c2);
            if (n3 != null) writeEntry(out, n3, c3);
        }
        return buf.toByteArray();
    }

    private static void writeEntry(ZipOutputStream out, String name, String content)
            throws IOException {
        out.putNextEntry(new ZipEntry(name));
        out.write(content.getBytes());
        out.closeEntry();
    }
}
