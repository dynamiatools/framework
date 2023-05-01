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
package tools.dynamia.zk.reports.ui;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.io.FileInfo;
import tools.dynamia.reports.ReportExplorer;
import tools.dynamia.reports.birt.BIRTExplorerFilter;
import tools.dynamia.reports.repo.DefaultReportRepository;
import tools.dynamia.reports.repo.ReportsRepository;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.reports.BIRTReportUtils;
import tools.dynamia.zk.util.ZKUtil;

import java.text.DateFormat;
import java.util.List;

public class BIRTReportViewer extends Div {

    /**
     *
     */
    private static final long serialVersionUID = -4279530105262380784L;

    private ReportsRepository reportsRepository;

    private String location;
    private String viewerURL;
    private String resourceFolder;
    private Listbox listbox;

    public BIRTReportViewer() {
        try {
            initParameters();
            initRepository();
            initUI();
            initModel();

        } catch (Exception e) {
            UIMessages.showMessage(Messages.get(BIRTReportViewer.class, "NoConfigError"), MessageType.ERROR);
            e.printStackTrace();
        }
    }

    private void initParameters() {
        location = BIRTReportUtils.getReportsPath();
        viewerURL = BIRTReportUtils.getViewerURL();
        resourceFolder = BIRTReportUtils.getResourcesPath();
        if (!viewerURL.endsWith("/")) {
            viewerURL = viewerURL + "/";
        }

    }

    private void initRepository() {
        ReportExplorer explorer = new ReportExplorer(new BIRTExplorerFilter());
        reportsRepository = new DefaultReportRepository(location, explorer);
    }

    private void initModel() {
        List<FileInfo> model = reportsRepository.scan("");
        ZKUtil.fillListbox(listbox, model, true);
    }

    private void initUI() {
        setClass("birtReportViewer");
        setVflex("1");

        listbox = new Listbox();
        listbox.setHflex("1");
        listbox.setVflex("1");

        Listhead head = new Listhead();
        head.setParent(listbox);

        Listheader header = new Listheader("", "", "30px");
        header.setParent(head);

        header = new Listheader(Messages.get(BIRTReportViewer.class, "Reports"));
        header.setParent(head);

        header = new Listheader(Messages.get(BIRTReportViewer.class, "LastUpdate"), "", "250px");
        header.setParent(head);

        listbox.setItemRenderer((item, data, index) -> {
            FileInfo info = (FileInfo) data;
            item.setValue(data);

            String label = info.getDescription();

            Listcell cell = new Listcell(String.valueOf(index + 1));
            cell.setParent(item);

            cell = new Listcell();
            cell.setParent(item);
            A link = renderLink(label, info);
            link.setParent(cell);

            cell = new Listcell(DateFormat.getDateTimeInstance().format(info.getFileDate()));
            cell.setParent(item);
        });

        listbox.setParent(this);

    }

    private A renderLink(String label, FileInfo info) {
        A link = new A(label);
        link.setTooltiptext(Messages.get(BIRTReportViewer.class, "ViewReport"));
        link.setClass("birtReportLink");
        link.addEventListener(Events.ON_CLICK, evt -> viewReport(label, info));
        return link;
    }

    private void viewReport(String label, FileInfo info) {
        String reportURL = viewerURL +
                "frameset?__report=" +
                StringUtils.cleanPath(info.getFile().getAbsolutePath()) +
                "&__resourceFolder=" +
                resourceFolder +
                "&__title=" +
                label;

        Iframe frame = new Iframe(reportURL);
        frame.setHflex("1");
        frame.setVflex("1");

        ZKUtil.showDialog(Messages.get(BIRTReportViewer.class, "ViewReport"), frame, "99%", "99%");

    }

    public FileInfo getSelectedReport() {
        try {
            return listbox.getSelectedItem().getValue();
        } catch (Exception e) {
            return null;
        }
    }

    public void refresh() {
        initModel();

    }

}
