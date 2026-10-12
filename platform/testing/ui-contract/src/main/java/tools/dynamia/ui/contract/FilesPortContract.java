/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.ui.contract;

import org.junit.jupiter.api.Test;
import tools.dynamia.ui.UIFiles;
import tools.dynamia.ui.files.UploadOptions;
import tools.dynamia.ui.files.UploadRejectedException;
import tools.dynamia.ui.files.UploadedFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

/** {@code UIFiles}: uploads arrive as streaming handles within the limits; downloads reach the user without size limits. */
public abstract class FilesPortContract extends PortContract {

    private static String text(UploadedFile file) {
        try (var in = file.openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static UploadedFile json(String name, String content) {
        return UploadedFile.of(name, "application/json", content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void anUploadGivesTheActionAHandleItCanReadByStreaming() {
        var o = driver().run(fx -> UIFiles.uploadOne(".json", file -> fx.add(file.name() + ":" + file.size() + ":" + text(file))),
                List.of(Reply.upload(json("a.json", "{}"))));

        assertEquals(List.of("a.json:2:{}"), o.effects());
        assertEquals(List.of(Outcome.Asked.of("UPLOAD", null)), o.asked());
    }

    @Test
    void cancellingNeverCallsTheCallback() {
        var o = driver().run(fx -> UIFiles.uploadOne(".json", file -> fx.add("called")), List.of(Reply.cancel()));

        assertEquals(List.of(), o.effects());
        assertNull(o.failure());
    }

    @Test
    void theAcceptedTypesAreEnforcedOnTheServer() {
        var o = driver().run(fx -> UIFiles.uploadOne(".json", file -> fx.add("called")),
                List.of(Reply.upload(UploadedFile.of("virus.exe", "application/octet-stream", new byte[]{1}))));

        assertInstanceOf(UploadRejectedException.class, o.failure());
        assertEquals(List.of(), o.effects());
    }

    @Test
    void theMaximumSizeIsEnforcedOnTheServer() {
        var options = new UploadOptions(null, null, 1, 3, 0);
        var o = driver().run(fx -> UIFiles.upload(options, files -> fx.add("called")),
                List.of(Reply.upload(json("big.json", "{\"a\":1}"))));

        assertInstanceOf(UploadRejectedException.class, o.failure());
    }

    @Test
    void theMaximumNumberOfFilesIsEnforcedOnTheServer() {
        var options = new UploadOptions(null, null, 1, 1000, 0);
        var o = driver().run(fx -> UIFiles.upload(options, files -> fx.add("called")),
                List.of(Reply.upload(json("a.json", "{}"), json("b.json", "{}"))));

        assertInstanceOf(UploadRejectedException.class, o.failure());
    }

    @Test
    void aDownloadFromBytesReachesTheUserWithItsNameAndType() {
        var o = driver().run(fx -> UIFiles.download("out.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)), List.of());

        assertEquals(1, o.downloads().size());
        assertEquals("out.txt", o.downloads().get(0).name());
        assertEquals("text/plain", o.downloads().get(0).contentType());
        assertEquals("hello", o.downloads().get(0).text());
    }

    @Test
    void aDownloadFromAStreamIsReadWhenTheUserGetsIt() {
        var o = driver().run(fx -> UIFiles.download("out.bin", "application/octet-stream",
                () -> new ByteArrayInputStream("streamed".getBytes(StandardCharsets.UTF_8))), List.of());

        assertEquals("streamed", o.downloads().get(0).text());
    }
}
