// crudQuery.ts — maps the neutral query of a TableView to the parameters of the REST CRUD read.
import type { CrudFilter, QuerySpec } from '@dynamia-tools/sdk';

/** Query values as the SDK `findAll` accepts them. */
export type CrudRestQuery = Record<string, string | number | boolean | undefined | null>;

/**
 * Translates what a {@link TableView} asks for (`q`, `sortField`, `sortDir`, plus `page` and the field filters) to the REST
 * contract of `RestNavigationQuerySupport`: the free text search is `_q` and the order is `_sort` / `_order`. Everything else
 * (page, size, field filters) passes through unchanged.
 */
export function toRestQuery(params: Record<string, unknown>): CrudRestQuery {
  const { q, sortField, sortDir, filters: _filters, ...rest } = params;
  const query = { ...rest } as CrudRestQuery;
  if (typeof q === 'string' && q !== '') query['_q'] = q;
  if (typeof sortField === 'string' && sortField !== '') {
    query['_sort'] = sortField;
    query['_order'] = typeof sortDir === 'string' && sortDir !== '' ? sortDir : 'asc';
  }
  return query;
}


/**
 * The `QuerySpec` of `POST /find` for what a {@link TableView} asks for, or `null` when it has no field filter and the plain
 * `GET` (page, size, `_q`, order) is enough. The filters come from toolbar controls (`filters`, already with their operator)
 * and from the advanced search panel (`field: value`, which becomes `like` for text fields and `eq` for the rest).
 *
 * @param params       the neutral query of the table
 * @param fieldClassOf the Java class name of a field of the table, to pick the operator of a plain `field: value`
 */
export function toQuerySpec(params: Record<string, unknown>, fieldClassOf: (field: string) => string | undefined): QuerySpec | null {
  const { q, sortField, sortDir, filters, page, size, ...rest } = params;
  const list: CrudFilter[] = Array.isArray(filters) ? [...(filters as CrudFilter[])] : [];
  for (const [field, value] of Object.entries(rest)) {
    if (value === undefined || value === null || String(value).trim() === '') continue;
    list.push({ field, op: fieldClassOf(field) === 'java.lang.String' ? 'like' : 'eq', value: value as string | number | boolean });
  }
  if (list.length === 0) return null;

  const spec: QuerySpec = { filters: list };
  if (typeof q === 'string' && q !== '') spec.search = q;
  if (typeof sortField === 'string' && sortField !== '') {
    spec.sort = [{ field: sortField, dir: sortDir === 'desc' ? 'desc' : 'asc' }];
  }
  if (typeof page === 'number') spec.page = page;
  if (typeof size === 'number') spec.size = size;
  return spec;
}
