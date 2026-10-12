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
package tools.dynamia.zk.ui;

import org.junit.jupiter.api.Test;
import org.zkoss.util.media.AMedia;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.ui.UIFacades;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.UIUnavailableException;
import tools.dynamia.ui.files.UploadOptions;
import tools.dynamia.ui.files.UploadPolicy;
import tools.dynamia.ui.files.UploadRejectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The parts of the ZK adapters that need no desktop. What needs one (windows, {@code Fileupload}, {@code Executions}) is
 * not covered by contract tests here: ZATS is not available in this project.
 */
class ZKAdaptersContractTest {

    @Test
    void aBinaryUploadIsCopiedByStreamingToAHandle() throws IOException {
        var media = new AMedia("data.bin", "bin", "application/octet-stream", new ByteArrayInputStream(new byte[]{1, 2, 3}));

        var file = ZKFileTransfer.wrap(media);

        assertEquals("data.bin", file.name());
        assertEquals(3, file.size());
        try (var in = file.openStream()) {
            assertEquals(3, in.readAllBytes().length);
        }
        Files.deleteIfExists(file.toTempFile());
    }

    @Test
    void aTextUploadIsReadAsTextAndStoredAsUtf8() throws IOException {
        var media = new AMedia("a.json", "json", "application/json", "{\"name\":\"Niño\"}");

        var file = ZKFileTransfer.wrap(media);

        try (var in = file.openStream()) {
            assertEquals("{\"name\":\"Niño\"}", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void theLimitsOfTheUploadAreCheckedWithTheSamePolicyAsRemoteClients() {
        var file = ZKFileTransfer.wrap(new AMedia("virus.exe", "exe", "application/octet-stream", new byte[]{1}));

        assertThrows(UploadRejectedException.class, () -> UploadPolicy.check(UploadOptions.single(".json"), List.of(file)));
        UploadPolicy.check(UploadOptions.single(".exe"), List.of(file));
    }

    @Test
    void theTaskOfAProgressOperationHasNoUiWhateverThreadRunsIt() {
        var seen = new AtomicReference<String>();
        var failure = new AtomicReference<Throwable>();

        ZKProgressRunner.runWithoutUi(monitor -> {
            seen.set(UIFacades.current().name());
            try {
                UIMessages.showQuestion("In the task?", () -> {
                });
            } catch (UIUnavailableException e) {
                failure.set(e);
            }
        }, new ProgressMonitor());

        assertEquals("none", seen.get());
        assertEquals("messages", ((UIUnavailableException) failure.get()).getPort());
    }

    @Test
    void aCheckedExceptionOfTheTaskReachesOnErrorAsAnUncheckedOne() {
        var failure = assertThrows(IllegalStateException.class, () -> ZKProgressRunner.runWithoutUi(monitor -> {
            throw new java.io.IOException("disk full");
        }, new ProgressMonitor()));

        assertEquals("disk full", failure.getMessage());
    }
}
