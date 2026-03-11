// ─── CRUD types mirroring the Dynamia Platform Java model ───────────────────

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
