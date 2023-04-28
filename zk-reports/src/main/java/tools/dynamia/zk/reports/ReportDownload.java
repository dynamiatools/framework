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
package tools.dynamia.zk.reports;

import org.zkoss.zul.Filedownload;
import tools.dynamia.reports.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ReportDownload {

    public static void save(ReportDescriptor reportDescriptor) {
        Report report = ReportFiller.fill(reportDescriptor);
        save(report, reportDescriptor.getName(), reportDescriptor.getDefaultOutputType(), reportDescriptor.getExporterParameters());
    }

    public static void save(Report report) {
        save(report, report.getDefaultOutputType());
    }

    public static void save(Report report, ReportOutputType ouputType) {
        save(report, "export", ouputType);
    }

    public static void save(Report report, String fileName, ReportOutputType outputType) {
        save(report, fileName, outputType, null);
    }

    public static void save(Report report, String fileName, ReportOutputType outputType, Map exporterParams) {
        List<Report> reports = Collections.singletonList(report);
        save(reports, fileName, outputType, exporterParams);
    }

    public static void save(List<Report> reports, String fileName, ReportOutputType outputType) {
        save(reports, fileName, outputType, null);
    }

    public static void save(List<Report> reports, String fileName, ReportOutputType outputType, Map exporterParams) {
        try {
            File reportFile = createFile(fileName, outputType);
            ReportExporter.export(reports, reportFile, outputType, exporterParams);
            Filedownload.save(reportFile, outputType.getContentType());
        } catch (Exception e) {
            throw new ReportExporterException(e);
        }
    }

    private static File createFile(String fileName, ReportOutputType ouputType) throws IOException {

        String prefix = "export" + System.currentTimeMillis();
        if (fileName != null) {
            prefix = fileName + "_" + System.currentTimeMillis();
        }
        String suffix = "." + ouputType.getExtension();

        return File.createTempFile(prefix, suffix);
    }

    private ReportDownload() {
    }

}
