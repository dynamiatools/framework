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

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.io.FileInfo;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class ReportFiller {

    private static LoggingService logger = new SLF4JLoggingService(ReportFiller.class);

    /**
     *
     * @param reportDescriptor
     * @param inMemory
     * @return
     */
    public static Report fill(ReportDescriptor reportDescriptor) {
        Report report = null;
        try {
            Object content = createTemporaryReport(reportDescriptor);
            report = new Report(content, reportDescriptor.getName(), reportDescriptor.getDefaultOutputType());
        } catch (Exception e) {
            throw new ReportFillerException(e);
        }
        return report;
    }

    public static Report fill(ReportDescriptor reportDescriptor, boolean inMemory) {
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

    private static File createTemporaryReport(ReportDescriptor reportDescriptor) throws Exception {
        Object datasource = getDatasource(reportDescriptor);
        File filledReportFile = File.createTempFile("dmreport" + System.currentTimeMillis(), ".jrprint");
        try (FileOutputStream out = new FileOutputStream(filledReportFile)) {
            Object template = getTemplate(reportDescriptor);
            Map<String, Object> params = new HashMap<>();
            params.putAll(reportDescriptor.getParameters());
            
            if (datasource instanceof JRDataSource) {
                JRDataSource jrds = (JRDataSource) datasource;
                if (template instanceof String) {
                    InputStream is = ReportFiller.class.getResourceAsStream(reportDescriptor.getTemplate().toString());
                    JasperFillManager.fillReportToStream(is, out, params, jrds);
                } else if (template instanceof JasperReport) {
                    JasperReport jr = (JasperReport) template;
                    JasperFillManager.fillReportToStream(jr, out, params, jrds);
                } else if (template instanceof File) {
                    File file = (File) template;
                    params.put("CURRENT_DIRECTORY", file.getParentFile().getAbsolutePath());
                    InputStream is = new FileInputStream(file);
                    JasperFillManager.fillReportToStream(is, out, params, jrds);
                } else if (template instanceof URL) {
                    InputStream is = ((URL) template).openStream();
                    JasperFillManager.fillReportToStream(is, out, params, jrds);
                } else {
                    throw new ReportFillerException("Unknow report template type :" + reportDescriptor.getTemplate());
                }
            } else if (datasource instanceof Connection) {
                Connection connection = (Connection) datasource;
                if (template instanceof String) {
                    InputStream is = ReportFiller.class.getResourceAsStream(reportDescriptor.getTemplate().toString());
                    JasperFillManager.fillReportToStream(is, out, params, connection);
                } else if (template instanceof JasperReport) {
                    JasperReport jr = (JasperReport) template;
                    JasperFillManager.fillReportToStream(jr, out, params, connection);
                } else if (template instanceof File) {
                    File file = (File) template;
                    params.put("CURRENT_DIRECTORY", file.getParentFile().getAbsolutePath());
                    InputStream is = new FileInputStream(file);
                    JasperFillManager.fillReportToStream(is, out, params, connection);
                } else if (template instanceof URL) {
                    InputStream is = ((URL) template).openStream();
                    JasperFillManager.fillReportToStream(is, out, params, connection);
                } else {
                    throw new ReportFillerException("Unknow report template type :" + reportDescriptor.getTemplate());
                }
            }
        }

        return filledReportFile;
    }

    private static JasperPrint createInMemoryReport(ReportDescriptor reportDescriptor) throws Exception {
        Object datasource = getDatasource(reportDescriptor);
        JasperPrint jasperPrint = null;

        Object template = getTemplate(reportDescriptor);
        Map<String, Object> params = new HashMap<>();
        params.putAll(reportDescriptor.getParameters());
        if (datasource instanceof JRDataSource) {
            JRDataSource jrds = (JRDataSource) datasource;
            if (template instanceof String) {
                InputStream is = ReportFiller.class.getResourceAsStream(reportDescriptor.getTemplate().toString());
                jasperPrint = JasperFillManager.fillReport(is, params, jrds);
            } else if (template instanceof JasperReport) {
                JasperReport jr = (JasperReport) template;
                jasperPrint = JasperFillManager.fillReport(jr, params, jrds);
            } else if (template instanceof File) {
                File file = (File) template;
                params.put("CURRENT_DIRECTORY", file.getParentFile().getAbsolutePath());
                InputStream is = new FileInputStream(file);
                jasperPrint = JasperFillManager.fillReport(is, params, jrds);
            } else if (template instanceof URL) {
                InputStream is = ((URL) template).openStream();
                jasperPrint = JasperFillManager.fillReport(is, params, jrds);
            } else {
                throw new ReportFillerException("Unknow report template type :" + reportDescriptor.getTemplate());
            }
        } else if (datasource instanceof Connection) {
            Connection connection = (Connection) datasource;
            if (template instanceof String) {
                InputStream is = ReportFiller.class.getResourceAsStream(reportDescriptor.getTemplate().toString());
                jasperPrint = JasperFillManager.fillReport(is, params, connection);
            } else if (template instanceof JasperReport) {
                JasperReport jr = (JasperReport) template;
                jasperPrint = JasperFillManager.fillReport(jr, params, connection);
            } else if (template instanceof File) {
                File file = (File) template;
                params.put("CURRENT_DIRECTORY", file.getParentFile().getAbsolutePath());
                InputStream is = new FileInputStream(file);
                jasperPrint = JasperFillManager.fillReport(is, params, connection);
            } else if (template instanceof URL) {
                InputStream is = ((URL) template).openStream();
                jasperPrint = JasperFillManager.fillReport(is, params, connection);
            } else {
                throw new ReportFillerException("Unknow report template type :" + reportDescriptor.getTemplate());
            }
        }

        return jasperPrint;
    }

    private static Object getDatasource(ReportDescriptor reportDescriptor) {
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
        } else if (dataSource instanceof DataSource) {
            try {
                DataSource jdbcDatasource = (DataSource) dataSource;
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

    private ReportFiller() {
    }
}
