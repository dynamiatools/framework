// LayoutEngine: computes grid layout from descriptor and resolved fields

import type { ViewDescriptor } from '@dynamia-tools/sdk';
import type { ResolvedField } from '../types/field.js';
import type { ResolvedLayout, ResolvedGroup, ResolvedRow } from '../types/layout.js';

/**
 * Computes the grid layout for a form view from its descriptor and resolved fields.
 * Groups fields by their group assignment and arranges them into rows and columns.
 *
 * Example:
 * <pre>{@code
 * const layout = LayoutEngine.computeLayout(descriptor, resolvedFields);
 * // layout.columns = 3
 * // layout.groups[0].rows[0].fields = [nameField, emailField, phoneField]
 * }</pre>
 */
export class LayoutEngine {
  /**
   * Compute the full layout for a set of resolved fields.
   * @param descriptor - View descriptor containing layout params
   * @param fields - Fully resolved fields from FieldResolver
   * @returns Computed ResolvedLayout with groups, rows and column info
   */
  static computeLayout(descriptor: ViewDescriptor, fields: ResolvedField[]): ResolvedLayout {
    const params = descriptor.params ?? {};
    const columns = LayoutEngine._resolveColumns(params);

    // Group fields by their group name
    const groupedFields = new Map<string, ResolvedField[]>();
    const defaultGroupKey = '';
    for (const field of fields) {
      const key = field.group ?? defaultGroupKey;
      if (!groupedFields.has(key)) groupedFields.set(key, []);
      groupedFields.get(key)!.push(field);
    }

    // Build resolved groups in insertion order
    const groups: ResolvedGroup[] = [];
    for (const [groupName, groupFields] of groupedFields) {
      const rows = LayoutEngine._buildRows(groupFields, columns);
      const groupParams = LayoutEngine._findGroupParams(descriptor, groupName);
      const resolvedGroup: ResolvedGroup = { name: groupName, rows };
      const label = groupParams?.label ?? (groupName || undefined);
      if (label !== undefined) resolvedGroup.label = label;
      if (groupParams?.icon !== undefined) resolvedGroup.icon = groupParams.icon;
      groups.push(resolvedGroup);
    }

    // Update row/col indices on the fields
    for (const group of groups) {
      for (const row of group.rows) {
        let colIdx = 0;
        for (const field of row.fields) {
          (field as ResolvedField).rowIndex = groups.indexOf(group) * 1000 + group.rows.indexOf(row);
          (field as ResolvedField).colIndex = colIdx;
          colIdx += field.gridSpan;
        }
      }
    }

    return { columns, groups, allFields: fields };
  }

  private static _resolveColumns(params: Record<string, unknown>): number {
    const cols = params['columns'];
    if (typeof cols === 'number') return cols;
    if (typeof cols === 'string') { const n = parseInt(cols, 10); return isNaN(n) ? 1 : n; }
    return 1;
  }

  private static _buildRows(fields: ResolvedField[], columns: number): ResolvedRow[] {
    const rows: ResolvedRow[] = [];
    let currentRow: ResolvedField[] = [];
    let currentWidth = 0;

    for (const field of fields) {
      const span = Math.min(field.gridSpan, columns);
      if (currentWidth + span > columns && currentRow.length > 0) {
        rows.push({ fields: currentRow });
        currentRow = [];
        currentWidth = 0;
      }
      currentRow.push({ ...field, gridSpan: span });
      currentWidth += span;
    }
    if (currentRow.length > 0) rows.push({ fields: currentRow });

    return rows;
  }

  private static _findGroupParams(descriptor: ViewDescriptor, groupName: string): { label?: string; icon?: string } | null {
    if (!groupName) return null;
    const groupsParam = descriptor.params?.['groups'];
    if (groupsParam && typeof groupsParam === 'object') {
      const groups = groupsParam as Record<string, unknown>;
      const gp = groups[groupName];
      if (gp && typeof gp === 'object') return gp as { label?: string; icon?: string };
    }
    return null;
  }
}
