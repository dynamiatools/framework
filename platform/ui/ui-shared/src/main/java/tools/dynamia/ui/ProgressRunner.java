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
 * SPI behind {@link UIProgress}. ZK runs the task in a background thread and shows a progress window; a headless run
 * runs it right away, inside the request.
 */
@UIPort(name = "progress")
public interface ProgressRunner {

    /**
     * @param title           what the user sees while it runs
     * @param messageTemplate progress text with {0} current and {1} max, may be {@code null}
     * @param task            the work
     * @param onFinish        runs when the task completed, may be {@code null}
     * @param onError         runs when the task failed, never {@code null}
     */
    void run(String title, String messageTemplate, ProgressTask task, Callback onFinish, Consumer<Throwable> onError);
}
