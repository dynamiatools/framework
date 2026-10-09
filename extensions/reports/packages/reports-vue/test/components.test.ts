import { beforeEach, describe, expect, it, vi } from 'vitest';
import { defineComponent, h } from 'vue';
import { flushPromises, mount } from '@vue/test-utils';
import {
  DynamiaReportList,
  DynamiaReportViewer,
  DynamiaReportsVue,
  ReportChart,
  ReportFilters,
  ReportTable,
  ReportWidget,
  registerReportWidget,
  spanishLabels,
} from '../src/index.js';
import { bodyOf, callsTo, column, definition, filter, makeClient, runResult } from './helpers.js';

const chartCtor = vi.fn();
const chartDestroy = vi.fn();
vi.mock('chart.js/auto', () => ({
  Chart: class {
    constructor(...args: unknown[]) {
      chartCtor(...args);
    }
    destroy = chartDestroy;
  },
}));

beforeEach(() => {
  chartCtor.mockClear();
  chartDestroy.mockClear();
});

describe('ReportList', () => {
  const catalog = [
    {
      name: 'Sales',
      reports: [
        { id: 1, name: 'By region', title: 'Sales by region', description: 'Totals', chartable: true, hasFilters: true },
        { id: 2, name: 'Top customers', chartable: false, hasFilters: false },
      ],
    },
    { name: 'Finance', reports: [{ id: 3, name: 'Cash flow', chartable: false, hasFilters: true }] },
  ];

  it('lists the groups and emits the selected report', async () => {
    const { client } = makeClient({ '/api/reports/v2/catalog': catalog });
    const wrapper = mount(DynamiaReportList, { props: { client } });
    await flushPromises();

    expect(wrapper.findAll('h3').map((h) => h.text())).toEqual(['Sales', 'Finance']);
    expect(wrapper.get('[data-report="1"]').text()).toContain('Sales by region');

    await wrapper.get('[data-report="2"]').trigger('click');
    expect(wrapper.emitted('select')![0]![0]).toMatchObject({ id: 2, name: 'Top customers' });
  });

  it('filters by name, title or description and hides empty groups', async () => {
    const { client } = makeClient({ '/api/reports/v2/catalog': catalog });
    const wrapper = mount(DynamiaReportList, { props: { client } });
    await flushPromises();

    await wrapper.get('input[type="search"]').setValue('totals');
    expect(wrapper.findAll('h3').map((h) => h.text())).toEqual(['Sales']);
    expect(wrapper.findAll('[data-report]')).toHaveLength(1);

    await wrapper.get('input[type="search"]').setValue('zzz');
    expect(wrapper.text()).toContain('There are no reports available');
  });

  it('shows errors and supports custom labels', async () => {
    const { client } = makeClient({ '/api/reports/v2/catalog': () => ({ status: 500, body: { message: 'boom' } }) });
    const wrapper = mount(DynamiaReportList, { props: { client, labels: spanishLabels } });
    await flushPromises();

    expect(wrapper.get('[role="alert"]').text()).toBe('boom');
    expect(wrapper.get('input').attributes('placeholder')).toBe('Buscar informes');
  });
});

describe('ReportFilters', () => {
  const filters = [
    filter('year', { dataType: 'NUMBER', order: 2, required: true }),
    filter('from', { dataType: 'DATE', order: 1 }),
    filter('at', { dataType: 'DATE_TIME', order: 3 }),
    filter('region', { optionsSource: 'QUERY', order: 4 }),
    filter('active', { dataType: 'BOOLEAN', order: 5 }),
    filter('hidden', { hideLabel: true, order: 6 }),
  ];

  it('renders the right control for each type, in order, and marks required filters', () => {
    const wrapper = mount(ReportFilters, { props: { filters, modelValue: {} } });

    expect(wrapper.findAll('.dynamia-report-filter').map((f) => f.attributes('data-filter')))
      .toEqual(['from', 'year', 'at', 'region', 'active', 'hidden']);
    expect(wrapper.get('[data-filter="from"] input').attributes('type')).toBe('date');
    expect(wrapper.get('[data-filter="year"] input').attributes('type')).toBe('number');
    expect(wrapper.get('[data-filter="year"] input').attributes('required')).toBeDefined();
    expect(wrapper.get('[data-filter="year"] label').text()).toContain('*');
    expect(wrapper.get('[data-filter="at"] input').attributes('type')).toBe('datetime-local');
    expect(wrapper.get('[data-filter="active"] select').findAll('option')).toHaveLength(3);
    expect(wrapper.find('[data-filter="hidden"] label').exists()).toBe(false);
  });

  it('emits the new model when a value changes', async () => {
    const wrapper = mount(ReportFilters, { props: { filters, modelValue: { year: '2025' } } });
    await wrapper.get('[data-filter="year"] input').setValue('2026');

    expect(wrapper.emitted('update:modelValue')![0]![0]).toEqual({ year: '2026' });
  });

  it('loads the options of filters with predefined values and shows them', async () => {
    const loadOptions = vi.fn(async (name: string) =>
      name === 'region' ? [{ value: 'n', label: 'North' }, { value: 5, label: 'Five' }] : [],
    );
    const wrapper = mount(ReportFilters, { props: { filters, modelValue: { region: '5' }, loadOptions } });
    await flushPromises();

    expect(loadOptions).toHaveBeenCalledTimes(1);
    const select = wrapper.get('[data-filter="region"] select');
    expect(select.findAll('option').map((o) => o.text())).toEqual(['Select...', 'North', 'Five']);
    expect((select.element as HTMLSelectElement).value).toBe('5');
  });

  it('searches the options of entity filters while typing', async () => {
    const loadOptions = vi.fn(async () => []);
    const wrapper = mount(ReportFilters, {
      props: { filters: [filter('customer', { dataType: 'ENTITY', optionsSource: 'ENTITY' })], modelValue: {}, loadOptions },
    });
    await flushPromises();
    await wrapper.get('input[type="search"]').setValue('ana');

    expect(loadOptions).toHaveBeenLastCalledWith('customer', 'ana');
  });

  it('survives options that fail to load', async () => {
    const loadOptions = vi.fn(async () => {
      throw new Error('nope');
    });
    const wrapper = mount(ReportFilters, { props: { filters: [filter('region', { optionsSource: 'QUERY' })], modelValue: {}, loadOptions } });
    await flushPromises();

    expect(wrapper.findAll('option')).toHaveLength(1);
  });

  it('emits submit when the form is submitted', async () => {
    const wrapper = mount(ReportFilters, { props: { filters, modelValue: {} } });
    await wrapper.get('form').trigger('submit');
    expect(wrapper.emitted('submit')).toHaveLength(1);
  });
});

describe('ReportTable', () => {
  const columns = [column('region'), column('total', { dataType: 'NUMBER', align: 'RIGHT' })];
  const rows = [{ region: 'north', total: 1200.5 }];

  it('renders headers, aligned and formatted cells', () => {
    const wrapper = mount(ReportTable, { props: { columns, rows, locale: 'en-US' } });

    expect(wrapper.findAll('th').map((th) => th.text())).toEqual(['region', 'total']);
    const cells = wrapper.findAll('td');
    expect(cells[1]!.text()).toBe('1,200.5');
    expect(cells[1]!.attributes('style')).toContain('text-align: right');
  });

  it('shows a message when there are no rows', () => {
    const wrapper = mount(ReportTable, { props: { columns, rows: [] } });
    expect(wrapper.text()).toContain('The report returned no results');
  });

  it('emits sort, and exposes the sort state to assistive technology', async () => {
    const wrapper = mount(ReportTable, { props: { columns, rows, sortColumn: 'total', sortDirection: 'desc' } });

    expect(wrapper.get('[data-column="total"]').attributes('aria-sort')).toBe('descending');
    expect(wrapper.get('[data-column="region"]').attributes('aria-sort')).toBe('none');
    await wrapper.get('[data-column="region"] button').trigger('click');
    expect(wrapper.emitted('sort')![0]).toEqual(['region']);
  });

  it('pages and changes the page size', async () => {
    const wrapper = mount(ReportTable, { props: { columns, rows, total: 60, page: 1, size: 25, totalPages: 3 } });

    expect(wrapper.text()).toContain('60 rows');
    expect(wrapper.text()).toContain('Page 2 of 3');
    const [previous, next] = wrapper.findAll('.dynamia-report-paging button');
    await previous!.trigger('click');
    await next!.trigger('click');
    expect(wrapper.emitted('page')).toEqual([[0], [2]]);

    await wrapper.get('select').setValue('50');
    expect(wrapper.emitted('size')![0]).toEqual([50]);
  });

  it('disables the page buttons at the ends', () => {
    const wrapper = mount(ReportTable, { props: { columns, rows, total: 10, page: 0, size: 25, totalPages: 1 } });
    const [previous, next] = wrapper.findAll('.dynamia-report-paging button');
    expect(previous!.attributes('disabled')).toBeDefined();
    expect(next!.attributes('disabled')).toBeDefined();
  });
});

describe('ReportChart', () => {
  const chart = {
    index: 0,
    title: 'Totals',
    type: 'pie',
    labels: ['a', 'b'],
    datasets: [{ label: 'Totals', data: [1, 2], backgroundColor: ['#111', '#222'] }],
  };

  it('draws the chart with the data of the result and destroys it on unmount', async () => {
    const wrapper = mount(ReportChart, { props: { chart } });
    await flushPromises();

    expect(chartCtor).toHaveBeenCalledTimes(1);
    const config = chartCtor.mock.calls[0]![1] as { type: string; data: { labels: string[] }; options: { plugins: { legend: { display: boolean } } } };
    expect(config.type).toBe('pie');
    expect(config.data.labels).toEqual(['a', 'b']);
    expect(config.options.plugins.legend.display).toBe(true);
    expect(wrapper.get('figcaption').text()).toBe('Totals');

    wrapper.unmount();
    expect(chartDestroy).toHaveBeenCalled();
  });

  it('hides the legend of bar charts and redraws when the data changes', async () => {
    const wrapper = mount(ReportChart, { props: { chart: { ...chart, type: 'bar' } } });
    await flushPromises();
    expect((chartCtor.mock.calls[0]![1] as { options: { plugins: { legend: { display: boolean } } } }).options.plugins.legend.display).toBe(false);

    await wrapper.setProps({ chart: { ...chart, type: 'bar', labels: ['x'] } });
    await flushPromises();
    expect(chartCtor).toHaveBeenCalledTimes(2);
    expect(chartDestroy).toHaveBeenCalled();
  });
});

describe('ReportViewer', () => {
  const report = definition(
    [filter('year', { dataType: 'NUMBER', defaultValue: '2026' }), filter('region', { required: true, optionsSource: 'QUERY' })],
    { charts: [{ index: 0, title: 'Totals', type: 'bar', labelField: 'region', valueField: 'total', grouped: true }] },
  );

  function routes(extra: Record<string, unknown> = {}) {
    return makeClient({
      '/api/reports/v2/7/filters/': [{ value: 'north', label: 'North' }],
      '/api/reports/v2/7/export': () => ({ blob: new Blob(['x']), contentType: 'text/csv' }),
      '/api/reports/v2/7/run': () => ({
        body: runResult({
          charts: [{ index: 0, title: 'Totals', type: 'bar', labels: ['north'], datasets: [{ label: 'Totals', data: [1], backgroundColor: ['#111'] }] }],
        }),
      }),
      '/api/reports/v2/7': report,
      ...extra,
    });
  }

  it('shows the report, waits for the required filters, then runs and shows table and charts', async () => {
    const { client, fetchMock } = routes();
    const wrapper = mount(DynamiaReportViewer, { props: { id: 7, client, locale: 'en-US' } });
    await flushPromises();

    expect(wrapper.get('h2').text()).toBe('Sales by region');
    expect(callsTo(fetchMock, '/run')).toHaveLength(0);
    expect(wrapper.get('.dynamia-report-run').attributes('disabled')).toBeDefined();
    expect(wrapper.findAll('.dynamia-report-export').every((b) => b.attributes('disabled') !== undefined)).toBe(true);

    await wrapper.get('[data-filter="region"] select').setValue('north');
    await wrapper.get('.dynamia-report-run').trigger('click');
    await flushPromises();

    expect(bodyOf(fetchMock, '/run')).toMatchObject({ filters: { year: '2026', region: 'north' }, page: 0, size: 25 });
    expect(wrapper.findAll('tbody tr')).toHaveLength(2);
    expect(wrapper.text()).toContain('1,200.5');
    expect(chartCtor).toHaveBeenCalledTimes(1);
    expect(wrapper.emitted('result')).toHaveLength(1);
  });

  it('can hide the charts', async () => {
    const { client } = routes({ '/api/reports/v2/7': definition([]) });
    mount(DynamiaReportViewer, { props: { id: 7, client, showCharts: false } });
    await flushPromises();
    expect(chartCtor).not.toHaveBeenCalled();
  });

  it('warns when the result is truncated', async () => {
    const { client } = routes({
      '/api/reports/v2/7/run': () => ({ body: runResult({ truncated: true }) }),
      '/api/reports/v2/7': definition([]),
    });
    const wrapper = mount(DynamiaReportViewer, { props: { id: 7, client } });
    await flushPromises();

    expect(wrapper.get('.dynamia-report-truncated').text()).toContain('maximum number of rows');
  });

  it('shows run errors without losing the filters', async () => {
    const { client } = routes({
      '/api/reports/v2/7/run': () => ({ status: 400, body: { message: 'Invalid value for filter [year]' } }),
      '/api/reports/v2/7': definition([filter('year', { dataType: 'NUMBER' })]),
    });
    const wrapper = mount(DynamiaReportViewer, { props: { id: 7, client } });
    await flushPromises();

    expect(wrapper.get('[role="alert"]').text()).toContain('year');
    expect(wrapper.find('[data-filter="year"]').exists()).toBe(true);
  });

  it('shows definition errors and a custom error slot', async () => {
    const { client } = routes({ '/api/reports/v2/7': () => ({ status: 403, body: { message: 'Access denied to this report' } }) });
    const wrapper = mount(DynamiaReportViewer, {
      props: { id: 7, client },
      slots: { error: '<template #error="{ error }"><b class="custom">{{ error }}</b></template>' },
    });
    await flushPromises();

    expect(wrapper.get('.custom').text()).toBe('Access denied to this report');
  });

  it('sorts and pages through the table', async () => {
    const { client, fetchMock } = routes({
      '/api/reports/v2/7/run': () => ({ body: runResult({ total: 60 }) }),
      '/api/reports/v2/7': definition([]),
    });
    const wrapper = mount(DynamiaReportViewer, { props: { id: 7, client } });
    await flushPromises();

    await wrapper.get('[data-column="region"] button').trigger('click');
    await flushPromises();
    expect(bodyOf(fetchMock, '/run', 1)).toMatchObject({ sort: 'region', direction: 'asc', page: 0 });

    const next = wrapper.findAll('.dynamia-report-paging button')[1]!;
    await next.trigger('click');
    await flushPromises();
    expect(bodyOf(fetchMock, '/run', 2)).toMatchObject({ page: 1 });
  });

  it('exports in the chosen format', async () => {
    const { client, fetchMock } = routes({ '/api/reports/v2/7': definition([]) });
    const create = vi.fn(() => 'blob:x');
    vi.stubGlobal('URL', Object.assign(URL, { createObjectURL: create, revokeObjectURL: vi.fn() }));
    const wrapper = mount(DynamiaReportViewer, { props: { id: 7, client } });
    await flushPromises();

    await wrapper.get('[data-format="pdf"]').trigger('click');
    await flushPromises();

    expect(callsTo(fetchMock, '/export')[0]).toContain('format=pdf');
    expect(create).toHaveBeenCalledTimes(1);
    vi.unstubAllGlobals();
  });

  it('needs a client', () => {
    expect(() => mount(DynamiaReportViewer, { props: { id: 7 } })).toThrow();
  });
});

describe('ReportWidget and plugin', () => {
  it('renders the widget data as a compact table', () => {
    const wrapper = mount(ReportWidget, {
      props: {
        data: {
          columns: [column('region'), column('total', { dataType: 'NUMBER', align: 'RIGHT' })],
          rows: [{ region: 'north', total: 5 }],
          total: 40,
          truncated: true,
        },
      },
    });

    expect(wrapper.findAll('th')).toHaveLength(2);
    expect(wrapper.text()).toContain('1 of 40 rows');
    expect(wrapper.text()).toContain('maximum number of rows');
  });

  it('shows a message for an empty widget', () => {
    const wrapper = mount(ReportWidget, { props: { data: { columns: [column('a')], rows: [], total: 0, truncated: false } } });
    expect(wrapper.text()).toContain('no results');
  });

  it('registers the report renderer in a dashboard registry', () => {
    const registry = { register: vi.fn() };
    registerReportWidget(registry);
    expect(registry.register).toHaveBeenCalledWith('report', ReportWidget);
  });

  it('the plugin registers the global components', () => {
    const component = vi.fn();
    DynamiaReportsVue.install({ component } as never);
    expect(component.mock.calls.map((c) => c[0])).toEqual(['DynamiaReportList', 'DynamiaReportViewer', 'DynamiaReportDesigner']);
  });

  it('works inside a dashboard-like host component', () => {
    const Host = defineComponent({ render: () => h(ReportWidget, { data: { columns: [], rows: [], total: 0, truncated: false } }) });
    expect(mount(Host).find('table').exists()).toBe(true);
  });
});
