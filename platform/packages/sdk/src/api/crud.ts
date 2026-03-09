import type { HttpClient } from '../http.js';
import type { CrudListResult, CrudQueryParams, CrudRawResponse } from '../types.js';

/**
 * CRUD operations for a single CrudPage resource (navigation-based endpoints).
 * Base path: /api/{virtualPath}
 *
 * The server returns:
 *   { data: T[], pageable: { page, pageSize, totalSize, pagesNumber, ... }, response: "OK" }
 * The SDK normalises this into CrudListResult<T>.
 */
export class CrudResourceApi<T = unknown> {
  private readonly http: HttpClient;
  private readonly basePath: string;

  constructor(http: HttpClient, virtualPath: string) {
    this.http = http;
    // Normalise: strip leading slash so we can always prepend /api/
    const clean = virtualPath.replace(/^\/+/, '');
    this.basePath = `/api/${clean}`;
  }

  /** GET /api/{path} — List all (with optional pagination / filter params) */
  async findAll(params?: CrudQueryParams): Promise<CrudListResult<T>> {
    const raw = await this.http.get<CrudRawResponse<T>>(
      this.basePath,
      params as Record<string, string | number | boolean | undefined | null>,
    );
    return normaliseCrudResponse(raw);
  }

  /** GET /api/{path}/{id} — Get by ID */
  findById(id: string | number): Promise<T> {
    return this.http.get<T>(`${this.basePath}/${id}`);
  }

  /** POST /api/{path} — Create */
  create(entity: Partial<T>): Promise<T> {
    return this.http.post<T>(this.basePath, entity);
  }

  /** PUT /api/{path}/{id} — Full update */
  update(id: string | number, entity: Partial<T>): Promise<T> {
    return this.http.put<T>(`${this.basePath}/${id}`, entity);
  }

  /** DELETE /api/{path}/{id} — Delete */
  delete(id: string | number): Promise<void> {
    return this.http.delete<void>(`${this.basePath}/${id}`);
  }
}

// ── helpers ──────────────────────────────────────────────────────────────────

/**
 * Map the raw `RestNavigationContext.ListResult` envelope to the normalised `CrudListResult`.
 *
 * - `pageable` may be `null` (annotated `@JsonInclude(NON_NULL)` in Java) when the
 *   endpoint returns a flat, non-paginated list.
 * - Defensive: also handles the already-normalised flat format (e.g. test mocks).
 */
function normaliseCrudResponse<T>(raw: CrudRawResponse<T> | CrudListResult<T>): CrudListResult<T> {
  // Already normalised (e.g. mocked in tests with flat format)
  if ('content' in raw && Array.isArray((raw as CrudListResult<T>).content)) {
    return raw as CrudListResult<T>;
  }

  const r = raw as CrudRawResponse<T>;
  const p = r.pageable; // may be null — @JsonInclude(NON_NULL)

  return {
    content: r.data ?? [],
    total: p?.totalSize ?? (r.data?.length ?? 0),
    page: p?.page ?? 1,
    pageSize: p?.pageSize ?? (r.data?.length ?? 0),
    totalPages: p?.pagesNumber ?? 1,
  };
}

