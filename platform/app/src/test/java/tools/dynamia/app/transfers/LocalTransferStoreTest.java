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
package tools.dynamia.app.transfers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.dynamia.ui.files.FlowPrincipal;
import tools.dynamia.ui.files.TransferMeta;
import tools.dynamia.ui.files.TransferTooLargeException;
import tools.dynamia.ui.files.UploadRejectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalTransferStoreTest {

    @TempDir
    Path dir;

    private static final FlowPrincipal ANA = new Owner("ana", "1");

    record Owner(String subject, String tenant) implements FlowPrincipal {
    }

    private static TransferMeta meta(FlowPrincipal owner) {
        return TransferMeta.of("report.json", "application/json", TransferMeta.Direction.UPLOAD, owner);
    }

    private static InputStream bytes(String text) {
        return new ByteArrayInputStream(text.getBytes());
    }

    private long files() throws IOException {
        try (Stream<Path> list = Files.list(dir)) {
            return list.count();
        }
    }

    @Test
    void whatIsStoredCanBeReadBackByItsOwner() throws IOException {
        var store = new LocalTransferStore(dir, Duration.ofMinutes(5), 0);

        var ref = store.put(bytes("{\"a\":1}"), meta(ANA), 1024);

        assertEquals("report.json", ref.name());
        assertEquals(7, ref.size());
        var found = store.get(ref.ref(), ANA).orElseThrow();
        try (var in = found.openStream()) {
            assertEquals("{\"a\":1}", new String(in.readAllBytes()));
        }
        assertEquals("application/json", found.ref().contentType());
    }

    @Test
    void anotherUserOrTenantGetsNothing() {
        var store = new LocalTransferStore(dir, Duration.ofMinutes(5), 0);
        var ref = store.put(bytes("x"), meta(ANA), 1024);

        assertTrue(store.get(ref.ref(), new Owner("luis", "1")).isEmpty());
        assertTrue(store.get(ref.ref(), new Owner("ana", "2")).isEmpty());
        assertTrue(store.get(ref.ref(), ANA).isPresent());
    }

    @Test
    void aFileOverTheLimitIsCutAndNothingStaysOnDisk() throws IOException {
        var store = new LocalTransferStore(dir, Duration.ofMinutes(5), 0);

        assertThrows(TransferTooLargeException.class, () -> store.put(bytes("0123456789"), meta(ANA), 5));

        assertEquals(0, files());
    }

    @Test
    void expiredFilesAreNotServedAndThePurgeDeletesThem() throws IOException {
        var store = new LocalTransferStore(dir, Duration.ofMillis(-1), 0);
        var ref = store.put(bytes("x"), meta(ANA), 1024);
        assertEquals(2, files());

        assertTrue(store.get(ref.ref(), ANA).isEmpty());
        store.purgeExpired();

        assertEquals(0, files());
    }

    @Test
    void theFreshFilesSurviveThePurge() throws IOException {
        var store = new LocalTransferStore(dir, Duration.ofMinutes(5), 0);
        var ref = store.put(bytes("x"), meta(ANA), 1024);

        store.purgeExpired();

        assertTrue(store.get(ref.ref(), ANA).isPresent());
        store.delete(ref.ref());
        assertFalse(store.get(ref.ref(), ANA).isPresent());
        assertEquals(0, files());
    }

    @Test
    void identifiersThatAreNotReferencesNeverTouchTheDisk() {
        var store = new LocalTransferStore(dir, Duration.ofMinutes(5), 0);

        assertTrue(store.get("../../etc/passwd", ANA).isEmpty());
        store.delete("../something");
    }

    @Test
    void aFullStoreRefusesNewFiles() {
        var store = new LocalTransferStore(dir, Duration.ofMinutes(5), 10);
        store.put(bytes("0123456789"), meta(ANA), 1024);

        assertThrows(UploadRejectedException.class, () -> store.put(bytes("x"), meta(ANA), 1024));
    }
}
