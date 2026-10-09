import { describe, it, expect } from 'vitest';
import { DynamiaApiError } from '@dynamia-tools/sdk';
import { ReportsApi } from '../src/index.js';
import { mockFetch, makeHttpClient } from './helpers.js';

function api(status: number, body: unknown, contentType?: string) {
  const fetch = mockFetch(status, body, contentType);
  return { api: new ReportsApi(makeHttpClient(fetch)), fetch };
}

function call(fetch: ReturnType<typeof mockFetch>, index = 0): { url: string; init: RequestInit } {
  const [url, init] = fetch.mock.calls[index] as [string, RequestInit];
  return { url, init };
}

describe('ReportsApi legacy endpoints', () => {
  it('list() calls GET /api/reports', async () => {
    const reports = [{ name: 'sales', endpoint: 'sales/monthly' }];
    const { api: reportsApi, fetch } = api(200, reports);
    expect(await reportsApi.list()).toEqual(reports);
    expect(call(fetch).url).toBe('https://app.example.com/api/reports');
  });

  it('get() sends query-string filters and encodes the path', async () => {
    const { api: reportsApi, fetch } = api(200, { data: [] });
    await reportsApi.get('my group', 'monthly', { year: 2026, region: undefined });
    expect(call(fetch).url).toBe('https://app.example.com/api/reports/my%20group/monthly?year=2026');
  });

  it('post() sends the structured filters', async () => {
    const { api: reportsApi, fetch } = api(200, { data: [{ a: 1 }], truncated: true });
    const result = await reportsApi.post('sales', 'monthly', { options: [{ name: 'year', value: '2025' }] });
    expect(call(fetch).init.method).toBe('POST');
    expect(JSON.parse(call(fetch).init.body as string)).toEqual({ options: [{ name: 'year', value: '2025' }] });
    expect(result.truncated).toBe(true);
  });
});

describe('ReportsApi UI endpoints', () => {
  it('catalog() calls GET /api/reports/v2/catalog', async () => {
    const catalog = [{ name: 'Sales', reports: [{ id: 1, name: 'By region', chartable: false, hasFilters: true }] }];
    const { api: reportsApi, fetch } = api(200, catalog);
    expect(await reportsApi.catalog()).toEqual(catalog);
    expect(call(fetch).url).toBe('https://app.example.com/api/reports/v2/catalog');
  });

  it('definition() calls GET /api/reports/v2/{id}', async () => {
    const { api: reportsApi, fetch } = api(200, { report: { id: 7 }, filters: [], columns: [], charts: [] });
    await reportsApi.definition(7);
    expect(call(fetch).url).toBe('https://app.example.com/api/reports/v2/7');
  });

  it('filterOptions() sends q and limit and skips undefined', async () => {
    const { api: reportsApi, fetch } = api(200, [{ value: 'a', label: 'A' }]);
    await reportsApi.filterOptions(7, 'region', { q: 'no', limit: 10 });
    expect(call(fetch).url).toBe('https://app.example.com/api/reports/v2/7/filters/region/options?q=no&limit=10');

    const second = api(200, []);
    await second.api.filterOptions(7, 'a/b');
    expect(call(second.fetch).url).toBe('https://app.example.com/api/reports/v2/7/filters/a%2Fb/options');
  });

  it('run() posts the request and returns the typed result', async () => {
    const result = { columns: [], rows: [{ a: 1 }], total: 1, page: 0, size: 1, truncated: false, durationMs: 3, charts: [] };
    const { api: reportsApi, fetch } = api(200, result);
    const run = await reportsApi.run(7, { filters: { year: 2025 }, page: 1, size: 20, sort: 'a', direction: 'desc' });
    expect(call(fetch).url).toBe('https://app.example.com/api/reports/v2/7/run');
    expect(call(fetch).init.method).toBe('POST');
    expect(JSON.parse(call(fetch).init.body as string)).toEqual({
      filters: { year: 2025 }, page: 1, size: 20, sort: 'a', direction: 'desc',
    });
    expect(run.total).toBe(1);
  });

  it('run() without a request sends an empty object', async () => {
    const { api: reportsApi, fetch } = api(200, {});
    await reportsApi.run(7);
    expect(JSON.parse(call(fetch).init.body as string)).toEqual({});
  });

  it('export() requests the format and returns a Blob', async () => {
    const { api: reportsApi, fetch } = api(200, 'x', 'text/csv');
    const blob = await reportsApi.export(7, 'csv', { filters: { year: 1 } });
    expect(call(fetch).url).toBe('https://app.example.com/api/reports/v2/7/export?format=csv');
    expect(blob).toBeInstanceOf(Blob);
  });

  it('turns API errors into DynamiaApiError with the server message', async () => {
    const { api: reportsApi } = api(400, { message: 'Invalid value for filter [year]', error: 'INVALID_REQUEST' });
    const error = await reportsApi.run(7).catch((e) => e);
    expect(error).toBeInstanceOf(DynamiaApiError);
    expect(error.status).toBe(400);
    expect(error.message).toContain('year');
  });

  it('keeps the bearer token', async () => {
    const { api: reportsApi, fetch } = api(200, []);
    await reportsApi.catalog();
    expect((call(fetch).init.headers as Record<string, string>).Authorization).toBe('Bearer test-token');
  });
});

describe('ReportsApi designer endpoints', () => {
  it('designer() calls GET /design/info', async () => {
    const { api: reportsApi, fetch } = api(200, { allowed: true, previewLimit: 50 });
    expect((await reportsApi.designer()).allowed).toBe(true);
    expect(call(fetch).url).toBe('https://app.example.com/api/reports/v2/design/info');
  });

  it('preview() posts the query being designed', async () => {
    const { api: reportsApi, fetch } = api(200, { columns: ['a'], rows: [], truncated: false, durationMs: 1 });
    await reportsApi.preview({ queryLang: 'sql', queryScript: 'select 1', dataSourceId: 3 });
    expect(call(fetch).url).toBe('https://app.example.com/api/reports/v2/design/preview');
    expect(JSON.parse(call(fetch).init.body as string)).toEqual({ queryLang: 'sql', queryScript: 'select 1', dataSourceId: 3 });
  });

  it('exports and imports definitions', async () => {
    const exported = api(200, { name: 'x' });
    expect(await exported.api.exportDefinition(5)).toEqual({ name: 'x' });
    expect(call(exported.fetch).url).toBe('https://app.example.com/api/reports/v2/design/5/definition');

    const imported = api(200, { id: 9 });
    expect(await imported.api.importDefinition({ name: 'x' })).toEqual({ id: 9 });
    expect(call(imported.fetch).url).toBe('https://app.example.com/api/reports/v2/design/import');
  });

  it('testDataSource() posts without a body', async () => {
    const { api: reportsApi, fetch } = api(200, { ok: true, message: 'Connection ok' });
    expect((await reportsApi.testDataSource(2)).ok).toBe(true);
    expect(call(fetch).url).toBe('https://app.example.com/api/reports/v2/design/datasources/2/test');
    expect(call(fetch).init.body).toBeUndefined();
  });
});
