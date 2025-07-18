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

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * The Interface ReportCompiler. Represents a service that can compile and process reports.
 * This interface defines the contract for report compilation engines that can transform report
 * templates into executable reports and generate various output formats. Report compilers handle
 * the complete report lifecycle including template compilation, data binding, report filling,
 * and export to different formats like PDF, Excel, HTML, or images. They are essential components
 * in business intelligence and document generation systems.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * ReportCompiler compiler = reportEngine.getCompiler("jasper");
 * 
 * // Compile a report template
 * File compiledReport = compiler.compile(new File("invoice.jrxml"));
 * 
 * // Fill report with data
 * ReportDescriptor descriptor = new ReportDescriptor(compiledReport, dataMap);
 * Report filledReport = compiler.fill(descriptor);
 * 
 * // Export to PDF
 * FileOutputStream output = new FileOutputStream("invoice.pdf");
 * compiler.export(Arrays.asList(filledReport), output, 
 *                ReportOutputType.PDF, exportParams);
 * </code>
 *
 * @author Mario A. Serrano Leones
 */
public interface ReportCompiler {

    /**
     * Gets the unique identifier of this compiler.
     *
     * @return the compiler ID
     */
    String getId();

    /**
     * Compile the reportFile
     *
     * @param reportFile the report file to compile
     * @return compiled report
     */
    File compile(File reportFile);

    /**
     * Fills a report with data based on the report descriptor.
     *
     * @param reportDescriptor the report descriptor
     * @return the filled report
     */
    Report fill(ReportDescriptor reportDescriptor);

    /**
     * Fills a report with data based on the report descriptor.
     *
     * @param reportDescriptor the report descriptor
     * @param inMemory whether to process the report in memory
     * @return the filled report
     */
    Report fill(ReportDescriptor reportDescriptor, boolean inMemory);

    /**
     * Exports reports to the specified output stream.
     *
     * @param reports the reports to export
     * @param outputStream the output stream
     * @param outputType the output type
     * @param exportParameters additional export parameters
     */
    void export(List<Report> reports, OutputStream outputStream, ReportOutputType outputType, Map exportParameters);
}
