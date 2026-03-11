import { describe, it, expect } from 'vitest';
import { mockFetch, makeClient } from '../helpers.js';

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
