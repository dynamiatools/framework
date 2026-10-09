import { describe, expect, it, vi } from 'vitest';
import { flushPromises } from '@vue/test-utils';
import { useReport } from '../src/index.js';
import { bodyOf, callsTo, definition, filter, makeClient, runResult } from './helpers.js';

const filters = [
  filter('year', { dataType: 'NUMBER', defaultValue: '2026' }),
  filter('from', { dataType: 'DATE_TIME', defaultValue: '2026-01-01 08:00:00' }),
  filter('region', { required: true, optionsSource: 'QUERY' }),
];

function setup(extra: Record<string, unknown> = {}, report = definition(filters)) {
  const download = vi.fn();
  const { client, fetchMock } = makeClient({
    '/api/reports/v2/7/filters/': [{ value: 'n', label: 'North' }],
    '/api/reports/v2/7/export': () => ({ blob: new Blob(['x']), contentType: 'text/csv' }),
    '/api/reports/v2/7/run': () => ({ body: runResult() }),
    '/api/reports/v2/7': report,
    ...extra,
  });
  return { client, fetchMock, download };
}

describe('useReport', () => {
  it('loads the definition and starts the form values from the defaults', async () => {
    const { client } = setup();
    const report = useReport(client, 7, { download: vi.fn() });
    await flushPromises();

    expect(report.definition.value?.report.name).toBe('Sales by region');
    expect(report.values).toEqual({ year: '2026', from: '2026-01-01T08:00', region: '' });
    expect(report.loading.value).toBe(false);
  });

  it('does not run automatically while a required filter is missing', async () => {
    const { client, fetchMock } = setup();
    const report = useReport(client, 7);
    await flushPromises();

    expect(report.missingRequired.value).toEqual(['region']);
    expect(report.canRun.value).toBe(false);
    expect(callsTo(fetchMock, '/run')).toHaveLength(0);
  });

  it('runs automatically when nothing is required', async () => {
    const { client, fetchMock } = setup({}, definition([filter('year')]));
    const report = useReport(client, 7);
    await flushPromises();

    expect(callsTo(fetchMock, '/run')).toHaveLength(1);
    expect(report.result.value?.total).toBe(2);
  });

  it('does not run automatically when autoRun is off', async () => {
    const { client, fetchMock } = setup({}, definition([]));
    useReport(client, 7, { autoRun: false });
    await flushPromises();
    expect(callsTo(fetchMock, '/run')).toHaveLength(0);
  });

  it('sends the converted filters, paging and sorting', async () => {
    const { client, fetchMock } = setup();
    const report = useReport(client, 7, { pageSize: 10 });
    await flushPromises();
    report.values.region = 'north';
    await report.search();

    expect(bodyOf(fetchMock, '/run')).toEqual({
      filters: { year: '2026', from: '2026-01-01 08:00:00', region: 'north' },
      page: 0,
      size: 10,
    });
  });

  it('toggles the sort direction and goes back to the first page', async () => {
    const { client, fetchMock } = setup({}, definition([]));
    const report = useReport(client, 7);
    await flushPromises();

    await report.sortBy('total');
    expect(bodyOf(fetchMock, '/run', 1)).toMatchObject({ sort: 'total', direction: 'asc', page: 0 });
    await report.sortBy('total');
    expect(bodyOf(fetchMock, '/run', 2)).toMatchObject({ sort: 'total', direction: 'desc' });
    await report.sortBy('region');
    expect(bodyOf(fetchMock, '/run', 3)).toMatchObject({ sort: 'region', direction: 'asc' });
  });

  it('pages within the available pages and resets to the first page when the size changes', async () => {
    const { client, fetchMock } = setup({ '/api/reports/v2/7/run': () => ({ body: runResult({ total: 95 }) }) }, definition([]));
    const report = useReport(client, 7, { pageSize: 25 });
    await flushPromises();

    expect(report.totalPages.value).toBe(4);
    await report.setPage(99);
    expect(bodyOf(fetchMock, '/run', 1)).toMatchObject({ page: 3 });
    await report.setPage(-5);
    expect(bodyOf(fetchMock, '/run', 2)).toMatchObject({ page: 0 });
    await report.setPageSize(50);
    expect(bodyOf(fetchMock, '/run', 3)).toMatchObject({ page: 0, size: 50 });
  });

  it('keeps the error message of a failed run', async () => {
    const { client } = setup({ '/api/reports/v2/7/run': () => ({ status: 400, body: { message: 'Invalid value for filter [year]' } }) }, definition([]));
    const report = useReport(client, 7);
    await flushPromises();

    expect(report.runError.value).toContain('year');
    expect(report.result.value).toBeNull();
    expect(report.running.value).toBe(false);
  });

  it('reports a definition that cannot be loaded', async () => {
    const { client } = setup({ '/api/reports/v2/7': () => ({ status: 404, body: { message: 'Report not found: 7' } }) });
    const report = useReport(client, 7);
    await flushPromises();

    expect(report.error.value).toBe('Report not found: 7');
    expect(report.definition.value).toBeNull();
  });

  it('exports with all the rows: no paging, but filters and sorting', async () => {
    const { client, fetchMock, download } = setup({}, definition([filter('year', { defaultValue: '2026' })]));
    const report = useReport(client, 7, { download });
    await flushPromises();
    await report.sortBy('total');
    await report.exportAs('csv');

    const [url] = callsTo(fetchMock, '/export');
    expect(url).toContain('/api/reports/v2/7/export?format=csv');
    expect(bodyOf(fetchMock, '/export')).toEqual({ filters: { year: '2026' }, sort: 'total', direction: 'asc' });
    expect(download).toHaveBeenCalledTimes(1);
    expect(download.mock.calls[0]![1]).toMatch(/^sales-by-region-\d{4}-\d{2}-\d{2}\.csv$/);
    expect(report.exporting.value).toBe(false);
  });

  it('does not export while a required filter is missing', async () => {
    const { client, fetchMock, download } = setup();
    const report = useReport(client, 7, { download });
    await flushPromises();
    await report.exportAs('xlsx');

    expect(callsTo(fetchMock, '/export')).toHaveLength(0);
    expect(download).not.toHaveBeenCalled();
  });

  it('keeps an export error', async () => {
    const { client } = setup({ '/api/reports/v2/7/export': () => ({ status: 403, body: { message: 'Access denied to this report' } }) }, definition([]));
    const report = useReport(client, 7, { download: vi.fn() });
    await flushPromises();
    await report.exportAs('pdf');

    expect(report.runError.value).toBe('Access denied to this report');
  });

  it('loads the options of a filter', async () => {
    const { client, fetchMock } = setup();
    const report = useReport(client, 7);
    await flushPromises();

    expect(await report.filterOptions('region', 'no')).toEqual([{ value: 'n', label: 'North' }]);
    expect(callsTo(fetchMock, '/filters/region/options')[0]).toContain('q=no');
  });

  it('reset clears the result and restores the defaults', async () => {
    const { client } = setup({}, definition([filter('year', { defaultValue: '2026' })]));
    const report = useReport(client, 7);
    await flushPromises();
    report.values.year = '1999';
    report.reset();

    expect(report.values.year).toBe('2026');
    expect(report.result.value).toBeNull();
  });

  it('ignores the answer of an older run', async () => {
    let call = 0;
    const { client } = setup({
      '/api/reports/v2/7/run': () => {
        call++;
        return { body: runResult({ total: call }) };
      },
    }, definition([]));
    const report = useReport(client, 7, { autoRun: false });
    await flushPromises();
    const first = report.run();
    const second = report.run();
    await Promise.all([first, second]);

    expect(report.result.value?.total).toBe(2);
  });
});
