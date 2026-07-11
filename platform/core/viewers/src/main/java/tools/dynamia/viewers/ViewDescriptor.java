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

import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.dynamia.actions.ActionReference;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


/**
 * Metadata descriptor that defines the structure and configuration of a {@link View}.
 *
 * <p>A {@code ViewDescriptor} acts as a blueprint that specifies which fields to display,
 * how they are grouped, what layout is applied, which actions are available, and any additional
 * rendering parameters. It is consumed by a {@link ViewRenderer} to construct the actual UI
 * component.</p>
 *
 * <p>Descriptors are typically loaded from classpath resources (XML/YAML) or built programmatically
 * and registered through a {@link ViewDescriptorFactory}. They can also extend other descriptors
 * via {@link #getExtends()} to support inheritance-based configuration.</p>
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Defines the ordered list of {@link Field}s and {@link FieldGroup}s to render.</li>
 *   <li>Holds the {@link ViewLayout} that controls visual arrangement.</li>
 *   <li>Exposes rendering parameters via {@link #getParams()}.</li>
 *   <li>References the target domain class ({@link #getBeanClass()}) and view type
 *       ({@link #getViewTypeName()}).</li>
 *   <li>Optionally declares a {@link ViewCustomizer} and/or a custom {@link ViewRenderer} to
 *       override framework defaults.</li>
 * </ul>
 * </p>
 *
 * @see Field
 * @see FieldGroup
 * @see ViewLayout
 * @see ViewFactory
 * @see ViewDescriptorFactory
 */
public interface ViewDescriptor extends Serializable {

    /**
     * Returns the unique identifier of this descriptor.
     *
     * <p>The identifier is used by {@link ViewDescriptorFactory#getDescriptor(String)} to look up
     * this descriptor. It typically follows the pattern {@code "<ClassName>-<viewType>"}.</p>
     *
     * @return the non-null, non-empty descriptor identifier
     */
    String getId();

    /**
     * Returns the base name of the resource bundle used for i18n label resolution within this view.
     *
     * <p>When present, field labels and group titles are resolved against this messages bundle
     * before falling back to the default application messages.</p>
     *
     * @return the resource bundle base name, or {@code null} if no custom bundle is configured
     */
    String getMessages();

    /**
     * Returns the domain class this descriptor is associated with.
     *
     * <p>This class is used by the renderer to introspect field types and apply default
     * type-based configurations.</p>
     *
     * @return the target bean class; never {@code null}
     */
    Class<?> getBeanClass();

    /**
     * Returns the ordered list of {@link Field}s defined by this descriptor.
     *
     * <p>The list reflects the display order. Use {@link #sortFields()} to get a copy
     * sorted explicitly by each field's index attribute.</p>
     *
     * @return a mutable, non-null list of fields; may be empty
     */
    List<Field> getFields();

    /**
     * Returns the {@link Field} with the given name, or {@code null} if not found.
     *
     * @param name the field name; must not be {@code null}
     * @return the matching {@link Field}, or {@code null}
     */
    Field getField(String name);

    /**
     * Returns the list of {@link FieldGroup}s defined by this descriptor.
     *
     * <p>Field groups allow related fields to be visually clustered (e.g., inside a tab or
     * a bordered panel).</p>
     *
     * @return a mutable, non-null list of field groups; may be empty
     */
    List<FieldGroup> getFieldGroups();

    /**
     * Returns the {@link FieldGroup} with the given name, or {@code null} if not found.
     *
     * @param name the field group name; must not be {@code null}
     * @return the matching {@link FieldGroup}, or {@code null}
     */
    FieldGroup getFieldGroup(String name);

    /**
     * Returns the {@link ViewLayout} that controls how fields are arranged visually.
     *
     * @return the layout configuration; may be {@code null} if not explicitly set
     */
    ViewLayout getLayout();

    /**
     * Returns the name of the view type this descriptor targets (e.g., {@code "form"},
     * {@code "table"}).
     *
     * @return the non-null view type name
     */
    String getViewTypeName();

    /**
     * Returns the identifier of another {@link ViewDescriptor} that this descriptor extends.
     *
     * <p>When set, the framework merges the parent descriptor's fields, groups, and parameters
     * into this one before rendering, allowing incremental customization.</p>
     *
     * @return the parent descriptor id, or {@code null} if this descriptor has no parent
     */
    String getExtends();

    /**
     * Returns the {@link ViewCustomizer} class to be instantiated and applied after the view
     * is built by the renderer.
     *
     * <p>Customizers can post-process the resulting component (e.g., add event listeners,
     * adjust styles) in a renderer-agnostic way.</p>
     *
     * @return the customizer class, or {@code null} if no customizer is configured
     */
    Class<? extends ViewCustomizer> getViewCustomizerClass();

    /**
     * Returns a custom {@link ViewRenderer} class that overrides the default renderer registered
     * for this descriptor's view type.
     *
     * @return the custom renderer class, or {@code null} to use the default renderer
     */
    Class<? extends ViewRenderer> getCustomViewRenderer();

    /**
     * Returns the map of arbitrary rendering parameters associated with this descriptor.
     *
     * <p>Parameters are key-value pairs interpreted by the renderer. Common examples include
     * {@code "height"}, {@code "width"}, {@code "pageable"}, and {@code "selectable"}.</p>
     *
     * @return a non-null, mutable map of parameters; may be empty
     */
    Map<String, Object> getParams();

    /**
     * Appends the given {@link Field} to the end of this descriptor's field list.
     *
     * @param field the field to add; must not be {@code null}
     */
    void addField(Field field);

    /**
     * Appends the given {@link FieldGroup} to this descriptor's group list.
     *
     * @param fieldGroup the field group to add; must not be {@code null}
     */
    void addFieldGroup(FieldGroup fieldGroup);

    /**
     * Adds or replaces a rendering parameter.
     *
     * @param name  the parameter name; must not be {@code null}
     * @param value the parameter value; may be {@code null}
     */
    void addParam(String name, Object value);

    /**
     * Removes the {@link Field} with the given name from this descriptor.
     *
     * <p>If the field belongs to a {@link FieldGroup}, it is also removed from that group.</p>
     *
     * @param name the field name to remove; must not be {@code null}
     */
    void removeField(String name);

    /**
     * Reorders the field list to match the given sequence of field names.
     *
     * <p>Fields not present in {@code fieldNames} are appended at the end in their original order.</p>
     *
     * @param fieldNames the desired field order; must not be {@code null}
     */
    void sortFields(List<String> fieldNames);

    /**
     * Reorders the field group list to match the given sequence of group names.
     *
     * <p>Groups not present in {@code fieldGroupNames} are appended at the end in their original order.</p>
     *
     * @param fieldGroupNames the desired group order; must not be {@code null}
     */
    void sortFieldGroups(List<String> fieldGroupNames);

    /**
     * Returns the target device identifier for this descriptor (e.g., {@code "screen"},
     * {@code "mobile"}).
     *
     * <p>Defaults to {@code "screen"} when not explicitly configured.</p>
     *
     * @return the non-null device name
     */
    String getDevice();

    /**
     * Returns {@code true} if the descriptor should automatically discover and include all
     * introspectable fields of {@link #getBeanClass()} when no explicit field list is provided.
     *
     * @return {@code true} to enable auto-field discovery; {@code false} to use only explicitly
     *         declared fields
     */
    boolean isAutofields();

    /**
     * Returns the field list sorted by each field's {@code index} attribute in ascending order.
     *
     * <p>Unlike {@link #getFields()}, this method returns a freshly sorted copy without modifying
     * the internal list.</p>
     *
     * @return a non-null list of fields sorted by index
     */
    List<Field> sortFields();

    /**
     * Returns the list of {@link ActionReference}s associated with this descriptor.
     *
     * <p>Actions are typically rendered as buttons or menu items in the generated view
     * (e.g., save, delete, search).</p>
     *
     * @return a non-null, possibly empty list of action references
     */
    List<ActionReference> getActions();

    /**
     * Returns the first {@link Field} in the field list.
     *
     * <p>Useful when only the leading field is needed (e.g., for default focus or label
     * extraction) without iterating the entire list.</p>
     *
     * @return the first {@link Field}, or {@code null} if the field list is empty
     */
    @JsonIgnore
    default Field getFirstField() {
        if (!getFields().isEmpty()) {
            return getFields().getFirst();
        }
        return null;
    }

    /**
     * Removes all fields that satisfy the given predicate from both the field list and any
     * {@link FieldGroup} that contains them.
     *
     * <p>This is a convenience method that combines a field-list removal with a group-level
     * removal in a single call.</p>
     *
     * @param filter the predicate that identifies fields to remove; must not be {@code null}
     */
    default void removeFieldsIf(Predicate<Field> filter) {
        getFields().removeIf(filter);
        getFieldGroups().forEach(fieldGroup -> {
            fieldGroup.getFields().removeIf(filter);
        });
    }
}
