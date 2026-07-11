// FieldResolver: resolves FieldComponent, label, required, visible, and other field attributes

import type { ViewDescriptor, ViewField, EntityMetadata } from '@dynamia-tools/sdk';
import { FieldComponent } from '../types/field.js';
import type { ResolvedField } from '../types/field.js';

/**
 * Resolves field descriptors into fully resolved ResolvedField objects.
 * Handles component type inference, label generation, and default values.
 *
 * Example:
 * <pre>{@code
 * const resolved = FieldResolver.resolveFields(descriptor, metadata);
 * }</pre>
 */
export class FieldResolver {
  /**
   * Resolve all fields from a view descriptor into ResolvedField objects.
   * @param descriptor - The view descriptor
   * @param metadata - Optional entity metadata for additional type info
   * @returns Array of resolved fields in display order
   */
  static resolveFields(descriptor: ViewDescriptor, metadata: EntityMetadata | null): ResolvedField[] {
    const fields = descriptor.fields ?? [];

    // Build a reverse map: fieldName → groupName from descriptor.fieldGroups[].fields.
    // Java serializes FieldGroup with @JsonProperty("fields") listing field names.
    const fieldGroupMap = new Map<string, string>();
    if (descriptor.fieldGroups?.length) {
      for (const fg of descriptor.fieldGroups) {
        if (fg.fields?.length) {
          for (const fieldName of fg.fields) {
            fieldGroupMap.set(fieldName, fg.name);
          }
        }
      }
    }

    return fields
      .filter(f => f.visible !== false)
      .map((field, index) => FieldResolver.resolveField(field, index, metadata, fieldGroupMap));
  }

  /**
   * Resolve a single field descriptor.
   * @param field - The raw field descriptor
   * @param index - Field index for layout positioning
   * @param metadata - Optional entity metadata
   * @param fieldGroupMap - Optional reverse map: fieldName → groupName built from descriptor.fieldGroups
   * @returns A fully resolved field
   */
  static resolveField(field: ViewField, index: number, _metadata: EntityMetadata | null, fieldGroupMap?: Map<string, string>): ResolvedField {
    const params = field.params ?? {};
    const component = FieldResolver._resolveComponent(field, params);
    const label = FieldResolver._resolveLabel(field);
    const span = FieldResolver._resolveSpan(params);

    // Group: 1) fieldGroups reverse map (Java serialized), 2) field.params.group (legacy/manual)
    const group = fieldGroupMap?.get(field.name)
      ?? (typeof params['group'] === 'string' ? params['group'] : undefined);

    const resolved: ResolvedField = {
      ...field,
      resolvedComponent: component,
      resolvedLabel: label,
      gridSpan: span,
      resolvedVisible: field.visible !== false,
      resolvedRequired: field.required === true,
      rowIndex: 0,
      colIndex: index,
    };
    if (group !== undefined) resolved.group = group;
    return resolved;
  }

  private static _resolveComponent(field: ViewField, params: Record<string, unknown>): FieldComponent | string {
    // 1. Direct component property on the field (mapped from Java Field.component)
    if (field.component) return field.component;

    // 2. Explicit component in params (legacy / override)
    const explicitComponent = params['component'];
    if (typeof explicitComponent === 'string' && explicitComponent) return explicitComponent;

    // 3. Infer from field class
    const fieldClass = field.fieldClass ?? '';
    return FieldResolver._inferComponent(fieldClass);
  }

  private static _inferComponent(fieldClass: string): FieldComponent | string {
    const lc = fieldClass.toLowerCase();
    if (lc === 'string' || lc === 'java.lang.string') return FieldComponent.Textbox;
    if (lc === 'integer' || lc === 'int' || lc === 'java.lang.integer') return FieldComponent.Intbox;
    if (lc === 'long' || lc === 'java.lang.long') return FieldComponent.Longbox;
    if (lc === 'double' || lc === 'float' || lc === 'java.lang.double' || lc === 'java.lang.float') return FieldComponent.Decimalbox;
    if (lc.includes('bigdecimal') || lc === 'java.math.bigdecimal') return FieldComponent.Decimalbox;
    if (lc === 'boolean' || lc === 'java.lang.boolean') return FieldComponent.Checkbox;
    if (lc.includes('date') || lc.includes('localdate') || lc.includes('localdatetime')) return FieldComponent.Datebox;
    if (lc.includes('enum')) return FieldComponent.Combobox;
    // Default: textbox for unknown types
    return FieldComponent.Textbox;
  }

  private static _resolveLabel(field: ViewField): string {
    if (field.label) return field.label;
    // Convert camelCase to Title Case
    return field.name
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, s => s.toUpperCase())
      .trim();
  }

  private static _resolveSpan(params: Record<string, unknown>): number {
    const span = params['span'];
    if (typeof span === 'number') return span;
    if (typeof span === 'string') { const n = parseInt(span, 10); return isNaN(n) ? 1 : n; }
    return 1;
  }
}
