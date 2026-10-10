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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.integration.scheduling.SchedulerUtil;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A task started with {@code SchedulerUtil} has no UI, whatever environment started it.
 */
class UIEnvironmentContextCapturerTest {

    @BeforeEach
    void install() {
        var beans = new SimpleObjectContainer("capturer-test");
        beans.addObject(new UIEnvironmentContextCapturer());
        Containers.get().installObjectContainer(beans);
    }

    @AfterEach
    void uninstall() {
        Containers.get().removeAllContainers();
    }

    private static UIEnvironment withMessages(MessageDisplayer displayer) {
        return new UIEnvironment() {
            @Override
            public String name() {
                return "test";
            }

            @Override
            public <S> Optional<S> port(Class<S> spi) {
                return spi == MessageDisplayer.class ? Optional.of(spi.cast(displayer)) : Optional.empty();
            }
        };
    }

    @Test
    void theTaskRunsInNoUiEvenWhenTheCallerHasOne() throws Exception {
        var shown = new ArrayList<String>();
        var env = withMessages(new RecordingDisplayer(shown));

        String inside = UIFacades.with(env, () -> {
            try {
                assertEquals("test", UIFacades.current().name());
                return SchedulerUtil.runWithResult(() -> UIFacades.current().name()).get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        assertEquals("none", inside);
    }

    @Test
    void aFacadeThatNeedsTheUserFailsInsideTheTask() throws Exception {
        var env = withMessages(new RecordingDisplayer(new ArrayList<>()));

        Throwable failure = UIFacades.with(env, () -> {
            try {
                return SchedulerUtil.runWithResult(() -> assertThrows(UIUnavailableException.class,
                        () -> UINavigation.open("/somewhere"))).get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        assertEquals("navigation", ((UIUnavailableException) failure).getPort());
    }

    @Test
    void theCallerKeepsItsEnvironmentAfterStartingTheTask() throws Exception {
        var env = withMessages(new RecordingDisplayer(new ArrayList<>()));

        UIFacades.with(env, () -> {
            try {
                SchedulerUtil.runWithResult(() -> "x").get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            assertEquals("test", UIFacades.current().name());
            return null;
        });
    }
}
