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
package tools.dynamia.viewers;

import java.util.Collection;

/**
 * Contributes a collection of {@link ViewDescriptor}s to the application descriptor registry.
 *
 * <p>A {@code ViewDescriptorsProvider} is a pluggable source of descriptors. Instead of (or in
 * addition to) loading descriptors from classpath resource files, modules can implement this
 * interface to supply descriptors that are built programmatically or loaded from an external
 * store (e.g., a database, a remote configuration service).</p>
 *
 * <p>Implementations are discovered by the framework via the service container during the
 * loading phase triggered by {@link ViewDescriptorFactory#loadViewDescriptors()}. All descriptors
 * returned by {@link #getDescriptors()} are registered alongside those loaded from files.</p>
 *
 * @see ViewDescriptorFactory#loadViewDescriptors()
 * @see ViewDescriptor
 */
public interface ViewDescriptorsProvider {

    /**
     * Returns the descriptors that this provider contributes to the registry.
     *
     * <p>This method may be called multiple times (e.g., on hot-reload). Implementations should
     * return a fresh, consistent snapshot each time. An empty collection is valid and simply
     * means this provider contributes no descriptors.</p>
     *
     * @return a non-null, possibly empty collection of {@link ViewDescriptor}s
     */
    Collection<ViewDescriptor> getDescriptors();

}
