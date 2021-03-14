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

import tools.dynamia.domain.query.Parameters;
import tools.dynamia.integration.Containers;

public abstract class BIRTReportUtils {

    private static final String BIRT_VIEWER_URL = "BirtViewerURL";
    private static final String BIRT_REPORTS_PATH = "BirtReportsPath";
    private static final String BIRT_RESOURCES_PATH = "BirtResourcesPath";

    public static String getViewerURL() {
        Parameters appParams = Containers.get().findObject(Parameters.class);
        return appParams.getValue(BIRT_VIEWER_URL, "");
    }

    public static String getReportsPath() {
        Parameters appParams = Containers.get().findObject(Parameters.class);
        return appParams.getValue(BIRT_REPORTS_PATH, "");
    }

    public static String getResourcesPath() {
        Parameters appParams = Containers.get().findObject(Parameters.class);
        return appParams.getValue(BIRT_RESOURCES_PATH, getReportsPath());
    }

    private BIRTReportUtils() {
    }

}
