// crudQuery.ts — maps the neutral query of a TableView to the parameters of the REST CRUD read.

/** Query values as the SDK `findAll` accepts them. */
export type CrudRestQuery = Record<string, string | number | boolean | undefined | null>;

/**
 * Translates what a {@link TableView} asks for (`q`, `sortField`, `sortDir`, plus `page` and the field filters) to the REST
 * contract of `RestNavigationQuerySupport`: the free text search is `_q` and the order is `_sort` / `_order`. Everything else
 * (page, size, field filters) passes through unchanged.
 */
export function toRestQuery(params: Record<string, unknown>): CrudRestQuery {
  const { q, sortField, sortDir, ...rest } = params;
  const query = { ...rest } as CrudRestQuery;
  if (typeof q === 'string' && q !== '') query['_q'] = q;
  if (typeof sortField === 'string' && sortField !== '') {
    query['_sort'] = sortField;
    query['_order'] = typeof sortDir === 'string' && sortDir !== '' ? sortDir : 'asc';
  }
  return query;
}
