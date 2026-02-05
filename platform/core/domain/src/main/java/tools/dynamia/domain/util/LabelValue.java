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

package tools.dynamia.domain.util;

/**
 * Utility class that represents a value paired with a human-readable label.
 * This class is commonly used in UI components like dropdowns, lists, and forms
 * where you need to display a user-friendly label while working with an underlying value.
 *
 * <p>The class supports ordering, classification, and auxiliary values, making it
 * versatile for various data representation scenarios. It implements {@link Comparable}
 * to enable sorting based on the order field.</p>
 *
 * <p>Common use cases include:</p>
 * <ul>
 *   <li>Dropdown/select options in forms</li>
 *   <li>Key-value pairs for configuration</li>
 *   <li>Categorized data with classifiers</li>
 *   <li>Sorted lists with explicit ordering</li>
 * </ul>
 *
 * <pre>{@code
 * // Example 1: Simple label-value pair
 * LabelValue option = new LabelValue("United States", "US");
 *
 * // Example 2: With classifier for grouping
 * LabelValue city = new LabelValue("New York", "NYC", "USA");
 *
 * // Example 3: With explicit ordering
 * LabelValue priority = new LabelValue("High", 1, 10);
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
public class LabelValue implements Comparable<LabelValue> {
    /**
     * The human-readable label displayed to users
     */
    private String label;

    /**
     * The underlying value associated with the label
     */
    private Object value;

    /**
     * Optional auxiliary value for additional data storage
     */
    private Object auxValue;

    /**
     * The order/position for sorting purposes (default: 0)
     */
    private int order;

    /**
     * Optional classifier for grouping or categorizing label-value pairs
     */
    private String classifier;

    /**
     * Optional image URL or path associated with the label-value pair
     */
    private String image;

    /**
     * Default constructor. Creates an empty LabelValue instance.
     */
    public LabelValue() {
    }

    /**
     * Creates a LabelValue with the specified label and value.
     *
     * @param label the human-readable label
     * @param value the underlying value
     *
     *              <pre>{@code
     *                           // Example:
     *                           LabelValue country = new LabelValue("Canada", "CA");
     *                           }</pre>
     */
    public LabelValue(String label, Object value) {
        this.label = label;
        this.value = value;
    }

    /**
     * Creates a LabelValue with label, value, and classifier for grouping.
     *
     * @param label      the human-readable label
     * @param value      the underlying value
     * @param classifier a classifier string for categorizing or grouping this label-value pair
     *
     *                   <pre>{@code
     *                                     // Example: Cities grouped by country
     *                                     LabelValue city = new LabelValue("Toronto", "TOR", "Canada");
     *                                     }</pre>
     */
    public LabelValue(String label, Object value, String classifier) {
        this.label = label;
        this.value = value;
        this.classifier = classifier;
    }

    /**
     * Creates a LabelValue with label, value, and explicit ordering.
     *
     * @param label the human-readable label
     * @param value the underlying value
     * @param order the sort order (lower values appear first when sorted)
     *
     *              <pre>{@code
     *                           // Example: Priority levels with explicit ordering
     *                           LabelValue high = new LabelValue("High Priority", "HIGH", 1);
     *                           LabelValue medium = new LabelValue("Medium Priority", "MEDIUM", 2);
     *                           LabelValue low = new LabelValue("Low Priority", "LOW", 3);
     *                           }</pre>
     */
    public LabelValue(String label, Object value, int order) {
        this.label = label;
        this.value = value;
        this.order = order;
    }

    /**
     * Creates a LabelValue with label, value, order, and classifier for complete control.
     *
     * @param label      the human-readable label
     * @param value      the underlying value
     * @param order      the sort order (lower values appear first when sorted)
     * @param classifier a classifier string for categorizing or grouping this label-value pair
     *
     *                   <pre>{@code
     *                                     // Example: Products with categories and priority
     *                                     LabelValue laptop = new LabelValue("Dell XPS 15", "DELL-XPS-15", 1, "Laptops");
     *                                     LabelValue mouse = new LabelValue("Logitech MX Master", "LOG-MX", 2, "Accessories");
     *                                     }</pre>
     */
    public LabelValue(String label, Object value, int order, String classifier) {
        this.label = label;
        this.value = value;
        this.order = order;
        this.classifier = classifier;
    }

    /**
     * Compares this LabelValue with another based on the order field.
     * This enables sorting of LabelValue objects in collections.
     *
     * @param labelValue the LabelValue to compare to
     * @return a negative integer, zero, or a positive integer as this object's order
     * is less than, equal to, or greater than the specified object's order
     *
     * <pre>{@code
     * // Example:
     * List<LabelValue> items = Arrays.asList(
     *     new LabelValue("Third", 3, 30),
     *     new LabelValue("First", 1, 10),
     *     new LabelValue("Second", 2, 20)
     * );
     * Collections.sort(items);
     * // Result: [First, Second, Third]
     * }</pre>
     */
    @Override
    public int compareTo(LabelValue labelValue) {
        return Integer.compare(order, labelValue.order);
    }

    /**
     * Gets the human-readable label.
     *
     * @return the label string
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the human-readable label.
     *
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets the underlying value associated with this label.
     *
     * @return the value object
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the underlying value associated with this label.
     *
     * @param value the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Gets the sort order value.
     *
     * @return the order value used for sorting (lower values appear first)
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets the sort order value.
     *
     * @param order the order value to set (lower values appear first when sorted)
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Returns the string representation of this LabelValue, which is the label itself.
     *
     * @return the label string
     */
    @Override
    public String toString() {
        return label;
    }

    /**
     * Gets the classifier used for grouping or categorizing this label-value pair.
     *
     * @return the classifier string, or null if not set
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * Sets the classifier for grouping or categorizing this label-value pair.
     *
     * @param classifier the classifier string to set
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    /**
     * Gets the auxiliary value for additional data storage.
     *
     * @return the auxiliary value object, or null if not set
     */
    public Object getAuxValue() {
        return auxValue;
    }

    /**
     * Sets the auxiliary value for additional data storage.
     *
     * @param auxValue the auxiliary value to set
     */
    public void setAuxValue(Object auxValue) {
        this.auxValue = auxValue;
    }

    /**
     * Gets the image URL or path associated with this label-value pair.
     *
     * @return the image string, or null if not set
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets the image URL or path associated with this label-value pair.
     *
     * @param image the image string to set
     */
    public void setImage(String image) {
        this.image = image;
    }

    public static LabelValue of(String label, Object value) {
        return new LabelValue(label, value);
    }

    public LabelValue classifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    public LabelValue image(String image) {
        this.image = image;
        return this;
    }

    public LabelValue order(int order) {
        this.order = order;
        return this;
    }

    public LabelValue auxValue(Object auxValue) {
        this.auxValue = auxValue;
        return this;
    }


}
