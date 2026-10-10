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
package tools.dynamia.modules.reports.ui.actions;

import org.junit.jupiter.api.Test;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.services.ReportsService;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.testing.ActionTester;
import tools.dynamia.ui.testing.TestFiles;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tools.dynamia.ui.testing.UIInteraction.Type.NOTIFY;
import static tools.dynamia.ui.testing.UIInteraction.Type.UPLOAD;

/**
 * Export and import of reports behave the same for ZK (direct) and for remote clients.
 */
class ReportActionsTest {

    private final ReportsService reports = mock(ReportsService.class);
    private final CrudService crudService = mock(CrudService.class);

    @Test
    void exportGivesTheReportFileToTheUser() throws Exception {
        var report = mock(Report.class);
        when(report.getId()).thenReturn(7L);
        when(crudService.load(Report.class, 7L)).thenReturn(report);
        when(reports.exportReport(report)).thenAnswer(call -> {
            var file = Files.createTempDirectory("report").resolve("sales.json").toFile();
            Files.writeString(file.toPath(), "{\"name\":\"sales\"}");
            return file;
        });

        var result = ActionTester.of(new ExportReportAction(reports))
                .crud(Report.class, crudService)
                .on(report)
                .runEverywhere();

        assertEquals(1, result.downloads().size());
        assertEquals("{\"name\":\"sales\"}", result.downloads().get(0).asString());
        assertEquals("sales.json", result.downloads().get(0).name());
        assertEquals("text/json", result.downloads().get(0).contentType());
        assertTrue(result.types().isEmpty());
    }

    @Test
    void exportAsksToSelectAReportWhenThereIsNone() {
        var result = ActionTester.of(new ExportReportAction(reports))
                .crud(Report.class, crudService)
                .runEverywhere();

        assertEquals(List.of(NOTIFY), result.types());
        assertEquals(MessageType.WARNING, result.notifications().get(0).messageType());
        assertTrue(result.downloads().isEmpty());
    }

    @Test
    void importReadsTheUploadedJsonAndRefreshesTheList() {
        var result = ActionTester.of(new ImportReportAction(reports))
                .crud(Report.class, crudService)
                .user(u -> u.upload(TestFiles.of("sales.json", "application/json", "{}".getBytes())))
                .runEverywhere();

        assertEquals(List.of(UPLOAD, NOTIFY), result.types());
        assertEquals("Imported OK", result.notifications().get(0).message());
        assertTrue(result.crud().queried());
        verify(reports, org.mockito.Mockito.times(2)).importReport(any(File.class));
    }

    @Test
    void importDoesNothingWhenTheUserCancels() {
        var result = ActionTester.of(new ImportReportAction(reports))
                .crud(Report.class, crudService)
                .user(u -> u.cancel())
                .runEverywhere();

        assertEquals(List.of(UPLOAD), result.types());
        org.mockito.Mockito.verifyNoInteractions(reports);
    }
}
