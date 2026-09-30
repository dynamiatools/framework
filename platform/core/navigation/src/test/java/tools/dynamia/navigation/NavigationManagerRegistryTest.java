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
package tools.dynamia.navigation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link NavigationManagerRegistry}: the session-level registry of active
 * {@link NavigationManager} instances that lets a host shell (or any session-level code) enumerate
 * or target a specific open tab/iframe instead of only reaching "the current" implicit instance.
 */
public class NavigationManagerRegistryTest {

    @BeforeEach
    public void resetContainers() {
        Containers.get().removeAllContainers();
    }

    @AfterEach
    public void cleanupContainers() {
        Containers.get().removeAllContainers();
    }

    @Test
    public void registerShouldMakeInstanceFindableById() {
        NavigationManagerRegistry registry = new NavigationManagerRegistry();
        TestNavigationManager manager = new TestNavigationManager();

        registry.register(manager);

        assertSame(manager, registry.find(manager.getId()));
    }

    @Test
    public void registerShouldIgnoreNull() {
        NavigationManagerRegistry registry = new NavigationManagerRegistry();
        registry.register(null);
        assertTrue(registry.getActiveInstances().isEmpty());
    }

    @Test
    public void unregisterShouldRemoveInstance() {
        NavigationManagerRegistry registry = new NavigationManagerRegistry();
        TestNavigationManager manager = new TestNavigationManager();
        registry.register(manager);

        registry.unregister(manager.getId());

        assertNull(registry.find(manager.getId()));
        assertTrue(registry.getActiveInstances().isEmpty());
    }

    @Test
    public void findWithUnknownOrNullIdShouldReturnNull() {
        NavigationManagerRegistry registry = new NavigationManagerRegistry();
        assertNull(registry.find("does-not-exist"));
        assertNull(registry.find(null));
    }

    @Test
    public void getActiveInstancesShouldReflectMultipleRegistrations() {
        NavigationManagerRegistry registry = new NavigationManagerRegistry();
        TestNavigationManager tab1 = new TestNavigationManager();
        TestNavigationManager tab2 = new TestNavigationManager();

        registry.register(tab1);
        registry.register(tab2);

        assertEquals(2, registry.getActiveInstances().size());
        assertTrue(registry.getActiveInstances().contains(tab1));
        assertTrue(registry.getActiveInstances().contains(tab2));
    }

    @Test
    public void getActiveInstancesShouldBeUnmodifiable() {
        NavigationManagerRegistry registry = new NavigationManagerRegistry();
        assertTrue(assertThrowsUnsupported(() -> registry.getActiveInstances().add(new TestNavigationManager())));
    }

    private boolean assertThrowsUnsupported(Runnable action) {
        try {
            action.run();
            return false;
        } catch (UnsupportedOperationException e) {
            return true;
        }
    }

    /**
     * {@code BaseNavigationManager} self-registers on construction when a
     * {@link NavigationManagerRegistry} is reachable through {@link Containers}, which is how
     * registration actually happens outside of tests (no explicit wiring needed by callers).
     */
    @Test
    public void baseNavigationManagerShouldSelfRegisterWhenRegistryIsAvailable() {
        NavigationManagerRegistry registry = new NavigationManagerRegistry();
        SimpleObjectContainer container = new SimpleObjectContainer();
        container.addObject(registry);
        Containers.get().installObjectContainer(container);

        TestNavigationManager manager = new TestNavigationManager();

        assertSame(manager, registry.find(manager.getId()));
    }

    /**
     * Outside a Spring/Containers context (e.g. a plain unit test instantiating a manager directly,
     * as most of this module's own tests do), construction must not fail just because no registry
     * is reachable.
     */
    @Test
    public void baseNavigationManagerShouldNotFailWithoutARegistry() {
        new TestNavigationManager();
    }
}
