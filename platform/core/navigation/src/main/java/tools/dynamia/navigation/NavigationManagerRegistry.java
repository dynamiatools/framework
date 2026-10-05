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

import org.springframework.context.annotation.Scope;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session-level registry of every {@link NavigationManager} instance currently active for the
 * user's session, keyed by {@link NavigationManager#getId()}.
 * <p>
 * With one {@code NavigationManager} instance per ZK desktop (real browser tab, or one of several
 * {@code <iframe>}s each loading its own ZK page), this registry lets a host shell — or any
 * session-level code — enumerate or look up every tab/iframe currently open, instead of only being
 * able to reach "the current one" implicitly resolved from whatever request/desktop is executing.
 * </p>
 * <p>
 * Registration happens as soon as a manager instance is built (see {@code BaseNavigationManager}), except for
 * managers that opt out through {@code BaseNavigationManager#shouldRegister()}: the ZK manager only registers when it
 * is built inside a desktop, because only a desktop cleanup ever unregisters it.
 * Explicit unregistration (e.g. a ZK desktop being destroyed because its tab/iframe was closed) is
 * best-effort — see the UI-framework-specific wiring for details. It is not required for
 * correctness: since this registry is itself session-scoped, every entry is discarded automatically
 * when the whole session ends, regardless of whether individual instances were unregistered first.
 * </p>
 * <p>
 * The registry holds strong references to desktop-scoped managers, which are only meant to be used inside their own
 * ZK execution. Code that obtains another desktop's manager here (through {@link #find(String)} or
 * {@link #getActiveInstances()}) must not manipulate it outside that desktop's own execution (it is not thread-safe
 * and a desktop may only be touched while it is activated); use the registry to enumerate or identify desktops, not
 * to drive them from another request or thread.
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
@Component
@Scope("session")
public class NavigationManagerRegistry implements Serializable {

    private final Map<String, NavigationManager> instances = new ConcurrentHashMap<>();

    public static NavigationManagerRegistry getInstance() {
        return Containers.get().findObject(NavigationManagerRegistry.class);
    }

    /**
     * Registers a manager instance, keyed by {@link NavigationManager#getId()}.
     *
     * @param manager the instance to register; ignored if null
     */
    public void register(NavigationManager manager) {
        if (manager != null) {
            instances.put(manager.getId(), manager);
        }
    }

    /**
     * Removes a manager instance from the registry.
     *
     * @param id the id of the instance to remove, as returned by {@link NavigationManager#getId()}
     */
    public void unregister(String id) {
        if (id != null) {
            instances.remove(id);
        }
    }

    /**
     * Returns every manager instance currently registered for this session.
     *
     * @return an unmodifiable snapshot-view of the active instances
     */
    public Collection<NavigationManager> getActiveInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    /**
     * Finds a registered manager instance by id.
     *
     * @param id the id, as returned by {@link NavigationManager#getId()}
     * @return the matching instance, or null if none is registered with that id
     */
    public NavigationManager find(String id) {
        return id != null ? instances.get(id) : null;
    }
}
