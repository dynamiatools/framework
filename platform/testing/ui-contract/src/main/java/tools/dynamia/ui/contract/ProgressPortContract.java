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
package tools.dynamia.ui.contract;

import org.junit.jupiter.api.Test;
import tools.dynamia.ui.UIFacades;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.UIProgress;
import tools.dynamia.ui.UIUnavailableException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code UIProgress}: the task runs with no UI, then {@code onFinish} or {@code onError} continues the action.
 * <p>
 * What the task does is recorded in a list of the case itself and not in {@link Effects}: a remote adapter runs the task once,
 * in the background, and the action again for every answer, so the task and the callbacks may not share a run.
 */
public abstract class ProgressPortContract extends PortContract {

    @Test
    void onFinishContinuesAfterTheTaskRanOnce() {
        var task = new CopyOnWriteArrayList<String>();

        var o = driver().run(fx -> UIProgress.run("Working", monitor -> task.add("task"),
                () -> fx.add("finished after " + task.size() + " run(s) of the task")), List.of());

        assertEquals(List.of("task"), task);
        assertEquals(List.of("finished after 1 run(s) of the task"), o.effects());
    }

    @Test
    void aFailingTaskGoesToOnErrorWithTheException() {
        var o = driver().run(fx -> UIProgress.run("Working", null, monitor -> {
            throw new IllegalStateException("boom");
        }, () -> fx.add("finished"), e -> fx.add("error " + e.getMessage())), List.of());

        assertEquals(List.of("error boom"), o.effects());
    }

    @Test
    void noUiFacadeCanBeUsedInsideTheTask() {
        var task = new CopyOnWriteArrayList<String>();

        var o = driver().run(fx -> UIProgress.run("Working", monitor -> {
            try {
                UIMessages.showQuestion("In the task?", () -> {
                });
                task.add("asked");
            } catch (UIUnavailableException e) {
                task.add("unavailable " + e.getPort());
            }
            task.add("environment " + UIFacades.current().name());
        }, null), List.of());

        assertEquals(List.of("unavailable messages", "environment none"), task);
        assertEquals(List.of(), o.asked());
    }

    @Test
    void theTaskCanReportProgressThroughItsMonitor() {
        var task = new CopyOnWriteArrayList<String>();

        driver().run(fx -> UIProgress.run("Working", monitor -> {
            monitor.setMax(3);
            monitor.setCurrent(3);
            task.add("progress " + monitor.getCurrent() + "/" + monitor.getMax());
        }, null), List.of());

        assertEquals(List.of("progress 3/3"), task);
    }
}
