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
package tools.dynamia.ui;

import tools.dynamia.commons.Callback;

import java.util.function.Consumer;

/**
 * Facade for work that takes long enough to show progress. Same idea as {@link UIMessages}: the action calls this
 * class and the {@link ProgressRunner} of the environment decides how (see {@link UIFacades}).
 *
 * <pre>{@code
 * UIProgress.run("Moving files", monitor -> {
 *     monitor.setMax(files.size());
 *     files.forEach(f -> { move(f); monitor.increment(); });
 * }, () -> UIMessages.showMessage("Done"));
 * }</pre>
 * <p>
 * In ZK the work runs in the background with a progress window and {@code run} returns immediately. In a headless run
 * it runs inside the request and {@code run} returns when it ends, so it is bound by the request timeout. Because a
 * headless action is executed again for every answer of the user, a call to {@code run} must be the last interaction
 * of the action: asking something after it fails with an {@link IllegalStateException}.
 */
public final class UIProgress {

    private UIProgress() {
    }

    /**
     * Runs the task and shows {@code "Error: ..."} if it fails.
     */
    public static void run(String title, ProgressTask task, Callback onFinish) {
        run(title, null, task, onFinish, e -> UIMessages.showMessage("Error: " + e.getMessage(), MessageType.ERROR));
    }

    /**
     * Runs the task.
     *
     * @param title           what the user sees while it runs
     * @param messageTemplate progress text with {0} current and {1} max, may be {@code null}
     * @param task            the work
     * @param onFinish        runs when it completed, may be {@code null}
     * @param onError         runs when it failed
     */
    public static void run(String title, String messageTemplate, ProgressTask task, Callback onFinish, Consumer<Throwable> onError) {
        UIFacades.resolve(ProgressRunner.class).run(title, messageTemplate, task, onFinish, onError);
    }
}
