/**
 * Well-known values of {@link DashboardWidgetResponse.type}. Mirrors
 * `tools.dynamia.modules.dashboard.DashboardWidgetTypes`. Applications may use any other string for their own
 * widget kinds, so the type is open.
 */
export type DashboardWidgetType = 'chart' | 'viewer' | 'kpi' | 'html' | 'custom' | (string & {});

/**
 * Data of a `chart` widget, shaped like a Chart.js configuration.
 * Mirrors `tools.dynamia.modules.dashboard.ChartWidgetData`.
 */
export interface ChartWidgetData {
  /** Chart.js chart type, e.g. `bar`, `line`, `pie` */
  type: string;
  /** Chart.js `data` object (labels and datasets) */
  data: unknown;
  /** Chart.js `options` object; `null` to use the defaults */
  options?: unknown;
}

/**
 * Data of a `kpi` widget: a single key indicator.
 * Mirrors `tools.dynamia.modules.dashboard.KpiWidgetData`.
 */
export interface KpiWidgetData {
  value: unknown;
  label?: string | null;
  unit?: string | null;
  /** Variation against the previous period, e.g. `0.12` for +12% */
  trend?: number | null;
}

/**
 * Data of a `viewer` widget. Render `value` with the view described by `descriptorId`
 * (see `MetadataApi.getView`) or, when there is none, with a default view of `viewType`.
 * Mirrors `tools.dynamia.modules.dashboard.ViewerWidgetData`.
 */
export interface ViewerWidgetData {
  descriptorId?: string | null;
  viewType?: string | null;
  value: unknown;
}

/**
 * Response of `GET /api/dashboard/{descriptorId}/widgets/{field}`.
 * Mirrors `tools.dynamia.modules.dashboard.DashboardWidgetResponse`.
 *
 * @typeParam T - the shape of `data`, which depends on `type`
 */
export interface DashboardWidgetResponse<T = unknown> {
  /** Dashboard descriptor field name the widget is bound to */
  field: string;
  /** Widget id */
  widget: string;
  type: DashboardWidgetType;
  title?: string | null;
  titleVisible: boolean;
  editable: boolean;
  closable: boolean;
  maximizable: boolean;
  data: T | null;
}

/** Query parameters forwarded to the widget's `update(params)` on the server. */
export type DashboardWidgetParams = Record<string, string | number | boolean | undefined | null>;

/**
 * Grid spans of a dashboard cell, in units of a 12-column grid.
 * `xs` is absent when the descriptor does not define `span-xs`.
 */
export interface DashboardCellSpan {
  md: number;
  sm: number;
  xs?: number;
}

/** One widget slot of a {@link DashboardLayout}. */
export interface DashboardLayoutCell {
  /** Descriptor field name; pass it to `DashboardApi.widget()` */
  field: string;
  /** Widget id (the `widget` param of the field); `undefined` if the field does not declare one */
  widget?: string;
  span: DashboardCellSpan;
  /** The field's params, i.e. the widget configuration */
  params: Record<string, unknown>;
}

/**
 * The layout of a dashboard: rows of cells on a 12-column grid. Computed by `resolveDashboardLayout` with the
 * same rules as the ZK dashboard renderer.
 */
export interface DashboardLayout {
  /** Number of columns declared by the descriptor (4 by default) */
  columns: number;
  rows: DashboardLayoutCell[][];
}
