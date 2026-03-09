// ── Main client ─────────────────────────────────────────────────────────────
export { DynamiaClient } from './client.js';

// ── Error ────────────────────────────────────────────────────────────────────
export { DynamiaApiError } from './errors.js';

// ── Types ────────────────────────────────────────────────────────────────────
export type {
  DynamiaClientConfig,
  // Application
  ApplicationMetadata,
  // Navigation
  NavigationTree,
  NavigationModule,
  NavigationGroup,
  NavigationPage,
  // Metadata
  BasicMetadata,
  ApplicationMetadataEntities,
  EntityMetadata,
  ApplicationMetadataActions,
  ActionMetadata,
  // Actions
  ActionExecutionRequest,
  ActionExecutionResponse,
  // Views
  ViewDescriptorMetadata,
  ViewDescriptor,
  ViewField,
  // CRUD
  CrudListResult,
  CrudQueryParams,
  CrudRawResponse,
  CrudPageable,
  // Reports
  ReportDTO,
  ReportFilter,
  ReportFilters,
  // SaaS
  AccountDTO,
} from './types.js';

// ── API classes (for advanced usage / extension) ─────────────────────────────
export { MetadataApi } from './api/metadata.js';
export { CrudResourceApi } from './api/crud.js';
export { CrudServiceApi } from './api/crud-service.js';
export { ActionsApi } from './api/actions.js';
export { ReportsApi } from './api/reports.js';
export { FilesApi } from './api/files.js';
export { SaasApi } from './api/saas.js';
export { ScheduleApi } from './api/schedule.js';

