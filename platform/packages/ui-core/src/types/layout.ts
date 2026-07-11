// Layout types for grid-based form layout computation

import type { ResolvedField } from './field.js';

/**
 * A single row in a grid layout containing resolved fields.
 */
export interface ResolvedRow {
  /** Fields in this row */
  fields: ResolvedField[];
}

/**
 * A named group of rows, corresponding to a form fieldgroup/tab.
 */
export interface ResolvedGroup {
  /** Group name (or empty string for the default group) */
  name: string;
  /** Display label for the group */
  label?: string;
  /** Icon for the group */
  icon?: string;
  /** Ordered list of rows in this group */
  rows: ResolvedRow[];
}

/**
 * The fully computed layout for a form or other grid-based view.
 */
export interface ResolvedLayout {
  /** Total number of grid columns */
  columns: number;
  /** All groups in display order (default group first if it has fields) */
  groups: ResolvedGroup[];
  /** All fields in display order (flat list, across all groups) */
  allFields: ResolvedField[];
}
