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
import org.zkoss.zul.*;
import tools.dynamia.commons.ClassMessages;
import tools.dynamia.commons.Messages;
import tools.dynamia.integration.ProgressEvent;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.util.LongOperation;
import tools.dynamia.zk.util.ZKUtil;

public class LongOperationMonitorWindow extends Window {

    private static final long serialVersionUID = 1L;

    private final ClassMessages messages = ClassMessages.get(LongOperationMonitorWindow.class);
    private final ProgressMonitor monitor;
    private final LongOperation longOperation;

    private Progressmeter progress;
    private Caption titleCaption;
    private Label messageLabel;

    private String messageTemplate = messages.get("DefaultProgressMessage");

    public LongOperationMonitorWindow(LongOperation longOperation, ProgressMonitor monitor) {
        this.longOperation = longOperation;
        this.monitor = monitor;
        initUI();
        bindEventListeners();
        setPage(ZKUtil.getFirstPage());
    }

    public static LongOperationMonitorWindow show(String title,
                                                  LongOperation longOperation,
                                                  ProgressMonitor monitor) {
        LongOperationMonitorWindow win = new LongOperationMonitorWindow(longOperation, monitor);
        win.setTitle(title);
        win.setPosition("center");
        win.doModal();
        return win;
    }

    public static LongOperationMonitorWindow start(String title,
                                                   String finishMessage,
                                                   java.util.function.Consumer<ProgressMonitor> op) {
        var monitor = new ProgressMonitor();
        LongOperation longOp = LongOperation.create()
                .execute(() -> op.accept(monitor))
                .onFinish(() -> UIMessages.showMessage(finishMessage));

        longOp.start();
        return show(title, longOp, monitor);
    }

    private void bindEventListeners() {
        if (monitor != null) {
            monitor.onProgressChanged(longOperation::progress);
        }

        longOperation.onEvent(event -> {
            switch (event.getType()) {
                case START -> showStartUI();
                case PROGRESS -> updateProgress(event.getProgress());
                case FINISH, CANCEL -> finish();
                case EXCEPTION -> {
                    UIMessages.showMessageDialog(messages.get("OperationErrorMessage") + ": " + event.getError().getMessage(),
                            messages.get("OperationErrorTitle"), MessageType.ERROR);
                    finish();
                }
            }
        });

        // Cleanup on window close → cancel op
        addEventListener(Events.ON_CLOSE, evt -> {
            evt.stopPropagation();
            longOperation.cancel();
        });
    }

    private void showStartUI() {
        titleCaption.setIconSclass("fa fa-refresh fa-spin fa-2x");
    }

    private void updateProgress(ProgressEvent evt) {
        progress.setValue(evt.getPercent());
        progress.setTooltiptext(evt.getPercent() + "%");
        messageLabel.setValue(evt.getMessage());

        String title = Messages.format(messageTemplate,
                evt.getCurrent(), evt.getMax(), evt.getPercent());
        setTitle(title);
    }

    private void finish() {
        titleCaption.setIconSclass("fa fa-check");
        detach(); // UI thread safe — EventQueue guarantees it
    }

    private void initUI() {
        setWidth("500px");
        setClosable(true);
        setStyle("padding:10px");

        titleCaption = new Caption("");
        titleCaption.setParent(this);

        Vlayout layout = new Vlayout();
        layout.setHflex("1");
        layout.setParent(this);

        progress = new Progressmeter();
        progress.setHflex("2");
        progress.setValue(0);
        progress.setParent(layout);

        Div msg = new Div();
        msg.setStyle("text-align:center");
        messageLabel = new Label();
        messageLabel.setParent(msg);
        msg.setParent(layout);
    }

    @Override
    public void setTitle(String title) {
        titleCaption.setLabel(title);
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }
}
