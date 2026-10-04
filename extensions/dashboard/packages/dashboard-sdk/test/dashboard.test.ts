import { describe, it, expect } from 'vitest';
import type { ViewDescriptor } from '@dynamia-tools/sdk';
import { DashboardApi, resolveDashboardLayout } from '../src/index.js';
import { makeHttpClient, mockFetch } from './helpers.js';

describe('DashboardApi', () => {
  it('widget() calls GET /api/dashboard/{id}/widgets/{field} with encoded segments and params', async () => {
    const fetchMock = mockFetch(200, { field: 'total sales', widget: 'sales-kpi', type: 'kpi', data: { value: 1 } });
    const api = new DashboardApi(makeHttpClient(fetchMock));

    const result = await api.widget('main dash', 'total sales', { range: 'lastMonth', skip: undefined });

    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toBe('https://app.example.com/api/dashboard/main%20dash/widgets/total%20sales?range=lastMonth');
    expect(init.method).toBe('GET');
    expect(result.type).toBe('kpi');
  });

  it('widget() rejects with DynamiaApiError on 404', async () => {
    const fetchMock = mockFetch(404, { message: 'Widget field not found: nope' });
    const api = new DashboardApi(makeHttpClient(fetchMock));

    await expect(api.widget('main', 'nope')).rejects.toMatchObject({ status: 404 });
  });
});

function dashboard(columns: number | undefined, fields: Array<[string, Record<string, unknown>]>): ViewDescriptor {
  return {
    id: 'main',
    beanClass: '',
    view: 'dashboard',
    params: {},
    ...(columns !== undefined ? { layout: { params: { columns } } } : {}),
    fields: fields.map(([name, params]) => ({ name, params })),
  };
}

describe('resolveDashboardLayout', () => {
  it('uses 4 columns by default: 4 one-span fields fill a row of 3-wide cells', () => {
    const layout = resolveDashboardLayout(
      dashboard(undefined, [['a', {}], ['b', {}], ['c', {}], ['d', {}], ['e', {}]]),
    );

    expect(layout.columns).toBe(4);
    expect(layout.rows.map((r) => r.map((c) => c.field))).toEqual([['a', 'b', 'c', 'd'], ['e']]);
    expect(layout.rows[0]![0]!.span).toEqual({ md: 3, sm: 6 });
  });

  it('honours columns, span, span-sm and span-xs and exposes the widget id', () => {
    const layout = resolveDashboardLayout(
      dashboard(3, [
        ['wide', { widget: 'sales-chart', span: 2, 'span-sm': '3', 'span-xs': 1 }],
        ['narrow', { widget: 'total', span: 1 }],
        ['next', { widget: 'other' }],
      ]),
    );

    expect(layout.rows.map((r) => r.map((c) => c.field))).toEqual([['wide', 'narrow'], ['next']]);
    expect(layout.rows[0]![0]).toMatchObject({ widget: 'sales-chart', span: { md: 8, sm: 12, xs: 12 } });
    expect(layout.rows[0]![1]!.span).toEqual({ md: 4, sm: 6 });
  });

  it('clamps a span larger than the columns to the whole row and ignores invalid values', () => {
    const layout = resolveDashboardLayout(dashboard(4, [['a', { span: 99 }], ['b', { span: 'x' }]]));

    expect(layout.rows[0]![0]!.span.md).toBe(12);
    expect(layout.rows[1]![0]!.span.md).toBe(3);
  });

  it('returns no rows for a descriptor without fields', () => {
    expect(resolveDashboardLayout(dashboard(undefined, [])).rows).toEqual([]);
  });
});
