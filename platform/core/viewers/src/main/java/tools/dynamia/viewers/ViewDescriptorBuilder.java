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

import tools.dynamia.commons.MapBuilder;
import tools.dynamia.viewers.impl.DefaultViewDescriptor;
import tools.dynamia.viewers.util.Viewers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Builder class for creating and configuring ViewDescriptor instances using a fluent API.
 * This builder provides a convenient way to programmatically construct view descriptors
 * for forms, tables, and other view types without using YAML configuration files.
 *
 * <p>The builder follows the Builder pattern, allowing method chaining for a more
 * readable and maintainable code structure. It supports field configuration, layout
 * customization, field grouping, and parameter specification.</p>
 *
 * Example usage:
 * <pre>{@code
 * ViewDescriptor descriptor = ViewDescriptorBuilder
 *     .viewDescriptor("form", Customer.class)
 *     .fields(
 *         field("name").label("Customer Name"),
 *         field("email").component("email"),
 *         field("phone")
 *     )
 *     .layout("columns", 2)
 *     .build();
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 * @see ViewDescriptor
 * @see FieldBuilder
 * @see FieldGroupBuilder
 */
@SuppressWarnings("unchecked")
public class ViewDescriptorBuilder {

    /**
     * The internal view descriptor being built.
     */
    protected DefaultViewDescriptor descriptor;

    /**
     * Protected constructor to enforce the use of static factory methods.
     */
    protected ViewDescriptorBuilder() {
    }

    /**
     * Creates a new ViewDescriptorBuilder for building view descriptors programmatically.
     *
     * <p>This factory method initializes a builder with the specified view type and bean class,
     * allowing you to control whether fields should be automatically detected from the bean class.</p>
     *
     * @param type       the view type (e.g., "form", "table", "tree")
     * @param beanClass  the Java class representing the entity or bean for this view
     * @param autofields if true, fields will be automatically detected from the bean class;
     *                   if false, only explicitly added fields will be included
     * @return a new ViewDescriptorBuilder instance configured with the specified parameters
     *
     * Example:
     * <pre>{@code
     * ViewDescriptorBuilder builder = ViewDescriptorBuilder
     *     .viewDescriptor("table", Product.class, true);
     * }</pre>
     */
    public static ViewDescriptorBuilder viewDescriptor(String type, Class beanClass, boolean autofields) {
        ViewDescriptorBuilder builder = new ViewDescriptorBuilder();
        builder.descriptor = new DefaultViewDescriptor(beanClass, type, autofields);
        return builder;
    }

    /**
     * Creates a new ViewDescriptorBuilder with automatic field detection enabled by default.
     *
     * <p>This is a convenience method that calls {@link #viewDescriptor(String, Class, boolean)}
     * with autofields set to true.</p>
     *
     * @param type      the view type (e.g., "form", "table", "tree")
     * @param beanClass the Java class representing the entity or bean for this view
     * @return a new ViewDescriptorBuilder instance with autofields enabled
     *
     * Example:
     * <pre>{@code
     * ViewDescriptor descriptor = ViewDescriptorBuilder
     *     .viewDescriptor("form", Customer.class)
     *     .layout("columns", 3)
     *     .build();
     * }</pre>
     */
    public static ViewDescriptorBuilder viewDescriptor(String type, Class beanClass) {
        ViewDescriptorBuilder builder = new ViewDescriptorBuilder();
        builder.descriptor = new DefaultViewDescriptor(beanClass, type);
        return builder;
    }

    /**
     * Creates a new ViewDescriptorBuilder without a bean class, useful for generic views.
     *
     * <p>This method is suitable when creating view descriptors that don't map to a specific
     * Java bean or entity, such as custom views or composite views.</p>
     *
     * @param type the view type (e.g., "form", "table", "custom")
     * @return a new ViewDescriptorBuilder instance without a bean class
     */
    public static ViewDescriptorBuilder viewDescriptor(String type) {
        ViewDescriptorBuilder builder = new ViewDescriptorBuilder();
        builder.descriptor = new DefaultViewDescriptor(null, type);
        return builder;
    }

    /**
     * Sets the unique identifier for this view descriptor.
     *
     * @param id the unique identifier for the view descriptor
     * @return this builder instance for method chaining
     */
    public ViewDescriptorBuilder id(String id) {
        descriptor.setId(id);
        return this;
    }

    /**
     * Sets a custom ViewCustomizer class for this view descriptor.
     *
     * <p>ViewCustomizers allow for dynamic modification of view descriptors at runtime,
     * enabling custom logic to adjust field visibility, layout, or other view properties.</p>
     *
     * @param customizer the ViewCustomizer class to apply custom modifications
     * @return this builder instance for method chaining
     */
    public ViewDescriptorBuilder customizer(Class<? extends ViewCustomizer> customizer) {
        descriptor.setViewCustomizerClass(customizer);
        return this;
    }

    /**
     * Defines the order in which fields should be displayed in the view.
     *
     * <p>This method allows you to specify a custom ordering for the fields,
     * overriding their default or alphabetical order.</p>
     *
     * @param fields the field names in the desired display order
     * @return this builder instance for method chaining
     */
    public ViewDescriptorBuilder sortFields(String... fields) {
        descriptor.sortFields(Arrays.asList(fields));
        return this;
    }

    /**
     * Marks the specified fields as hidden in the view.
     *
     * <p>Hidden fields will not be displayed to the user but may still be present
     * in the underlying data structure for processing purposes.</p>
     *
     * @param fields the names of the fields to hide
     * @return this builder instance for method chaining
     */
    public ViewDescriptorBuilder hidden(String... fields) {
        descriptor.hideFields(fields);
        return this;
    }

    /**
     * Adds multiple fields to the view descriptor using FieldBuilder instances.
     *
     * <p>This method allows you to configure fields with specific properties such as labels,
     * components, and parameters before adding them to the view.</p>
     *
     * @param fields an array of FieldBuilder instances defining the fields to add
     * @return this builder instance for method chaining
     *
     * Example:
     * <pre>{@code
     * builder.fields(
     *     field("name").label("Full Name"),
     *     field("email").component("email"),
     *     field("phone")
     * );
     * }</pre>
     */
    public ViewDescriptorBuilder fields(FieldBuilder... fields) {
        for (FieldBuilder field : fields) {
            descriptor.addField(field.build());
        }
        return this;
    }

    /**
     * Adds fields to the view descriptor using a Supplier that provides a list of Field instances.
     *
     * <p>This method is useful when fields are generated dynamically or retrieved from an
     * external source at runtime.</p>
     *
     * @param fieldsSupplier a Supplier that returns a list of Field instances to add
     * @return this builder instance for method chaining
     *
     * Example:
     * <pre>{@code
     * builder.fields(() -> getFieldsFromConfiguration());
     * }</pre>
     */
    public ViewDescriptorBuilder fields(Supplier<List<Field>> fieldsSupplier) {
        fieldsSupplier.get().forEach(descriptor::addField);
        return this;
    }

    /**
     * Adds field groups to the view descriptor for organizing fields into logical sections.
     *
     * <p>Field groups help structure complex forms by grouping related fields together,
     * improving the user interface organization and usability.</p>
     *
     * @param groups an array of FieldGroupBuilder instances defining the field groups
     * @return this builder instance for method chaining
     *
     * Example:
     * <pre>{@code
     * builder.groups(
     *     group("personal", "Personal Information").fields("name", "email"),
     *     group("address", "Address Details").fields("street", "city", "zipCode")
     * );
     * }</pre>
     */
    public ViewDescriptorBuilder groups(FieldGroupBuilder... groups) {
        for (FieldGroupBuilder fieldGroupBuilder : groups) {
            FieldGroup group = fieldGroupBuilder.build();
            for (String field : fieldGroupBuilder.getFields()) {
                group.addField(descriptor.getField(field));
            }
        }
        return this;
    }

    /**
     * Adds multiple parameters to the view descriptor as key-value pairs.
     *
     * <p>Parameters are used to configure view-level settings and behaviors. This method
     * accepts varargs in key-value format (key1, value1, key2, value2, ...).</p>
     *
     * @param keyValue the parameters in key-value pairs (must be an even number of arguments)
     * @return this builder instance for method chaining
     *
     * Example:
     * <pre>{@code
     * builder.params("readOnly", true, "showHeader", false);
     * }</pre>
     */
    public ViewDescriptorBuilder params(Object... keyValue) {
        Map params = MapBuilder.put(keyValue);
        //noinspection unchecked
        descriptor.getParams().putAll(params);
        return this;
    }

    /**
     * Adds a single global parameter to the view descriptor.
     *
     * <p>This method allows you to add individual parameters one at a time,
     * which is useful for conditional parameter addition.</p>
     *
     * @param key   the parameter key or name
     * @param value the parameter value
     * @return this builder instance for method chaining
     *
     * Example:
     * <pre>{@code
     * builder.addParam("customStyle", "border: 1px solid red");
     * }</pre>
     */
    public ViewDescriptorBuilder addParam(String key, Object value) {
        descriptor.getParams().put(key, value);
        return this;
    }

    /**
     * Configures layout parameters for the view descriptor.
     *
     * <p>Layout parameters control how fields are arranged and displayed in the view.
     * Common parameters include columns, spacing, alignment, and responsive behavior.</p>
     *
     * @param keyValue the layout parameters in key-value pairs (key1, value1, key2, value2, ...)
     * @return this builder instance for method chaining
     *
     * Example:
     * <pre>{@code
     * builder.layout("columns", 3, "spacing", 10);
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    public ViewDescriptorBuilder layout(Object... keyValue) {
        Map params = MapBuilder.put(keyValue);
        //noinspection unchecked
        descriptor.getLayout().getParams().putAll(params);
        return this;
    }

    /**
     * Adds a single parameter to the layout configuration.
     *
     * <p>This method allows you to add individual layout parameters one at a time,
     * which is useful for conditional layout configuration.</p>
     *
     * @param key   the layout parameter key or name
     * @param value the layout parameter value
     * @return this builder instance for method chaining
     *
     * Example:
     * <pre>{@code
     * builder.addLayoutParam("responsive", true);
     * }</pre>
     */
    public ViewDescriptorBuilder addLayoutParam(String key, Object value) {
        descriptor.getLayout().getParams().put(key, value);
        return this;
    }

    /**
     * Creates a new FieldBuilder instance for defining a field in the view.
     *
     * <p>This factory method creates a basic field with the specified name.
     * Additional properties like label and component can be configured using the builder's fluent API.</p>
     *
     * @param name the field name (typically matches a property name in the bean class)
     * @return a new FieldBuilder instance for configuring the field
     *
     * Example:
     * <pre>{@code
     * FieldBuilder nameField = field("name");
     * }</pre>
     */
    public static FieldBuilder field(String name) {
        return field(name, null);
    }

    /**
     * Creates a new FieldBuilder instance with a custom label.
     *
     * <p>This factory method allows you to specify a user-friendly label for the field,
     * which will be displayed in the UI instead of the raw field name.</p>
     *
     * @param name  the field name (typically matches a property name in the bean class)
     * @param label the display label for the field (human-readable text)
     * @return a new FieldBuilder instance configured with the specified name and label
     *
     * Example:
     * <pre>{@code
     * FieldBuilder emailField = field("email", "Email Address");
     * }</pre>
     */
    public static FieldBuilder field(String name, String label) {
        return field(name, label, null);
    }

    /**
     * Creates a new FieldBuilder instance with label and custom component type.
     *
     * <p>This factory method allows full customization of the field by specifying
     * the display label and the UI component type to be used for rendering.</p>
     *
     * @param name      the field name (typically matches a property name in the bean class)
     * @param label     the display label for the field (human-readable text)
     * @param component the component type to use for rendering (e.g., "textbox", "email", "datepicker")
     * @return a new FieldBuilder instance configured with the specified properties
     *
     * Example:
     * <pre>{@code
     * FieldBuilder passwordField = field("password", "Password", "password");
     * }</pre>
     */
    public static FieldBuilder field(String name, String label, String component) {
        FieldBuilder fb = new FieldBuilder(name);
        fb.label(label).component(component);
        return fb;
    }

    /**
     * Creates a new FieldGroupBuilder for organizing fields into a named group.
     *
     * <p>This factory method creates a field group using the provided name as both
     * the internal identifier and the display label.</p>
     *
     * @param name the group name (used as both identifier and display label)
     * @return a new FieldGroupBuilder instance for configuring the field group
     */
    public static FieldGroupBuilder group(String name) {
        return group(name, name);
    }

    /**
     * Creates a new FieldGroupBuilder with a custom display label.
     *
     * <p>This factory method creates a field group with separate name (identifier)
     * and label (display text) values.</p>
     *
     * @param name  the group identifier (used internally)
     * @param label the display label for the group (shown to users)
     * @return a new FieldGroupBuilder instance configured with name and label
     */
    public static FieldGroupBuilder group(String name, String label) {
        return group(name, label, null);
    }

    /**
     * Creates a new FieldGroupBuilder with label and icon.
     *
     * <p>This factory method creates a fully customized field group with name,
     * display label, and an optional icon for visual representation.</p>
     *
     * @param name  the group identifier (used internally)
     * @param label the display label for the group (shown to users)
     * @param icon  the icon identifier or path to display with the group (optional)
     * @return a new FieldGroupBuilder instance configured with name, label, and icon
     */
    public static FieldGroupBuilder group(String name, String label, String icon) {
        return new FieldGroupBuilder(name);
    }

    /**
     * Builds and returns the configured ViewDescriptor instance.
     *
     * <p>This method completes the building process and returns the final
     * ViewDescriptor object with all configured properties, fields, groups,
     * layout, and parameters.</p>
     *
     * @return the fully configured ViewDescriptor instance
     */
    public ViewDescriptor build() {
        return descriptor;
    }

    /**
     * Configures whether fields should be automatically detected from the bean class.
     *
     * <p>When autofields is true, the framework will introspect the bean class
     * and automatically create field descriptors for its properties. When false,
     * only explicitly defined fields will be included in the view.</p>
     *
     * @param autofields true to enable automatic field detection, false otherwise
     * @return this builder instance for method chaining
     */
    public ViewDescriptorBuilder autofields(boolean autofields) {
        descriptor.setAutofields(autofields);
        return this;
    }

    /**
     * Creates a new ViewDescriptorBuilder by copying an existing ViewDescriptor.
     *
     * <p>This factory method creates a builder initialized with all properties, fields,
     * field groups, layout parameters, and actions from the source descriptor. This is useful
     * for creating variations of existing descriptors or extending them with additional configuration.</p>
     *
     * @param other the source ViewDescriptor to copy from
     * @return a new ViewDescriptorBuilder instance initialized with the source descriptor's configuration
     *
     * Example:
     * <pre>{@code
     * ViewDescriptor existing = getExistingDescriptor();
     * ViewDescriptor modified = ViewDescriptorBuilder
     *     .from(existing)
     *     .addParam("readOnly", true)
     *     .build();
     * }</pre>
     */
    public static ViewDescriptorBuilder from(ViewDescriptor other) {
        return from(other.getViewTypeName(), other.getBeanClass(), other);
    }

    /**
     * Creates a new ViewDescriptorBuilder by copying an existing ViewDescriptor with a different view type and bean class.
     *
     * <p>This factory method creates a builder initialized with all properties from the source descriptor,
     * but allows you to override the view type and bean class. This is particularly useful when you need
     * to create a descriptor for a different view type (e.g., converting a "form" descriptor to a "table" descriptor)
     * or adapt a descriptor to work with a different entity class while preserving the field configuration.</p>
     *
     * @param viewType  the new view type to use (e.g., "form", "table", "tree")
     * @param beanClass the new bean class to associate with the descriptor
     * @param other     the source ViewDescriptor to copy configuration from
     * @return a new ViewDescriptorBuilder instance with the specified view type and bean class,
     *         initialized with the source descriptor's configuration
     *
     * Example:
     * <pre>{@code
     * ViewDescriptor formDescriptor = getFormDescriptor();
     * ViewDescriptor tableDescriptor = ViewDescriptorBuilder
     *     .from("table", Customer.class, formDescriptor)
     *     .hidden("description")
     *     .build();
     * }</pre>
     */
    public static ViewDescriptorBuilder from(String viewType, Class<?> beanClass, ViewDescriptor other) {
        ViewDescriptorBuilder builder = new ViewDescriptorBuilder();
        builder.descriptor = new DefaultViewDescriptor();
        builder.descriptor.setAutofields(other.isAutofields());
        builder.descriptor.setViewTypeName(viewType);
        builder.descriptor.setBeanClass(beanClass);
        builder.descriptor.setMessages(other.getMessages());
        if (other.getActions() != null && !other.getActions().isEmpty()) {
            builder.descriptor.setActions(new ArrayList<>(other.getActions()));
        }
        builder.descriptor.setExtends(other.getExtends());
        other.getFields().forEach(f -> {
            builder.descriptor.addField(f.clone());
        });

        other.getFieldGroups().forEach(group -> {
            FieldGroup newGroup = group.clone();
            group.getFields().forEach(f -> {
                newGroup.addField(builder.descriptor.getField(f.getName()));
            });
            builder.descriptor.addFieldGroup(newGroup);
        });

        builder.descriptor.getLayout().addParams(other.getLayout().getParams());
        builder.descriptor.getParams().putAll(other.getParams());
        return builder;
    }
}
