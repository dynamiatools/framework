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
    // columns lives in ViewLayout.params (descriptor.layout.params.columns),
    // NOT in the top-level descriptor.params
    const layoutParams = descriptor.layout?.params ?? {};
    const columns = LayoutEngine._resolveColumns(layoutParams);

    // Pre-collect which fields belong to each group (preserving flat-list order within each group)
    const groupedFields = new Map<string, ResolvedField[]>();
    for (const field of fields) {
      const key = field.group ?? '';
      if (!groupedFields.has(key)) groupedFields.set(key, []);
      groupedFields.get(key)!.push(field);
    }

    // Build sections by scanning fields in declaration order.
    //
    // Named groups are emitted at the position of their FIRST field in the flat list,
    // so the group header appears where the first group field would have been — matching
    // the intent expressed in the YAML `fields:` order.
    //
    // Ungrouped fields that appear before, between, or after named groups form separate
    // unnamed sections (no group header rendered). This keeps declaration order intact
    // without mixing ungrouped and grouped fields into a single blob at the top or bottom.
    const groups: ResolvedGroup[] = [];
    const emittedGroups = new Set<string>();
    let pendingDefault: ResolvedField[] = [];
    let defaultIdx = 0;

    const flushDefault = () => {
      if (pendingDefault.length > 0) {
        groups.push({
          name: `__default_${defaultIdx++}`,
          rows: LayoutEngine._buildRows(pendingDefault, columns),
        });
        pendingDefault = [];
      }
    };

    for (const field of fields) {
      const groupName = field.group;

      if (groupName && !emittedGroups.has(groupName)) {
        // First occurrence of this named group → flush accumulated ungrouped fields first,
        // then emit the complete group (all its fields collected earlier in one section).
        flushDefault();
        const allGroupFields = groupedFields.get(groupName) ?? [];
        const groupParams = LayoutEngine._findGroupParams(descriptor, groupName);
        const resolvedGroup: ResolvedGroup = {
          name: groupName,
          rows: LayoutEngine._buildRows(allGroupFields, columns),
        };
        const label = groupParams?.label ?? groupName;
        if (label) resolvedGroup.label = label;
        if (groupParams?.icon) resolvedGroup.icon = groupParams.icon;
        groups.push(resolvedGroup);
        emittedGroups.add(groupName);
      } else if (!groupName) {
        // Ungrouped field — accumulate into the current default section
        pendingDefault.push(field);
      }
      // Fields belonging to an already-emitted group are skipped here
      // (they were already added via allGroupFields above)
    }

    // Flush any remaining ungrouped fields (those that appeared after all named groups)
    flushDefault();

    // Update row/col indices on the resolved fields
    for (let gi = 0; gi < groups.length; gi++) {
      const group = groups[gi]!;
      for (let ri = 0; ri < group.rows.length; ri++) {
        const row = group.rows[ri]!;
        let colIdx = 0;
        for (const field of row.fields) {
          (field as ResolvedField).rowIndex = gi * 1000 + ri;
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

    // 1. Use the typed fieldGroups array (from Java ViewDescriptor.getFieldGroups())
    if (descriptor.fieldGroups?.length) {
      const fg = descriptor.fieldGroups.find((g: { name: string }) => g.name === groupName);
      if (fg) {
        const result: { label?: string; icon?: string } = {};
        if (fg.label !== undefined) result.label = fg.label;
        if (fg.icon !== undefined) result.icon = fg.icon;
        return result;
      }
    }

    // 2. Fallback: legacy params['groups'] map
    const groupsParam = descriptor.params?.['groups'];
    if (groupsParam && typeof groupsParam === 'object') {
      const groups = groupsParam as Record<string, unknown>;
      const gp = groups[groupName];
      if (gp && typeof gp === 'object') return gp as { label?: string; icon?: string };
    }
    return null;
  }
}
