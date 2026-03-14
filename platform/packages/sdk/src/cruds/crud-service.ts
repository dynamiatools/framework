import type { HttpClient } from '../http.js';

type FindParams = Record<string, string | number | boolean | undefined | null>;

/**
 * Low-level generic CRUD endpoint by fully-qualified Java class name.
 * Base path: /crud-service/{className}
 */
export class CrudServiceApi<T = unknown> {
  private readonly http: HttpClient;
  private readonly basePath: string;

  constructor(http: HttpClient, className: string) {
    this.http = http;
    this.basePath = `/crud-service/${encodeURIComponent(className)}`;
  }

  /**
   * `POST /crud-service/{className}` — Persist an entity (create or update).
   *
   * The Java controller uses a single `@PostMapping` for both create and
   * update operations.  Whether the request results in an INSERT or UPDATE is
   * determined server-side based on the presence of a non-null entity ID.
   * There is no separate PUT endpoint.
   */
  save(entity: Partial<T>): Promise<T> {
    return this.http.post<T>(this.basePath, entity);
  }

  /** GET /crud-service/{className}/{id} — Find by ID */
  findById(id: string | number): Promise<T> {
    return this.http.get<T>(`${this.basePath}/${id}`);
  }

  /** DELETE /crud-service/{className}/{id} — Delete by ID */
  delete(id: string | number): Promise<void> {
    return this.http.delete<void>(`${this.basePath}/${id}`);
  }

  /** POST /crud-service/{className}/find — Find by query parameters */
  find(params: FindParams): Promise<T[]> {
    return this.http.post<T[]>(`${this.basePath}/find`, params);
  }

  /** POST /crud-service/{className}/id — Get just the ID matching query parameters */
  getId(params: FindParams): Promise<string | number> {
    return this.http.post<string | number>(`${this.basePath}/id`, params);
  }
}
