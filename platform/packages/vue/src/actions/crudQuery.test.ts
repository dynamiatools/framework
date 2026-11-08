import { describe, expect, it } from 'vitest';
import { toQuerySpec, toRestQuery } from './crudQuery.js';

describe('toRestQuery', () => {
  it('maps the search text to _q', () => {
    expect(toRestQuery({ q: 'dune', page: 2 })).toEqual({ _q: 'dune', page: 2 });
  });

  it('maps the order to _sort and _order, ascending by default', () => {
    expect(toRestQuery({ sortField: 'year', sortDir: 'desc' })).toEqual({ _sort: 'year', _order: 'desc' });
    expect(toRestQuery({ sortField: 'year', sortDir: null })).toEqual({ _sort: 'year', _order: 'asc' });
  });

  it('passes the field filters and the page through, and drops an empty search', () => {
    expect(toRestQuery({ q: '', year: '1999', page: 1 })).toEqual({ year: '1999', page: 1 });
  });
});

describe('toQuerySpec', () => {
  const fieldClassOf = (field: string) => (field === 'title' ? 'java.lang.String' : 'java.lang.Integer');

  it('is null when the table asks for no field filter, so the plain GET is enough', () => {
    expect(toQuerySpec({ page: 2, q: 'dune', sortField: 'title' }, fieldClassOf)).toBeNull();
  });

  it('keeps the filters of the controls and carries search, order and paging', () => {
    const spec = toQuerySpec({
      filters: [{ field: 'publishDate', op: 'between', value: ['2026-01-01', null] }],
      q: 'dune', sortField: 'title', sortDir: 'desc', page: 3, size: 20,
    }, fieldClassOf);

    expect(spec).toEqual({
      filters: [{ field: 'publishDate', op: 'between', value: ['2026-01-01', null] }],
      search: 'dune', sort: [{ field: 'title', dir: 'desc' }], page: 3, size: 20,
    });
  });

  it('turns the advanced search values into like for text and eq for the rest, skipping blanks', () => {
    const spec = toQuerySpec({ title: 'Clean', year: '2018', isbn: '  ' }, fieldClassOf);

    expect(spec?.filters).toEqual([
      { field: 'title', op: 'like', value: 'Clean' },
      { field: 'year', op: 'eq', value: '2018' },
    ]);
  });

  it('keeps the filters of the controls out of the plain GET', () => {
    expect(toRestQuery({ page: 1, filters: [{ field: 'f', op: 'eq', value: 1 }] })).toEqual({ page: 1 });
  });
});
