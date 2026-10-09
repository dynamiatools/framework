// ─── Reports types mirroring the Dynamia Platform Java model ────────────────

/**
 * Descriptor for a single filter field within a report.
 *
 * Mirrors `tools.dynamia.reports.ReportFilterDTO` — returned by `GET /api/reports`
 * inside `ReportDTO.filters`. Describes the metadata of a filter input
 * (label, datatype, whether it is required, etc.).
 */
export interface ReportFilterDTO {
  /** Parameter name used as the key when submitting the report */
  name: string;
  /** Java datatype hint (e.g. `"java.time.LocalDate"`, `"java.lang.String"`) */
  datatype?: string;
  /** Human-readable label shown in the UI */
  label?: string;
  /** Whether this filter must be provided before the report can be run */
  required?: boolean;
  /** Predefined selectable values (used for enum / select-type filters) */
  values?: string[];
  /** Date/number format pattern for parsing the value */
  format?: string;
}

/**
 * A single resolved filter value to be sent as part of a report execution request.
 *
 * Mirrors `tools.dynamia.reports.ReportFilterOption` — used as items in the
 * `options` array of `ReportFilters` (the POST body for report execution).
 */
export interface ReportFilterOption {
  /** Filter parameter name (matches `ReportFilterDTO.name`) */
  name: string;
  /** Resolved string value for this filter */
  value: string;
}

/**
 * POST body sent to the report execution endpoint.
 *
 * Mirrors `tools.dynamia.reports.ReportFilters`.
 * The field is named `options` in Java (`List<ReportFilterOption> options`) —
 * NOT `filters`.
 */
export interface ReportFilters {
  options: ReportFilterOption[];
}

/**
 * Report descriptor returned by `GET /api/reports`.
 *
 * Mirrors `tools.dynamia.reports.ReportDTO`.
 * Optional fields are absent from the JSON when the report has not configured them.
 */
export interface ReportDTO {
  /** Unique report identifier */
  id?: string;
  /** Internal report name / key */
  name: string;
  /** Display title of the report */
  title?: string;
  /** Optional subtitle shown below the title */
  subtitle?: string;
  /** Human-readable description of what the report contains */
  description?: string;
  /** Grouping category for the report */
  group?: string;
  /** REST endpoint used to execute / download this report */
  endpoint: string;
  /** List of filter descriptors accepted by this report */
  filters?: ReportFilterDTO[];
}

// ─── UI API (/api/reports/v2) ────────────────────────────────────────────────

/** Data types of columns and filters. Mirrors the Java `DataType` enum. */
export type ReportDataType =
  | 'TEXT'
  | 'NUMBER'
  | 'CURRENCY'
  | 'DATE'
  | 'DATE_TIME'
  | 'TIME'
  | 'BOOLEAN'
  | 'ENUM'
  | 'ENTITY';

/** Where the options of a filter come from. Anything other than `NONE` can be loaded with `filterOptions()`. */
export type ReportFilterOptionsSource = 'NONE' | 'STATIC' | 'ENUM' | 'ENTITY' | 'QUERY';

/** Export formats of `ReportsApi.export()`. */
export type ReportExportFormat = 'xlsx' | 'csv' | 'pdf';

/** A report in the catalog. Mirrors `tools.dynamia.modules.reports.api.v2.ReportSummary`. */
export interface ReportSummary {
  id: number;
  name: string;
  title?: string | null;
  subtitle?: string | null;
  description?: string | null;
  group?: string | null;
  chartable: boolean;
  hasFilters: boolean;
  /** Legacy export endpoint, empty when the report is not exported */
  endpoint?: string | null;
}

/** A group of reports in the catalog. */
export interface ReportCatalogGroup {
  name: string;
  endpointName?: string | null;
  reports: ReportSummary[];
}

/** A column of a report result. */
export interface ReportColumn {
  /** Key of the value in every row */
  name: string;
  label: string;
  dataType: ReportDataType;
  align: 'LEFT' | 'CENTER' | 'RIGHT';
  format?: string | null;
  width?: string | null;
  upperCase: boolean;
}

/** A filter a report accepts. */
export interface ReportFilterDefinition {
  name: string;
  label: string;
  dataType: ReportDataType;
  required: boolean;
  hideLabel: boolean;
  defaultValue?: string | null;
  order: number;
  optionsSource: ReportFilterOptionsSource;
  /** Date/time pattern for DATE, DATE_TIME and TIME filters */
  format?: string | null;
}

/** A chart of a report. Its data comes with the run result. */
export interface ReportChartDefinition {
  index: number;
  title: string;
  /** Chart.js type: bar, line, pie, doughnut... */
  type: string;
  labelField: string;
  valueField: string;
  grouped: boolean;
}

/** Everything a UI needs to show a report. */
export interface ReportDefinition {
  report: ReportSummary;
  /** True when the columns come from the query, so `columns` is empty until the report runs */
  autofields: boolean;
  columns: ReportColumn[];
  filters: ReportFilterDefinition[];
  charts: ReportChartDefinition[];
  exportFormats: ReportExportFormat[];
}

/** An option of a filter with predefined values. */
export interface ReportFilterOptionItem {
  value: unknown;
  label: string;
}

/** A single filter value: text, number, boolean, `yyyy-MM-dd` date, `yyyy-MM-dd HH:mm:ss`, `HH:mm:ss`, enum name or entity id. */
export type ReportFilterValue = string | number | boolean | null | undefined;

/** Request to run or export a report. */
export interface ReportRunRequest {
  /** Filter values by filter name */
  filters?: Record<string, ReportFilterValue> | undefined;
  /** Zero based page */
  page?: number | undefined;
  /** Rows per page; empty or 0 returns every row */
  size?: number | undefined;
  /** Column to sort by */
  sort?: string | undefined;
  direction?: 'asc' | 'desc' | undefined;
}

/** Data of a chart, shaped like a Chart.js configuration. */
export interface ReportChartResult {
  index: number;
  title: string;
  type: string;
  labels: string[];
  datasets: Array<{ label: string; data: number[]; backgroundColor: string[] }>;
}

/** A row: one value per column name. Dates are ISO text, enums their name, other objects text. */
export type ReportRow = Record<string, string | number | boolean | null>;

/** Result of running a report. */
export interface ReportRunResult {
  columns: ReportColumn[];
  rows: ReportRow[];
  /** Rows of the whole result, before paging */
  total: number;
  page: number;
  size: number;
  /** True when the report reached the maximum rows (`dynamia.reports.max-rows`) and has more data */
  truncated: boolean;
  durationMs: number;
  charts: ReportChartResult[];
}

/** Result of the legacy endpoints `/api/reports/{group}/{endpoint}`. */
export interface ReportEndpointResult {
  data: Array<Record<string, unknown>>;
  truncated?: boolean;
}

// ─── Designer API (/api/reports/v2/design) ───────────────────────────────────

/** Preview of a query being designed: the first rows, never more than the preview limit. */
export interface ReportPreviewRequest {
  queryLang: 'sql' | 'jpql';
  queryScript: string;
  /** Id of an external datasource; empty uses the application database (or the entity manager for JPQL) */
  dataSourceId?: number | null | undefined;
  /** Values for the `:name` parameters of the query */
  parameters?: Record<string, ReportFilterValue> | undefined;
}

/** Result of a preview. */
export interface ReportPreviewResult {
  columns: string[];
  rows: ReportRow[];
  /** True when there are more rows than the preview shows */
  truncated: boolean;
  durationMs: number;
}

/** Result of testing the connection of a datasource. */
export interface DataSourceTestResult {
  ok: boolean;
  message: string;
}

/** What the current user can do in the designer. */
export interface ReportDesignerInfo {
  allowed: boolean;
  /** Maximum rows a preview returns */
  previewLimit: number;
}
