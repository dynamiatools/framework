import { describe, it, expect } from 'vitest';
import { mockFetch, makeClient, rawPageResponse } from '../helpers.js';

describe('CrudResourceApi', () => {
  it('findAll() calls GET /api/{path}', async () => {
    const fetchMock = mockFetch(200, rawPageResponse([]));
    const client = makeClient(fetchMock);
    await client.crud('store/books').findAll();
    const [url] = fetchMock.mock.calls[0] as [string];
    expect(url).toContain('/api/store/books');
  });
  it('findAll() attaches query params', async () => {
    const fetchMock = mockFetch(200, rawPageResponse([]));
    const client = makeClient(fetchMock);
    await client.crud('books').findAll({ page: 2, size: 10 });
    const [url] = fetchMock.mock.calls[0] as [string];
    expect(url).toContain('page=2');
    expect(url).toContain('size=10');
  });
  it('findAll() normalises raw pageable envelope into CrudListResult', async () => {
    const books = [
      { id: 13, title: 'Clean Code', stockStatus: 'OUT_STOCK', price: 25.00 },
      { id: 14, title: 'Design Patterns', stockStatus: 'IN_STOCK', price: 0.00 },
    ];
    const fetchMock = mockFetch(200, rawPageResponse(books, 1, 50));
    const client = makeClient(fetchMock);
    const result = await client.crud('library/books').findAll();
    expect(result.content).toHaveLength(2);
    expect(result.content[0]).toMatchObject({ id: 13, title: 'Clean Code' });
    expect(result.total).toBe(2);
    expect(result.page).toBe(1);
    expect(result.pageSize).toBe(50);
    expect(result.totalPages).toBe(1);
  });
  it('findAll() handles null pageable (non-paginated flat response)', async () => {
    const rawResp = {
      data: [{ id: 1, title: 'Book A' }, { id: 2, title: 'Book B' }],
      pageable: null,
      response: 'OK',
    };
    const fetchMock = mockFetch(200, rawResp);
    const client = makeClient(fetchMock);
    const result = await client.crud('books').findAll();
    expect(result.content).toHaveLength(2);
    expect(result.total).toBe(2);
    expect(result.page).toBe(1);
    expect(result.totalPages).toBe(1);
  });
  it('findAll() handles multi-page results correctly', async () => {
    const rawResp = {
      data: [{ id: 1, title: 'Book A' }],
      pageable: { firstResult: 0, page: 2, pageSize: 10, pagesNumber: 5, totalSize: 48 },
      response: 'OK',
    };
    const fetchMock = mockFetch(200, rawResp);
    const client = makeClient(fetchMock);
    const result = await client.crud('books').findAll({ page: 2, size: 10 });
    expect(result.page).toBe(2);
    expect(result.pageSize).toBe(10);
    expect(result.totalPages).toBe(5);
    expect(result.total).toBe(48);
  });
  it('findById() calls GET /api/{path}/{id}', async () => {
    const fetchMock = mockFetch(200, { id: 42, title: 'Clean Code' });
    const client = makeClient(fetchMock);
    const book = await client.crud<{ id: number; title: string }>('books').findById(42);
    const [url] = fetchMock.mock.calls[0] as [string];
    expect(url).toContain('/api/books/42');
    expect(book.title).toBe('Clean Code');
  });
  it('create() calls POST /api/{path}', async () => {
    const fetchMock = mockFetch(200, { id: 1, title: 'New Book' });
    const client = makeClient(fetchMock);
    await client.crud('books').create({ title: 'New Book' });
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toContain('/api/books');
    expect(init.method).toBe('POST');
  });
  it('delete() calls DELETE /api/{path}/{id}', async () => {
    const fetchMock = mockFetch(204, null);
    fetchMock.mockResolvedValue({ ok: true, status: 204, headers: { get: () => '' } } as unknown as Response);
    const client = makeClient(fetchMock);
    await client.crud('books').delete(5);
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toContain('/api/books/5');
    expect(init.method).toBe('DELETE');
  });
});
