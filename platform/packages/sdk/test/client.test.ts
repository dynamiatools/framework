import { describe, it, expect, vi, beforeEach } from 'vitest';
import { DynamiaClient, DynamiaApiError } from '../src/index.js';
// ── Raw response helper ───────────────────────────────────────────────────────
function rawPageResponse<T>(items: T[], page = 1, pageSize = 50) {
  return {
    data: items,
    pageable: { firstResult: 0, page, pageSize, pagesNumber: 1, totalSize: items.length },
    response: 'OK',
  };
}
// ── Fetch mock helpers ────────────────────────────────────────────────────────
function mockFetch(status: number, body: unknown, contentType = 'application/json') {
  return vi.fn().mockResolvedValue({
    ok: status >= 200 && status < 300,
    status,
    statusText: status === 200 ? 'OK' : 'Error',
    headers: { get: (key: string) => (key === 'content-type' ? contentType : null) },
    json: () => Promise.resolve(body),
    text: () => Promise.resolve(String(body)),
    blob: () => Promise.resolve(new Blob()),
  } as unknown as Response);
}
function makeClient(fetchMock: ReturnType<typeof vi.fn>) {
  return new DynamiaClient({ baseUrl: 'https://app.example.com', token: 'test-token', fetch: fetchMock });
}
// ── DynamiaClient construction ────────────────────────────────────────────────
describe('DynamiaClient', () => {
  it('creates an instance with all sub-APIs', () => {
    const client = makeClient(mockFetch(200, {}));
    expect(client.metadata).toBeDefined();
    expect(client.actions).toBeDefined();
    expect(client.reports).toBeDefined();
    expect(client.files).toBeDefined();
    expect(client.saas).toBeDefined();
    expect(client.schedule).toBeDefined();
  });
  it('crud() returns a CrudResourceApi', () => {
    const client = makeClient(mockFetch(200, rawPageResponse([])));
    expect(client.crud('books')).toBeDefined();
  });
  it('crudService() returns a CrudServiceApi', () => {
    const client = makeClient(mockFetch(200, rawPageResponse([])));
    expect(client.crudService('com.example.Book')).toBeDefined();
  });
});
// ── MetadataApi ───────────────────────────────────────────────────────────────
describe('MetadataApi', () => {
  let fetchMock: ReturnType<typeof vi.fn>;
  let client: DynamiaClient;
  beforeEach(() => {
    fetchMock = mockFetch(200, { name: 'Demo App', version: '1.0.0' });
    client = makeClient(fetchMock);
  });
  it('getApp() calls GET /api/app/metadata', async () => {
    const result = await client.metadata.getApp();
    const [url] = fetchMock.mock.calls[0] as [string];
    expect(url).toContain('/api/app/metadata');
    expect(result.name).toBe('Demo App');
  });
  it('getNavigation() calls GET /api/app/metadata/navigation', async () => {
    fetchMock.mockResolvedValue({
      ok: true, status: 200,
      headers: { get: () => 'application/json' },
      json: () => Promise.resolve({ modules: [] }),
    } as unknown as Response);
    const result = await client.metadata.getNavigation();
    const [url] = fetchMock.mock.calls[0] as [string];
    expect(url).toContain('/api/app/metadata/navigation');
    expect(result.modules).toEqual([]);
  });
  it('getEntity(className) encodes the class name in the URL', async () => {
    await client.metadata.getEntity('com.example.Book');
    const [url] = fetchMock.mock.calls[0] as [string];
    expect(url).toContain('/api/app/metadata/entities/com.example.Book');
  });
});
// ── CrudResourceApi — normalisation ──────────────────────────────────────────
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
    // Java: @JsonInclude(NON_NULL) — pageable is absent when not paginated
    const rawResp = {
      data: [{ id: 1, title: 'Book A' }, { id: 2, title: 'Book B' }],
      pageable: null,
      response: 'OK',
    };
    const fetchMock = mockFetch(200, rawResp);
    const client = makeClient(fetchMock);

    const result = await client.crud('books').findAll();
    expect(result.content).toHaveLength(2);
    expect(result.total).toBe(2);   // falls back to data.length
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
// ── DynamiaApiError ───────────────────────────────────────────────────────────
describe('DynamiaApiError', () => {
  it('is thrown on non-2xx responses', async () => {
    const client = makeClient(mockFetch(404, { message: 'Not Found' }));
    await expect(client.crud('books').findById(999)).rejects.toThrow(DynamiaApiError);
  });
  it('carries the status code and URL', async () => {
    const client = makeClient(mockFetch(403, { message: 'Forbidden' }));
    try {
      await client.metadata.getApp();
    } catch (err) {
      expect(err).toBeInstanceOf(DynamiaApiError);
      const apiErr = err as DynamiaApiError;
      expect(apiErr.status).toBe(403);
      expect(apiErr.url).toContain('/api/app/metadata');
      expect(apiErr.message).toBe('Forbidden');
    }
  });
});
// ── Authentication ────────────────────────────────────────────────────────────
describe('Authentication', () => {
  it('sends Bearer token header', async () => {
    const fetchMock = mockFetch(200, {});
    const client = new DynamiaClient({ baseUrl: 'https://example.com', token: 'mytoken', fetch: fetchMock });
    await client.metadata.getApp();
    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect((init.headers as Record<string, string>)['Authorization']).toBe('Bearer mytoken');
  });
  it('sends Basic auth header', async () => {
    const fetchMock = mockFetch(200, {});
    const client = new DynamiaClient({ baseUrl: 'https://example.com', username: 'admin', password: 'pass', fetch: fetchMock });
    await client.metadata.getApp();
    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect((init.headers as Record<string, string>)['Authorization']).toBe('Basic ' + btoa('admin:pass'));
  });
});
// ── FilesApi ──────────────────────────────────────────────────────────────────
describe('FilesApi', () => {
  it('getUrl() returns a URL with uuid query param', () => {
    const client = makeClient(mockFetch(200, {}));
    const url = client.files.getUrl('photo.png', 'uuid-123');
    expect(url).toContain('/storage/photo.png');
    expect(url).toContain('uuid=uuid-123');
  });
});
// ── ActionsApi ────────────────────────────────────────────────────────────────
describe('ActionsApi', () => {
  it('executeGlobal() calls POST /api/app/metadata/actions/{action}', async () => {
    const fetchMock = mockFetch(200, { message: 'ok', status: 'SUCCESS', code: 200 });
    const client = makeClient(fetchMock);
    await client.actions.executeGlobal('sendEmail', { params: { to: 'a@b.com' } });
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toContain('/api/app/metadata/actions/sendEmail');
    expect(init.method).toBe('POST');
  });
});
