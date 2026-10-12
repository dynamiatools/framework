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

import org.junit.jupiter.api.Test;
import tools.dynamia.integration.scheduling.SchedulerUtil;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link SchedulerUtil} carries the caller's context into the async task through {@link ScopedValueObjectContainer},
 * and keeps filling the legacy {@link ThreadLocalObjectContainer} for code that still reads it.
 */
class SchedulerUtilContextTest {

    private static final class CurrentUser {
        final String name;

        CurrentUser(String name) {
            this.name = name;
        }
    }

    private static SimpleObjectContainer containerWith(Object object) {
        var container = new SimpleObjectContainer();
        container.addObject(object);
        return container;
    }

    @Test
    void theAsyncTaskSeesTheCallersScopedContext() throws Exception {
        var user = new CurrentUser("ana");

        String seen = ScopedValueObjectContainer.get(containerWith(user), () -> {
            try {
                return SchedulerUtil.runWithResult(() -> ScopedValueObjectContainer.getObject(CurrentUser.class).name)
                        .get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        assertEquals("ana", seen);
    }

    @Test
    void theLegacyThreadLocalContainerIsAlsoFilledInsideTheTask() throws Exception {
        var user = new CurrentUser("luis");

        var fromLegacy = ScopedValueObjectContainer.get(containerWith(user), () -> {
            try {
                return SchedulerUtil.runWithResult(() -> ThreadLocalObjectContainer.getObject(CurrentUser.class))
                        .get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        assertSame(user, fromLegacy);
    }

    @Test
    void getWithContextRestoresThePreviousLegacyContextInsteadOfClearingIt() {
        var outer = containerWith("outer");
        ThreadLocalObjectContainer.set(outer);
        try {
            var wrapped = ScopedValueObjectContainer.get(containerWith(new CurrentUser("x")),
                    () -> SchedulerUtil.getWithContext(() -> {
                    }));

            wrapped.run();

            assertSame(outer, ThreadLocalObjectContainer.get());
        } finally {
            ThreadLocalObjectContainer.clear();
        }
    }

    @Test
    void nothingIsLeftBoundAfterTheTaskRuns() {
        var wrapped = ScopedValueObjectContainer.get(containerWith(new CurrentUser("x")),
                () -> SchedulerUtil.getWithContext(() -> {
                }));

        wrapped.run();

        assertFalse(ScopedValueObjectContainer.isBound());
        assertNull(ThreadLocalObjectContainer.get());
    }
}
