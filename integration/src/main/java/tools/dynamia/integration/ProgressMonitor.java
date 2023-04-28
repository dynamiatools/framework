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
package tools.dynamia.integration;

import java.util.ArrayList;
import java.util.List;


/**
 * The Class ProgressMonitor.
 */
public class ProgressMonitor {

    /**
     * The max.
     */
    private long max;

    /**
     * The current.
     */
    private long current;

    /**
     * The message.
     */
    private String message;

    /**
     * The callback.
     */
    private ProgressMonitorListener callback;

    /**
     * The messages.
     */
    private final List<String> messages = new ArrayList<>();

    private boolean stopped;

    public ProgressMonitor() {

    }

    /**
     * Instantiates a new progress monitor.
     *
     * @param callback the callback
     */
    public ProgressMonitor(ProgressMonitorListener callback) {
        super();
        this.callback = callback;
    }

    /**
     * Gets the max.
     *
     * @return the max
     */
    public long getMax() {
        return max;
    }

    /**
     * Sets the max.
     *
     * @param max the new max
     */
    public void setMax(long max) {
        this.max = max;
    }

    /**
     * Gets the current.
     *
     * @return the current
     */
    public long getCurrent() {
        return current;
    }

    /**
     * Sets the current.
     *
     * @param current the new current
     */
    public void setCurrent(long current) {
        this.current = current;
        notifyChange();
    }

    public void notifyChange() {
        if (callback != null && !isStopped()) {
            callback.progressChanged(new ProgressEvent(getCurrent(), getMax(), getPercent(), getMessage()));
        }
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     *
     * @param message the new message
     */
    public void setMessage(String message) {
        this.message = message;
        messages.add(message);
    }

    /**
     * Gets the messages.
     *
     * @return the messages
     */
    public List<String> getMessages() {
        return messages;
    }

    public void stop() {
        this.stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void onProgressChanged(ProgressMonitorListener callback) {
        this.callback = callback;
    }

    public int getPercent() {
        double result = ((double) current) / max;
        return (int) (result * 100.0);
    }

}
