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

/**
 * Cache for pages that belong to <em>dynamic</em> {@link PageGroup}s (see {@link PageGroup#isDynamic()}).
 * <p>
 * Dynamic groups build their pages on demand and the result may differ per user or tenant, so
 * {@link ModuleContainer} must never index them in its global indexes. Implementations scope the cache to
 * whoever owns the current request (typically the HTTP session). Both methods must be safe to call when there
 * is no scope available: {@link #get(String)} returns null and {@link #put(String, Page)} does nothing.
 *
 * @author Mario A. Serrano Leones
 */
public interface DynamicPageCache {

    /**
     * Returns the page cached under the key in the current scope, or null.
     */
    Page get(String key);

    /**
     * Caches the page under the key in the current scope, if there is one.
     */
    void put(String key, Page page);
}
