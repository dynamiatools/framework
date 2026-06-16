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
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Verifies the GZIP auto-detection contract relied on by {@code ImportPipeline.detectAndWrapGzip()}.
 *
 * <p>The fix (P1) requires that {@code ImportWorker} wraps the {@code FileInputStream} with a
 * {@code BufferedInputStream} before passing it to the pipeline, so that {@code mark()} is
 * supported and GZIP detection can read the magic bytes and reset the position.
 */
public class GzipStreamTest {

    @Test
    public void bufferedInputStreamSupportsMark() {
        InputStream raw = new ByteArrayInputStream(new byte[]{1, 2, 3});
        BufferedInputStream buffered = new BufferedInputStream(raw);
        Assert.assertTrue("BufferedInputStream must support mark()", buffered.markSupported());
    }

    @Test
    public void plainByteArrayInputStreamSupportsMark() {
        // ByteArrayInputStream also supports mark — used for in-memory (clone) paths
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{1, 2, 3});
        Assert.assertTrue(bais.markSupported());
    }

    @Test
    public void gzipMagicBytesAreDetectable() throws IOException {
        byte[] gzipData = gzip("{}");

        // Verify magic bytes 0x1f 0x8b
        Assert.assertEquals(0x1f, gzipData[0] & 0xFF);
        Assert.assertEquals(0x8b, gzipData[1] & 0xFF);
    }

    @Test
    public void gzipWrappedInBufferedStreamIsReadable() throws IOException {
        String json = "{\"key\":\"value\"}";
        byte[] gzipData = gzip(json);

        InputStream in = new BufferedInputStream(new ByteArrayInputStream(gzipData));
        in.mark(2);
        int b1 = in.read();
        int b2 = in.read();
        in.reset(); // must be able to reset for detection to work

        Assert.assertEquals(0x1f, b1 & 0xFF);
        Assert.assertEquals(0x8b, b2 & 0xFF);

        // Now read the full content via GZIP
        String decompressed = new String(new GZIPInputStream(in).readAllBytes());
        Assert.assertEquals(json, decompressed);
    }

    @Test
    public void plainJsonInBufferedStreamIsPassedThrough() throws IOException {
        String json = "{\"hello\":\"world\"}";
        byte[] jsonBytes = json.getBytes();

        InputStream in = new BufferedInputStream(new ByteArrayInputStream(jsonBytes));
        in.mark(2);
        int b1 = in.read();
        int b2 = in.read();
        in.reset();

        // Not GZIP magic bytes
        boolean isGzip = (b1 == 0x1f && b2 == 0x8b);
        Assert.assertFalse("Plain JSON must not match GZIP magic", isGzip);

        // After reset, the full content is still readable
        String content = new String(in.readAllBytes());
        Assert.assertEquals(json, content);
    }

    @Test
    public void gzipRoundTrip() throws IOException {
        String original = "{\"version\":\"1\",\"entities\":{}}";
        byte[] compressed = gzip(original);

        InputStream in = new GZIPInputStream(new ByteArrayInputStream(compressed));
        String decompressed = new String(in.readAllBytes());
        Assert.assertEquals(original, decompressed);
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private static byte[] gzip(String text) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(buf)) {
            gz.write(text.getBytes());
        }
        return buf.toByteArray();
    }
}
