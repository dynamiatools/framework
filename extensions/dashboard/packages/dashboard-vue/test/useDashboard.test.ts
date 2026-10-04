import { describe, expect, it } from 'vitest';
import { nextTick, ref } from 'vue';
import { useDashboard } from '../src/index.js';
import { dashboardDescriptor, makeClient, widgetResponse } from './helpers.js';

const descriptor = dashboardDescriptor([
  ['totalSales', { widget: 'sales-kpi' }],
  ['monthSales', { widget: 'sales-chart', span: 2 }],
]);

async function settled(): Promise<void> {
  for (let i = 0; i < 5; i++) await new Promise((r) => setTimeout(r, 0));
}

describe('useDashboard', () => {
  it('loads the descriptor, computes the layout and loads every widget', async () => {
    const { client, fetchMock } = makeClient({
      '/api/app/metadata/views/main': descriptor,
      '/widgets/totalSales': widgetResponse('totalSales', 'kpi', { value: 7 }),
      '/widgets/monthSales': widgetResponse('monthSales', 'chart', { type: 'bar', data: {} }),
    });

    const d = useDashboard(client, 'main');
    await settled();

    expect(d.loading.value).toBe(false);
    expect(d.error.value).toBeNull();
    expect(d.layout.value?.rows.map((r) => r.map((c) => c.field))).toEqual([['totalSales', 'monthSales']]);
    expect(d.widgets.value['totalSales']?.response?.type).toBe('kpi');
    expect(d.widgets.value['monthSales']?.response?.type).toBe('chart');
    expect(fetchMock.mock.calls.map((c) => String(c[0]))).toEqual(
      expect.arrayContaining([
        'https://app.example.com/api/dashboard/main/widgets/totalSales',
        'https://app.example.com/api/dashboard/main/widgets/monthSales',
      ]),
    );
  });

  it('keeps a failing widget from affecting the others', async () => {
    const { client } = makeClient({
      '/api/app/metadata/views/main': descriptor,
      '/widgets/totalSales': () => ({ status: 500, body: { message: 'Error loading widget totalSales' } }),
      '/widgets/monthSales': widgetResponse('monthSales', 'chart', null),
    });

    const d = useDashboard(client, 'main');
    await settled();

    expect(d.widgets.value['totalSales']).toMatchObject({ loading: false, response: null });
    expect(d.widgets.value['totalSales']?.error).toContain('Error loading widget totalSales');
    expect(d.widgets.value['monthSales']?.error).toBeNull();
    expect(d.widgets.value['monthSales']?.response).not.toBeNull();
  });

  it('exposes a descriptor loading error', async () => {
    const { client } = makeClient({
      '/api/app/metadata/views/missing': () => ({ status: 404, body: { message: 'not found' } }),
    });

    const d = useDashboard(client, 'missing');
    await settled();

    expect(d.error.value).toContain('not found');
    expect(d.layout.value).toBeNull();
  });

  it('sends params to every widget and reloads a single widget with other params', async () => {
    const { client, fetchMock } = makeClient({
      '/api/app/metadata/views/main': descriptor,
      '/widgets/': (url: string) => ({ body: widgetResponse(url.includes('totalSales') ? 'totalSales' : 'monthSales', 'kpi', 1) }),
    });

    const d = useDashboard(client, 'main', { params: { range: 'month' } });
    await settled();
    expect(fetchMock.mock.calls.map((c) => String(c[0]))).toContain(
      'https://app.example.com/api/dashboard/main/widgets/totalSales?range=month',
    );

    await d.reloadWidget('totalSales', { range: 'year' });
    expect(String(fetchMock.mock.calls.at(-1)![0])).toBe(
      'https://app.example.com/api/dashboard/main/widgets/totalSales?range=year',
    );
  });

  it('reloads when the dashboard id changes and ignores stale results', async () => {
    const other = dashboardDescriptor([['only', { widget: 'x' }]]);
    other.id = 'second';
    const { client } = makeClient({
      '/api/app/metadata/views/main': descriptor,
      '/api/app/metadata/views/second': other,
      '/widgets/': (url: string) => ({ body: widgetResponse(url.split('/widgets/')[1]!, 'kpi', 1) }),
    });
    const id = ref('main');

    const d = useDashboard(client, id);
    await settled();
    id.value = 'second';
    await nextTick();
    await settled();

    expect(Object.keys(d.widgets.value)).toEqual(['only']);
  });

  it('does not load until load() when immediate is false', async () => {
    const { client, fetchMock } = makeClient({ '/api/app/metadata/views/main': descriptor });

    useDashboard(client, 'main', { immediate: false });
    await settled();

    expect(fetchMock).not.toHaveBeenCalled();
  });
});
