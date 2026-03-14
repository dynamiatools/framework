// ── Main client ─────────────────────────────────────────────────────────────
export { DynamiaClient } from './client.js';

// ── Error ────────────────────────────────────────────────────────────────────
export { DynamiaApiError } from './errors.js';

// ── Core types ────────────────────────────────────────────────────────────────
export type { DynamiaClientConfig } from './types.js';

// ── Metadata module ──────────────────────────────────────────────────────────
export { MetadataApi, ActionsApi } from './metadata/index.js';
export type {
  // Application
  ApplicationMetadata,
  // Navigation
  NavigationTree,
  NavigationNode,
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
} from './metadata/index.js';

// ── CRUD module ───────────────────────────────────────────────────────────────
export { CrudResourceApi, CrudServiceApi } from './cruds/index.js';
export type {
  CrudListResult,
  CrudQueryParams,
  CrudRawResponse,
  CrudPageable,
} from './cruds/index.js';

// ── Reports module ────────────────────────────────────────────────────────────
export { ReportsApi } from './reports/index.js';
export type {
  ReportDTO,
  ReportFilter,
  ReportFilters,
} from './reports/index.js';

// ── SaaS module ───────────────────────────────────────────────────────────────
export { SaasApi } from './saas/index.js';
export type { AccountDTO } from './saas/index.js';

// ── API classes (files & schedule — core extensions) ─────────────────────────
export { FilesApi } from './files/index.js';
export { ScheduleApi } from './schedule/index.js';

