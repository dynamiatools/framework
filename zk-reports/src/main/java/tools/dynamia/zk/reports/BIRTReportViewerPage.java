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
package tools.dynamia.zk.reports;

import tools.dynamia.navigation.RendereablePage;
import tools.dynamia.zk.reports.ui.BIRTReportViewer;

public class BIRTReportViewerPage extends RendereablePage<BIRTReportViewer> {

    /**
     *
     */
    private static final long serialVersionUID = -4954057497249174494L;

    public BIRTReportViewerPage() {
        super();

    }

    public BIRTReportViewerPage(String id, String name) {
        super(id, name, "");

    }

    @Override
    public BIRTReportViewer renderPage() {
        return new BIRTReportViewer();
    }

}
