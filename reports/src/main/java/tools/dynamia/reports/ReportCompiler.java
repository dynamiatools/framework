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

public interface ReportCompiler {

    String getId();

    /**
     * Compile the reportFile
     *
     * @return compiled report
     */
    File compile(File reportFile);

    Report fill(ReportDescriptor reportDescriptor);

    Report fill(ReportDescriptor reportDescriptor, boolean inMemory);

    void export(List<Report> reports, OutputStream outputStream, ReportOutputType outputType, Map exportParameters);
}
