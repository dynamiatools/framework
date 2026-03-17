import type { HttpClient } from '@dynamia-tools/sdk';
import type { ReportDTO, ReportFilters } from './types.js';

type ReportGetParams = Record<string, string | number | boolean | undefined | null>;

/**
 * Access the Reports extension REST API.
 * Base path: /api/reports
 */
export class ReportsApi {
  private readonly http: HttpClient;

  constructor(http: HttpClient) {
    this.http = http;
  }

  /** GET /api/reports — List all exportable reports */
  list(): Promise<ReportDTO[]> {
    return this.http.get<ReportDTO[]>('/api/reports');
  }

  /** GET /api/reports/{group}/{endpoint} — Fetch report data with query-string filters */
  get(group: string, endpoint: string, params?: ReportGetParams): Promise<unknown> {
    return this.http.get<unknown>(
      `/api/reports/${encodeURIComponent(group)}/${encodeURIComponent(endpoint)}`,
      params,
    );
  }

  /** POST /api/reports/{group}/{endpoint} — Fetch report data with structured filters */
  post(group: string, endpoint: string, filters?: ReportFilters): Promise<unknown> {
    return this.http.post<unknown>(
      `/api/reports/${encodeURIComponent(group)}/${encodeURIComponent(endpoint)}`,
      filters,
    );
  }
}

