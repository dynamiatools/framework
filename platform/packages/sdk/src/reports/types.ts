// ─── Reports types mirroring the Dynamia Platform Java model ────────────────

export interface ReportDTO {
  id: string;
  name: string;
  group: string;
  endpoint: string;
  description?: string;
}

export interface ReportFilter {
  name: string;
  value: string;
}

export interface ReportFilters {
  filters: ReportFilter[];
}
