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

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.HtmlResourceHandler;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRGraphics2DExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterInputItem;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleExporterInputItem;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import tools.dynamia.commons.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
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
     */
    public static void export(Report report, File outputFile, ReportOutputType outputType) {
        export(Collections.singletonList(report), outputFile, outputType, null);
    }

    /**
     */
    public static void export(Report report, File outputFile, ReportOutputType outputType, Map exportParameters) {
        export(Collections.singletonList(report), outputFile, outputType, exportParameters);
    }

    /**
     */
    public static void export(Report report, OutputStream outputStream, ReportOutputType outputType) {
        export(Collections.singletonList(report), outputStream, outputType, null);
    }

    /**
     */
    public static void export(Report report, OutputStream outputStream, ReportOutputType outputType, Map exportParameters) {
        export(Collections.singletonList(report), outputStream, outputType, exportParameters);
    }

    /**
     */
    public static void export(List<Report> reports, File outputFile, ReportOutputType ouputType) {
        export(reports, outputFile, ouputType, null);
    }

    /**
     */
    public static void export(List<Report> reports, File outputFile, ReportOutputType outputType, Map exportParameters) {
        try {
            export(reports, new FileOutputStream(outputFile), outputType, exportParameters);
        } catch (Exception e) {
            throw new ReportExporterException(e);
        }
    }

    /**
     */
    public static void export(List<Report> reports, OutputStream outputStream, ReportOutputType outputType, Map exportParameters) {
        try {
            var exporter = buildExporter(outputType, outputStream);

            List<JasperPrint> list = getJasperPrints(reports);

            if (!list.isEmpty()) {
                var items = new ArrayList<ExporterInputItem>();
                list.forEach(jp -> items.add(new SimpleExporterInputItem(jp)));
                //noinspection unchecked
                exporter.setExporterInput(new SimpleExporterInput(items));
            } else if (reports.size() == 1) {
                Report report = reports.get(0);
                if (report.getContent() instanceof File reportFile) {
                    //noinspection unchecked
                    exporter.setExporterInput(new SimpleExporterInput(reportFile));
                }
            }

            exporter.exportReport();
        } catch (Exception ex) {
            throw new ReportExporterException(ex);
        }
    }

    /**
     */
    public static byte[] export(Report report, ReportOutputType outputType) {
        return export(Collections.singletonList(report), outputType, null);
    }

    /**
     */
    public static byte[] export(Report report, ReportOutputType outputType, Map exportParameters) {
        return export(Collections.singletonList(report), outputType, exportParameters);
    }

    /**
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
            case CSV -> exporter = new JRCsvExporter();
            case HTML -> {
                exporter = buildHtmlExporter(outputStream);
                ready = true;
            }
            case JAVA2D -> exporter = new JRGraphics2DExporter();
            case OPENOFFICE -> exporter = new JROdtExporter();
            case PDF -> exporter = new JRPdfExporter();
            case PLAIN -> exporter = new JRTextExporter();
            case EXCEL -> exporter = new JRXlsxExporter();
            case PRINTER -> exporter = new JRPrintServiceExporter();
        }
        if (!ready) {
            //noinspection unchecked
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
            if (report.getContent() instanceof JasperPrint jrPrint) {
                jasperPrints.add(jrPrint);
            }
        }
        return jasperPrints;
    }

    private ReportExporter() {
    }
}
