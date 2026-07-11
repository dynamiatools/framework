import { describe, it, expect, vi } from 'vitest';
import { ReportsApi } from '../src/index.js';
import { mockFetch, makeHttpClient } from './helpers.js';

describe('ReportsApi', () => {
  it('list() calls GET /api/reports', async () => {
    const reports = [{ name: 'sales', endpoint: 'sales/monthly' }];
    const fetch = mockFetch(200, reports);
    const api = new ReportsApi(makeHttpClient(fetch));
    const result = await api.list();
    expect(result).toEqual(reports);
    expect(fetch).toHaveBeenCalledOnce();
  });

  it('post() calls POST /api/reports/{group}/{endpoint}', async () => {
    const fetch = mockFetch(200, { rows: [] });
    const api = new ReportsApi(makeHttpClient(fetch));
    await api.post('sales', 'monthly', { options: [{ name: 'year', value: '2025' }] });
    const [url] = fetch.mock.calls[0] as [string, RequestInit];
    expect(url).toContain('/api/reports/sales/monthly');
  });
});

