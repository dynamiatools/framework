// ─── Core types mirroring the Dynamia Platform Java model ──────────────────

// ── Basic / shared ─────────────────────────────────────────────────────────

export interface BasicMetadata {
  id: string;
  name: string;
  endpoint?: string;
  description?: string;
  icon?: string;
}

// ── Application metadata ───────────────────────────────────────────────────

export interface ApplicationMetadata {
  name: string;
  version: string;
  description?: string;
  logo?: string;
  url?: string;
  modules?: NavigationModule[];
}

// ── Navigation ─────────────────────────────────────────────────────────────

export interface NavigationTree {
  modules: NavigationModule[];
}

export interface NavigationModule {
  id: string;
  name: string;
  description?: string;
  icon?: string;
  groups: NavigationGroup[];
}

export interface NavigationGroup {
  id: string;
  name: string;
  description?: string;
  icon?: string;
  pages: NavigationPage[];
}

export interface NavigationPage {
  id: string;
  name: string;
  description?: string;
  icon?: string;
  virtualPath: string;
  prettyVirtualPath: string;
  pageClass?: string;
}

// ── Entity metadata ────────────────────────────────────────────────────────

export interface ApplicationMetadataEntities {
  entities: EntityMetadata[];
}

export interface EntityMetadata extends BasicMetadata {
  className: string;
  actions: ActionMetadata[];
  descriptors: ViewDescriptorMetadata[];
  actionsEndpoint: string;
}

// ── Actions ────────────────────────────────────────────────────────────────

export interface ApplicationMetadataActions {
  actions: ActionMetadata[];
}

export interface ActionMetadata extends BasicMetadata {
  actionClass?: string;
  params?: Record<string, unknown>;
}

export interface ActionExecutionRequest {
  data?: Record<string, unknown>;
  params?: Record<string, unknown>;
}

export interface ActionExecutionResponse {
  message: string;
  status: string;
  code: number;
  data?: unknown;
}

// ── View descriptors ───────────────────────────────────────────────────────

export interface ViewDescriptorMetadata {
  view: string;
  descriptor: ViewDescriptor;
}

export interface ViewDescriptor {
  id: string;
  beanClass: string;
  viewTypeName: string;
  fields: ViewField[];
  params: Record<string, unknown>;
}

export interface ViewField {
  name: string;
  fieldClass?: string;
  label?: string;
  visible?: boolean;
  required?: boolean;
  params: Record<string, unknown>;
}

// ── CRUD ───────────────────────────────────────────────────────────────────

/**
 * Maps to Java `DataPaginator` — serialized as the `pageable` field inside `ListResult`.
 *
 * Java source: `tools.dynamia.domain.query.DataPaginator`
 *
 * | Java field      | Java type | JSON key       |
 * |-----------------|-----------|----------------|
 * | `totalSize`     | `long`    | `totalSize`    |
 * | `pageSize`      | `int`     | `pageSize`     |
 * | `firstResult`   | `int`     | `firstResult`  |
 * | `page`          | `int`     | `page`         |
 * | `pagesNumber`   | `int`     | `pagesNumber`  |
 */
export interface CrudPageable {
  /** Total number of records across all pages (`DataPaginator.totalSize` — Java `long`) */
  totalSize: number;
  /** Number of records per page (`DataPaginator.pageSize` — default 30) */
  pageSize: number;
  /** Zero-based offset of the first record on this page (`DataPaginator.firstResult`) */
  firstResult: number;
  /** Current 1-based page number (`DataPaginator.page`) */
  page: number;
  /** Total number of pages (`DataPaginator.pagesNumber`) */
  pagesNumber: number;
}

/**
 * Raw envelope returned by `RestNavigationContext.ListResult`.
 *
 * Java source: `tools.dynamia.web.navigation.RestNavigationContext.ListResult`
 *
 * | Java field  | Annotation                         | JSON key   |
 * |-------------|------------------------------------|------------|
 * | `data`      | —                                  | `data`     |
 * | `pageable`  | `@JsonInclude(NON_NULL)` — nullable | `pageable` |
 * | `response`  | —                                  | `response` |
 *
 * `pageable` is `null` when the result is not paginated (e.g. a flat list endpoint).
 */
export interface CrudRawResponse<T = unknown> {
  /** The page records */
  data: T[];
  /**
   * Pagination metadata. `null` when the response is not paginated
   * (`@JsonInclude(JsonInclude.Include.NON_NULL)` in Java — field may be absent from JSON).
   */
  pageable: CrudPageable | null;
  /** Status string, typically `"OK"` */
  response: string;
}

/**
 * Normalised result returned by all SDK `CrudResourceApi.findAll()` calls.
 * The SDK maps `CrudRawResponse` → `CrudListResult` so consumers never deal with the raw envelope.
 */
export interface CrudListResult<T = unknown> {
  /** The records for this page */
  content: T[];
  /** Total number of records across all pages (`DataPaginator.totalSize`) */
  total: number;
  /** Current 1-based page number (`DataPaginator.page`) */
  page: number;
  /** Number of records per page (`DataPaginator.pageSize`) */
  pageSize: number;
  /** Total number of pages (`DataPaginator.pagesNumber`) */
  totalPages: number;
}

export type CrudQueryParams = Record<string, string | number | boolean | undefined | null>;

// ── Reports ────────────────────────────────────────────────────────────────

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

// ── SaaS ───────────────────────────────────────────────────────────────────

export interface AccountDTO {
  id: number;
  uuid: string;
  name: string;
  status: string;
  statusDescription?: string;
  subdomain?: string;
}

// ── Client config ──────────────────────────────────────────────────────────

export interface DynamiaClientConfig {
  /** Base URL of your Dynamia Platform instance, e.g. https://app.example.com */
  baseUrl: string;
  /** Bearer / JWT access token */
  token?: string;
  /** HTTP Basic username */
  username?: string;
  /** HTTP Basic password */
  password?: string;
  /** Forward cookies on cross-origin requests */
  withCredentials?: boolean;
  /** Custom fetch implementation (useful for Node.js or tests) */
  fetch?: typeof fetch;
}

