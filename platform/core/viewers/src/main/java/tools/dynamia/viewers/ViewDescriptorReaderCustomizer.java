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


/**
 * Extension point that allows post-processing of raw parsed content during descriptor loading.
 *
 * <p>A {@code ViewDescriptorReaderCustomizer} is paired with a specific
 * {@link ViewDescriptorReader} implementation and is invoked by that reader after it has
 * parsed the source file content (e.g., the YAML document or XML DOM). This lets third-party
 * modules augment or transform the descriptor without modifying the reader itself.</p>
 *
 * <p>Typical use cases include:
 * <ul>
 *   <li>Injecting additional field definitions from an external source.</li>
 *   <li>Applying security or tenant-specific overrides to the raw content.</li>
 *   <li>Translating proprietary format nodes into standard descriptor constructs.</li>
 * </ul>
 * </p>
 *
 * <p>Implementations are discovered by the framework through the service container and are
 * automatically applied when their {@link #getTargetReader() target reader} is used.</p>
 *
 * @param <T> the type of the raw parsed content (e.g., a YAML {@code Map}, a DOM {@code Document})
 * @see ViewDescriptorReader
 */
public interface ViewDescriptorReaderCustomizer<T> {

    /**
     * Returns the {@link ViewDescriptorReader} class this customizer is associated with.
     *
     * <p>The framework passes this customizer to the {@link ViewDescriptorReader#read} method
     * only when the reader's runtime type matches the class returned here.</p>
     *
     * @return the target reader class; never {@code null}
     */
    Class<? extends ViewDescriptorReader> getTargetReader();

    /**
     * Applies customization logic using the raw parsed {@code content} and the partially or
     * fully built {@code viewDescriptor}.
     *
     * <p>Implementations may modify the {@code viewDescriptor} in-place (e.g., add fields,
     * change parameters) based on information found in {@code content}.</p>
     *
     * @param content        the raw parsed content from the descriptor resource; never {@code null}
     * @param viewDescriptor the descriptor being built; never {@code null}
     */
    void customize(T content, ViewDescriptor viewDescriptor);

}
