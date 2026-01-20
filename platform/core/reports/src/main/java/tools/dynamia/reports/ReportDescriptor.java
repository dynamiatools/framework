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

import java.util.Map;

/**
 * Defines the contract for report descriptors in the reporting module.
 * A ReportDescriptor provides all necessary information to generate and render
 * a report, including the template, data source, parameters, and output configuration.
 *
 * <p>Implementations of this interface encapsulate report metadata and allow
 * the reporting engine to process reports in a standardized way.</p>
 *
 * Example:
 * <pre>{@code
 * ReportDescriptor descriptor = new MyReportDescriptor();
 * Object template = descriptor.getTemplate();
 * Map<String, Object> params = descriptor.getParameters();
 * ReportOutputType outputType = descriptor.getDefaultOutputType();
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
public interface ReportDescriptor {

    /**
     * Returns the report template to be used for generating the report.
     * The template can be a file path, an InputStream, a compiled report object,
     * or any format supported by the underlying reporting engine.
     *
     * @return the report template object, typically a JasperReport template or file path
     */
    Object getTemplate();

    /**
     * Returns the parameters to be passed to the report engine during generation.
     * These parameters can include user inputs, configuration values, or any
     * data required by the report template at runtime.
     *
     * @return a map containing parameter names as keys and their values
     */
    Map<String, Object> getParameters();

    /**
     * Returns the data source for the report. This can be a collection of objects,
     * a database connection, a JRDataSource implementation, or any compatible data
     * source that provides the report data.
     *
     * @return the data source object used to populate the report
     */
    Object getDataSource();

    /**
     * Returns the name of the report. This identifier is typically used for
     * display purposes, logging, or file naming when exporting the report.
     *
     * @return the report name as a string
     */
    String getName();

    /**
     * Returns the default output type for the report. This determines the format
     * in which the report will be rendered by default, such as PDF, HTML, Excel, etc.
     *
     * @return the default {@link ReportOutputType} for this report
     */
    ReportOutputType getDefaultOutputType();

    /**
     * Returns parameters specific to the report exporter. These parameters control
     * export-specific behavior, such as page size, orientation, compression settings,
     * or other format-specific options.
     *
     * @return a map containing exporter parameter names as keys and their values
     */
    Map getExporterParameters();

    /**
     * Returns the preferred compiler for the report template. This allows specifying
     * a particular compiler implementation when multiple options are available.
     *
     * @return the preferred compiler name, or null to use the default compiler
     */
    String getPreferedCompiler();
}
