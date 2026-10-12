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

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScopedValueObjectContainerTest {

    private static SimpleObjectContainer containerWith(Object... objects) {
        var container = new SimpleObjectContainer();
        for (Object o : objects) {
            container.addObject(o);
        }
        return container;
    }

    @Test
    void isUnboundOutsideAScope() {
        assertFalse(ScopedValueObjectContainer.isBound());
        assertNull(ScopedValueObjectContainer.current());
    }

    @Test
    void bindsTheContainerOnlyWhileTheTaskRuns() {
        var container = containerWith("hello");

        ScopedValueObjectContainer.run(container, () -> {
            assertTrue(ScopedValueObjectContainer.isBound());
            assertSame(container, ScopedValueObjectContainer.current());
            assertEquals("hello", ScopedValueObjectContainer.getObject(String.class));
        });

        assertFalse(ScopedValueObjectContainer.isBound());
    }

    @Test
    void nestedBindingsShadowTheOuterOneAndRestoreIt() {
        var outer = containerWith("outer");
        var inner = containerWith("inner");

        ScopedValueObjectContainer.run(outer, () -> {
            ScopedValueObjectContainer.run(inner, () -> assertEquals("inner", ScopedValueObjectContainer.getObject(String.class)));
            assertSame(outer, ScopedValueObjectContainer.current());
            assertEquals("outer", ScopedValueObjectContainer.getObject(String.class));
        });
    }

    @Test
    void theBindingIsUndoneWhenTheTaskFails() {
        assertThrows(IllegalStateException.class, () -> ScopedValueObjectContainer.run(containerWith("x"), () -> {
            throw new IllegalStateException("boom");
        }));

        assertFalse(ScopedValueObjectContainer.isBound());
    }

    @Test
    void getAndCallReturnTheResultAndCallMayThrowCheckedExceptions() throws Exception {
        var container = containerWith(42);

        assertEquals(42, ScopedValueObjectContainer.get(container, () -> ScopedValueObjectContainer.getObject(Integer.class)));
        assertEquals("checked", ScopedValueObjectContainer.call(container, () -> "checked"));
        var thrown = assertThrows(java.io.IOException.class, () -> ScopedValueObjectContainer.call(container, () -> {
            throw new java.io.IOException("io");
        }));
        assertEquals("io", thrown.getMessage());
    }

    @Test
    void anotherThreadDoesNotInheritTheBinding() throws Exception {
        var seen = new AtomicReference<Boolean>();

        ScopedValueObjectContainer.run(containerWith("x"), () -> {
            try {
                Thread.ofVirtual().start(() -> seen.set(ScopedValueObjectContainer.isBound())).join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertEquals(Boolean.FALSE, seen.get());
    }

    @Test
    void fallsBackToTheSpringContainersWhenTheBoundOneHasNoObject() {
        var fallback = containerWith(3.5d);
        Containers.get().installObjectContainer(fallback);
        try {
            assertEquals(3.5d, ScopedValueObjectContainer.getObject(Double.class));
            ScopedValueObjectContainer.run(containerWith("only-string"), () ->
                    assertEquals(3.5d, ScopedValueObjectContainer.getObject(Double.class)));
        } finally {
            Containers.get().removeAllContainers();
        }
    }

    @Test
    void copyToCopiesTheBoundObjects() {
        var target = new SimpleObjectContainer();

        ScopedValueObjectContainer.run(containerWith("copied"), () -> ScopedValueObjectContainer.copyTo(target));

        assertEquals("copied", target.getObject(String.class));
        assertNotSame(target, ScopedValueObjectContainer.current());
    }

    @Test
    void rejectsANullContainer() {
        assertThrows(IllegalArgumentException.class, () -> ScopedValueObjectContainer.run(null, () -> {
        }));
    }
}
