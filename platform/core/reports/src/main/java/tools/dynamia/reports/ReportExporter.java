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
package tools.dynamia.reports;

import tools.dynamia.integration.Containers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
public abstract class ReportExporter {

    public static void export(Report report, Map exportParameters) {
        export(report, (OutputStream) null, report.getDefaultOutputType(), exportParameters);
    }

    /**
     *
     */
    public static void export(Report report, File outputFile, ReportOutputType outputType) {
        export(Collections.singletonList(report), outputFile, outputType, null);
    }

    /**
     *
     */
    public static void export(Report report, File outputFile, ReportOutputType outputType, Map exportParameters) {
        export(Collections.singletonList(report), outputFile, outputType, exportParameters);
    }

    /**
     *
     */
    public static void export(Report report, OutputStream outputStream, ReportOutputType outputType) {
        export(Collections.singletonList(report), outputStream, outputType, null);
    }

    /**
     *
     */
    public static void export(Report report, OutputStream outputStream, ReportOutputType outputType, Map exportParameters) {
        export(Collections.singletonList(report), outputStream, outputType, exportParameters);
    }

    /**
     *
     */
    public static void export(List<Report> reports, File outputFile, ReportOutputType ouputType) {
        export(reports, outputFile, ouputType, null);
    }

    /**
     *
     */
    public static void export(List<Report> reports, File outputFile, ReportOutputType outputType, Map exportParameters) {
        try {
            export(reports, new FileOutputStream(outputFile), outputType, exportParameters);
        } catch (Exception e) {
            throw new ReportExporterException(e);
        }
    }

    /**
     *
     */
    public static void export(List<Report> reports, OutputStream outputStream, ReportOutputType outputType, Map exportParameters) {
        ReportCompiler reportCompiler = Containers.get().findObject(ReportCompiler.class);
        reportCompiler.export(reports, outputStream, outputType, exportParameters);
    }

    /**
     *
     */
    public static byte[] export(Report report, ReportOutputType outputType) {
        return export(Collections.singletonList(report), outputType, null);
    }

    /**
     *
     */
    public static byte[] export(Report report, ReportOutputType outputType, Map exportParameters) {
        return export(Collections.singletonList(report), outputType, exportParameters);
    }

    /**
     *
     */
    public static byte[] export(List<Report> reports, ReportOutputType outputType, Map exportParameters) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        export(reports, baos, outputType, exportParameters);
        try {
            return baos.toByteArray();
        } finally {
            try {
                baos.close();
            } catch (IOException ex) {
                throw new ReportExporterException(ex);
            }
        }
    }


    private ReportExporter() {
    }
}
