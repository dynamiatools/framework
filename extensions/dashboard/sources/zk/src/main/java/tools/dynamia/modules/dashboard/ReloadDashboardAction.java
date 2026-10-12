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

package tools.dynamia.modules.dashboard;

import tools.dynamia.actions.ActionEvent;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.viewers.ViewCustomizer;

/**
 * Simple action that reload all dashboard widget
 */
//@InstallAction
public class ReloadDashboardAction extends DashboardAction {

    public ReloadDashboardAction() {
        setName("Reload");
        setImage("refresh");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Dashboard dashboard = (Dashboard) evt.getData();
        dashboard.initWidgets();

        if (dashboard.getViewDescriptor().getViewCustomizerClass() != null) {
            try {
                ViewCustomizer customizer = ObjectOperations.newInstance(dashboard.getViewDescriptor().getViewCustomizerClass());
                customizer.customize(dashboard);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
