import { describe, it, expect } from 'vitest';
import { mockFetch, makeClient } from '../helpers.js';

describe('ConfigResourceApi', () => {
  it('read() calls GET /api/{path} and returns the descriptor id and the values', async () => {
    const fetchMock = mockFetch(200, { descriptor: 'DemoCFG', data: { location: '/data', useHttps: true }, response: 'OK' });
    const result = await makeClient(fetchMock).config('/system/config/demo').read();

    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toContain('/api/system/config/demo');
    expect(init.method ?? 'GET').toBe('GET');
    expect(result.descriptor).toBe('DemoCFG');
    expect(result.data).toEqual({ location: '/data', useHttps: true });
  });

  it('save() calls PUT /api/{path} with the values as the body', async () => {
    const fetchMock = mockFetch(200, { descriptor: 'DemoCFG', data: { location: '/srv' }, response: 'OK' });
    await makeClient(fetchMock).config('system/config/demo').save({ location: '/srv' });

    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toContain('/api/system/config/demo');
    expect(init.method).toBe('PUT');
    expect(JSON.parse(init.body as string)).toEqual({ location: '/srv' });
  });
});
