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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Covers {@link NavigationManager#getId()} / {@link BaseNavigationManager}'s implementation: a
 * stable, framework-agnostic identifier per instance, used to correlate a manager with "which
 * tab/iframe/desktop" it belongs to (see {@link NavigationManagerRegistry}).
 */
public class BaseNavigationManagerIdTest {

    @Test
    public void shouldHaveNonNullId() {
        NavigationManager manager = new TestNavigationManager();
        assertNotNull(manager.getId());
    }

    @Test
    public void shouldBeStableAcrossCalls() {
        NavigationManager manager = new TestNavigationManager();
        assertEquals(manager.getId(), manager.getId());
    }

    @Test
    public void differentInstancesShouldHaveDifferentIds() {
        NavigationManager one = new TestNavigationManager();
        NavigationManager other = new TestNavigationManager();
        assertNotEquals(one.getId(), other.getId());
    }
}
