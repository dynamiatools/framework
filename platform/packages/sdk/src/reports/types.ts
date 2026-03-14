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
