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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReportExplorer {

    private ReportExplorerFilter filter;
    private ReportCompiler reportCompiler;

    public ReportExplorer() {

    }

    public ReportExplorer(ReportExplorerFilter filter, ReportCompiler reportCompiler) {
        super();
        this.filter = filter;
        this.reportCompiler = reportCompiler;
    }

    public ReportExplorer(ReportExplorerFilter filter) {
        this(filter, null);
    }

    public List<FileInfo> scan(File location) {
        return scan(location, true);
    }

    public List<FileInfo> scan(File location, boolean autoCompile) {
        if (!location.isDirectory()) {
            throw new IllegalArgumentException("Report's location must be a directory");
        }

        List<FileInfo> reports = new ArrayList<>();
        for (File file : location.listFiles()) {
            if (file.isDirectory()) {
                List<FileInfo> subreports = scan(file, autoCompile);
                reports.addAll(subreports);
            } else if (filter != null && filter.match(file)) {
                if (autoCompile && reportCompiler != null) {
                    file = reportCompiler.compile(file);
                }
                reports.add(new FileInfo(file));
            }
        }
        return reports;

    }

    public FileInfo find(File location, String name) {
        return find(location, name, true);
    }

    public FileInfo find(File location, String name, boolean autoCompile) {
        if (!location.isDirectory()) {
            throw new IllegalArgumentException("Report's location must be a directory");
        }

        File file = new File(location, name);
        if (file.exists()) {
            if (autoCompile && reportCompiler != null) {
                try {
                    file = reportCompiler.compile(file);
                } catch (Exception e) {
                    // ignore
                }
            }
            return new FileInfo(file);
        }
        return null;
    }

}
