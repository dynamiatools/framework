import { HttpClient } from './http.js';
import { MetadataApi } from './api/metadata.js';
import { CrudResourceApi } from './api/crud.js';
import { CrudServiceApi } from './api/crud-service.js';
import { ActionsApi } from './api/actions.js';
import { ReportsApi } from './api/reports.js';
import { FilesApi } from './api/files.js';
import { SaasApi } from './api/saas.js';
import { ScheduleApi } from './api/schedule.js';
import type { DynamiaClientConfig } from './types.js';

/**
 * Root client for the Dynamia Platform REST API.
 *
 * @example
 * ```typescript
 * const client = new DynamiaClient({ baseUrl: 'https://app.example.com', token: '...' });
 * const app = await client.metadata.getApp();
 * const books = await client.crud('store/catalog/books').findAll();
 * ```
 */
export class DynamiaClient {
  private readonly http: HttpClient;

  /** Application metadata API */
  readonly metadata: MetadataApi;
  /** Global & entity actions API */
  readonly actions: ActionsApi;
  /** Reports extension API */
  readonly reports: ReportsApi;
  /** Entity-files extension API */
  readonly files: FilesApi;
  /** SaaS extension API */
  readonly saas: SaasApi;
  /** Scheduled-tasks API */
  readonly schedule: ScheduleApi;

  constructor(config: DynamiaClientConfig) {
    this.http = new HttpClient(config);
    this.metadata = new MetadataApi(this.http);
    this.actions = new ActionsApi(this.http);
    this.reports = new ReportsApi(this.http);
    this.files = new FilesApi(this.http);
    this.saas = new SaasApi(this.http);
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
}


