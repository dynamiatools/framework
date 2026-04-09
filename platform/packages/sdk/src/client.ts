import { HttpClient } from './http.js';
import { MetadataApi } from './metadata/api.js';
import { ActionsApi } from './metadata/actions.js';
import { CrudResourceApi } from './cruds/crud-resource.js';
import { CrudServiceApi } from './cruds/crud-service.js';
import { ScheduleApi } from './schedule/api.js';
import type { DynamiaClientConfig } from './types.js';

/**
 * Root client for the Dynamia Platform REST API.
 *
 * Extension-specific APIs (reports, files, saas) are available as separate packages:
 *   - `@dynamia-tools/reports-sdk`  → `new ReportsApi(client.http)`
 *   - `@dynamia-tools/files-sdk`    → `new FilesApi(client.http)`
 *   - `@dynamia-tools/saas-sdk`     → `new SaasApi(client.http)`
 *
 * @example
 * ```typescript
 * const client = new DynamiaClient({ baseUrl: 'https://app.example.com', token: '...' });
 * const app = await client.metadata.getApp();
 * const books = await client.crud('store/catalog/books').findAll();
 * ```
 */
export class DynamiaClient {
  /** @internal exposed so extension SDK packages can build their own API instances */
  readonly http: HttpClient;

  /** Application metadata API */
  readonly metadata: MetadataApi;
  /** Global & entity actions API */
  readonly actions: ActionsApi;
  /** Scheduled-tasks API */
  readonly schedule: ScheduleApi;

  constructor(config: DynamiaClientConfig) {
    this.http = new HttpClient(config);
    this.metadata = new MetadataApi(this.http);
    this.actions = new ActionsApi(this.http);
    this.schedule = new ScheduleApi(this.http);
  }

  /**
   * Returns a typed CRUD resource client for a CrudPage virtual path.
   *
   * @param virtualPath - The CrudPage virtual path (e.g. `'store/catalog/books'`).
   *
   * @example
   * ```typescript
   * const books = client.crud<Book>('store/catalog/books');
   * const page = await books.findAll({ page: 1, size: 20 });
   * ```
   */
  crud<T = unknown>(virtualPath: string): CrudResourceApi<T> {
    return new CrudResourceApi<T>(this.http, virtualPath);
  }

  /**
   * Returns a typed CRUD service client for a fully-qualified Java class name.
   *
   * @param className - Fully-qualified Java class name (e.g. `'com.example.domain.Book'`).
   *
   * @example
   * ```typescript
   * const svc = client.crudService<Book>('com.example.domain.Book');
   * const book = await svc.findById('42');
   * ```
   */
  crudService<T = unknown>(className: string): CrudServiceApi<T> {
    return new CrudServiceApi<T>(this.http, className);
  }

  /**
   * Invalidates the in-memory ViewDescriptor cache held by {@link MetadataApi}.
   *
   * Call this after a backend hot-reload or when you know a descriptor has changed,
   * so the next request fetches a fresh copy from the server.
   *
   * @param className - When provided, only entries for that entity class are removed.
   *   When omitted, the entire cache is cleared.
   *
   * @example
   * // Invalidate just one entity
   * client.clearViewDescriptorCache('mybookstore.domain.Book');
   * // Invalidate everything
   * client.clearViewDescriptorCache();
   */
  clearViewDescriptorCache(className?: string): void {
    this.metadata.clearViewCache(className);
  }
}
