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
 * Interface for objects that can be indexed with a numeric position.
 * <p>
 * This interface defines a simple contract for objects that need to maintain an ordinal
 * position or sequence number. It is commonly used for managing the order of fields,
 * components, or other elements within a collection that requires explicit positioning.
 * </p>
 * <p>
 * Implementations typically use the index to control display order, sorting, or
 * processing sequence of elements in a view or descriptor.
 * </p>
 *
 * Example:
 * <pre>{@code
 * public class Field implements Indexable {
 *     private int index;
 *
 *     public int getIndex() {
 *         return index;
 *     }
 *
 *     public void setIndex(int index) {
 *         this.index = index;
 *     }
 * }
 *
 * // Sort fields by index
 * List<Field> fields = ...;
 * fields.sort(Comparator.comparingInt(Indexable::getIndex));
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
public interface Indexable {

    /**
     * Gets the index value representing the position of this object.
     * <p>
     * The index is typically used to determine the order in which objects are
     * displayed, processed, or sorted. Lower index values usually indicate
     * earlier positions in the sequence.
     * </p>
     *
     * @return the index value
     */
    int getIndex();

    /**
     * Sets the index value representing the position of this object.
     * <p>
     * This method allows the position to be updated, which may affect the
     * ordering of objects in collections or views.
     * </p>
     *
     * @param index the new index value
     */
    void setIndex(int index);
}
