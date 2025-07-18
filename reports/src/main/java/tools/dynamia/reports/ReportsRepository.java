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

import tools.dynamia.io.FileInfo;

import java.util.List;

/**
 * The Interface ReportsRepository. Provides access to report files and templates.
 * This interface defines the contract for report storage and retrieval systems that manage
 * report templates, compiled reports, and related resources. Report repositories abstract
 * the underlying storage mechanism (file system, database, cloud storage) and provide
 * standardized access to report resources. They support hierarchical organization through
 * subdirectories and enable efficient report discovery and management in enterprise applications.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * ReportsRepository repository = reportService.getRepository();
 * 
 * // Find a specific report
 * FileInfo reportFile = repository.findReport("invoice-template");
 * 
 * // Find report in subdirectory
 * FileInfo customerReport = repository.findReport("customers", "monthly-summary");
 * 
 * // Scan all available reports
 * List&lt;FileInfo&gt; allReports = repository.scan();
 * 
 * // Scan reports in specific directory
 * List&lt;FileInfo&gt; salesReports = repository.scan("sales");
 * </code>
 *
 * @author Mario A. Serrano Leones
 */
public interface ReportsRepository {

    /**
     * Finds a report by name.
     *
     * @param name the report name
     * @return the file info for the report
     */
	FileInfo findReport(String name);

    /**
     * Finds a report in a specific subdirectory.
     *
     * @param subdirectory the subdirectory to search in
     * @param name the report name
     * @return the file info for the report
     */
	FileInfo findReport(String subdirectory, String name);

    /**
     * Scans for all available reports.
     *
     * @return the list of available reports
     */
	List<FileInfo> scan();

    /**
     * Scans for reports in a specific subdirectory.
     *
     * @param subdirectory the subdirectory to scan
     * @return the list of reports in the subdirectory
     */
	List<FileInfo> scan(String subdirectory);

    /**
     * Gets the location where reports are stored.
     *
     * @return the report location path
     */
	String getReportLocation();

}
