import type { ViewDescriptor, ViewField } from '@dynamia-tools/sdk';
import type { DashboardCellSpan, DashboardLayout, DashboardLayoutCell } from './types.js';

const GRID = 12;
const DEFAULT_COLUMNS = 4;
const DEFAULT_SM_SPAN = 6;

function toInt(value: unknown): number | undefined {
  const n = typeof value === 'number' ? value : typeof value === 'string' ? Number.parseInt(value, 10) : NaN;
  return Number.isFinite(n) && n > 0 ? Math.trunc(n) : undefined;
}

function buildCell(field: ViewField, columns: number): DashboardLayoutCell {
  const params = field.params ?? {};
  const unit = GRID / columns;
  const span = Math.min(toInt(params['span']) ?? 1, columns);
  const sm = toInt(params['span-sm']);
  const xs = toInt(params['span-xs']);

  const cellSpan: DashboardCellSpan = {
    md: unit * span,
    sm: sm !== undefined ? unit * sm : DEFAULT_SM_SPAN,
  };
  if (xs !== undefined) cellSpan.xs = GRID / xs;

  const widget = params['widget'];
  return {
    field: field.name,
    ...(typeof widget === 'string' ? { widget } : {}),
    span: cellSpan,
    params,
  };
}

/**
 * Computes the layout of a dashboard view descriptor, following the same rules as the server-side ZK renderer:
 * `layout.params.columns` (default 4) divides a 12-column grid, each field takes `span` columns (default 1) and
 * a new row starts once a row is full. `span-sm` and `span-xs` give the spans for smaller screens.
 *
 * @param descriptor - a `dashboard` view descriptor, e.g. from `client.metadata.getView(id)`
 *
 * @example
 * ```ts
 * const descriptor = await client.metadata.getView('mainDashboard');
 * const { rows } = resolveDashboardLayout(descriptor);
 * ```
 */
export function resolveDashboardLayout(descriptor: ViewDescriptor): DashboardLayout {
  const columns = Math.min(toInt(descriptor.layout?.params?.['columns']) ?? DEFAULT_COLUMNS, GRID);
  const rows: DashboardLayoutCell[][] = [];
  let current: DashboardLayoutCell[] = [];
  let spaceLeft = GRID;

  for (const field of descriptor.fields ?? []) {
    const cell = buildCell(field, columns);
    current.push(cell);
    spaceLeft -= cell.span.md;
    if (spaceLeft <= 0) {
      rows.push(current);
      current = [];
      spaceLeft = GRID;
    }
  }
  if (current.length > 0) rows.push(current);

  return { columns, rows };
}
