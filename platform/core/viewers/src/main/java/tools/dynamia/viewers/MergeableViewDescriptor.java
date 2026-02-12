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
 * Interface for view descriptors that can be merged with another descriptor.
 * <p>
 * This interface extends {@link ViewDescriptor} to provide the capability of merging
 * descriptor configurations. This is useful when you need to combine base descriptors
 * with overrides or extensions, allowing for descriptor composition and reuse.
 * </p>
 * <p>
 * The merging process typically combines fields, layouts, and parameters from another
 * descriptor into the current descriptor, allowing for flexible descriptor inheritance
 * and customization patterns.
 * </p>
 *
 * Example:
 * <pre>{@code
 * MergeableViewDescriptor baseDescriptor = ...;
 * ViewDescriptor customDescriptor = ...;
 *
 * // Merge custom descriptor into base descriptor
 * baseDescriptor.merge(customDescriptor);
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
public interface MergeableViewDescriptor extends ViewDescriptor {

    /**
     * Merges the current descriptor with another view descriptor.
     * <p>
     * This method combines the configuration from the provided descriptor into the
     * current descriptor. The merge strategy (e.g., overwrite, append, or merge)
     * is implementation-specific and may vary depending on the descriptor type.
     * </p>
     * <p>
     * Common merging operations include:
     * <ul>
     *   <li>Adding or overriding fields from the source descriptor</li>
     *   <li>Combining layout configurations</li>
     *   <li>Merging parameters and metadata</li>
     * </ul>
     * </p>
     *
     * @param anotherViewDescriptor the descriptor to merge with
     */
    void merge(ViewDescriptor anotherViewDescriptor);
}
