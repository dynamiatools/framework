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
import tools.dynamia.commons.Callback;
import tools.dynamia.commons.ClassMessages;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.integration.ProgressEvent;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.util.LongOperation;
import tools.dynamia.zk.util.ZKUtil;

import java.util.Date;

/**
 * A ZK Window that monitors the progress of a long operation.
 */
public class LongOperationMonitorWindow extends Window {

    private static final long serialVersionUID = 1L;

    private final ClassMessages messages = ClassMessages.get(LongOperationMonitorWindow.class);
    private final ProgressMonitor monitor;
    private final LongOperation longOperation;

    private Progressmeter progress;
    private Caption titleCaption;
    private Label messageLabel;

    private String messageTemplate = messages.get("DefaultProgressMessage");
    private Listbox logListbox;
    private boolean showLog;


    public LongOperationMonitorWindow(LongOperation longOperation, ProgressMonitor monitor) {
        this.longOperation = longOperation;
        this.monitor = monitor;
        initUI();
        bindEventListeners();
        setPage(ZKUtil.getFirstPage());
    }

    /**
     * Shows a long operation monitor window.
     *
     * @param title         The title of the monitor window.
     * @param longOperation The long operation to monitor.
     * @param monitor       The progress monitor.
     * @return The LongOperationMonitorWindow instance.
     */
    public static LongOperationMonitorWindow show(String title,
                                                  LongOperation longOperation,
                                                  ProgressMonitor monitor) {
        LongOperationMonitorWindow win = new LongOperationMonitorWindow(longOperation, monitor);
        win.setTitle(title);
        win.setPosition("center");
        win.doModal();
        longOperation.onException(e -> win.detach());
        return win;
    }

    /**
     * Starts a long operation with a progress monitor and shows the monitor window.
     *
     * @param title         The title of the monitor window.
     * @param finishMessage The message to display when the operation finishes.
     * @param op            A consumer that accepts a ProgressMonitor to perform the long operation.
     * @return The LongOperationMonitorWindow instance.
     */
    public static LongOperationMonitorWindow start(String title,
                                                   String finishMessage,
                                                   java.util.function.Consumer<ProgressMonitor> op) {

        return start(title, op, () -> UIMessages.showMessage(finishMessage));
    }

    /**
     * Starts a long operation with a progress monitor and shows the monitor window.
     *
     * @param title    The title of the monitor window.
     * @param op       A consumer that accepts a ProgressMonitor to perform the long operation.
     * @param onFinish A callback to execute when the operation finishes.
     * @return The LongOperationMonitorWindow instance.
     */
    public static LongOperationMonitorWindow start(String title,
                                                   java.util.function.Consumer<ProgressMonitor> op,
                                                   Callback onFinish) {
        var monitor = new ProgressMonitor();
        LongOperation longOp = LongOperation.create()
                .execute(() -> op.accept(monitor))
                .onFinish(onFinish);

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
        if (isShowLog()) {
            var item = logListbox.appendItem(DateTimeUtils.formatTime(new Date()) + " - " + evt.getMessage(), "");
            logListbox.scrollToIndex(item.getIndex());
        }

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

        logListbox = new Listbox();
        logListbox.setVisible(false);
        logListbox.setHeight("150px");
        logListbox.setParent(layout);

        progress = new Progressmeter();
        progress.setHflex("2");
        progress.setValue(0);
        progress.setParent(layout);

        Div msg = new Div();
        msg.setStyle("text-align:center");
        messageLabel = new Label();
        messageLabel.setParent(msg);
        msg.setParent(layout);

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

    protected void stop() {
        try {
            longOperation.onFinish(null);
            monitor.stop();
        } catch (Exception e) {
            LoggingService.get(LongOperationMonitorWindow.class).error("Error stopping long operation", e);
        } finally {
            finish();
        }

    }

    @Override
    public void setTitle(String title) {
        titleCaption.setLabel(title);
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
        logListbox.setVisible(showLog);
    }
}
