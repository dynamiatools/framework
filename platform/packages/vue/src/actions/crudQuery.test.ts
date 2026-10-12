import { describe, expect, it } from 'vitest';
import { toRestQuery } from './crudQuery.js';

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
