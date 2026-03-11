import { describe, it, expect } from 'vitest';
import { DynamiaClient, DynamiaApiError } from '../src/index.js';
import { mockFetch, makeClient, rawPageResponse } from './helpers.js';

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
