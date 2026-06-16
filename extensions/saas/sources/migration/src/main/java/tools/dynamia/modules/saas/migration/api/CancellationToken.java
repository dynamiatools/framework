/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package tools.dynamia.modules.saas.migration.api;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cooperative cancellation signal for long-running migration pipelines.
 *
 * <p>The pipeline checks {@link #isCancelled()} between processing chunks.
 * When cancellation is requested, the pipeline exits cleanly at the next checkpoint.
 *
 * <pre>{@code
 * CancellationToken token = new CancellationToken();
 * // Pass to pipeline; later, from another thread:
 * token.cancel();
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public class CancellationToken {

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private volatile String reason;

    /** Signals the pipeline to stop at the next checkpoint. */
    public void cancel() {
        cancel("Cancelled by user");
    }

    /**
     * Signals cancellation with a reason message.
     *
     * @param reason human-readable reason for cancellation
     */
    public void cancel(String reason) {
        this.reason = reason;
        this.cancelled.set(true);
    }

    /**
     * Returns {@code true} if cancellation has been requested.
     * Pipelines should check this between chunks and exit gracefully.
     */
    public boolean isCancelled() {
        return cancelled.get();
    }

    /** Returns the cancellation reason, or {@code null} if not cancelled. */
    public String getReason() {
        return reason;
    }

    /** Factory method for convenience. */
    public static CancellationToken active() {
        return new CancellationToken();
    }
}

