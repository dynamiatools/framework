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

import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.PageGroup;

import java.util.List;

public class DynamiaReportsUserModule extends Module {
    public DynamiaReportsUserModule(String id, String name, String description) {
        super(id, name, description);
    }

    public DynamiaReportsUserModule(String id, String name) {
        super(id, name);
    }

    public DynamiaReportsUserModule(String id, String name, double position) {
        super(id, name);
        setPosition(position);
    }

    public DynamiaReportsUserModule() {

    }

    @Override
    public String getIcon() {
        return "list";
    }

    @Override
    public List<PageGroup> getPageGroups() {
        init();
        return super.getPageGroups();
    }

    private void init() {
        super.getPageGroups().clear();

        ReportListViewModel vm = new ReportListViewModel();
        vm.init();

        vm.getReports().forEach(rg -> {

            final PageGroup pageGroup = new PageGroup("group" + rg.getGroup().getId(), rg.getGroup().getName());
            addPageGroup(pageGroup);
            rg.getList().forEach(report -> pageGroup.addPage(new ReportPage(report)));
        });

    }


}
