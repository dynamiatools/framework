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

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.ui.files.FlowPrincipal;
import tools.dynamia.ui.files.TransferMeta;
import tools.dynamia.ui.files.TransferTooLargeException;
import tools.dynamia.ui.files.UploadDefaults;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The transfers endpoints stream: the surefire heap of this module is 256 MB and a 50 MB file goes in and comes out.
 */
class TransfersControllerTest {

    @TempDir
    Path dir;

    private final AtomicReference<FlowPrincipal> principal = new AtomicReference<>();
    private LocalTransferStore store;
    private TransfersController controller;

    @BeforeEach
    void setUp() {
        UploadDefaults.set(100L * 1024 * 1024, 10);
        store = new LocalTransferStore(dir, Duration.ofMinutes(5), 0);
        controller = new TransfersController(store);
        principal.set(new LocalTransferStoreTest.Owner("ana", "1"));
        var beans = new SimpleObjectContainer("transfers-test");
        beans.addObject((FlowPrincipal) new FlowPrincipal() {
            @Override
            public String subject() {
                return principal.get().subject();
            }

            @Override
            public String tenant() {
                return principal.get().tenant();
            }
        });
        Containers.get().installObjectContainer(beans);
    }

    @AfterEach
    void tearDown() {
        UploadDefaults.set(UploadDefaults.BUILT_IN_MAX_FILE_SIZE, UploadDefaults.BUILT_IN_MAX_FILES);
        Containers.get().removeAllContainers();
    }

    /** A request whose body is generated on the fly and never held in memory. */
    private static MockHttpServletRequest streaming(long size, boolean declareLength) {
        var request = new MockHttpServletRequest("POST", TransfersController.PATH) {
            @Override
            public ServletInputStream getInputStream() {
                return new ServletInputStream() {
                    private long sent;

                    @Override
                    public int read() {
                        return sent++ < size ? 'x' : -1;
                    }

                    @Override
                    public int read(byte[] b, int off, int len) {
                        if (sent >= size) {
                            return -1;
                        }
                        int n = (int) Math.min(len, size - sent);
                        java.util.Arrays.fill(b, off, off + n, (byte) 'x');
                        sent += n;
                        return n;
                    }

                    @Override
                    public boolean isFinished() {
                        return sent >= size;
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setReadListener(ReadListener readListener) {
                    }
                };
            }

            @Override
            public long getContentLengthLong() {
                return declareLength ? size : -1;
            }
        };
        request.addHeader("X-File-Name", "big%20file.bin");
        request.addHeader("X-File-Type", "application/octet-stream");
        return request;
    }

    @Test
    void aFileOf50MbGoesInAndComesOutWithAQuarterGigabyteOfHeap() throws IOException {
        long size = 50L * 1024 * 1024;
        assertTrue(Runtime.getRuntime().maxMemory() < 400L * 1024 * 1024, "the module runs with a small heap");

        var response = controller.upload(streaming(size, true));

        var body = response.getBody();
        assertNotNull(body);
        assertEquals(size, body.get("size"));
        assertEquals("big file.bin", body.get("name"));
        var stored = store.get((String) body.get("ref"), principal.get()).orElseThrow();
        long read = 0;
        try (InputStream in = stored.openStream()) {
            byte[] buffer = new byte[64 * 1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                read += n;
            }
        }
        assertEquals(size, read);
    }

    @Test
    void aFileOverTheLimitIsRefusedAndNothingIsKept() {
        UploadDefaults.set(1024, 10);

        assertThrows(TransferTooLargeException.class, () -> controller.upload(streaming(5000, false)));
        assertThrows(TransferTooLargeException.class, () -> controller.upload(streaming(5000, true)));

        assertEquals(0, dir.toFile().list().length);
    }

    @Test
    void theTooLargeErrorBecomesHttp413() {
        var response = controller.tooLarge(new TransferTooLargeException(10));

        assertEquals(413, response.getStatusCode().value());
    }

    @Test
    void theFileCanBeFetchedByItsOwnerAndOnlyByIt() throws IOException {
        var ref = store.put(new ByteArrayInputStream("hello".getBytes()),
                TransferMeta.of("out.txt", "text/plain", TransferMeta.Direction.DOWNLOAD, principal.get()), 1024);

        principal.set(new LocalTransferStoreTest.Owner("luis", "1"));
        var foreign = new MockHttpServletResponse();
        controller.download(ref.ref(), foreign);
        assertEquals(404, foreign.getStatus());

        principal.set(new LocalTransferStoreTest.Owner("ana", "1"));
        var own = new MockHttpServletResponse();
        controller.download(ref.ref(), own);
        assertEquals(200, own.getStatus());
        assertEquals("hello", own.getContentAsString());
        assertEquals("text/plain", own.getContentType());
        assertEquals("no-store", own.getHeader("Cache-Control"));
        assertTrue(own.getHeader("Content-Disposition").contains("filename*=UTF-8''out.txt"));
    }

    @Test
    void aDownloadIsDeletedAfterTheFirstCompleteRead() throws IOException {
        var ref = store.put(new ByteArrayInputStream("hello".getBytes()),
                TransferMeta.of("out.txt", "text/plain", TransferMeta.Direction.DOWNLOAD, principal.get()), 1024);

        controller.download(ref.ref(), new MockHttpServletResponse());
        var again = new MockHttpServletResponse();
        controller.download(ref.ref(), again);

        assertEquals(404, again.getStatus());
    }

    @Test
    void anUploadSurvivesBeingReadAndCanBeCancelledByItsOwnerOnly() {
        var ref = store.put(new ByteArrayInputStream("x".getBytes()),
                TransferMeta.of("in.txt", null, TransferMeta.Direction.UPLOAD, principal.get()), 1024);

        principal.set(new LocalTransferStoreTest.Owner("luis", "1"));
        controller.cancel(ref.ref());
        principal.set(new LocalTransferStoreTest.Owner("ana", "1"));
        assertTrue(store.get(ref.ref(), principal.get()).isPresent(), "someone else cannot cancel it");

        controller.cancel(ref.ref());
        assertTrue(store.get(ref.ref(), principal.get()).isEmpty());
    }
}
