/*
 * Copyright (C)  2020. Dynamia Soluciones IT S.A.S - NIT 900302344-1 All Rights Reserved.
 * Colombia - South America
 *
 * This file is free software: you can redistribute it and/or modify it  under the terms of the
 *  GNU Lesser General Public License (LGPL v3) as published by the Free Software Foundation,
 *   either version 3 of the License, or (at your option) any later version.
 *
 *  This file is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *   without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *   See the GNU Lesser General Public License for more details. You should have received a copy of the
 *   GNU Lesser General Public License along with this file.
 *   If not, see <https://www.gnu.org/licenses/>.
 *
 */
package tools.dynamia.modules.reports.ui;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import tools.dynamia.integration.Containers;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleContainer;
import tools.dynamia.navigation.NavigationRestrictions;
import tools.dynamia.modules.reports.core.Reports;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.ui.actions.ViewReportAction;

import java.util.ArrayList;
import java.util.List;

public class ReportListViewModel {

    private List<Reports> reports;
    private ViewReportAction action = Containers.get().findObject(ViewReportAction.class);

    @Init
    public boolean init() {
        return loadReports();
    }

    public boolean loadReports() {
        reports = Reports.loadAll();

        return filterReportsByModules();
    }

    public boolean filterReportsByModules() {
        final ModuleContainer modules = Containers.get().findObject(ModuleContainer.class);
        final List<Reports> toRemove = new ArrayList<>();
        reports.forEach(r -> {
            if (r.getGroup().getModule() != null && !r.getGroup().getModule().isEmpty()) {
                Module module = modules.getModuleById(r.getGroup().getModule());
                if (module != null && !NavigationRestrictions.allowAccess(module)) {
                    toRemove.add(r);
                }

            }
        });

        return reports.removeAll(toRemove);
    }

    @Command
    public void viewReport(@BindingParam("report") Report report) {
        if (report != null) {
            action.view(report, false);
        }
    }

    public List<Reports> getReports() {
        return reports;
    }

    public void setReports(List<Reports> reports) {
        this.reports = reports;
    }

}
