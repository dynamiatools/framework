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

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import tools.dynamia.commons.Callback;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.integration.ProgressEvent;
import tools.dynamia.integration.scheduling.SchedulerUtil;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Executes a long-running operation asynchronously while safely notifying
 * the UI through a session-scoped EventQueue.
 * <p>
 * This class provides callback hooks for start, execution, finish,
 * cancel, cleanup and exception handling without requiring
 * manual server push or desktop activation.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * LongOperation.create()
 *     .onStart(() -> showBusyIndicator())
 *     .execute(() -> runHeavyTask())
 *     .onFinish(() -> showSuccess())
 *     .onCleanup(() -> hideBusyIndicator())
 *     .start();
 * }</pre>
 */
public class LongOperation implements Runnable {

    private static final LoggingService LOGGER = LoggingService.get(LongOperation.class);

    /**
     * Types of UI events triggered by a running LongOperation.
     */
    public enum LongOpEventType {
        START, FINISH, CANCEL, EXCEPTION, CLEANUP, PROGRESS
    }

    /**
     * Simple UI event payload for queue notifications.
     */
    public static class LongOpEvent extends Event {
        private LongOpEventType type;
        private ProgressEvent progress;
        private Exception error;

        public LongOpEvent(String name) {
            super(name);
        }

        public LongOpEvent(LongOpEventType type, ProgressEvent progress, Exception error) {
            super(type.name());
            this.type = type;
            this.progress = progress;
            this.error = error;
        }

        public LongOpEventType getType() {
            return type;
        }

        public ProgressEvent getProgress() {
            return progress;
        }

        public Exception getError() {
            return error;
        }
    }

    private final UUID taskId = UUID.randomUUID();
    private String name;

    private Callback onStartCallback;
    private Callback executeCallback;
    private Callback onFinishCallback;
    private Callback onCancelCallback;
    private Callback onCleanupCallback;
    private Consumer<Exception> onExceptionConsumer;
    private Consumer<ProgressEvent> onProgressConsumer;

    private CompletableFuture<Void> future;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    /**
     * Create a new LongOperation instance.
     */
    public LongOperation() {
        this.name = "LongOperation-" + taskId;
    }

    /**
     * Assign a human-readable name used for logs and debugging.
     */
    public LongOperation name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Register callback executed before the long task begins.
     */
    public LongOperation onStart(Callback cb) {
        this.onStartCallback = cb;
        return this;
    }

    /**
     * Register long-running asynchronous operation code.
     */
    public LongOperation execute(Callback cb) {
        this.executeCallback = cb;
        return this;
    }

    /**
     * Register callback when task completes successfully.
     */
    public LongOperation onFinish(Callback cb) {
        this.onFinishCallback = cb;
        return this;
    }

    /**
     * Register callback when task is cancelled.
     */
    public LongOperation onCancel(Callback cb) {
        this.onCancelCallback = cb;
        return this;
    }

    /**
     * Register callback when an uncaught exception occurs.
     */
    public LongOperation onException(Consumer<Exception> cb) {
        this.onExceptionConsumer = cb;
        return this;
    }

    /**
     * Register callback always executed at the end.
     */
    public LongOperation onCleanup(Callback cb) {
        this.onCleanupCallback = cb;
        return this;
    }

    /**
     * Register callback to receive UI progress reports (0-100).
     */
    public LongOperation onProgress(Consumer<ProgressEvent> cb) {
        this.onProgressConsumer = cb;
        return this;
    }

    /**
     * Notify the UI of current progress.
     * Safe to call from background execution.
     */
    public void progress(ProgressEvent progress) {
        postEvent(LongOpEventType.PROGRESS, progress, null);
    }

    /**
     * Cancel the running task.
     * It will trigger a CANCEL event on the UI.
     */
    public void cancel() {
        cancelled.set(true);
        if (future != null) future.cancel(true);
        postEvent(LongOpEventType.CANCEL, null, null);
        LOGGER.warn("Task {} cancelled", name);
    }

    /**
     * @return whether this operation has been cancelled
     */
    public boolean isCancelled() {
        return cancelled.get();
    }

    /**
     * Launch the long-running operation.
     * Registers internal UI event listeners automatically.
     */
    public LongOperation start() {
        registerQueueListener();
        postEvent(LongOpEventType.START, null, null);
        LOGGER.info("Starting task: {}", name);
        future = SchedulerUtil.run(this);
        return this;
    }

    @Override
    public void run() {
        try {
            if (!cancelled.get()) safeExecute(executeCallback);
            if (!cancelled.get()) postEvent(LongOpEventType.FINISH, null, null);
        } catch (Exception ex) {
            postEvent(LongOpEventType.EXCEPTION, null, cast(ex));
        } finally {
            postEvent(LongOpEventType.CLEANUP, null, null);
        }
    }

    private Exception cast(Throwable t) {
        return (t instanceof Exception e) ? e : new Exception(t);
    }

    private void safeExecute(Callback cb) {
        if (cb != null) cb.doSomething();
    }

    // ========================
    //   UI Notification Layer
    // ========================

    private EventQueue<LongOpEvent> queue() {
        return EventQueues.lookup(name + "-" + taskId, EventQueues.SESSION, true);
    }

    private void postEvent(LongOpEventType type, ProgressEvent progress, Exception ex) {
        queue().publish(new LongOpEvent(type, progress, ex));
    }

    private void registerQueueListener() {
        queue().subscribe(event -> {
            try {
                switch (event.getType()) {
                    case START -> safeExecute(onStartCallback);
                    case FINISH -> safeExecute(onFinishCallback);
                    case CANCEL -> safeExecute(onCancelCallback);
                    case EXCEPTION -> safeException(event.getError());
                    case PROGRESS -> safeProgress(event.getProgress());
                    case CLEANUP -> cleanup();
                }
            } catch (Exception e) {
                LOGGER.error("Error during UI callback for {}", name, e);
            }
        });
    }

    public void onEvent(EventListener<LongOpEvent> listener) {
        queue().subscribe(listener);
    }

    private void safeException(Exception e) {
        if (onExceptionConsumer != null) onExceptionConsumer.accept(e);
        LOGGER.error("Unhandled exception in task {}", name, e);
    }

    private void safeProgress(ProgressEvent p) {
        if (onProgressConsumer != null && p != null) onProgressConsumer.accept(p);
    }

    private void cleanup() {
        safeExecute(onCleanupCallback);
        removeQueue();
        LOGGER.info("Task {} finished and cleaned", name);
    }

    private void removeQueue() {
        try {
            EventQueues.remove(name + "-" + taskId, EventQueues.SESSION);
        } catch (Exception e) {
            LOGGER.warn("Failed cleaning queue for task {}", name, e);
        }
    }

    /**
     * Factory shortcut
     */
    public static LongOperation create() {
        return new LongOperation();
    }
}
