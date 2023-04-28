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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Window;
import tools.dynamia.reports.ReportOutputType;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.zk.util.ZKUtil;

public class ReportOutputTypeSelector extends Window implements EventListener<Event> {

    private static final String REPORT_OUTPUT_TYPE = "ReportOutputType";
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ReportOutputType selected;

    public void initUI() {
        Hlayout layout = new Hlayout();

        layout.appendChild(createOutputButton(ReportOutputType.PDF));
        layout.appendChild(createOutputButton(ReportOutputType.EXCEL));
        layout.appendChild(createOutputButton(ReportOutputType.PRINTER));
    }

    private Component createOutputButton(ReportOutputType outputType) {
        Button button = new Button();
        button.setAttribute(REPORT_OUTPUT_TYPE, outputType);
        button.setZclass("btn btn-primary");
        ZKUtil.configureComponentIcon("export-" + outputType.getExtension(), button, IconSize.LARGE);

        button.addEventListener(Events.ON_CLICK, this);

        return button;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getTarget() instanceof Button) {
            Button button = (Button) event.getTarget();
            this.selected = (ReportOutputType) button.getAttribute(REPORT_OUTPUT_TYPE);
            Events.sendEvent(new Event(Events.ON_SELECT, this, selected));
        }

    }

    public ReportOutputType getSelected() {
        return selected;
    }

    public void setSelected(ReportOutputType selected) {
        this.selected = selected;
    }

    public static void showSelector(String title, EventListener<Event> onSelectedListener) {
        ReportOutputTypeSelector selector = new ReportOutputTypeSelector();
        selector.addEventListener(Events.ON_SELECT, onSelectedListener);
        ZKUtil.showDialog(title, selector);
    }

}
