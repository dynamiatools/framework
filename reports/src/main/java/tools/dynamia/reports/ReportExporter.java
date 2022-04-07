/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;
import tools.dynamia.commons.StringUtils;

import java.io.*;
import java.util.*;

/**
 * @author Mario A. Serrano Leones
 */
public abstract class ReportExporter {

    public static void export(Report report, Map exportParameters) {
        export(report, (OutputStream) null, report.getDefaultOutputType(), exportParameters);
    }

    /**
     * @param report
     * @param outputFile
     * @param outputType
     */
    public static void export(Report report, File outputFile, ReportOutputType outputType) {
        export(Collections.singletonList(report), outputFile, outputType, null);
    }

    /**
     * @param report
     * @param outputFile
     * @param outputType
     * @param exportParameters
     */
    public static void export(Report report, File outputFile, ReportOutputType outputType, Map exportParameters) {
        export(Collections.singletonList(report), outputFile, outputType, exportParameters);
    }

    /**
     * @param report
     * @param outputStream
     * @param outputType
     */
    public static void export(Report report, OutputStream outputStream, ReportOutputType outputType) {
        export(Collections.singletonList(report), outputStream, outputType, null);
    }

    /**
     * @param report
     * @param outputStream
     * @param outputType
     * @param exportParameters
     */
    public static void export(Report report, OutputStream outputStream, ReportOutputType outputType, Map exportParameters) {
        export(Collections.singletonList(report), outputStream, outputType, exportParameters);
    }

    /**
     * @param reports
     * @param outputFile
     * @param ouputType
     */
    public static void export(List<Report> reports, File outputFile, ReportOutputType ouputType) {
        export(reports, outputFile, ouputType, null);
    }

    /**
     * @param reports
     * @param outputFile
     * @param outputType
     * @param exportParameters
     */
    public static void export(List<Report> reports, File outputFile, ReportOutputType outputType, Map exportParameters) {
        try {
            export(reports, new FileOutputStream(outputFile), outputType, exportParameters);
        } catch (Exception e) {
            throw new ReportExporterException(e);
        }
    }

    /**
     * @param reports
     * @param outputStream
     * @param outputType
     * @param exportParameters
     */
    public static void export(List<Report> reports, OutputStream outputStream, ReportOutputType outputType, Map exportParameters) {
        try {
            var exporter = buildExporter(outputType, outputStream);

            List<JasperPrint> list = getJasperPrints(reports);

            if (!list.isEmpty()) {
                var items = new ArrayList<ExporterInputItem>();
                list.forEach(jp -> items.add(new SimpleExporterInputItem(jp)));
                exporter.setExporterInput(new SimpleExporterInput(items));
            } else if (reports.size() == 1) {
                Report report = reports.get(0);
                if (report.getContent() instanceof File) {
                    File reportFile = (File) report.getContent();
                    exporter.setExporterInput(new SimpleExporterInput(reportFile));
                }
            }

            exporter.exportReport();
        } catch (Exception ex) {
            throw new ReportExporterException(ex);
        }
    }

    /**
     * @param report
     * @param outputType
     * @return
     */
    public static byte[] export(Report report, ReportOutputType outputType) {
        return export(Collections.singletonList(report), outputType, null);
    }

    /**
     * @param report
     * @param outputType
     * @param exportParameters
     * @return
     */
    public static byte[] export(Report report, ReportOutputType outputType, Map exportParameters) {
        return export(Collections.singletonList(report), outputType, exportParameters);
    }

    /**
     * @param reports
     * @param outputType
     * @param exportParameters
     * @return
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

    private static Exporter buildExporter(ReportOutputType reportOuputType, OutputStream outputStream) throws Exception {
        Exporter exporter = null;
        boolean ready = false;
        switch (reportOuputType) {
            case CSV:
                exporter = new JRCsvExporter();
                break;
            case HTML:
                exporter = buildHtmlExporter(outputStream);
                ready = true;
                break;
            case JAVA2D:
                exporter = new JRGraphics2DExporter();
                break;
            case OPENOFFICE:
                exporter = new JROdtExporter();
                break;
            case PDF:
                exporter = new JRPdfExporter();
                break;
            case PLAIN:
                exporter = new JRTextExporter();
                break;
            case EXCEL:
                exporter = new JRXlsxExporter();
                break;
            case PRINTER:
                exporter = new JRPrintServiceExporter();
                break;
        }
        if (!ready) {
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        }

        return exporter;
    }

    private static HtmlExporter buildHtmlExporter(OutputStream outputStream) {
        var html = new HtmlExporter();
        Map<String, String> images = new HashMap<>();
        Map<String, String> dataTypes = Map.of(
                "jpg", "image/jpg",
                "png", "image/png",
                "svg", "image/svg+xml");

        var htmlOutput = new SimpleHtmlExporterOutput(outputStream);
        htmlOutput.setImageHandler(new HtmlResourceHandler() {

            @Override
            public void handleResource(String id, byte[] data) {
                var ext = StringUtils.getFilenameExtension(id);
                String type = null;
                if (ext != null && !ext.isBlank()) {
                    type = dataTypes.get(ext.toLowerCase().trim());
                }
                if (type == null) {
                    type = dataTypes.get("jpg");
                }

                images.put(id, "data:" + type + ";base64," + Base64.getEncoder().encodeToString(data));
            }

            @Override
            public String getResourcePath(String id) {
                return images.get(id);
            }
        });
        html.setExporterOutput(htmlOutput);

        return html;
    }

    private static List<JasperPrint> getJasperPrints(List<Report> reports) {
        List<JasperPrint> jasperPrints = new ArrayList<>();
        for (Report report : reports) {
            if (report.getContent() instanceof JasperPrint) {
                JasperPrint jrPrint = (JasperPrint) report.getContent();
                jasperPrints.add(jrPrint);
            }
        }
        return jasperPrints;
    }

    private ReportExporter() {
    }
}
