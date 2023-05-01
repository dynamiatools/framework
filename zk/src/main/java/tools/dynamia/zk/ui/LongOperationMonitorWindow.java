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
package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;
import tools.dynamia.commons.Callback;
import tools.dynamia.commons.ClassMessages;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.StopWatch;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.util.LongOperation;
import tools.dynamia.zk.util.ZKUtil;

import java.util.function.Consumer;

public class LongOperationMonitorWindow extends Window {

    /**
     *
     */
    private static final long serialVersionUID = -2630380982547205553L;
    public static final int DEFAULT_REFRESH_RATE = 2000;
    private final ClassMessages messages = ClassMessages.get(LongOperationMonitorWindow.class);
    private final ProgressMonitor monitor;

    private Progressmeter progress;
    private String messageTemplate = messages.get("DefaultProgressMessage");
    private int refreshRate;
    private final LongOperation longOperation;
    private Caption titleCaption;
    private Label messageLabel;

    public LongOperationMonitorWindow(LongOperation longOperation, ProgressMonitor monitor) {
        this(longOperation, monitor, DEFAULT_REFRESH_RATE);
    }

    public LongOperationMonitorWindow(LongOperation longOperation, ProgressMonitor monitor, int refreshRate) {
        this.longOperation = longOperation;
        this.monitor = monitor;
        this.refreshRate = refreshRate;
        initUI();
        initMonitor();
        setPage(ZKUtil.getFirstPage());
    }

    public static LongOperationMonitorWindow show(String title, LongOperation longOperation, ProgressMonitor monitor) {
        return show(title, longOperation, monitor, DEFAULT_REFRESH_RATE);
    }

    public static LongOperationMonitorWindow show(String title, LongOperation longOperation, ProgressMonitor monitor, int refreshRate) {
        LongOperationMonitorWindow wind = new LongOperationMonitorWindow(longOperation, monitor, refreshRate);
        wind.setTitle(title);
        wind.setPosition("center");
        wind.doModal();
        return wind;
    }

    /**
     * Run and show a progress window for a long-running operation
     *
     */
    public static LongOperationMonitorWindow start(String title, Consumer<ProgressMonitor> operation, Callback onFinish) {
        var monitor = new ProgressMonitor();
        var longOp = LongOperation.create()
                .execute(() -> operation.accept(monitor))
                .onFinish(onFinish);

        longOp.start();

        return show(title, longOp, monitor);
    }

    /**
     * Run and show a progress window for a long-running operation
     *
     */
    public static LongOperationMonitorWindow start(String title, String finishMessage, Consumer<ProgressMonitor> operation) {
        return start(title, operation, () -> UIMessages.showMessage(finishMessage));
    }

    private void initMonitor() {
        if (monitor != null) {
            StopWatch stopWatch = new StopWatch(refreshRate);
            monitor.onProgressChanged(evt -> {
                if ((stopWatch.now() && !monitor.isStopped()) || monitor.getCurrent() >= monitor.getMax()) {
                    longOperation.updateUI(() -> {
                        progress.setValue(evt.getPercent());
                        progress.setTooltiptext(evt.getPercent() + "%");
                        messageLabel.setValue(evt.getMessage());
                        setTitle(Messages.format(messageTemplate, evt.getCurrent(), evt.getMax(), evt.getPercent()));
                    });
                }
            });

            longOperation.onCleanup(this::finish);
        }

    }

    private void finish() {
        if (ZKUtil.isInEventListener()) {
            detach();
        } else {
            longOperation.updateUI(this::detach);
        }

    }

    private void initUI() {

        setWidth("500px");
        setClosable(true);
        setStyle("padding: 10px");
        titleCaption = new Caption("");
        titleCaption.setIconSclass("fa fa-refresh fa-spin fa-2x");
        titleCaption.setParent(this);

        Vlayout layout = new Vlayout();
        layout.setHflex("1");
        layout.setParent(this);

        progress = new Progressmeter();
        progress.setHflex("2");
        progress.setParent(layout);
        progress.setValue(0);

        Div messageContainer = new Div();
        messageContainer.setStyle("text-align: center");
        messageLabel = new Label();
        messageLabel.setParent(messageContainer);
        messageContainer.setParent(layout);


        Hlayout hlayout = new Hlayout();
        hlayout.setHflex("1");
        hlayout.setParent(layout);
        hlayout.setVisible(false);

        Label confirmStopLabel = new Label(messages.get("ConfirmStopProcess"));
        confirmStopLabel.setStyle("font-weight:bold");
        confirmStopLabel.setParent(hlayout);

        Button yesBtn = new Button(messages.get("yes"));
        yesBtn.setZclass("btn btn-success  btn-sm");
        yesBtn.setParent(hlayout);
        yesBtn.addEventListener(Events.ON_CLICK, evt -> stop());

        Button noBtn = new Button(messages.get("no"));
        noBtn.setZclass("btn btn-danger btn-sm");
        noBtn.setParent(hlayout);
        noBtn.addEventListener(Events.ON_CLICK, evt -> hlayout.setVisible(false));

        addEventListener(Events.ON_CLOSE, evt -> {
            evt.stopPropagation();
            hlayout.setVisible(true);
        });

    }

    private void stop() {
        try {
            longOperation.onFinish(null);
            monitor.stop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            finish();
        }

    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    @Override
    public void setTitle(String title) {
        titleCaption.setLabel(title);
    }

}
