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

package tools.dynamia.domain;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for defining metadata descriptors on classes, fields, or methods that can be used
 * to automatically generate view descriptors and UI components.
 *
 * <p>This annotation serves as a powerful mechanism for declaring metadata about domain entities
 * and their fields, which is then used by the Viewer module to automatically generate
 * ViewDescriptor instances. These descriptors drive the automatic creation of user interface
 * views such as forms, tables, trees, and custom views without requiring separate YAML
 * configuration files.</p>
 *
 * <p><b>Key features:</b></p>
 * <ul>
 *   <li>Define metadata directly in Java code alongside entity definitions</li>
 *   <li>Automatic generation of ViewDescriptors by the Viewer module</li>
 *   <li>Support for multiple descriptors per element using {@link Descriptors}</li>
 *   <li>Customize labels, field order, validation, and UI components</li>
 *   <li>Configure view-specific parameters and behaviors</li>
 *   <li>Reduce or eliminate the need for external descriptor YAML files</li>
 * </ul>
 *
 * <p><b>Usage contexts:</b></p>
 * <ul>
 *   <li><b>TYPE level:</b> Define class-wide metadata and default view configurations</li>
 *   <li><b>FIELD level:</b> Customize individual field appearance and behavior in views</li>
 *   <li><b>METHOD level:</b> Include computed properties or getter methods in views</li>
 * </ul>
 *
 * <p><b>Basic usage on a class:</b></p>
 * <pre>{@code
 * @Entity
 * @Descriptor(
 *     label = "Customer",
 *     description = "Customer information management",
 *     view = "form",
 *     fields = {"name", "email", "phone", "address"}
 * )
 * public class Customer {
 *
 *     @Id
 *     @GeneratedValue
 *     private Long id;
 *
 *     private String name;
 *     private String email;
 *     private String phone;
 *     private String address;
 *
 *     // Getters and setters
 * }
 * }</pre>
 *
 * <p><b>Advanced field customization:</b></p>
 * <pre>{@code
 * @Entity
 * public class Product {
 *
 *     @Descriptor(
 *         label = "Product Name",
 *         params = {"required: true", "maxlength: 100"}
 *     )
 *     private String name;
 *
 *     @Descriptor(
 *         label = "Price",
 *         type = "currency",
 *         params = {"min: 0", "precision: 2"}
 *     )
 *     private BigDecimal price;
 *
 *     @Descriptor(
 *         label = "Description",
 *         type = "textarea",
 *         params = {"rows: 5", "cols: 50"}
 *     )
 *     private String description;
 *
 *     @Descriptor(
 *         label = "Active",
 *         type = "checkbox"
 *     )
 *     private boolean active;
 * }
 * }</pre>
 *
 * <p><b>Multiple descriptors for different views:</b></p>
 * <pre>{@code
 * @Entity
 * @Descriptor(
 *     view = "form",
 *     fields = {"name", "email", "phone", "address", "notes"}
 * )
 * @Descriptor(
 *     view = "table",
 *     fields = {"name", "email", "phone"}
 * )
 * public class Contact {
 *     private String name;
 *     private String email;
 *     private String phone;
 *     private String address;
 *     private String notes;
 * }
 * }</pre>
 *
 * <p><b>View-specific parameters:</b></p>
 * <pre>{@code
 * @Descriptor(
 *     view = "form",
 *     viewParams = {
 *         "columns: 3",
 *         "showBorder: true",
 *         "title: Customer Information"
 *     }
 * )
 * public class Customer {
 *     // Fields
 * }
 * }</pre>
 *
 * <p><b>Note:</b> This annotation is {@link Repeatable}, allowing multiple descriptors to be applied
 * to the same element for different views or contexts. When multiple descriptors are needed, they
 * are automatically wrapped in a {@link Descriptors} container annotation.</p>
 *
 * <p><b>Integration with Viewer Module:</b> The Viewer module scans for these annotations at runtime
 * and generates the corresponding ViewDescriptor objects automatically. This eliminates the need for
 * manual descriptor YAML files in many cases, though both approaches can be combined when needed.</p>
 *
 * @see Descriptors
 */
@Target({METHOD, FIELD, TYPE})
@Retention(RUNTIME)
@Repeatable(Descriptors.class)
public @interface Descriptor {

    /**
     * The human-readable label to display for this element in the UI.
     *
     * <p>This label is typically shown as field labels in forms, column headers in tables,
     * or section titles. If not specified, the framework will generate a label from the
     * field or class name.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * @Descriptor(label = "Full Name")
     * private String name;
     *
     * @Descriptor(label = "E-mail Address")
     * private String email;
     * }</pre>
     *
     * @return the display label, or empty string to use the default generated label
     */
    String label() default "";

    /**
     * Specifies the ordered list of fields to include in the generated view descriptor.
     *
     * <p>When applied at the TYPE level, this defines which entity fields should be included
     * in the view and in what order they should appear. This is particularly useful for
     * controlling the layout of forms and tables.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * @Descriptor(
     *     view = "form",
     *     fields = {"firstName", "lastName", "email", "phone", "address"}
     * )
     * public class Person {
     *     // Fields in the order specified above
     * }
     * }</pre>
     *
     * @return array of field names to include in the view, empty array to include all fields
     */
    String[] fields() default {};

    /**
     * Provides a detailed description or help text for this element.
     *
     * <p>This description can be used to display tooltips, help icons, or documentation
     * in the user interface. It's useful for providing additional context or instructions
     * to end users.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * @Descriptor(
     *     label = "Tax ID",
     *     description = "Enter the company's tax identification number without spaces or dashes"
     * )
     * private String taxId;
     * }</pre>
     *
     * @return the description text, or empty string if no description is provided
     */
    String description() default "";

    /**
     * Specifies the view type this descriptor applies to.
     *
     * <p>Common view types include:</p>
     * <ul>
     *   <li><b>form:</b> Detail view for creating/editing single entities</li>
     *   <li><b>table:</b> List view for displaying multiple entities in a table</li>
     *   <li><b>tree:</b> Hierarchical view for tree-structured data</li>
     *   <li><b>custom:</b> Application-specific custom views</li>
     * </ul>
     *
     * <p>When multiple descriptors are used, each can target a different view type.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * @Descriptor(view = "form", fields = {"name", "description", "details"})
     * @Descriptor(view = "table", fields = {"name", "status"})
     * public class Task {
     *     // Fields
     * }
     * }</pre>
     *
     * @return the view type name, or empty string to apply to all views
     */
    String view() default "";

    /**
     * Specifies the component type or data type for rendering this field.
     *
     * <p>This attribute controls which UI component is used to display or edit the field.
     * Common types include:</p>
     * <ul>
     *   <li><b>text, textbox:</b> Standard text input</li>
     *   <li><b>textarea:</b> Multi-line text input</li>
     *   <li><b>number, integer, decimal:</b> Numeric input</li>
     *   <li><b>date, datetime, time:</b> Date/time pickers</li>
     *   <li><b>checkbox, boolean:</b> Boolean checkbox</li>
     *   <li><b>select, combobox:</b> Dropdown selection</li>
     *   <li><b>radio:</b> Radio button group</li>
     *   <li><b>email, url, phone:</b> Specialized text inputs</li>
     *   <li><b>currency, percentage:</b> Formatted numeric inputs</li>
     *   <li><b>file, image:</b> File upload components</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * @Descriptor(type = "email")
     * private String email;
     *
     * @Descriptor(type = "textarea")
     * private String comments;
     *
     * @Descriptor(type = "currency")
     * private BigDecimal price;
     * }</pre>
     *
     * @return the component or data type, or empty string to auto-detect from the field type
     */
    String type() default "";

    /**
     * Additional parameters for customizing field behavior and appearance.
     *
     * <p>Parameters are specified as key-value pairs separated by a colon character.
     * These parameters are used to configure component-specific properties such as
     * validation rules, size constraints, formatting options, and more.</p>
     *
     * <p><b>Common parameters:</b></p>
     * <ul>
     *   <li><b>required:</b> Field is mandatory (true/false)</li>
     *   <li><b>readonly:</b> Field is read-only (true/false)</li>
     *   <li><b>visible:</b> Field visibility (true/false)</li>
     *   <li><b>maxlength:</b> Maximum character length</li>
     *   <li><b>min, max:</b> Numeric range constraints</li>
     *   <li><b>pattern:</b> Regex validation pattern</li>
     *   <li><b>rows, cols:</b> Size for textarea components</li>
     *   <li><b>width, height:</b> Component dimensions</li>
     *   <li><b>span:</b> Column span for grid layouts</li>
     *   <li><b>placeholder:</b> Placeholder text</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * @Descriptor(
     *     label = "Username",
     *     params = {
     *         "required: true",
     *         "maxlength: 50",
     *         "pattern: [a-zA-Z0-9_]+",
     *         "placeholder: Enter username"
     *     }
     * )
     * private String username;
     *
     * @Descriptor(
     *     label = "Description",
     *     type = "textarea",
     *     params = {"rows: 5", "cols: 80", "maxlength: 500"}
     * )
     * private String description;
     * }</pre>
     *
     * @return array of parameter strings in "key: value" format, empty array if no parameters
     */
    String[] params() default {};

    /**
     * Parameters specific to the view container or layout configuration.
     *
     * <p>View parameters control the overall behavior and appearance of the view itself,
     * rather than individual fields. These are typically used at the TYPE level to configure
     * how the entire form, table, or custom view is rendered.</p>
     *
     * <p><b>Common view parameters:</b></p>
     * <ul>
     *   <li><b>columns:</b> Number of columns in form layout</li>
     *   <li><b>title:</b> View title or header text</li>
     *   <li><b>showBorder:</b> Display border around view (true/false)</li>
     *   <li><b>sortable:</b> Enable sorting in tables (true/false)</li>
     *   <li><b>pageable:</b> Enable pagination (true/false)</li>
     *   <li><b>pageSize:</b> Number of rows per page</li>
     *   <li><b>autoclose:</b> Auto-close after save (true/false)</li>
     *   <li><b>width, height:</b> View dimensions</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * @Descriptor(
     *     view = "form",
     *     viewParams = {
     *         "columns: 3",
     *         "title: Customer Registration",
     *         "showBorder: true",
     *         "width: 800px"
     *     }
     * )
     * public class Customer {
     *     // Fields
     * }
     *
     * @Descriptor(
     *     view = "table",
     *     viewParams = {
     *         "sortable: true",
     *         "pageable: true",
     *         "pageSize: 50"
     *     }
     * )
     * public class Product {
     *     // Fields
     * }
     * }</pre>
     *
     * @return array of view parameter strings in "key: value" format, empty array if no parameters
     */
    String[] viewParams() default {};
}

