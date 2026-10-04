import { describe, it, expect, beforeEach, type Mock } from 'vitest';
import { DynamiaClient } from '../../src/index.js';
import { mockFetch, makeClient } from '../helpers.js';

describe('MetadataApi', () => {
  let fetchMock: Mock<typeof fetch>;
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
      json: () => Promise.resolve({ navigation: [] }),
    } as unknown as Response);
    const result = await client.metadata.getNavigation();
    const [url] = fetchMock.mock.calls[0] as [string];
    expect(url).toContain('/api/app/metadata/navigation');
    expect(result.navigation).toEqual([]);
  });
  it('getEntity(className) encodes the class name in the URL', async () => {
    await client.metadata.getEntity('com.example.Book');
    const [url] = fetchMock.mock.calls[0] as [string];
    expect(url).toContain('/api/app/metadata/entities/com.example.Book');
  });
  it('getView(id) calls GET /api/app/metadata/views/{id} and caches the result', async () => {
    fetchMock.mockResolvedValue({
      ok: true, status: 200,
      headers: { get: () => 'application/json' },
      json: () => Promise.resolve({ id: 'main dash', view: 'dashboard', fields: [], params: {} }),
    } as unknown as Response);
    const first = await client.metadata.getView('main dash');
    const second = await client.metadata.getView('main dash');
    const [url] = fetchMock.mock.calls[0] as [string];
    expect(url).toContain('/api/app/metadata/views/main%20dash');
    expect(first.view).toBe('dashboard');
    expect(second).toBe(first);
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });
  it('clearViewDescriptorCache(id) invalidates a descriptor fetched with getView', async () => {
    await client.metadata.getView('mainDashboard');
    client.clearViewDescriptorCache('mainDashboard');
    await client.metadata.getView('mainDashboard');
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });
  it('clearViewDescriptorCache() with no id clears descriptors fetched with getView', async () => {
    await client.metadata.getView('mainDashboard');
    client.clearViewDescriptorCache();
    await client.metadata.getView('mainDashboard');
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });
});
