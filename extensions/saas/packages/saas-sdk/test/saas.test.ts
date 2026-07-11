import { describe, it, expect } from 'vitest';
import { SaasApi } from '../src/index.js';
import { mockFetch, makeHttpClient } from './helpers.js';

describe('SaasApi', () => {
  it('getAccount() calls GET /api/saas/account/{uuid}', async () => {
    const account = { id: 1, uuid: 'abc-123', name: 'Acme Corp', status: 'ACTIVE' };
    const fetch = mockFetch(200, account);
    const api = new SaasApi(makeHttpClient(fetch));
    const result = await api.getAccount('abc-123');
    expect(result).toEqual(account);
    const [url] = fetch.mock.calls[0] as [string, RequestInit];
    expect(url).toContain('/api/saas/account/abc-123');
  });
});

