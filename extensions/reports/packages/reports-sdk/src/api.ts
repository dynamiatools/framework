import type { HttpClient } from '@dynamia-tools/sdk';
import type {
  DataSourceTestResult,
  ReportCatalogGroup,
  ReportDefinition,
  ReportDesignerInfo,
  ReportDTO,
  ReportEndpointResult,
  ReportExportFormat,
  ReportFilterOptionItem,
  ReportFilters,
  ReportPreviewRequest,
  ReportPreviewResult,
  ReportRunRequest,
  ReportRunResult,
} from './types.js';

type ReportGetParams = Record<string, string | number | boolean | undefined | null>;

/**
 * Access the Reports extension REST API.
 *
 * - UI API, `/api/reports/v2`: {@link ReportsApi.catalog}, {@link ReportsApi.definition},
 *   {@link ReportsApi.filterOptions}, {@link ReportsApi.run} and {@link ReportsApi.export}.
 * - Designer API, `/api/reports/v2/design`: {@link ReportsApi.designer}, {@link ReportsApi.preview},
 *   {@link ReportsApi.exportDefinition}, {@link ReportsApi.testDataSource}. Only for users with a designer role.
 * - Legacy endpoints, `/api/reports/{group}/{endpoint}`: {@link ReportsApi.list}, {@link ReportsApi.get},
 *   {@link ReportsApi.post}. Kept for existing integrations.
 */
export class ReportsApi {
  private readonly http: HttpClient;

  constructor(http: HttpClient) {
    this.http = http;
  }

  // ── UI API ────────────────────────────────────────────────────────────────

  /** GET /api/reports/v2/catalog — Reports the current user can run, grouped. */
  catalog(): Promise<ReportCatalogGroup[]> {
    return this.http.get<ReportCatalogGroup[]>('/api/reports/v2/catalog');
  }

  /**
   * GET /api/reports/v2/{id} — Filters, declared columns, charts and export formats of a report.
   *
   * @throws DynamiaApiError 404 when the report does not exist or is not visible, 403 when access is denied
   */
  definition(id: number): Promise<ReportDefinition> {
    return this.http.get<ReportDefinition>(`/api/reports/v2/${id}`);
  }

  /**
   * GET /api/reports/v2/{id}/filters/{filter}/options — Options of a filter with predefined values.
   *
   * @param q - optional text to search in the labels
   * @param limit - maximum options (the server caps it at 500)
   */
  filterOptions(
    id: number,
    filter: string,
    options: { q?: string | undefined; limit?: number | undefined } = {},
  ): Promise<ReportFilterOptionItem[]> {
    return this.http.get<ReportFilterOptionItem[]>(
      `/api/reports/v2/${id}/filters/${encodeURIComponent(filter)}/options`,
      { q: options.q, limit: options.limit },
    );
  }

  /**
   * POST /api/reports/v2/{id}/run — Runs a report with filters, paging and sorting.
   *
   * @throws DynamiaApiError 400 for an invalid or missing filter or an unknown sort column
   */
  run(id: number, request: ReportRunRequest = {}): Promise<ReportRunResult> {
    return this.http.post<ReportRunResult>(`/api/reports/v2/${id}/run`, request);
  }

  /**
   * POST /api/reports/v2/{id}/export — Runs a report and downloads it as a file. Paging is ignored: the file has the
   * whole result. The server suggests the name `report-name-yyyy-MM-dd.format`.
   */
  export(id: number, format: ReportExportFormat, request: ReportRunRequest = {}): Promise<Blob> {
    return this.http.post<Blob>(`/api/reports/v2/${id}/export?format=${encodeURIComponent(format)}`, request);
  }

  // ── Designer API ──────────────────────────────────────────────────────────

  /** GET /api/reports/v2/design/info — Whether the current user can use the designer endpoints. */
  designer(): Promise<ReportDesignerInfo> {
    return this.http.get<ReportDesignerInfo>('/api/reports/v2/design/info');
  }

  /**
   * POST /api/reports/v2/design/preview — Runs a query that is being designed and returns its first rows.
   * Read-only, bounded and validated like a saved report.
   *
   * @throws DynamiaApiError 403 when the user is not a designer, 400 when the query is not allowed
   */
  preview(request: ReportPreviewRequest): Promise<ReportPreviewResult> {
    return this.http.post<ReportPreviewResult>('/api/reports/v2/design/preview', request);
  }

  /** GET /api/reports/v2/design/{id}/definition — The report definition as the JSON used to move reports between systems. */
  exportDefinition(id: number): Promise<Record<string, unknown>> {
    return this.http.get<Record<string, unknown>>(`/api/reports/v2/design/${id}/definition`);
  }

  /**
   * POST /api/reports/v2/design/import — Imports a definition exported with {@link ReportsApi.exportDefinition}.
   * The imported report is inactive and has no datasource.
   *
   * @returns the id of the new report
   */
  importDefinition(definition: Record<string, unknown>): Promise<{ id: number }> {
    return this.http.post<{ id: number }>('/api/reports/v2/design/import', definition);
  }

  /** POST /api/reports/v2/design/datasources/{id}/test — Tests the connection of a saved datasource. */
  testDataSource(id: number): Promise<DataSourceTestResult> {
    return this.http.post<DataSourceTestResult>(`/api/reports/v2/design/datasources/${id}/test`);
  }

  // ── Legacy endpoints ──────────────────────────────────────────────────────

  /** GET /api/reports — List all exportable reports */
  list(): Promise<ReportDTO[]> {
    return this.http.get<ReportDTO[]>('/api/reports');
  }

  /** GET /api/reports/{group}/{endpoint} — Fetch report data with query-string filters */
  get(group: string, endpoint: string, params?: ReportGetParams): Promise<ReportEndpointResult> {
    return this.http.get<ReportEndpointResult>(
      `/api/reports/${encodeURIComponent(group)}/${encodeURIComponent(endpoint)}`,
      params,
    );
  }

  /** POST /api/reports/{group}/{endpoint} — Fetch report data with structured filters */
  post(group: string, endpoint: string, filters?: ReportFilters): Promise<ReportEndpointResult> {
    return this.http.post<ReportEndpointResult>(
      `/api/reports/${encodeURIComponent(group)}/${encodeURIComponent(endpoint)}`,
      filters,
    );
  }
}
