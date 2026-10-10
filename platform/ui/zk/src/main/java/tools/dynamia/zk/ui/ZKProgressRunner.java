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

import tools.dynamia.commons.Callback;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.ui.ProgressRunner;
import tools.dynamia.ui.ProgressTask;
import tools.dynamia.zk.util.LongOperation;

import java.util.function.Consumer;

/**
 * ZK implementation of {@link ProgressRunner}: the task runs as a {@link LongOperation} in the background and a
 * {@link LongOperationMonitorWindow} shows its progress and lets the user stop it.
 */
public class ZKProgressRunner implements ProgressRunner {

    @Override
    public void run(String title, String messageTemplate, ProgressTask task, Callback onFinish, Consumer<Throwable> onError) {
        var monitor = new ProgressMonitor();
        var operation = LongOperation.create()
                .execute(() -> {
                    try {
                        task.run(monitor);
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new IllegalStateException(e.getMessage(), e);
                    }
                })
                .onException(onError::accept);
        if (onFinish != null) {
            operation.onFinish(onFinish);
        }
        operation.start();

        var window = LongOperationMonitorWindow.show(title, operation, monitor);
        if (messageTemplate != null) {
            window.setMessageTemplate(messageTemplate);
        }
    }
}
