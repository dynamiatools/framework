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
package tools.dynamia.zk.reports.ui;

import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.North;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.integration.Containers;
import tools.dynamia.zk.actions.ActionToolbar;
import tools.dynamia.zk.reports.actions.birt.BIRTReportAction;

public class BIRTReportEditor extends Borderlayout {

    /**
     *
     */
    private static final long serialVersionUID = -5164486969432124060L;
    private ActionToolbar actionToolbar;
    private BIRTReportViewer viewer;

    public BIRTReportEditor() {
        initUI();
        initActions();
    }

    private void initActions() {
        for (BIRTReportAction action : Containers.get().findObjects(BIRTReportAction.class)) {
            actionToolbar.addAction(action);
        }

    }

    private void initUI() {
        setHflex("1");
        setVflex("1");

        viewer = new BIRTReportViewer();

        appendChild(new North());
        appendChild(new Center());

        getCenter().appendChild(viewer);

        actionToolbar = new ActionToolbar((source, params) -> new ActionEvent(viewer.getSelectedReport(), this));
        getNorth().appendChild(actionToolbar);
        getNorth().setSclass(ActionToolbar.CONTAINER_SCLASS);

    }

    public void refresh() {
        viewer.refresh();

    }

}
