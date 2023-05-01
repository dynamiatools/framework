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
package tools.dynamia.zk.util;

import org.springframework.core.task.TaskExecutor;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.DesktopUnavailableException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.sys.DesktopCache;
import org.zkoss.zk.ui.sys.DesktopCtrl;
import org.zkoss.zk.ui.sys.WebAppCtrl;
import tools.dynamia.commons.Callback;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class LongOperation implements Runnable {

    private String desktopId;
    private DesktopCache desktopCache;
    private Thread thread;
    private TaskExecutor taskExecutor;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private Callback onFinishCallbak;
    private Callback onCancelCallback;
    private Consumer<Exception> onExceptionConsumer;
    private Callback onCleanupCallback;
    private Callback executeCallback;
    private Callback onStartCallback;

    public LongOperation() {

    }

    public LongOperation(TaskExecutor taskExecutor) {
        super();
        this.taskExecutor = taskExecutor;
    }

    public LongOperation onStart(Callback onStart) {
        this.onStartCallback = onStart;
        return this;
    }

    /**
     * asynchronous callback for your long operation code
     */
    public LongOperation execute(Callback executeCallback) {
        this.executeCallback = executeCallback;
        return this;
    }

    /**
     * optional callback method when the task has completed successfully
     */
    public LongOperation onFinish(Callback onFinishCallback) {
        this.onFinishCallbak = onFinishCallback;
        return this;
    }

    /**
     * optional callback method when the task has been cancelled or was
     * interrupted otherwise
     */
    public LongOperation onCancel(Callback onCancelCallback) {
        this.onCancelCallback = onCancelCallback;
        return this;
    }

    /**
     * optional callback method when the task has completed with an uncaught
     * Excepion
     *
     */
    public LongOperation onException(Consumer<Exception> onExceptionConsumer) {
        this.onExceptionConsumer = onExceptionConsumer;
        return this;
    }

    /**
     * optional callback method when the task has completed (always called)
     */
    public LongOperation onCleanup(Callback onCleanupCallback) {
        this.onCleanupCallback = onCleanupCallback;
        return this;
    }

    /**
     * set the cancelled flag and try to interrupt the thread
     */
    public final void cancel() {
        cancelled.set(true);
        if (thread != null) {
            thread.interrupt();
        }
    }

    /**
     * check the cancelled flag
     *
     */
    public final boolean isCancelled() {
        return cancelled.get();
    }

    /**
     * activate the thread (and cached desktop) for UI updates call
     * {@link #deactivate()} once done updating the UI
     *
     */
    protected final void activate() throws InterruptedException {
        Executions.activate(getDesktop());
    }

    /**
     * deactivate the current active (see: {@link #activate()}) thread/desktop
     * after updates are done
     */
    protected final void deactivate() {
        Executions.deactivate(getDesktop());
    }

    /**
     * Checks if the task thread has been interrupted. Use this to check whether
     * or not to exit a busy operation in case.
     *
     * @throws InterruptedException when the current task has been
     *                              cancelled/interrupted
     */
    protected final void checkCancelled() throws InterruptedException {
        if (Thread.currentThread() != this.thread && taskExecutor == null) {
            throw new IllegalStateException("this method can only be called in the worker thread (i.e. during execute)");
        }
        boolean interrupted = Thread.interrupted();
        if (interrupted || cancelled.get()) {
            cancelled.set(true);
            throw new InterruptedException();
        }
    }

    /**
     * launch the long operation
     */
    public final LongOperation start() {
        // not caching the desktop directly to enable garbage collection, in
        // case the desktop destroyed during the long operation
        this.desktopId = Executions.getCurrent().getDesktop().getId();
        this.desktopCache = ((WebAppCtrl) WebApps.getCurrent()).getDesktopCache(Sessions.getCurrent());
        enableServerPushForThisTask();
        if (taskExecutor == null) {
            thread = new Thread(this);
            thread.start();
        } else {
            taskExecutor.execute(this);
        }
        return this;
    }

    @Override
    public final void run() {
        try {
            try {
                runCallback(onStartCallback);
                checkCancelled(); // avoid unnecessary execution
                execute();
                checkCancelled(); // final cancelled check before calling
                // onFinish
                activate();
                finish();
                deactivate();
            } catch (InterruptedException e) {
                try {
                    cancelled.set(true);
                    activate();
                    runCallback(onCancelCallback);
                    deactivate();
                } catch (InterruptedException e1) {
                    throw new RuntimeException("interrupted onCancel handling", e1);
                }
            } catch (Exception rte) {
                try {
                    activate();
                    if (onExceptionConsumer != null) {
                        onExceptionConsumer.accept(rte);
                    }
                    deactivate();
                } catch (InterruptedException e1) {
                    throw new RuntimeException("interrupted onException handling", e1);
                }
                throw rte;
            }
        } finally {
            updateUI(onCleanupCallback);
            disableServerPushForThisTask();
        }
    }

    protected void finish() {
        runCallback(onFinishCallbak);
    }

    protected void execute() {
        runCallback(executeCallback);
    }

    protected void runCallback(Callback callback) {
        if (callback != null) {
            callback.doSomething();
        }

    }

    private final UUID taskId = UUID.randomUUID();

    private void enableServerPushForThisTask() {
        ((DesktopCtrl) getDesktop()).enableServerPush(true, taskId);
    }

    private void disableServerPushForThisTask() {
        ((DesktopCtrl) getDesktop()).enableServerPush(false, taskId);
    }

    private Desktop getDesktop() {
        return desktopCache.getDesktop(desktopId);
    }

    public void updateUI(Callback callback) {
        try {
            activate();
            runCallback(callback);
            deactivate();
        } catch (DesktopUnavailableException | InterruptedException e) {

            e.printStackTrace();
        }
    }

    public static LongOperation create() {
        return new LongOperation();
    }

    public static LongOperation create(TaskExecutor taskExecutor) {
        return new LongOperation(taskExecutor);
    }

}
