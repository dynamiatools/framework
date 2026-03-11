import { describe, it, expect, beforeEach, vi } from 'vitest';
import { DynamiaClient } from '../../src/index.js';
import { mockFetch, makeClient } from '../helpers.js';

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
