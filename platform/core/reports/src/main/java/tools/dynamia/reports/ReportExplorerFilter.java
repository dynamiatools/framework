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

/**
 * The Interface ReportExplorerFilter. Used to filter files when exploring report directories.
 * This interface provides a flexible mechanism for defining custom file filtering criteria
 * when scanning report repositories or file systems for report templates. Filters can be
 * based on file extensions, naming patterns, file sizes, modification dates, or any other
 * file attributes. They are commonly used to exclude temporary files, focus on specific
 * report types, or implement security-based file access controls in report management systems.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * // Filter for JasperReports files only
 * ReportExplorerFilter jasperFilter = file -> 
 *     file.getName().endsWith(".jrxml") || file.getName().endsWith(".jasper");
 * 
 * // Filter for non-hidden files
 * ReportExplorerFilter visibleFilter = file -> !file.getName().startsWith(".");
 * 
 * // Composite filter
 * ReportExplorerFilter compositeFilter = file -> 
 *     jasperFilter.match(file) && visibleFilter.match(file);
 * 
 * // Usage in repository
 * List&lt;File&gt; reports = repository.findReports(compositeFilter);
 * </code>
 *
 * @author Mario A. Serrano Leones
 */
public interface ReportExplorerFilter {

    /**
     * Checks if the given file matches the filter criteria.
     *
     * @param file the file to check
     * @return true if the file matches, false otherwise
     */
    boolean match(File file);
}
