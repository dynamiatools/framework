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
package tools.dynamia.app.reports;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
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
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.io.FileInfo;
import tools.dynamia.io.IOUtils;
import tools.dynamia.reports.Report;
import tools.dynamia.reports.ReportCompileException;
import tools.dynamia.reports.ReportCompiler;
import tools.dynamia.reports.ReportDataSource;
import tools.dynamia.reports.ReportDescriptor;
import tools.dynamia.reports.ReportExporterException;
import tools.dynamia.reports.ReportFiller;
import tools.dynamia.reports.ReportFillerException;
import tools.dynamia.reports.ReportOutputType;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JasperReportCompiler is a ReportCompiler implementation for compiling and filling JasperReports.
 * It supports compiling .jrxml files to .jasper files and filling reports with various data sources.
 */
public class JasperReportCompiler implements ReportCompiler {

    private LoggingService logger = new SLF4JLoggingService(JasperReportCompiler.class);

    @Override
    public String getId() {
        return "jasperreports";
    }

    @Override
    public File compile(File reportFile) {
        JasperReportExplorerFilter filter = new JasperReportExplorerFilter();
        if (filter.match(reportFile)) {
            File jasperFile = findJasper(reportFile);
            if (!jasperFile.exists() || reportFile.lastModified() > jasperFile.lastModified()) {
                try (FileInputStream input = new FileInputStream(reportFile)) {
                    FileOutputStream output = new FileOutputStream(jasperFile);
                    JasperCompileManager.compileReportToStream(input, output);

                    output.close();
                } catch (Exception e) {
                    throw new ReportCompileException(e);
                }
            }
            return jasperFile;
        } else {
            throw new ReportCompileException("Unsupport report file, should be a JasperReports .jrxml file");
        }

    }

    private File findJasper(File reportFile) {
        File dir = reportFile.getParentFile();
        String name = IOUtils.getFileNameWithoutExtension(reportFile);
        return new File(dir, name + ".jasper");
    }

    @Override
    public Report fill(ReportDescriptor reportDescriptor) {
        Report report = null;
        try {
            Object content = createTemporaryReport(reportDescriptor);
            report = new Report(content, reportDescriptor.getName(), reportDescriptor.getDefaultOutputType());
        } catch (Exception e) {
            throw new ReportFillerException(e);
        }
        return report;
    }

    @Override
    public Report fill(ReportDescriptor reportDescriptor, boolean inMemory) {
        if (!inMemory) {
            return fill(reportDescriptor);
        } else {
            Report report = null;
            try {
                Object content = createInMemoryReport(reportDescriptor);
                report = new Report(content, reportDescriptor.getName(), reportDescriptor.getDefaultOutputType());
            } catch (Exception e) {
                throw new ReportFillerException(e);
            }
            return report;
        }
    }

    private File createTemporaryReport(ReportDescriptor reportDescriptor) throws Exception {
        Object datasource = getDatasource(reportDescriptor);
        File filledReportFile = File.createTempFile("dmreport" + System.currentTimeMillis(), ".jrprint");
        try (FileOutputStream out = new FileOutputStream(filledReportFile)) {
            Object template = getTemplate(reportDescriptor);
            Map<String, Object> params = buildParams(reportDescriptor.getParameters());

            if (datasource instanceof JRDataSource jrds) {
                switch (template) {
                    case String s -> {
                        InputStream is = ReportFiller.class.getResourceAsStream(reportDescriptor.getTemplate().toString());
                        JasperFillManager.fillReportToStream(is, out, params, jrds);
                    }
                    case JasperReport jr -> JasperFillManager.fillReportToStream(jr, out, params, jrds);
                    case File file -> {
                        params.put("CURRENT_DIRECTORY", file.getParentFile().getAbsolutePath());
                        InputStream is = new FileInputStream(file);
                        JasperFillManager.fillReportToStream(is, out, params, jrds);
                    }
                    case URL url -> {
                        InputStream is = url.openStream();
                        JasperFillManager.fillReportToStream(is, out, params, jrds);
                    }
                    case null, default ->
                            throw new ReportFillerException("Unknow report template type :" + reportDescriptor.getTemplate());
                }
            } else if (datasource instanceof Connection connection) {
                switch (template) {
                    case String s -> {
                        InputStream is = ReportFiller.class.getResourceAsStream(reportDescriptor.getTemplate().toString());
                        JasperFillManager.fillReportToStream(is, out, params, connection);
                    }
                    case JasperReport jr -> JasperFillManager.fillReportToStream(jr, out, params, connection);
                    case File file -> {
                        params.put("CURRENT_DIRECTORY", file.getParentFile().getAbsolutePath());
                        InputStream is = new FileInputStream(file);
                        JasperFillManager.fillReportToStream(is, out, params, connection);
                    }
                    case URL url -> {
                        InputStream is = url.openStream();
                        JasperFillManager.fillReportToStream(is, out, params, connection);
                    }
                    case null, default ->
                            throw new ReportFillerException("Unknow report template type :" + reportDescriptor.getTemplate());
                }
            }
        }

        return filledReportFile;
    }

    private static Map<String, Object> buildParams(Map<String, Object> parameters) {
        var params = new HashMap<>(parameters);

        params.entrySet()
                .stream().filter(e -> e.getValue() instanceof ReportDataSource)
                .forEach(e -> {
                    var datasource = (ReportDataSource) e.getValue();
                    if (datasource.getValue() instanceof Collection) {
                        params.put(e.getKey(), new JRBeanCollectionDataSource((Collection<?>) datasource.getValue()));
                    } else if (datasource.getValue() instanceof JRDataSource) {
                        params.put(e.getKey(), datasource.getValue());
                    }
                });

        return params;
    }

    private JasperPrint createInMemoryReport(ReportDescriptor reportDescriptor) throws Exception {
        Object datasource = getDatasource(reportDescriptor);
        JasperPrint jasperPrint = null;

        Object template = getTemplate(reportDescriptor);
        Map<String, Object> params = new HashMap<>(reportDescriptor.getParameters());
        if (datasource instanceof JRDataSource jrds) {
            switch (template) {
                case String s -> {
                    InputStream is = ReportFiller.class.getResourceAsStream(reportDescriptor.getTemplate().toString());
                    jasperPrint = JasperFillManager.fillReport(is, params, jrds);
                }
                case JasperReport jr -> jasperPrint = JasperFillManager.fillReport(jr, params, jrds);
                case File file -> {
                    params.put("CURRENT_DIRECTORY", file.getParentFile().getAbsolutePath());
                    InputStream is = new FileInputStream(file);
                    jasperPrint = JasperFillManager.fillReport(is, params, jrds);
                }
                case URL url -> {
                    InputStream is = url.openStream();
                    jasperPrint = JasperFillManager.fillReport(is, params, jrds);
                }
                case null, default ->
                        throw new ReportFillerException("Unknow report template type :" + reportDescriptor.getTemplate());
            }
        } else if (datasource instanceof Connection connection) {
            switch (template) {
                case String s -> {
                    InputStream is = ReportFiller.class.getResourceAsStream(reportDescriptor.getTemplate().toString());
                    jasperPrint = JasperFillManager.fillReport(is, params, connection);
                }
                case JasperReport jr -> jasperPrint = JasperFillManager.fillReport(jr, params, connection);
                case File file -> {
                    params.put("CURRENT_DIRECTORY", file.getParentFile().getAbsolutePath());
                    InputStream is = new FileInputStream(file);
                    jasperPrint = JasperFillManager.fillReport(is, params, connection);
                }
                case URL url -> {
                    InputStream is = url.openStream();
                    jasperPrint = JasperFillManager.fillReport(is, params, connection);
                }
                case null, default ->
                        throw new ReportFillerException("Unknow report template type :" + reportDescriptor.getTemplate());
            }
        }

        return jasperPrint;
    }

    private Object getDatasource(ReportDescriptor reportDescriptor) {
        Object dataSource = reportDescriptor.getDataSource();

        Object realDatasource = null;
        if (dataSource == null) {
            realDatasource = new JREmptyDataSource();
        } else if (dataSource instanceof JRDataSource) {
            realDatasource = dataSource;
        } else if (dataSource instanceof Collection) {
            realDatasource = new JRBeanCollectionDataSource((Collection) dataSource);
        } else if (dataSource.getClass().isArray()) {
            realDatasource = new JRBeanArrayDataSource((Object[]) dataSource);
        } else if (dataSource instanceof ResultSet) {
            realDatasource = new JRResultSetDataSource((ResultSet) dataSource);
        } else if (dataSource instanceof Connection) {
            realDatasource = dataSource;
        } else if (dataSource instanceof DataSource jdbcDatasource) {
            try {
                realDatasource = jdbcDatasource.getConnection();
            } catch (SQLException e) {
                logger.error("Error getting connection from JDBC DataSource for ReportDescriptor " + reportDescriptor, e);
            }
        }
        return realDatasource;
    }

    private static Object getTemplate(ReportDescriptor rd) {
        Object template = rd.getTemplate();
        if (template instanceof FileInfo) {
            template = ((FileInfo) template).getFile();
        }

        return template;
    }

    @Override
    public void export(List<Report> reports, OutputStream outputStream, ReportOutputType outputType, Map exportParameters) {
        try {
            var exporter = buildExporter(outputType, outputStream);

            List<JasperPrint> list = getJasperPrints(reports);

            if (!list.isEmpty()) {
                var items = new ArrayList<ExporterInputItem>();
                list.forEach(jp -> items.add(new SimpleExporterInputItem(jp)));
                //noinspection unchecked
                exporter.setExporterInput(new SimpleExporterInput(items));
            } else if (reports.size() == 1) {
                Report report = reports.getFirst();
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

    private Exporter buildExporter(ReportOutputType reportOuputType, OutputStream outputStream) throws Exception {
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

    private HtmlExporter buildHtmlExporter(OutputStream outputStream) {
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
}
